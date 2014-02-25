#!/usr/bin/perl -w
#*******************************************************************************
#* Copyright (c) 2011-2014 Forschungszentrum Juelich GmbH.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*
#* Contributors:
#*    Wolfgang Frings, Carsten Karbach (Forschungszentrum Juelich GmbH) 
#*******************************************************************************/ 
use FindBin;
use lib "$FindBin::RealBin/lib";
use Data::Dumper;
use Getopt::Long;
use Time::Local;
use Time::HiRes qw ( time );
use LML_file_obj;
use LML_da_workflow_obj;
use Data_cache;
use Storable qw(dclone); 
use File::Copy;

use strict;

my $patint="([\\+\\-\\d]+)";   # Pattern for Integer number
my $patfp ="([\\+\\-\\d.E]+)"; # Pattern for Floating Point number
my $patwrd="([\^\\s]+)";       # Pattern for Work (all nonblank characters)
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
#    - determine system and select corresponding query scripts
#
# 2. running on a LML raw file
#    - designed to run on a web server as backend to 
#      web server scripts 
#    - raw file must be specified by rawfile parameter
# 
# Common steps
#    - check command line option
#    - extract options from request
#    - step 1 or 2
#    - extract layout from request, if not given
#    - create default layout
#    - build workflow input
#    - run workflow by  LML_da.pl
#    - return output file
#
#
# Example call on a torque cluster from scratch:
#
# perl LML_da_driver.pl samples/request_sample_empty.xml lml.xml -rms=torque -verbose
#
# This call will try to use the torque adapter scripts for data retrieval. An empty
# request is sent, so that LML_da needs to generate everything from scratch.
# The output file is lml.xml and verbose print outs are requested. 
#

# option handling
my $hostname = `hostname`;chomp($hostname);
my $ppid = $$;
my %options = (
    "rawfile"             => "", # If used, a very simple workflow is generated consisting of step 1: copy this rawfile, step 2: call LML2LML with a given layout
    "tmpdir"              => "./tmp_".$hostname."_".$ppid, # Path to the temporary directory used for step data
    "permdir"             => "./perm_".$hostname, # Path to the permanent directory used over multiple LML_da calls
    "keeptmp"             => 0, # If true, do not delete the temp directory after LML generation is completed. Tmp directory is only deleted, if it was created by this script.
    "keepperm"            => 1,
    "verbose"             => 0, # If true, much debugging information is printed to STDOUT. Otherwise, only start and completion of the script are indicated by a print-out to stderr.
    "quiet"               => 0, # Do not print anything to stderr
    "nocheckrequest"      => 0, # The LML request can contain an RMS hint similar to the following rms option. If this option here is set to true, the rms hints from the request are ignored.
    "rms"                 => "undef", # A hint for the RMS type of the monitored system, if this hint is omitted, the driver tries to determine the target system on its own.
    "dump"                => 0, # If true, the given request object, workflow object and layout object are dumped to stderr
    "demo"                => 0, # generate anonymous data, if true
    "test"                => 0, # If true, do not generate the workflow and layout file. This can be used for automated testing.
    "cache"				  => 1,  # Use caching mechanism via /tmp directory. Try to find an existing raw LML file. If the file is not existant or it is too old, generate it. Caching is omitted, if test is enabled.
    "cachedir"			  => "/tmp/LMLCache_".$hostname."/", #Directory used for caching raw LML data, only needed if cache is activated
    "cacheinterval"		  => "60" #Update interval in seconds for the cache, if it is used
);
my @save_ARGV=(@ARGV);
my @options_from_file_found = ();

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
     		            	'dump'             => \$options{dump},
     		            	'cache=s'		   => \$options{cache},
     		            	'cachedir=s'	   => \$options{cachedir},
     		            	'cacheinterval=s'  => \$options{cacheinterval}
                            ) );
my $date=`date`;
chomp($date);

my $REPORT;

&open_report();
#Overwrite the given options with those placed in the options file
my $options_file=".LML_da_options";
overwriteOptionsWithLMLDAOptionsFile();

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

# check request input file, parse the file into hash datastructure
my $startRequestLocation = "./request_".$hostname."_".$ppid.".xml";
my $filehandler_request = parseLMLRequest($requestfile, $startRequestLocation, $options{verbose});
my @options_from_request = ();
#Try to parse additional options from the LML request
if(!$options{nocheckrequest} && defined($filehandler_request->{DATA}->{REQUEST}->{driver}) ){
	my $driver_ref=$filehandler_request->{DATA}->{REQUEST}->{driver};
	my %optionsFromRequest = parseOptionsFromRequest($driver_ref);

	foreach my $attribute( keys(%optionsFromRequest) ){
		my $value = 0;
		if(defined($optionsFromRequest{$attribute} ) ){
			$value = $optionsFromRequest{$attribute};
		}
		
		$options{$attribute} = $value;
		
		push(@options_from_request, $attribute);
	}
	
	#Overwrite with options from LML_da_options file again as this file has highest priority
	overwriteOptionsWithLMLDAOptionsFile();
}
#Join all options used in the script
my $actualOptions = "";
foreach my $argument (keys(%options)){
	my $value = "";
	if(defined($options{$argument}) ){
		$value = $options{$argument};
	}
	$actualOptions = $actualOptions.$argument."=>".$value." ";
}

