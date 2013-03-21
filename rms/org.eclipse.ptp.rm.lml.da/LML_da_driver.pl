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

my $version="1.18";
 
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
# 2. running on a LML raw file
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
my $hostname = `hostname`;chomp($hostname);
my $ppid = $$;
my %options = (
    "rawfile"             => "",
    "tmpdir"              => "./tmp_".$hostname."_".$ppid,
    "permdir"             => "./perm_".$hostname,
    "keeptmp"             => 0,
    "keepperm"            => 1,
    "verbose"             => 0,
    "quiet"               => 0,
    "nocheckrequest"      => 0,
    "rms"                 => "undef",
    "dump"                => 0,
    "demo"                => 0,
    "test"                => 0
);
my @save_ARGV=(@ARGV);
my @options_from_file_found;

usage($0) if( ! GetOptions( 
                            'verbose'          => \$options{verbose},
                            'quiet'            => \$options{quiet},
                            'rawfile=s'        => \$options{rawfile},
                            'tmpdir=s'         => \$options{tmpdir},
                            'permdir=s'        => \$options{permdir},
                            'keeptmp'          => \$options{keeptmp},
                            'rms=s'            => \$options{rms},
                            'nocheckrequest'   => \$options{nocheckrequest},
                            'demo'             => \$options{demo},
                            'test'             => \$options{test},
     		            'dump'             => \$options{dump}
                            ) );
my $date=`date`;
chomp($date);

my $REPORT;

&open_report();

my $options_file=".LML_da_options";
if (-f $options_file) {
    my ($line);
    open(IN,$options_file);
    while($line=<IN>) {
        if($line=~/$patwrd=\s*$patwrd\s*$/) {
	    my($opt_name,$opt_value)=($1,$2) ;
	    if(exists($options{$opt_name})) {
		$options{$opt_name}=$opt_value;
		push(@options_from_file_found,$opt_name);
	    } else {
		&report_if_verbose("WARNING found unknown option (%s) in option file file %s\n",$opt_name,$options_file);
	    }
        }
    }
    close(IN);
}

