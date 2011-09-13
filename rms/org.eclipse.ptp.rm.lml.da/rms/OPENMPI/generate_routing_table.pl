#!/usr/bin/perl -w
#*******************************************************************************
#* Copyright (c) 2011 IBM Corporation.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#* 
#* Contributors:
#*     IBM Corporation - Initial Implementation
#*******************************************************************************/ 
use strict;

my $patint="([\\+\\-\\d]+)";   # Pattern for Integer number
my $patfp ="([\\+\\-\\d.E]+)"; # Pattern for Floating Point number
my $patwrd="([\^\\s]+)";       # Pattern for Work (all noblank characters)
my $patbl ="\\s+";             # Pattern for blank space (variable length)

my $portbase=50000;
my $portrange=10000;
my $verbose=1;
my ($line,%job,$count);

#####################################################################
#
# Script to generate debugger routing table
#
# Routing table format is:
#
# num_tasks
# task_num host_name port_num
# ...
#
# where:
# 	num_tasks is the total number of tasks in the MPI job
#	task_num is the task number for a process (e.g. 0, 1, 2, etc.)
#	host_name is the hostname of the node the process is running on
#	port_num is a semi-random port number that the debugger will listen on
#
#####################################################################

if ($#ARGV != 1) {
  die " Usage: $0 <jobid> <filename> $#ARGV\n";
}
my $jobid = $ARGV[0];
my $filename = $ARGV[1];
my $totaltasks = -1;

my $cmd="orte-ps";
$cmd=$ENV{"CMD_JOBINFO"} if($ENV{"CMD_JOBINFO"}); 

if(open(IN,"$cmd -n -v 2>&1 |")) {
    my $opid=undef;
    while($line=<IN>) {
	chomp($line);
	if ($line=~/Gathering Information for HNP: \[\[$patint,$patint\],$patint\]:$jobid/) {
		my($opid)=($1);
		print "found job $jobid with pid $opid\n";

		# find job table
		while($line=<IN>) {
		    if ($line=~/^\s+JobID \|/) {
			last;
		    }
		}

		# scan job table
		print "line 2: $line\n";
		$line=<IN>;
		$line=<IN>;
		# job line
		print "line 3: $line\n";

		if($line=~/^\s*\[$patint,$patint\]\s*\|\s*$patwrd\s*\|\s*$patwrd\s*\|\s*$patwrd\s*\|/) {
		    my($ppid,$num,$state,$slots,$numproc)=($1,$2,$3,$4,$5);
		    print "found joblist $ppid,$num,$state,$slots,$numproc\n";
		    $totaltasks      = $numproc;
		    $line=<IN>;
		    if($line=~/^\s+Process Name \|/) {
			$line=<IN>;
			while($line=<IN>) {
			    if($line=~/^\s*([^\|\s]+)\s*\|\s*\[\[$patint,$patint\],$patint\]\s*\|\s*$patwrd\s*\|\s*$patwrd\s*\|\s*$patwrd\s*\|\s*$patwrd\s*\|/) {
				my($pname,$ortename1,$ortename2,$ortename3,$rank,$pid,$nodeid,$pstate)=($1,$2,$3,$4,$5,$6,$7,$8);
				print "found process $pname,$ortename1,$ortename2,$ortename3,$rank,$pid,$nodeid,$pstate\n";
				$job{nodelist}{$rank}=$nodeid;
				$job{pidlist}{$rank}=$pid;
			    }
			    if($line=~/^\s*$/) {
				last;
			    }
			}
		    }
		}
	    }
    }
    close(IN);
} 

if ($totaltasks < 0) {
    die "no tasks found";
}
open(OUT,"> $filename") || die "cannot open file $filename";
printf(OUT "%d\n", $totaltasks);
# job objs
for ($count=0; $count < $totaltasks; $count++) {
    printf(OUT "%d %s %d\n",$count,$job{nodelist}{$count},$portbase+int(rand($portrange)));
}
close(OUT);

