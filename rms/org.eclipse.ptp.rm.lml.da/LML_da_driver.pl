#!/usr/bin/perl -w
#*******************************************************************************
#* Copyright (c) 2011 Forschungszentrum Juelich GmbH.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*
#* Contributors:
#*    Wolfgang Frings (Forschungszentrum Juelich GmbH) 
#*******************************************************************************/ 
use FindBin;
use lib "$FindBin::RealBin/lib";
use Data::Dumper;
use Getopt::Long;
use Time::Local;
use Time::HiRes qw ( time );
use LML_file_obj;
use LML_da_workflow_obj;
use Storable qw(dclone); 

use strict;

my $patint="([\\+\\-\\d]+)";   # Pattern for Integer number
my $patfp ="([\\+\\-\\d.E]+)"; # Pattern for Floating Point number
my $patwrd="([\^\\s]+)";       # Pattern for Work (all noblank characters)
my $patbl ="\\s+";             # Pattern for blank space (variable length)

my $version="1.08";
 
my ($tstart,$tdiff,$rc);


#
# Usage: LML_da_driver.pl [<options>] <request-file> <output-file>
#   or   LML_da_driver.pl [<options>] < <request-file> > <output-file>
#
# option handling:
# LML_da_driver can run in two different modes:
# 
# 1. running from scratch
#    - designed to run on remote system in user mode
#    - determine system and select corrensponding query scripts
#
# 2. runnung from a LML raw file
#    - designed to run on a web server as backend to 
#      web server scripts 
#    - raw file must be speciied by rawfile parameter
# 
# Common steps
#    - check comand line option
#    - extract options from request
#    - step 1 or 2
#    - extract layout from request, if not given
#    - create default layout
#    - build workflow input
#    - run workflow by  LML_da.pl
#    - return output file

# option handling
my $opt_rawfile=""; # unset
my $opt_verbose=0;
my $opt_quiet=0;
my $hostname = `hostname`;chomp($hostname);
my $ppid = $$;
my $opt_tmpdir=sprintf("./tmp_%s_%d",$hostname,$ppid);
my $opt_keeptmp=0;
my $opt_dump=0;
my $opt_demo=0;
usage($0) if( ! GetOptions( 
			    'verbose'          => \$opt_verbose,
			    'quiet'            => \$opt_quiet,
			    'rawfile=s'        => \$opt_rawfile,
			    'tmpdir=s'         => \$opt_tmpdir,
			    'keeptmp'          => \$opt_keeptmp,
			    'demo'             => \$opt_demo,
			    'dump'             => \$opt_dump
			    ) );
my $date=`date`;
chomp($date);

# print header
print STDERR "-"x90,"\n" if($opt_verbose);
print STDERR"  LLVIEW Data Access Workflow Manager Driver $version, starting at ($date)\n" if(!$opt_quiet); 
print STDERR "-"x90,"\n" if($opt_verbose);


