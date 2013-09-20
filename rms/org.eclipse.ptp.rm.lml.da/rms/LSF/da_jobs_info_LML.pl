#!/usr/bin/perl -w
#*******************************************************************************
#* Copyright (c) 2012 Forschungszentrum Juelich GmbH.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*
#* Contributors:
#*    Wolfgang Frings, Carsten Karbach (Forschungszentrum Juelich GmbH) 
#*******************************************************************************/ 
use strict;

use FindBin;
use lib "$FindBin::RealBin/../../lib";
use LML_da_util;

my $srcDir = $0;
if($srcDir =~ /\// ){
	$srcDir =~ s/[^\/]+\.pl$//;
}
else{
	$srcDir = "./";
}

require $srcDir."helper_functions.pl";#Include shared helper functions

my $patint="([\\+\\-\\d]+)";   # Pattern for Integer number
my $patfp ="([\\+\\-\\d.E]+)"; # Pattern for Floating Point number
my $patwrd="([\^\\s]+)";       # Pattern for Work (all noblank characters)
my $patbl ="\\s+";             # Pattern for blank space (variable length)
my $patdate = "^(.*):";		   # Pattern for date format, allow any date format here

#LML definitions
my $schemaURL = "http://eclipse.org/ptp/lml";

#####################################################################
# get user info / check system 
#####################################################################
my $UserID = getpwuid($<);
my $Hostname = `hostname`;
my $verbose=1;
my ($line,%jobs,%jobnr,$key,$value,$count,%notmappedkeys,%notfoundkeys);

#####################################################################
# get command line parameter
#####################################################################
if ($#ARGV != 0) {
  die " Usage: $0 <filename> $#ARGV\n";
}
my $filename = $ARGV[0];

# Mapping of command output key names to LML key names
my %mapping = (
    "Job"                               	 => "name",
    "User"		                             => "owner",
    "Job Priority"                           => "priority",
    "JobPriority"                            => "priority",
    "Started on"                             => "nodelist",
    "Startedon"                              => "nodelist",
    "Status"                            	 => "status",
    "state"                            	 	 => "state",
    "SUBMIT_TIME"                            => "queuedate",
    "Queue"                                  => "queue",
    "START_TIME"                             => "dispatchdate",
    "CWD"                          			 => "",
    "Submitted from host"                    => "",
    "Submittedfrom host"                     => "",
    "Submittedfromhost"                      => "",
    "Submitted fromhost"                     => "",
    "Requested Resources"                    => "",
    "RequestedResources"                     => "",
    "Job Group"                              => "group",
    "JobGroup"                               => "group",
    "Project"							     => "",
    "Totalcores"							 => "totalcores",
    "Totaltasks"							 => "totaltasks",
    "detailedstatus"						 => "detailedstatus",
	"totalgpus"                              => "",
	"Command"                                => "",
	"Error File"                             => "",
	"ErrorFile"                              => "",
	"Error File (overwrite)"                 => "",
	"Error File(overwrite)"                  => "",
	"ErrorFile(overwrite)"                   => "",
	"ErrorFile (overwrite)"                  => "",
	"Input File"                             => "",
	"InputFile"                              => "",
	"Job Name"                               => "",
	"JobName"                                => "",
	"Mail"                                   => "",
	"Output File"                            => "",
	"OutputFile"                             => "",
	"Output File (overwrite)"                => "",
	"Output File(overwrite)"                 => "",
	"OutputFile (overwrite)"                 => "",
	"OutputFile(overwrite)"                  => "",
	"Share group charged"                    => "",
	"Sharegroup charged"                     => "",
	"Sharegroupcharged"                      => "",
	"Share groupcharged"                     => "",
	"vnodelist"								 => "vnodelist",
	"step"								 	 => "step",
	"wall"									 => "wall"
    );

#####################################################################
# Start parsing command's output
#####################################################################
my $cmd="/usr/bin/bjobs";
$cmd=$ENV{"CMD_JOBINFO"} if($ENV{"CMD_JOBINFO"}); 