# print header
&report_if_verbose("%s%s","-"x90,"\n");
&report("%s","  LLVIEW Data Access Workflow Manager Driver $version, starting at ($date)\n");
&report_if_verbose("  %s%s%s"," command line args: ",join(" ",@save_ARGV),"\n"); 
&report_if_verbose("  %s%s%s%s"," request file args: ",join(" ",@options_from_request),"     (from LML request)","\n") if($#options_from_request>=0);
&report_if_verbose("  %s%s%s%s"," option file  args: ",join(" ",@options_from_file_found),"     (from file $options_file)","\n") if($#options_from_file_found>=0);
&report_if_verbose("  %s", " final options used are: ".$actualOptions."\n" );
&report_if_verbose("%s%s", "-"x90,"\n");

my $tmpdir       = $options{tmpdir};
my $permdir       = $options{permdir};
my $rawfile      = undef;
my $removetmpdir = 0; # remove only if directory was create 

my $workflowxml = "";
my $laststep    = "";

my $cache; #Helping object for caching raw lml files, only used if options{cache} is true
my $updateCacheFile=0;# Set to 1, if caching is active and file update is required.
my $cacheIsBeingUpdatedAlready=0;# Set to 1, if another lml_da instance is currently updating the lml cache, wait for the other instance to finish

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
#Move the request file in root directory to the used tmp directory
move($startRequestLocation, $tmpdir."/request.xml");

# check permanent directory
if(! -d $permdir) {
    &report_if_verbose("$0: permanent directory not found, create new directory $permdir ...\n");
    if(!mkdir($permdir,0755)) {
        &exit_witherror($outputfile,"$0: could not create $permdir ...$!\n");
    } else {
        &report_if_verbose("%s", "$0: permdir created ($permdir)\n");
    }
}

if( $options{cache} ){
	$cache = Data_cache->new($options{cachedir}, $options{cacheinterval});#With negative cache interval, this is automatically made to a read only cache
	
	if(! $cache->isCacheUsable() ){ #Deactivate caching, if it is not allowed on this system
		$options{cache} = 0;
		&report_if_verbose("%s", "$0: Cannot use cache directory ".$cache->getCacheDirectory()."\n");
	}
	else{
	
		if($cache->isAlreadyUpdating()){
			$cacheIsBeingUpdatedAlready = 1;
			&report_if_verbose("%s", "$0: Another LML_da instance is currently updating raw data, will wait for it to finish\n");
		}
		
		if($cache->isUpdateRequired() && !$cacheIsBeingUpdatedAlready ){#Update only, if there is not another lml_da instance aready updating
			$updateCacheFile = 1;
			&report_if_verbose("%s", "$0: A cache update is required, will perform raw data update\n");
		}
		
		if(!$updateCacheFile && !$cacheIsBeingUpdatedAlready){
			&report_if_verbose("%s", "$0: Reusing existing raw LML data from cache\n");
		}
	
	}
}

# check rawfile
if($options{rawfile}) {
    if(! -f $options{rawfile}) {
        &exit_witherror($outputfile,"$0: rawfile $options{rawfile} specified but not found, exiting ...\n");
    }
    $rawfile=$options{rawfile};
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
} elsif( $options{cache} && ! $updateCacheFile){ #Caching is active and existing raw file can be used
	
	&report_if_verbose("%s","Caching is active, no update of raw data required, copying file from cache\n");
	
	#copy cached raw file in workflow
	$step="cachefilecp";
	my $cachedrawfile = $cache->getRawFilePath();
	add_exec_step_to_workflow($workflowxml,$step, $laststep, 
                                   "cp $cachedrawfile \$stepoutfile");
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
    my $colorpermdir = "\$permdir"; #The directory used for remembering LML colors over multiple sessions
    if($updateCacheFile ){
    	$colorpermdir = $cache->getCacheDirectory();
    }
    &add_exec_step_to_workflow($workflowxml,$step, $laststep, 
                               "$^X \$instdir/LML_color/LML_color_obj.pl -colordefs \$instdir/LML_color/default.conf " .
                               "-dbdir $colorpermdir " .
                               "-o     \$stepoutfile \$stepinfile");
    $laststep=$step;

	#Copy the raw file with added colors to the LML cache, if requested
	if($updateCacheFile ){
		
		$step="cacherawfile";
		my $cachedrawfile = $cache->getRawFilePath();
	    &add_exec_step_to_workflow($workflowxml,$step, $laststep, 
	                               "cp \$stepinfile $cachedrawfile.new", 
	                               "mv $cachedrawfile.new $cachedrawfile");#For most possible atomicity use mv
	    $laststep=$step;
		
	}
}

#########################
# working on layout
#########################
# check, if default layout should be used (by request)
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

	if($cacheIsBeingUpdatedAlready){
    	report_if_verbose("$0: %s\n","Waiting for an updating lml_da instance to finish the cache update");
    	$cache->waitUntilMutexIsUnlocked();
    	report_if_verbose("$0: %s\n","Cache was updated");
    }
    
    if($updateCacheFile){
    	report_if_verbose("$0: %s\n","Waiting for cache mutex to be available");
    	$cache->startMutex();
    	report_if_verbose("$0: %s\n","Entered cache mutex");
    }
    
    &report_if_verbose("$0: executing: %s ...\n",$cmd);
    $tstart=time;
    system($cmd);$rc=$?;
    $tdiff=time-$tstart;
    &report_if_verbose("$0: %60s -> ready, time used %10.4ss\n","",$tdiff);
    
    if($updateCacheFile){
    	$cache->stopMutex();
    	report_if_verbose("$0: %s\n","Unlocked cache mutex");
    }
    
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
                -cache=0/1               : activate/deactivate caching raw LML files
                -cachedir=<dir>          : directory for storing cache files
                -cacheinterval=s         : time in seconds until the cache is updated 

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


#***********************************************************************************
#
# There are three ways to provide options for this driver script:
#  1) Pass options via the calling command, e.g. LML_da.pl -tmpdir=./mytmp -verbose request.xml
#  2) Provide arguments in the LML input file, e.g. 
#		<driver name="TORQUE">
#			<arg attribute="verbose" />
#			<arg attribute="keeptmp" />
#			<arg attribute="tmpdir" value="./mytmp" />
#		</driver>
#  3) Store options in .LML_da_options file, e.g.
#     keeptmp=1
#     verbose=1
#
# The options are evaluated in this order. I.e. options of the LML request overwrite
# options handed via command line, and options stored in the .LML_da_options file
# overwrite those of the previous option definitions. Note, that the LML request
# checking can be deactivated with the nocheckrequest option. 
#
#
# @param $_[0] reference to the used driver data, this must represent
#               one driver as shown in LML_file_obj.pm, it grants access to
#               options, rms hints and commands
#
# @return hash of options parsed from the driver reference
#
#***********************************************************************************
sub parseOptionsFromRequest{
	my $driverRef = shift;
	my %parsedOptions;
	
	foreach my $attribute ( keys(%{$driverRef->{args}}) ){
		my $value = $driverRef->{args}->{$attribute};
		if(!defined($value)){ #Set value to 1, if it is not defined. This allows to activate some option by ommitting a value
			$value = 1;
		}
		$parsedOptions{$attribute} = $value;
	}
	
	return %parsedOptions;
}

#****************************************************************************
# Converts the LML request into a PERL hash data structure. Uses LML_file_obj
# for the conversion. Exits the driver, if requestfile cannot be found or
# if it cannot be parsed correctly. The request file generated for further
# processing is placed at the path generatedRequestPath.
#
# @param $_[0] the path to the LML request file or "-", 
#              if the request should be parsed from STDIN
#
# @param $_[1] generatedRequestPath path to the request file, which is later parsed by LML_file_obj
#
# @param $_[2] 1, if verbose mode is activated, 0 otherwise
#
# @return reference to a datastructure parsed from LML_file_obj holding all
#			data parsed from the LML request
#
#****************************************************************************
sub parseLMLRequest{
	
	my $requestPath = shift;
	my $generatedRequestPath = shift;
	my $isVerbose = shift;
	
	# check request input file
	if ($requestPath ne "-") {
    	if(! -f $requestPath) {
        	&exit_witherror($outputfile,"$0: requestPath $requestPath not found, exiting ...\n");
    	}
	}

	# read config file
	&report_if_verbose("%s", "$0: requestPath=$requestPath\n");
	my $tstart=time;

	# debug request file, copy request into special file
	open(OUT,"> $generatedRequestPath");
	#Read LML request from STDIN
	if ($requestPath eq "-") {
    	while(<>) {
        	print OUT $_;
    	}
    	$requestPath=$generatedRequestPath;
	} else { #Read LML request from specific request path passed as argument
    	open(IN,$requestPath);
    	while(<IN>) {
        	print OUT $_;
    	}
    	close(IN);
	}
	close(OUT);

	my $filehandler_request = LML_file_obj->new($isVerbose,1);
	$filehandler_request->read_lml_fast($requestPath);
	my $tdiff=time-$tstart;
	&report_if_verbose("$0: parsing XML requestPath in %6.4f sec\n",$tdiff);
	if(!$filehandler_request) {
    	&exit_witherror($outputfile,"$0: could not parse requestPath $requestPath, exiting ...\n");
	}
	
	return $filehandler_request;
}

#********************************************************************
# This function tries to parse the LML_da_options file, if it exists.
# Available options are placed into the %options hash. Options,
# which were successfully parsed, are placed into the 
# @options_from_file_found array.
# As a result, this function adapts the global variables 
# @options_from_file_found and %options.
#********************************************************************
sub overwriteOptionsWithLMLDAOptionsFile{
	@options_from_file_found = ();
	
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
}
