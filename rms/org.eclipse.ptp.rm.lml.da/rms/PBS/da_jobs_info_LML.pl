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
use strict;

my $patint="([\\+\\-\\d]+)";   # Pattern for Integer number
my $patfp ="([\\+\\-\\d.E]+)"; # Pattern for Floating Point number
my $patwrd="([\^\\s]+)";       # Pattern for Work (all noblank characters)
my $patbl ="\\s+";             # Pattern for blank space (variable length)

#####################################################################
# get user info / check system 
#####################################################################
my $UserID = getpwuid($<);
my $Hostname = `hostname`;
my $verbose=1;
my ($line,%jobs,%jobnr,$key,$value,$count,%notmappedkeys,%notfoundkeys);

#unless( ($Hostname =~ /jugenes\d/) && ($UserID =~ /llstat/) ) {
#  die "da_jobs_info_LML.pl can only be used as llstat on jugenesX!";
#}

#####################################################################
# get command line parameter
#####################################################################
if ($#ARGV != 0) {
  die " Usage: $0 <filename> $#ARGV\n";
}
my $filename = $ARGV[0];

my $system_sysprio=-1;
my $maxtopdogs=-1;

my %mapping = (
    "Checkpoint"                             => "",
    "Error_Path"                             => "",
    "Hold_Types"                             => "",
    "Job_Name"                               => "name",
    "Job_Owner"                              => "owner",
    "Join_Path"                              => "",
    "Keep_Files"                             => "",
    "Mail_Points"                            => "",
    "Mail_Users"                             => "",
    "Output_Path"                            => "",
    "Priority"                               => "",
    "Rerunable"                              => "restart",
    "Resource_List.cput"                     => "",
    "Resource_List.depend"                   => "dependency",
    "Resource_List.mem"                      => "",
    "Resource_List.nodect"                   => "",
    "Resource_List.pmem"                     => "",
    "Resource_List.walltime"                 => "wall",
    "Shell_Path_List"                        => "",
    "Walltime.Remaining"                     => "",
    "comment"                                => "",
    "ctime"                                  => "",
    "depend"                                 => "",
    "etime"                                  => "",
    "exec_host"                              => "nodelist",
    "exec_vnode"                             => "vnodelist",
    "exit_status"                            => "",
    "fault_tolerant"                         => "",
    "interactive"                            => "",
    "job_state"                              => "state",
    "mtime"                                  => "dispatchdate",
    "qtime"                                  => "queuedate",
    "queue"                                  => "queue",
    "resources_used.cput"                    => "",
    "resources_used.mem"                     => "",
    "resources_used.vmem"                    => "",
    "resources_used.walltime"                => "",
    "server"                                 => "",
    "session_id"                             => "",
    "start_count"                            => "",
    "start_time"                             => "",
    "submit_args"                            => "",

    "step"                                   => "step",
    "totaltasks"                             => "totaltasks",
    "totalcores"                             => "totalcores",
    "spec"                                   => "spec",

    "status"                                 => "status",
    "detailedstatus"                         => "detailedstatus",

    "Resource_List.backfill"                 => "",
    "Resource_List.bandwidth"                => "",
    "Resource_List.enabled"                  => "",
    "Resource_List.job_type"                 => "",
    "Resource_List.ncpus"                    => "",
    "Resource_List.nightrun"                 => "",
    "Resource_List.node_type"                => "",
    "Resource_List.operational"              => "",
    "Resource_List.place"                    => "",
    "Resource_List.select"                   => "",
    "Submit_arguments"                       => "",
    "Variable_List"                          => "",
    "alt_id"                                 => "",
    "jobdir"                                 => "",
    "pset"                                   => "",
    "resources_used.cpupercent"              => "",
    "resources_used.ncpus"                   => "",
    "stime"                                  => "",
    "substate"                               => "",

# unknown attributes
    "group"                                  => "group",
    "Account_Name"                           => "",
    "Exit_status"                            => "",
    "Resource_List.Qlist"                    => "",
    "estimated.exec_vnode"                   => "",
    "estimated.start_time"                   => "",
    "Resource_List.nodes"                    => "",
    "argument_list"                          => "",
    "array"                                  => "",
    "array_indices_remaining"                => "",
    "array_indices_submitted"                => "",
    "array_state_count"                      => "",
    "executable"                             => "",
    "group_list"                             => "",
    "umask"                                  => "",
    );