open(IN,"$cmd -u all -r -p -s -l |");
my $jobid="-";
my $lastkey="-";
my $data="";
my $jobcount = 0;
my $totalcores = 1;

while($line=<IN>) {
    chomp($line);
    #Parse the first data block, which looks like this:
    #Job <ID>, User <username>, Project <project>, Job Group <group>, Sta
    #                 tus <status>, ... 
    #                 Command <command>
    if($line =~ /Job\s+<(\S+)>/ ){
    	$jobid = $1;
    	$jobid =~ s/\[/_/;
    	$jobid =~ s/\]/_/;
    	$totalcores = 1;
    	$jobcount++;
    	#Collect all lines, which contain simple key value job data
    	$data = concatLines(*IN, $line);
    	
    	# Check for BSUB -W to detect wall clock limits
    	if($data =~ /BSUB\s*\-W\s*(\d+):(\d+)/ ){
    		$jobs{$jobid}{wall} = $1*60+$2;
    	}
    	elsif($data =~ /BSUB\s*\-W\s*(\d+)/ ){
    		$jobs{$jobid}{wall} = $1;
    	}
    	
    	my %keyvalues = %{extractKeyValuePairs($data)};
    	#Transfer key value pairs into job hash
    	foreach $key (keys(%keyvalues)){
    		$jobs{$jobid}{$key} = $keyvalues{$key};
    	}
    }
    #Parse the second data block, which looks like this:
    #Thu Nov 15 07:18:41: Submitted from host <login1>, CWD <$HOME>, Requested Resou
    #                 rces <select[memavail>=(1700+slotsavail*300)&&maxmemperslo
    #                t>=2000&&mempercore<=4000] rusage[memavail=2000,slotsavail
    #                =1] span[hosts=1] order[-memavail_perslot:-slotsavail:-mem
    #                 percore]>;
    if($line =~ /$patdate\s*Submitted/ ){
    	my $submitdate = $1;
    	my $year = getCurrentYear();
    	if(index($submitdate, $year) == -1 ){
    		$submitdate.=" ".getCurrentYear();#Attach the current year to this date -> LML_da lib/LML_da_date_manip.pm expects date format e.g. Wed Nov 21 14:10:08 2012
    	}
    	#Store submit time
    	$jobs{$jobid}{SUBMIT_TIME} = $submitdate;
    	
    	#Remove date string from key-value string
    	$line =~ s/$patdate//;
    	
    	#Collect all lines, which contain simple key value job data
    	$data = concatLines(*IN, $line);
    	
    	#Check for total processor count given by outputs like "2 Processors Requested", which seems 
    	#to be available only for jobs with multiple processorcs requested.
    	if($data =~ /(\d+)\s+Processors Requested/ ){
    		$totalcores = $1;
    		$jobs{$jobid}{Totalcores} = $totalcores;
    	}
    	
    	my %keyvalues = %{extractKeyValuePairs($data)};
    	#Transfer key value pairs into job hash
    	foreach $key (keys(%keyvalues)){
    		$jobs{$jobid}{$key} = $keyvalues{$key};
    	}
    }
    #Parse start data block, which looks like this:
    #Thu Nov 15 07:18:51: Started on <n065>;
    #
    #A job might also start on multiple nodes:
    #Fri Nov 16 07:58:23: Started on 128 Hosts/Processors <32*n055> <32*n082> <32*n0
    #                 73> <32*n077>, Execution Home </home/chuantu>, Execution C
    #                 WD </home/chuantu/fastfs/C_jump/T0.85N0.78_t1p50/restart>;
    #
    #This block might also look like this:
    #
    #Fri Nov 16 15:25:56: [43] started on <n064>, Execution Home </home/sureinh>, Ex
    #                 ecution CWD </fastfs/sureinh/BatchJobs>;
    #Parse special lines for recently dispatched jobs, which can look like this:
    #Wed Nov 21 15:04:07: Dispatched to <n061>;
    if($line =~ /$patdate\s*(\[.+\])?\s*(((S|s)tarted)|((D|d)ispatched))/ ){
    	my $dispatchdate = $1;
    	my $year = getCurrentYear();
    	if(index($dispatchdate, $year) == -1 ){
    		$dispatchdate.=" ".getCurrentYear();#Attach the current year to this date -> LML_da lib/LML_da_date_manip.pm expects date format e.g. Wed Nov 21 14:10:08 2012
    	}
    	#Store dispatch time
    	$jobs{$jobid}{START_TIME} = $dispatchdate;

		#Concat all lines connected to the nodes information, on which this job is started
	   	$data = concatLines(*IN, $line);
		#Parse total tasks (e.g. "Started on <n065>" -> 1 task, "Started on 128 Hosts/Processors" -> 128) 	
    	if($data =~ /(S|s)tarted on (\d+) Hosts/ ){
    		$totalcores = $2;
    	}
    	if($data =~ /(D|d)ispatched to (\d+) Hosts/ ){
    		$totalcores = $2;
    	}
    	#Note: $totalcores can also be set by the previous if statement
    	$jobs{$jobid}{Totalcores} = $totalcores;
    	
    	#Parse node list
    	my $nodelist = "";
    	if($data =~ /((<[^<>]+>\s*)+)/ ){
    		my $nodes = removeWhiteSpaces($1);
    		
    		my @nodeentries = split(/>\s*</, $nodes);
    		foreach my $node (@nodeentries){
    			$node = removeWhiteSpaces($node);
    			$node =~ s/^<//;
    			$node =~ s/>$//;
    			
    			if($node =~ /(\d+)\*(\S+)/){
    				$nodelist = $nodelist."(".$2.",".$1.")";
    			}
    			else{
    				$nodelist = $nodelist."(".$node.",1)";
    			}
    		}
    	}
    	
    	#Generate expanded nodelist containing each core used by this job
    	$jobs{$jobid}{"vnodelist"} = $nodelist;
    }
}
close(IN);