# print header
&report_if_verbose("%s%s","-"x90,"\n");
&report("%s","  LLVIEW Data Access Workflow Manager Driver $version, starting at ($date)\n");
&report_if_verbose("  %s%s%s"," command line args: ",join(" ",@save_ARGV),"\n"); 
&report_if_verbose("  %s%s%s%s"," option file  args: ",join(" ",@options_from_file_found),"     (from file $options_file)","\n") if($#options_from_file_found>=0); 
&report_if_verbose("%s%s", "-"x90,"\n");


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

my $tmpdir       = $options{tmpdir};
my $permdir       = $options{permdir};
my $rawfile      = undef;
my $removetmpdir = 0; # remove only if directory was create 

my $workflowxml = "";
my $laststep    = "";

# init global vars
my $pwd=`pwd`;
chomp($pwd);

if(! $options{test}) {

# check and/or create temporary directory
if(! -d $tmpdir) {
    &report_if_verbose("%s","$0: temporary directory not found, create new directory $tmpdir ...\n");
    if(!mkdir($tmpdir,0755)) {
        &exit_witherror($outputfile,"$0: could not create $tmpdir ...$!\n");
    } else {
        &report_if_verbose("%s", "$0: tmpdir created ($tmpdir)\n");
    }
    $removetmpdir=1;
}

# check permanent directory
if(! -d $permdir) {
    &report_if_verbose("$0: permanent directory not found, create new directory $permdir ...\n");
    if(!mkdir($permdir,0755)) {
        &exit_witherror($outputfile,"$0: could not create $permdir ...$!\n");
    } else {
        &report_if_verbose("%s", "$0: permdir created ($permdir)\n");
    }
}

# check request input file
if ($requestfile ne "-") {
    if(! -f $requestfile) {
        &exit_witherror($outputfile,"$0: requestfile $requestfile not found, exiting ...\n");
    }
}

# check rawfile
if($options{rawfile}) {
    if(! -f $options{rawfile}) {
        &exit_witherror($outputfile,"$0: rawfile $rawfile specified but not found, exiting ...\n");
    }
    $rawfile=$options{rawfile};
}


# read config file
&report_if_verbose("%s", "$0: requestfile=$requestfile\n");
$tstart=time;

# debug request file
open(OUT,"> $options{tmpdir}/request.xml");
if ($requestfile eq "-") {
    while(<>) {
        print OUT $_;
    }
    $requestfile="$options{tmpdir}/request.xml";
} else {
    open(IN,$requestfile);
    while(<IN>) {
        print OUT $_;
    }
    close(IN);
}
close(OUT);

my $filehandler_request = LML_file_obj->new($options{verbose},1);
$filehandler_request->read_lml_fast($requestfile);
$tdiff=time-$tstart;
&report_if_verbose("$0: parsing XML requestfile in %6.4f sec\n",$tdiff);
if(!$filehandler_request) {
    &exit_witherror($outputfile,"$0: could not parse requestfile $requestfile, exiting ...\n");
}



#########################
# create workflow
#########################
$workflowxml=&create_workflow($tmpdir,$permdir);
my $step     = "";

# hashes containing function references to rms specific functions
my $check_functions;
my $generate_functions;

my $rms="undef";

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
    my %cmds=();

    if($options{rms} ne "undef") { 
	&report_if_verbose("$0: rms given by command line option: $options{rms} ...\n");
	$rms=uc($options{rms});
    } else {
	&report_if_verbose("$0: check request for rms hint ...\n");
	if(exists($filehandler_request->{DATA}->{REQUEST})) {
	    if(exists($filehandler_request->{DATA}->{REQUEST}->{driver})) {
		my $driver_ref=$filehandler_request->{DATA}->{REQUEST}->{driver};
		if(!$options{nocheckrequest}) {
		    # check rms name
		    if(exists($driver_ref->{attr})) {
			if(exists($driver_ref->{attr}->{name})) {
			    $rms=uc($driver_ref->{attr}->{name}); # upper case, except:  
			    &report_if_verbose("$0: check_for rms, got hint from request ... ($rms)\n");
                        }
		    }
		}
		
		if(!$options{nocheckrequest}) {
		    # check rms commands
		    if(exists($driver_ref->{command})) {
			my($key);
			foreach $key ( keys(%{$driver_ref->{command}}) ) {
			    if(exists($driver_ref->{command}->{$key}->{exec})) {
				my $cmd_key="cmd_".$key;
				$cmds{$cmd_key}=$driver_ref->{command}->{$key}->{exec};
				&report_if_verbose("$0: check_for rms, got hint from for cmd $cmd_key ... ($cmds{$cmd_key})\n");
			    }
			}
		    }
		}
	    }
	} 
    }

    if ($rms ne "undef") {
	if (do "rms/$rms/da_check_info_LML.pl") {
	    if (exists($main::check_functions->{$rms})) {
		if ( &{$main::check_functions->{$rms}}(\$rms,\%cmds,$options{verbose})) {
		    &report_if_verbose("$0: rms/$rms/da_check_info_LML.pl --> rms=$rms\n");
		} else {
		    &report_if_verbose("$0: rms/$rms/da_check_info_LML.pl unable to locate rms $rms\n");
		    $rms="undef";
		} 
	    } else { 
		&report_if_verbose("$0:  WARNING rms/$rms/da_check_info_LML.pl defines no check function\n");
		$rms="undef";
	    }
	} else {
	    &report_if_verbose("$0: ERROR could not run rms/$rms/da_check_info_LML.pl (perhaps missing return code of script)\n");
	    $rms="undef";
	}
    } else {
        my ($r,$check_f,$generate_f,$test_rms);
	my %cmds_save=(%cmds);
        foreach $r (<rms/*>) {
	    %cmds=(%cmds_save);
	    $test_rms=$r;$test_rms=~s/(.*\/)//s;
	    &report_if_verbose("$0: found $r/da_check_info_LML.pl running test ...\n");
	    if (do "$r/da_check_info_LML.pl") {
		if (exists($main::check_functions->{$test_rms})) {
		    if ( &{$main::check_functions->{$test_rms}}(\$rms,\%cmds,$options{verbose})) {
			$rms=$test_rms;
			&report_if_verbose("$0: rms/$r/da_check_info_LML.pl --> rms=$rms\n");
			last;
		    } 
		} else {
		    &report_if_verbose("$0:  WARNING rms/$r/da_check_info_LML.pl defines no check function\n");
		}
	    } else {
		&report_if_verbose("$0:  ERROR could not run rms/$r/da_check_info_LML.pl (perhaps missing return code of script)\n");
	    }
	}
    }

    if($rms eq "undef") {
	&exit_witherror($outputfile,"$0: could not determine rms, exiting ...\n");
    }
    
    if (exists($main::generate_functions->{$rms})) {
	$laststep=&{$main::generate_functions->{$rms}}($workflowxml, $laststep, \%cmds);
    }

    $step="addcolor";
    &add_exec_step_to_workflow($workflowxml,$step, $laststep, 
                               "$^X \$instdir/LML_color/LML_color_obj.pl -colordefs \$instdir/LML_color/default.conf " .
                               "-dbdir \$permdir " .
                               "-o     \$stepoutfile \$stepinfile");
    $laststep=$step;

}

#########################
# working on layout
#########################
# check if default layout should used (by request)
my $usedefaultlayout=0;
if(!$options{nocheckrequest}) {
    if(exists($filehandler_request->{DATA}->{request})) {
	if(exists($filehandler_request->{DATA}->{request}->[0]->{getDefaultData})) {
	    if($filehandler_request->{DATA}->{request}->[0]->{getDefaultData}=~/^true$/i) {
		$usedefaultlayout=1;
	    }
	}
    }
} else {
    $usedefaultlayout=1;
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
&report_if_verbose("$0: layoutfound=$layoutfound usedefaultlayout=$usedefaultlayout\n");

my $filehandler_layout;
if($usedefaultlayout) {
    $filehandler_layout=&create_default_layout($filehandler_request,$rms);
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
$demo="-demo" if $options{demo};
&add_exec_step_to_workflow($workflowxml,$step, $laststep, 
                           "$^X LML2LML/LML2LML.pl -v $demo -layout \$tmpdir/layout.xml".
                           " -output \$stepoutfile \$stepinfile");
$laststep=$step;


#########################
# Dump?
#########################
if($options{dump}) {
    print STDERR Dumper($filehandler_request->{DATA});
    print STDERR Dumper($workflowxml);
    print STDERR Dumper($filehandler_layout->{DATA});
    exit(1);
}

} #  ! $options{test}

#########################
# execute Workflow
#########################
if(! $options{test}) {
    my $workflow_obj = LML_da_workflow_obj->new($options{verbose},0);
    $workflow_obj->{DATA}=$workflowxml;
    $workflow_obj->write_xml("$tmpdir/workflow.xml");
    my $cmd="$^X ./LML_da.pl";
    $cmd .= " -v"  if($options{verbose});
    $cmd .= " -c $tmpdir/workflow.xml";
    $cmd .= " > $tmpdir/LML_da.log";
    $cmd .= " 2> $tmpdir/LML_da.errlog";
    &report_if_verbose("$0: executing: %s ...\n",$cmd);
    $tstart=time;
    system($cmd);$rc=$?;
    $tdiff=time-$tstart;
    &report_if_verbose("$0: %60s -> ready, time used %10.4ss\n","",$tdiff);
    if($rc) {     
        &exit_witherror($outputfile,"$0 failed executing: $cmd rc=$rc\n");
    }
} else {
    $laststep="LML2LML";
    my $cmd="( cd $tmpdir/..; $^X $pwd/LML_da.pl";
    $cmd .= " -v"  if($options{verbose});
    $cmd .= " -c $tmpdir/workflow.xml";
    $cmd .= " > $tmpdir/LML_da.log";
    $cmd .= " 2> $tmpdir/LML_da.errlog )";
    &report_if_verbose("$0: executing: %s ...\n",$cmd);
    $tstart=time;
    system($cmd);$rc=$?;
    $tdiff=time-$tstart;
    &report_if_verbose("$0: %60s -> ready, time used %10.4ss\n","",$tdiff);
    if($rc) {     
        &exit_witherror($outputfile,"$0 failed executing: $cmd rc=$rc\n");
    }
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
if(($removetmpdir) && (!$options{keeptmp})) {
    my $file;
    foreach $file (`ls $tmpdir`) {
        chomp($file);
#       print STDERR "unlink $tmpdir/$file\n";
        unlink("$tmpdir/$file");
    }
    if(!rmdir($tmpdir)) {
        &report("$0: could not rmdir $tmpdir ...$!, exiting ...\n");
    } else {
        &report_if_verbose("$0: tmpdir removed ($tmpdir)\n");
    }
}


&report_if_verbose("%s%s","-"x90,"\n");
&report("  LLVIEW Data Access Workflow Manager Driver $version, ending at   ($date)\n");
&report_if_verbose("%s%s","-"x90,"\n");

if(! (($removetmpdir) && (!$options{keeptmp}))) {
    close_report("$tmpdir/report.log");
}

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
                -permdir  <dir>          : use this directory for permanent data 
                                           (e.g., databases, default: ./tmp) 
                -keeptmp                 : keep temporary directory
                -test                    : use input files from temporary directory
                -demo                    : generate anonymous data
                -rms <rms>               : check only for this rms
                -nocheckrequest          : don't check request for hints 
                -verbose                 : verbose mode
                -quiet                   : prints no messages on stderr

";
}

# generate dummy LML output containing only error messages
sub exit_witherror {
    my($outputfile,$errormsg)=@_;
    my $xmlout="";

    
    $xmlout.="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
    $xmlout.="<lml:lgui xmlns:lml=\"http://eclipse.org/ptp/lml\"\n";
    $xmlout.="          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
    $xmlout.="          version=\"1.1\" xsi:schemaLocation=\"http://eclipse.org/ptp/lml http://eclipse.org/ptp/schemas/v1.1/lgui.xsd\">\n";
    $xmlout.=" <objects>\n";
    $xmlout.="    <object id=\"sys000001\" name=\"error\" type=\"system\"/>\n";
    $xmlout.="</objects>\n";

    $xmlout.="<information>\n";
    $xmlout.=" <info oid=\"sys000001\" type=\"short\">\n";
    $xmlout.="  <data key=\"errormsg\" value=\"$errormsg\"/>\n";
    $xmlout.=" </info>\n";
    $xmlout.="</information>\n";

    $xmlout.="</lml:lgui>\n";

    &report("$0: ERROR $errormsg\n");
    
    if($outputfile eq "-") {
        print $xmlout;
    } else {
        open(OUT," > $outputfile") || die "could not open for write '$outputfile'";
        print OUT $xmlout;
        close(OUT);
    }

    close_report("$tmpdir/report.log");
    
    exit(1);

}

sub create_workflow {
    my($tmpdir,$permdir)=@_;
    my($datastructref, $vardefsref);

    $vardefsref={};
    $vardefsref->{key}  ="tmpdir";
    $vardefsref->{value}="$tmpdir";
    push(@{$datastructref->{vardefs}->[0]->{var}},$vardefsref);

    $vardefsref={};
    $vardefsref->{key}  ="permdir";
    $vardefsref->{value}="$permdir";
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
    my($filehandler_request,$rms)=@_;

    my $layoutfilename="$FindBin::RealBin/samples/layout_default.xml";
    my $layoutfilename_rms="$FindBin::RealBin/samples/layout_default_$rms.xml";
    
    my $filehandler_layout = LML_file_obj->new($options{verbose},1);
    if(-f $layoutfilename_rms) {
	$filehandler_layout->read_lml_fast($layoutfilename_rms);
    } else {
	$filehandler_layout->read_lml_fast($layoutfilename);
    }
    $filehandler_layout->check_lml();
    return($filehandler_layout);
}

sub create_layout_from_request {
    my($filehandler_request)=@_;
    my($key);

    my $filehandler_layout = LML_file_obj->new($options{verbose},1);
    $filehandler_layout -> init_file_obj();
    foreach $key ("TABLELAYOUT","TABLE","NODEDISPLAYLAYOUT","NODEDISPLAY","OBJECT") {
        if (exists($filehandler_request->{DATA}->{$key})) {
            $filehandler_layout->{DATA}->{$key}=dclone($filehandler_request->{DATA}->{$key});
        }
    }
    $filehandler_layout->check_lml();
    return($filehandler_layout);
}

sub open_report {
    $REPORT="";
}


sub report {
    my $format = shift;
    # print to protocol file
    $REPORT.=sprintf( $format, @_ );

    if(!$options{quiet}) {
        # print to stderr
        printf(STDERR $format, @_);
    }
}

sub report_if_verbose {
    my $format = shift;
    # print to protocol file
    $REPORT.=sprintf( $format, @_ );
    
    if($options{verbose}) {
        # print to stderr
        printf(STDERR $format, @_);
    }
}

sub close_report {
    my($reportfile)=@_;
    if(open(REPORT,"> $reportfile")) {
        print REPORT $REPORT;
        close(REPORT);
    }
}