my $cmd="/usr/bin/qstat";
$cmd=$ENV{"CMD_JOBINFO"} if($ENV{"CMD_JOBINFO"}); 

open(IN,"$cmd -f |");
my $jobid="-";
my $lastkey="-";

while($line=<IN>) {
    chomp($line);
    if($line=~/Job Id: ([^\s]+)$/) {
	$jobid=$1;
	$jobs{$jobid}{step}=$jobid;
#	print "line $line\n";
    } elsif($line=~/^\s+([^\:]+)\s+\=\s+(.*)$/) {
	($key,$value)=($1,$2);
	$key=~s/\s/_/gs;
	$lastkey=$key;
	$jobs{$jobid}{$key}=$value;
    } else {
	$line=~s/^\s*//gs;
	$jobs{$jobid}{$lastkey}.=$line;
    }
}
close(IN);

# add unknown but manatory attributes to jobs
foreach $jobid (sort(keys(%jobs))) {
    $jobs{$jobid}{group}      = "unknown" if(!exists($jobs{$jobid}{group}));
    $jobs{$jobid}{exec_host}  = "-" if(!exists($jobs{$jobid}{exec_host}));
    if(!exists($jobs{$jobid}{totaltasks})) {
	$jobs{$jobid}{totaltasks} = $jobs{$jobid}{"Resource_List.nodes"} if(exists($jobs{$jobid}{"Resource_List.nodes"}));
	$jobs{$jobid}{totaltasks} = $jobs{$jobid}{"Resource_List.ncpus"} if(exists($jobs{$jobid}{"Resource_List.ncpus"}));
    }
    if(!exists($jobs{$jobid}{spec})) {
	$jobs{$jobid}{spec}       = $jobs{$jobid}{"Resource_List.nodes"} if(!exists($jobs{$jobid}{"Resource_List.nodes"}));
	$jobs{$jobid}{spec}       = $jobs{$jobid}{"Resource_List.ncpus"} if(!exists($jobs{$jobid}{"Resource_List.ncpus"}));
	
    }    
    # check state
    ($jobs{$jobid}{status},$jobs{$jobid}{detailedstatus}) = &get_state($jobs{$jobid}{job_state},
								       $jobs{$jobid}{Hold_Types}); 
    $jobs{$jobid}{"totalcores"}=$jobs{$jobid}{"totaltasks"} if(!exists($jobs{$jobid}{"totalcores"}));
}

