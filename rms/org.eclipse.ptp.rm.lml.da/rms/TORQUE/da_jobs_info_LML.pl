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
    "Resource_List.nodes"                    => "totalcores",
    "Resource_List.pmem"                     => "",
    "Resource_List.walltime"                 => "wall",
    "Shell_Path_List"                        => "",
    "Walltime.Remaining"                     => "",
    "comment"                                => "",
    "ctime"                                  => "",
    "depend"                                 => "",
    "etime"                                  => "",
    "exec_host"                              => "nodelist",
    "exit_status"                            => "",
    "fault_tolerant"                         => "",
    "interactive"                            => "",
    "job_state"                              => "state",
    "mtime"                                  => "",
    "qtime"                                  => "queuedate",
    "queue"                                  => "queue",
    "resources_used.cput"                    => "",
    "resources_used.mem"                     => "",
    "resources_used.vmem"                    => "",
    "resources_used.walltime"                => "",
    "server"                                 => "",
    "session_id"                             => "",
    "start_count"                            => "",
    "start_time"                             => "dispatchdate",
    "submit_args"                            => "",

    "step"                                   => "step",
    "totaltasks"                             => "totaltasks",
    "spec"                                   => "spec",

# unknown attributes
    "group"                                  => "group",
    );

open(IN,"/usr/bin/qstat -f |");
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
    $jobs{$jobid}{totaltasks} = $jobs{$jobid}{"Resource_List.nodes"} if(!exists($jobs{$jobid}{totaltasks}));
    $jobs{$jobid}{spec}       = $jobs{$jobid}{"Resource_List.nodes"} if(!exists($jobs{$jobid}{spec}));
}

open(OUT,"> $filename") || die "cannot open file $filename";
printf(OUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
printf(OUT "<lml:lgui xmlns:lml=\"http://www.llview.de\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
printf(OUT "	xsi:schemaLocation=\"http://www.llview.de lgui.xsd\"\n");
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



sub modify {
    my($key,$mkey,$value)=@_;
    my $ret=$value;


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

    if($mkey eq "totalcores") {
	if($ret=~/$patint[:]ppn=$patint/) {
	    $ret=$1*$2;
	}
    }
    if($mkey eq "totaltasks") {
	if($ret=~/$patint[:]ppn=$patint/) {
	    $ret=$1*$2;
	}
    }

    if(($mkey eq "comment")) {
	$ret=~s/\"//gs;
    }
    if(($mkey eq "bgp_state")) {
	$ret=~s/\<unknown\>/unknown/gs;
    }

    # mask & in user input
    if($ret=~/\&/) {
	$ret=~s/\&/\&amp\;/gs;
    } 


    return($ret);
}