# check positional parameters 
my $requestfile  = "<unknown>";
my $outputfile   = "<unknown>";
if( ($#ARGV > 1) || ($#ARGV == 0 ) ) {
    &exit_witherror("-","$0: wrong number of arguments (",($#ARGV+1),"), exiting ...\n");
    usage($0);
    exit();
} elsif ($#ARGV == 1) {
    # in/output file specified as parameter
    $requestfile = $ARGV[0];
    $outputfile  = $ARGV[1];
} else {
    # in/output file specified over stdin/stdout
    $requestfile = "-"; 
    $outputfile  = "-";
}

my $tmpdir       = $opt_tmpdir;
my $rawfile      = undef;
my $removetmpdir = 0; # remove only if directory was create 

# check temporary directory
if(! -d $tmpdir) {
    printf(STDERR "$0: temporary directory not found, create new directory $tmpdir ...\n") if($opt_verbose);
    if(!mkdir($tmpdir,0755)) {
	&exit_witherror($outputfile,"$0: could not create $tmpdir ...$!\n");
    } else {
	print STDERR "$0: tmpdir created ($tmpdir)\n"  if($opt_verbose);
    }
    $removetmpdir=1;
}

# check request input file
if ($requestfile ne "-") {
    if(! -f $requestfile) {
	&exit_witherror($outputfile,"$0: requestfile $requestfile not found, exiting ...\n");
    }
}

# check rawfile
if($opt_rawfile) {
    if(! -f $opt_rawfile) {
	&exit_witherror($outputfile,"$0: rawfile $rawfile specified but not found, exiting ...\n");
    }
    $rawfile=$opt_rawfile;
}

# read config file
print STDERR "$0: requestfile=$requestfile\n" if($opt_verbose);
$tstart=time;

my $filehandler_request = LML_file_obj->new($opt_verbose,1);
$filehandler_request->read_lml_fast($requestfile);
$tdiff=time-$tstart;
printf(STDERR "$0: parsing XML requestfile in %6.4f sec\n",$tdiff) if($opt_verbose);
if(!$filehandler_request) {
    &exit_witherror($outputfile,"$0: could not parse requestfile $requestfile, exiting ...\n");
}

# init global vars
my $pwd=`pwd`;
chomp($pwd);


#########################
# create workflow
#########################
my $workflowxml=&create_workflow($tmpdir);
my $laststep = "";
my $step     = "";


#########################
# determine how raw file 
# will be generated
#########################
if($rawfile) {
    $step="rawfilecp";
    if($rawfile=~/\.gz$/) {
	&add_exec_step_to_workflow($workflowxml,$step, $laststep, 
				   "gunzip -c $rawfile > \$stepoutfile");
    } else {
	&add_exec_step_to_workflow($workflowxml,$step, $laststep, 
				   "cp $rawfile \$stepoutfile");
    }
    $laststep=$step;
} else {
    # get data from resource management system (RMS)


    # check for hints about queueing system
    my $rms="undef";
    my %cmds=();
    print STDERR "$0: check_for rms ...\n"  if($opt_verbose);
    if(exists($filehandler_request->{DATA}->{request})) {
	if(exists($filehandler_request->{DATA}->{request}->[0]->{rms})) {
	    $rms=lc($filehandler_request->{DATA}->{request}->[0]->{rms});
	    print STDERR "$0: check_for rms, got hint from request ... ($rms)\n" if($opt_verbose);
	    my($key);
	    foreach $key (keys(%{$filehandler_request->{DATA}->{request}->[0]}) ) {
		next if ($key !~/^cmd_/);
		$cmds{$key}=$filehandler_request->{DATA}->{request}->[0]->{$key};
		print STDERR "$0: check_for rms, got hint from for cmd $key ... ($cmds{$key})\n"  if($opt_verbose);
	    }
	}
    } 
    &check_rms_torque(\$rms,\%cmds) if(($rms eq "undef") || ($rms eq "torque"));
    &check_rms_LL(\$rms,\%cmds)     if(($rms eq "undef") || ($rms eq "ll"));
    &check_rms_OpenMPI(\$rms,\%cmds)if(($rms eq "undef") || ($rms eq "openmpi"));

    $laststep=&generate_step_rms_torque($workflowxml, $laststep, \%cmds)  if($rms eq "torque");
    $laststep=&generate_step_rms_LL($workflowxml, $laststep, \%cmds)      if($rms eq "ll");
    $laststep=&generate_step_rms_OpenMPI($workflowxml, $laststep, \%cmds) if($rms eq "openmpi");

    $step="addcolor";
    &add_exec_step_to_workflow($workflowxml,$step, $laststep, 
			       "$^X \$instdir/LML_color/LML_color_obj.pl -colordefs \$instdir/LML_color/default.conf ".
			       "-o         \$stepoutfile \$stepinfile");
    $laststep=$step;

}

#########################
# working on layout
#########################
# check if default layout should used (by request)
my $usedefaultlayout=0;
if(exists($filehandler_request->{DATA}->{request})) {
    if(exists($filehandler_request->{DATA}->{request}->[0]->{getDefaultData})) {
	if($filehandler_request->{DATA}->{request}->[0]->{getDefaultData}=~/^true$/i) {
	    $usedefaultlayout=1;
	}
    }
}
#check if layout is given in request
my $layoutfound=0;
if(!$usedefaultlayout) {
    my $key;
    foreach $key (keys(%{$filehandler_request->{DATA}})) {
	$layoutfound=1 if ($key=~/LAYOUT$/);
    }
    $usedefaultlayout=1 if(!$layoutfound);
}
print STDERR "$0: layoutfound=$layoutfound usedefaultlayout=$usedefaultlayout\n" if($opt_verbose);

my $filehandler_layout;
if($usedefaultlayout) {
    $filehandler_layout=&create_default_layout($filehandler_request);
} else {
    $filehandler_layout=&create_layout_from_request($filehandler_request);
}

# write layout to tmpdir
$filehandler_layout->write_lml("$tmpdir/layout.xml");


#########################
# add step: LML2LML
#########################
$step="LML2LML";
my $demo="";
$demo="-demo" if $opt_demo;
&add_exec_step_to_workflow($workflowxml,$step, $laststep, 
			   "$^X LML2LML/LML2LML.pl -v $demo -layout \$tmpdir/layout.xml".
			   " -output \$stepoutfile \$stepinfile");
$laststep=$step;

#########################
# Dump?
#########################
if($opt_dump) {
    print STDERR Dumper($filehandler_request->{DATA});
    print STDERR Dumper($workflowxml);
    print STDERR Dumper($filehandler_layout->{DATA});
    exit(1);
}

#########################
# execute Workflow
#########################
# write workflow to tmpdir

my $workflow_obj = LML_da_workflow_obj->new($opt_verbose,0);
$workflow_obj->{DATA}=$workflowxml;
$workflow_obj->write_xml("$tmpdir/workflow.xml");

my $cmd="$^X ./LML_da.pl";
$cmd .= " -v"  if($opt_verbose);
$cmd .= " -c $tmpdir/workflow.xml";
$cmd .= " > $tmpdir/LML_da.log";
$cmd .= " 2> $tmpdir/LML_da.errlog";
printf( STDERR  "$0: executing: %s ...\n",$cmd) if($opt_verbose);
$tstart=time;
system($cmd);$rc=$?;
$tdiff=time-$tstart;
printf(STDERR "$0: %60s -> ready, time used %10.4ss\n","",$tdiff) if($opt_verbose);
if($rc) {     
    &exit_witherror($outputfile,"$0 failed executing: $cmd rc=$rc\n");
}

#########################
# handle output
#########################
my $stepoutfile="$tmpdir/datastep_${laststep}.xml";
if(! -f $stepoutfile) {
    &exit_witherror($outputfile,"$0 failed, no output generated in last step ... rc=$rc\n");
}
if($outputfile eq "-") {
    open(IN,$stepoutfile);
    while(<IN>) {
	print $_;
    }
    close(IN);
} else {
    open(IN,$stepoutfile);
    open(OUT," > $outputfile") || die "could not open for write '$outputfile'";
    while(<IN>) {
	print OUT $_;
    }
    close(OUT);
    close(IN);
}

# clean up
if(($removetmpdir) && (!$opt_keeptmp)) {
    my $file;
    foreach $file (`ls $tmpdir`) {
	chomp($file);
#	print STDERR "unlink $tmpdir/$file\n";
	unlink("$tmpdir/$file");
    }
    if(!rmdir($tmpdir)) {
	printf(STDERR "$0: could not rmdir $tmpdir ...$!, exiting ...\n");
    } else {
	print STDERR "$0: tmpdir removed ($tmpdir)\n"  if($opt_verbose);
    }
}


print STDERR "-"x90,"\n" if($opt_verbose);
print STDERR"  LLVIEW Data Access Workflow Manager Driver $version, ending at   ($date)\n"  if(!$opt_quiet); 
print STDERR "-"x90,"\n" if($opt_verbose);


sub usage {
    die " 
          LLVIEW Data Access Workflow Manager Driver $version

          Usage: 
                $_[0] <options> <requestfile> <outputfile>
           or
                $_[0] <options> < <requestfile> > <outputfile>

                -rawfile <LML raw file>  : use LML raw file as data source
                                           (default: query system)
                -tmpdir  <dir>           : use this directory for temporary data
                                           (default: ./tmp) 
                -keeptmp                 : keep temporary directory
                -verbose                 : verbose mode
                -quiet                   : prints no messages on stderr

";
}

# generate dummy LML output containing only error messages
sub exit_witherror {
    my($outputfile,$errormsg)=@_;
    my $xmlout="";

    $xmlout.="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
    $xmlout.="<lml:lgui xmlns:lml=\"http://www.llview.de\"\n";
    $xmlout.="          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
    $xmlout.="          version=\"1\" xsi:schemaLocation=\"http://www.llview.de lgui.xsd\">\n";
    $xmlout.=" <objects>\n";
    $xmlout.="    <object id=\"sys000001\" name=\"error\" type=\"system\"/>\n";
    $xmlout.="</objects>\n";

    $xmlout.="<information>\n";
    $xmlout.=" <info oid=\"sys000001\" type=\"short\">\n";
    $xmlout.="  <data key=\"errormsg\" value=\"$errormsg\"/>\n";
    $xmlout.=" </info>\n";
    $xmlout.="</information>\n";

    $xmlout.="</lml:lgui>\n";
    
    printf(STDERR "$errormsg\n");

    if($outputfile eq "-") {
	print $xmlout;
    } else {
	open(OUT," > $outputfile") || die "could not open for write '$outputfile'";
	print OUT $xmlout;
	close(OUT);
    }

    exit(1);

}

sub create_workflow {
    my($tmpdir)=@_;
    my($datastructref, $vardefsref);

    $vardefsref={};
    $vardefsref->{key}  ="tmpdir";
    $vardefsref->{value}="$tmpdir";
    push(@{$datastructref->{vardefs}->[0]->{var}},$vardefsref);

    $vardefsref={};
    $vardefsref->{key}  ="permdir";
    $vardefsref->{value}="$tmpdir";
    push(@{$datastructref->{vardefs}->[0]->{var}},$vardefsref);

    return($datastructref);
}

sub add_exec_step_to_workflow {
    my($datastructref, $id, $exec_after, @cmds)=@_;
    my($stepref, $cmdref,$cmd);
    $stepref={};
    $stepref->{id}          = $id;
    $stepref->{active}      = 1;
    $stepref->{exec_after}  = $exec_after;
    $stepref->{type}        = "execute";
    
    foreach $cmd (@cmds) {
	$cmdref={};
	$cmdref->{exec}         = $cmd;
	push(@{$stepref->{cmd}},$cmdref);
    }
    
    $datastructref->{step}->{$id}=$stepref;
    return($datastructref);
}

#####################################################################
sub create_default_layout {
    my($filehandler_request)=@_;

    my $filehandler_layout = LML_file_obj->new($opt_verbose,1);
    $filehandler_layout->read_lml_fast("$FindBin::RealBin/samples/layout_default.xml");
    $filehandler_layout->check_lml();
    return($filehandler_layout);
}

sub create_layout_from_request {
    my($filehandler_request)=@_;
    my($key);

    my $filehandler_layout = LML_file_obj->new($opt_verbose,1);
    $filehandler_layout -> init_file_obj();
    foreach $key ("TABLELAYOUT","TABLE","NODEDISPLAYLAYOUT","NODEDISPLAY","OBJECT") {
	if (exists($filehandler_request->{DATA}->{$key})) {
	    $filehandler_layout->{DATA}->{$key}=dclone($filehandler_request->{DATA}->{$key});
	}
    }
    $filehandler_layout->check_lml();
    return($filehandler_layout);
}


#########################
# rms: Torque
#########################
sub check_rms_torque {
    my($rmsref,$cmdsref)=@_;
    my($key);
    my $rc=1;

    my %cmdname=(
	"job"  => "qstat",
	"node" => "pbsnodes",
	);
    my %cmdpath=(
	"job" => "/usr/bin/qstat",
	"node" => "/usr/bin/pbsnodes",
	);
   
    foreach $key (keys(%cmdname)) {
	# check for job query cmd
	if(exists($cmdsref->{"cmd_${key}info"})) {
	    $cmd=$cmdsref->{"cmd_${key}info"};
	} else {
	    $cmd=$cmdpath{$key};
	}
	if(! -f $cmd) {
	    my $cmdpath=`which $cmdname{$key} 2>/dev/null`; 	# last try: which 
	    if(!$?) {
		chomp($cmdpath);
		$cmd=$cmdpath;
		print STDERR "$0: check_rms_torque: found $cmdname{$key} by which ($cmd)\n" if($opt_verbose);
	    } else {
		last;
	    }
	}
	if(-f $cmd) {
	    $$rmsref="torque" if($$rmsref eq "undef");
	    $cmdsref->{"cmd_${key}info"}=$cmd;
	}
    }
    if($$rmsref eq "torque") {
	print STDERR "$0: check_rms_torque: found torque commands (",
	join(",",(values(%{$cmdsref}))),")\n" if($opt_verbose);
    } else {
	print STDERR "$0: check_rms_torque: seems not to be a torque system\n" if($opt_verbose);
    }

    return($rc);
}


sub generate_step_rms_torque {
    my($workflowxml, $laststep, $cmdsref)=@_;
    my($step,$envs,$key,$ukey);

    $envs="";
    foreach $key (keys(%{$cmdsref})) {
	$ukey=uc($key);
	$envs.="$ukey=$cmdsref->{$key} ";
    }
    $step="getdata";
    &add_exec_step_to_workflow($workflowxml,$step, $laststep, 
			       "$envs $^X rms/TORQUE/da_system_info_LML.pl               \$tmpdir/sysinfo_LML.xml",
			       "$envs $^X rms/TORQUE/da_nodes_info_LML.pl                \$tmpdir/nodes_LML.xml",
			       "$envs $^X rms/TORQUE/da_jobs_info_LML.pl                 \$tmpdir/jobs_LML.xml");
    $laststep=$step;

    $step="combineLML";
    &add_exec_step_to_workflow($workflowxml,$step, $laststep, 
			       "$^X \$instdir/LML_combiner/LML_combine_obj.pl  -v -o \$stepoutfile ".
			       "\$tmpdir/sysinfo_LML.xml \$tmpdir/jobs_LML.xml \$tmpdir/nodes_LML.xml");
    $laststep=$step;

    return($laststep);

}


#########################
# rms: LoadLeveler
#########################
sub check_rms_LL {
    my($rmsref,$cmdsref)=@_;
    my($key);
    my $rc=1;

    my %cmdname=(
	"job"  => "llq",
	"node" => "llstatus",
	);
    my %cmdpath=(
	"job" => "/usr/bin/llq",
	"node" => "/usr/bin/llstatus",
	);
   
    foreach $key (keys(%cmdname)) {
	# check for job query cmd
	if(exists($cmdsref->{"cmd_${key}info"})) {
	    $cmd=$cmdsref->{"cmd_${key}info"};
	} else {
	    $cmd=$cmdpath{$key};
	}
	if(! -f $cmd) {
	    my $cmdpath=`which $cmdname{$key} 2>/dev/null`; 	# last try: which 
	    if(!$?) {
		chomp($cmdpath);
		$cmd=$cmdpath;
		print STDERR "$0: check_rms_LL: found $cmdname{$key} by which ($cmd)\n" if($opt_verbose);
	    } else {
		last;
	    }
	}
	if(-f $cmd) {
	    $$rmsref="ll" if($$rmsref eq "undef");
	    $cmdsref->{"cmd_${key}info"}=$cmd;
	}
    }
    if($$rmsref eq "ll") {
	print STDERR "$0: check_rms_LL: found LL commands (",
	join(",",(values(%{$cmdsref}))),")\n" if($opt_verbose);
    } else {
	print STDERR "$0: check_rms_LL: seems not to be a LL system\n" if($opt_verbose);
    }

    return($rc);
}


sub generate_step_rms_LL {
    my($workflowxml, $laststep, $cmdsref)=@_;
    my($step,$envs,$key,$ukey);

    $envs="";
    foreach $key (keys(%{$cmdsref})) {
	$ukey=uc($key);
	$envs.="$ukey=$cmdsref->{$key} ";
    }

    $step="getdata";
    &add_exec_step_to_workflow($workflowxml,$step, $laststep, 
			       "$envs $^X rms/LL/da_system_info_LML.pl               \$tmpdir/sysinfo_LML.xml",
			       "$envs $^X rms/LL/da_nodes_info_LML.pl                \$tmpdir/nodes_LML.xml",
			       "$envs $^X rms/LL/da_jobs_info_LML.pl                 \$tmpdir/jobs_LML.xml");
    $laststep=$step;

    $step="combineLML";
    &add_exec_step_to_workflow($workflowxml,$step, $laststep, 
			       "$^X \$instdir/LML_combiner/LML_combine_obj.pl  -v -o \$stepoutfile ".
			       "\$tmpdir/sysinfo_LML.xml \$tmpdir/jobs_LML.xml \$tmpdir/nodes_LML.xml");
    $laststep=$step;

    return($laststep);

}

#########################
# rms: OpenMPI
#########################
sub check_rms_OpenMPI {
    my($rmsref,$cmdsref)=@_;
    my($key,$infocmd);
    my $rc=1;

    my %cmdname=(
	"job"  => "orte-ps",
	"node" => "orte-ps",
	"sys"  => "ompi_info",
	);
        
    my %cmdpath=(
	"job"  => "",
	"node" => "",
	"sys"  => "/usr/lib/mpi/gcc/openmpi/bin/ompi_info",
	);

    $infocmd=undef;

    # first:  check if ompi_info is given by request info
    if(exists($cmdsref->{"cmd_sysinfo"})) {
	    $infocmd=$cmdsref->{"cmd_sysinfo"}." --path bindir";
    } 

    # second: check if ompi_info is given in PATH
    if(!$infocmd) {
	my $cmdpath=`which $cmdname{sys} 2>/dev/null`; 	
	if(!$?) {
	    chomp($cmdpath);
	    $infocmd=$cmdpath." --path bindir";
	}
    }

    # second: check if ompi_info is given in default path
    if(!$infocmd) {
	my $cmdpath=$cmdpath{sys}; 	
	if(! -f $cmdpath) {
	    $infocmd=$cmdpath." --path bindir";
	}
    }
 
    # return if no ompi_info found
    if(!$infocmd) {
	return($rc);
    }
   
    # get openmpi bindir
    my $bindir=`$infocmd`;
    chomp($bindir);
    $bindir=~s/^Bindir:\s*//gs;
	
    foreach $key (keys(%cmdname)) {
	$cmdpath{$key}=$bindir."/".$cmdname{$key};

	if(exists($cmdsref->{"cmd_${key}info"})) {
	    $cmd=$cmdsref->{"cmd_${key}info"};
	} else {
	    $cmd=$cmdpath{$key};
	}
	if(! -f $cmd) {
	    my $cmdpath=`which $cmdname{$key} 2>/dev/null`; 	# last try: which 
	    if(!$?) {
		chomp($cmdpath);
		$cmd=$cmdpath;
		print STDERR "$0: check_rms_OpenMPI: found $cmdname{$key} by which ($cmd)\n" if($opt_verbose);
	    } else {
		last;
	    }
	}
	if(-f $cmd) {
	    $$rmsref="openmpi" if($$rmsref eq "undef");
	    $cmdsref->{"cmd_${key}info"}=$cmd;
	}
    }
    if($$rmsref eq "openmpi") {
	print STDERR "$0: check_rms_OpenMPI: found OpenMPI commands (",
	join(",",(values(%{$cmdsref}))),")\n" if($opt_verbose);
    } else {
	print STDERR "$0: check_rms_OpenMPI: seems not to be a OpenMPI system\n" if($opt_verbose);
    }

    return($rc);
}


sub generate_step_rms_OpenMPI {
    my($workflowxml, $laststep, $cmdsref)=@_;
    my($step,$envs,$key,$ukey);

    $envs="";
    foreach $key (keys(%{$cmdsref})) {
	$ukey=uc($key);
	$envs.="$ukey=$cmdsref->{$key} ";
    }

    $step="getdata";
    &add_exec_step_to_workflow($workflowxml,$step, $laststep, 
			       "$envs $^X rms/OPENMPI/da_orte_info_LML.pl   \$tmpdir/jobs_nodes_LML.xml",
			       "$envs $^X rms/OPENMPI/da_system_info_LML.pl \$tmpdir/sysinfo_LML.xml");
    $laststep=$step;

    $step="combineLML";
    &add_exec_step_to_workflow($workflowxml,$step, $laststep, 
			       "$^X \$instdir/LML_combiner/LML_combine_obj.pl  -v -o \$stepoutfile ".
			       "\$tmpdir/sysinfo_LML.xml \$tmpdir/jobs_nodes_LML.xml");
    $laststep=$step;

    return($laststep);

}