open(OUT,"> $filename") || die "cannot open file $filename";
printf(OUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
printf(OUT "<lml:lgui xmlns:lml=\"http://eclipse.org/ptp/schemas\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
printf(OUT "	xsi:schemaLocation=\"http://eclipse.org/ptp/schemas http://eclipse.org/ptp/schemas/lgui.xsd\"\n");
printf(OUT "	version=\"0.7\"\>\n");
printf(OUT "<objects>\n");
$count=0;
foreach $jobid (sort(keys(%jobs))) {
    $count++;$jobnr{$jobid}=$count;
    printf(OUT "<object id=\"j%06d\" name=\"%s\" type=\"job\"/>\n",$count,$jobid);
}
printf(OUT "</objects>\n");
printf(OUT "<information>\n");
foreach $jobid (sort(keys(%jobs))) {
    printf(OUT "<info oid=\"j%06d\" type=\"short\">\n",$jobnr{$jobid});
    foreach $key (sort(keys(%{$jobs{$jobid}}))) {
	if(exists($mapping{$key})) {
	    if($mapping{$key} ne "") {
		$value=&modify($key,$mapping{$key},$jobs{$jobid}{$key});
		if($value) {
		    printf(OUT " <data %-20s value=\"%s\"/>\n","key=\"".$mapping{$key}."\"",$value);
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

foreach $key (sort(keys(%notfoundkeys))) {
    printf("%-40s => \"\",\n","\"".$key."\"",$notfoundkeys{$key});
}


sub get_state {
    my($job_state,$Hold_types)=@_;
    my($state,$detailed_state);

    $state="UNDETERMINED";$detailed_state="";

    if($job_state eq "C") {
	$state="COMPLETED";$detailed_state="JOB_OUTERR_READY";
    }
    if($job_state eq "H") {
	$state="SUBMITTED";
	$detailed_state="USER_ON_HOLD"   if($Hold_types eq "u");
	$detailed_state="SYSTEM_ON_HOLD" if($Hold_types eq "s");
	$detailed_state="USER_SYSTEM_ON_HOLD" if($Hold_types=~"(us|su)");
	$detailed_state="SYSTEM_ON_HOLD" if($Hold_types eq "o");
    }
    if($job_state eq "E") {
	$state="COMPLETED";$detailed_state="JOB_OUTERR_READY";
    }    
    if($job_state eq "Q") {
	$state="SUBMITTED";$detailed_state="";
    }    
    if($job_state eq "W") {
	$state="SUBMITTED";$detailed_state="";
    }    
    if($job_state eq "T") {
	$state="SUBMITTED";$detailed_state="";
    }    
    if($job_state eq "R") {
	$state="RUNNING";$detailed_state="";
    }    

    return($state,$detailed_state);
}

sub modify {
    my($key,$mkey,$value)=@_;
    my $ret=$value;

    if(!$ret) {
	return(undef);
    }

    if($mkey eq "owner") {
	$ret=~s/\@.*//gs;
    }

    if($mkey eq "state") {
	$ret="Completed"   if ($value eq "C");
	$ret="Removed"     if ($value eq "E");
	$ret="System Hold" if ($value eq "H");
	$ret="Idle"        if ($value eq "Q");
	$ret="Idle"        if ($value eq "W");
	$ret="Idle"        if ($value eq "T");
	$ret="Running"     if ($value eq "R");
	$ret="System Hold" if ($value eq "S");
    }

    if(($mkey eq "wall") || ($mkey eq "wallsoft")) {
	if($value=~/\($patint seconds\)/) {
	    $ret=$1;
	}
	if($value=~/$patint minutes/) {
	    $ret=$1*60;
	}
	if($value=~/^$patint[:]$patint[:]$patint$/) {
	    $ret=$1*60*60+$2*60+$3;
	}
    }

    if($mkey eq "nodelist") {
	if($ret ne "-") {
	    $ret=~s/\//,/gs;
	    my @nodes = split(/\+/,$ret);
	    $ret="(".join(')(',@nodes).")";
	}
    }

    if($mkey eq "vnodelist") {
	if($ret ne "-") {
#	    my $save=$ret;
	    $ret=~s/\(//gs;$ret=~s/\)//gs;
	    my @nodes = split(/\+/,$ret);
	    my ($c,$nd,$num,$rest);
	    for($c=0;$c<=$#nodes;$c++) {
		# get nodename
		if($nodes[$c]=~/^([^\:]+)\:(.*)$/s) {
		    ($nd,$rest)=($1,$2);
		    # get number of procs
		    if($rest=~/ncpus=(\d+)/) {
			$num=$1;
		    } elsif($rest=~/mpiprocs=(\d+)/) {
			$num=$1;
		    } else {
			# default:  1 core
			$num=1;
		    }
		    $nodes[$c]="$nd,$num";
		} elsif($nodes[$c]=~/^([^\:]+)$/s) {
		    $nd=$1;
		    $num=1;
		    $nodes[$c]="$nd,$num";
		} else {
		    print STDERR "Error in job node list: $nodes[$c]\n";
		    $nodes[$c]="unknown,0";
		}
	    }
	    $ret="(".join(')(',@nodes).")";
#	    print "$save -> $ret\n";
	}
    }

    if($mkey eq "totalcores") {
	my $numcores=0;
	my ($spec);
	foreach $spec (split(/\s*\+\s*/,$ret)) {
	    # std job
	    if($ret=~/^$patint[:]ppn=$patint/) {
		$numcores+=$1*$2;
	    } elsif($ret=~/^$patwrd[:]ppn=$patint/) {
		$numcores+=1*$2;
	    }
	}
	$ret=$numcores if($numcores>0);
    }
    if($mkey eq "totaltasks") {
	my $numcores=0;
	my ($spec);
	foreach $spec (split(/\s*\+\s*/,$ret)) {
	    # std job
	    if($ret=~/^$patint[:]ppn=$patint/) {
		$numcores+=$1*$2;
	    } elsif($ret=~/^$patwrd[:]ppn=$patint/) {
		$numcores+=1*$2;
	    }
	}
	$ret=$numcores if($numcores>0);
    }

    if(($mkey eq "comment")) {
	$ret=~s/\"//gs;
    }

    # mask & in user input
    if($ret=~/\&/) {
	$ret=~s/\&/\&amp\;/gs;
    } 


    return($ret);
}