# add unknown but mandatory attributes to jobs
foreach $jobid (keys(%jobs)) {
    $jobs{$jobid}{"Job Group"}      = "unknown" if(!exists($jobs{$jobid}{"Job Group"}) && !exists($jobs{$jobid}{"JobGroup"}));
    $jobs{$jobid}{"Started on"}  = "-" if(!exists($jobs{$jobid}{"Started on"}));
    $jobs{$jobid}{Totalcores} = 1 if(!exists($jobs{$jobid}{Totalcores}));
    $jobs{$jobid}{Totaltasks} = $jobs{$jobid}{Totalcores};
    $jobs{$jobid}{totalgpus}  = 0 if(!exists($jobs{$jobid}{totalgpus}));
    # check state and map all kinds of job states to submitted, completed and running
    ($jobs{$jobid}{Status},$jobs{$jobid}{detailedstatus}) = get_state($jobs{$jobid}{Status}); 
    $jobs{$jobid}{state} = $jobs{$jobid}{Status};
    
    $jobs{$jobid}{step} = $jobid;
    
}

#Generate output LML file
open(OUT,"> $filename") || die "cannot open file $filename";
printf(OUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
printf(OUT "<lml:lgui xmlns:lml=\"$schemaURL\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
printf(OUT "	xsi:schemaLocation=\"$schemaURL http://eclipse.org/ptp/schemas/v1.1/lgui.xsd\"\n");
printf(OUT "	version=\"1.1\"\>\n");
printf(OUT "<objects>\n");
$count=0;

my @sortedJobIDs = sort{ jobCompare($a,$b) } keys(%jobs);

foreach $jobid (@sortedJobIDs) {
    $count++;$jobnr{$jobid}=$count;
    printf(OUT "<object id=\"j%06d\" name=\"%s\" type=\"job\"/>\n",$count,&LML_da_util::escapeForXML($jobid));
}
printf(OUT "</objects>\n");
printf(OUT "<information>\n");
foreach $jobid (@sortedJobIDs) {
    printf(OUT "<info oid=\"j%06d\" type=\"short\">\n",$jobnr{$jobid});
    foreach $key (sort(keys(%{$jobs{$jobid}}))) {
	if(exists($mapping{$key})) {
	    if($mapping{$key} ne "") {
		$value=&modify($key,$mapping{$key},$jobs{$jobid}{$key});
		if($value) {
		    printf(OUT " <data %-20s value=\"%s\"/>\n","key=\"".$mapping{$key}."\"",&LML_da_util::escapeForXML($value));
		}
	    } else {
		$notmappedkeys{$key}++;
	    }
	} else {
	    $notfoundkeys{$key}++;
	}
    }
    printf(OUT "</info>\n");
}
printf(OUT "</information>\n");
 
printf(OUT "</lml:lgui>\n");

close(OUT);
#Print unmapped keys, these keys could be added to the job's data attributes
foreach $key (sort(keys(%notfoundkeys))) {
    printf("%-40s => \"\",\n","\"".$key."\"",$notfoundkeys{$key});
}

#*******************************************
# Maps all different kinds of possible job states to
# three major job states (completed, submitted and running) along
# with a detailed status description.
#
# @param $_[0] the job state directly parsed from the command's output
#
# @return (major status, detailed status description)
#*******************************************
sub get_state {
    my $job_state=shift;
    my($state,$detailed_state);

    $state="UNDETERMINED";$detailed_state="";

	if($job_state eq "EXIT") {
	$state="COMPLETED";$detailed_state="FAILED";
    }
    if($job_state eq "DONE") {
	$state="COMPLETED";$detailed_state="JOB_OUTERR_READY";
    }
    if($job_state eq "SSUSP") {
	$state="SUSPENDED";$detailed_state="SYSTEM_SUSPENDED";
    }
    if($job_state eq "ZOMBI") {
	$state="COMPLETED";$detailed_state="FAILED";
    }
    if($job_state eq "PSUSP") {
	$state="SUBMITTED";$detailed_state="USER_ON_HOLD";
    }    
    if($job_state eq "PEND") {
	$state="SUBMITTED";$detailed_state="QUEUED_ACTIVE";
    }
    if($job_state eq "WAIT") {
	$state="SUBMITTED";$detailed_state="QUEUED_ACTIVE";
    }      
    if($job_state eq "USUSP") {
	$state="SUSPENDED";$detailed_state="USER_SUSPENDED";
    }    
    if($job_state eq "RUN") {
	$state="RUNNING";$detailed_state="";
    }    

   
    return($state,$detailed_state);
}

#**********************************
# Adjusts value for a job's attribute.
# Especially makes sure, that special characters are escaped in attribute values.
#
# @param $_[0] the original key name parsed from the command's output
# @param $_[1] the mapped key name provided by the global mapping hash
# @param $_[2] the raw value parsed from the command's output
# @return the adjusted value, which can be printed into the XML file
#**********************************
sub modify {
    my($key,$mkey,$value)=@_;
    my $ret=$value;

    if(!$ret) {
	return(undef);
    }

    if(($mkey eq "comment")) {
	$ret=~s/\"//gs;
    }
    return($ret);
}


#**********************************
# Compares two jobs for sorting the list of all jobs.
#
# @param $_[0] job a
# @param $_[1] job b
# @return -1 if job a is assumed to be smaller than job b, 1 for job a > job b and 0 for equal ordering
#**********************************
sub jobCompare{
	
	my $aID = shift;
	my $bID = shift;
	#Running jobs have to be listed first
	if( $jobs{$aID}{state} eq "RUNNING" && $jobs{$bID}{state} ne "RUNNING" ){
		return -1;
	}
	
	if( $jobs{$aID}{state} ne "RUNNING" && $jobs{$bID}{state} eq "RUNNING" ){
		return 1;
	}
	#If none of the jobs is running, compare them by their IDs
	return $aID cmp $bID;
}
