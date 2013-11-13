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
use File::Temp qw/tempfile/;
use Text::ParseWords;
use Cwd;

my $patint="([\\+\\-\\d]+)";   # Pattern for Integer number
my $patnode="([\^\\s]+(\\.[\^\\s]*)*)";    # Pattern for domain name (a.b.c)
my $patip="(\\d+\\.\\d+\\.\\d+\\.\\d+)";   # Pattern for ip address

my $portbase=50000;
my $portrange=10000;
my $verbose=1;
my $TOTAL_PROCS=0;
my @JOB;

my $line;
my $pid;
my $ROUTING_FILE;
my $debuggerId;
my $debuggerPath;
my @debuggerArgs;
my @child_pids;

#####################################################################
#
# Script to start the SDM and generate a routing table. Used when the
# ompi-ps command can't be used to obtain job information, such as
# interactive launch via job scheduler.
#
# The routing table is called 'routing_file' and it is generated in 
# the current working directory. The sdm's working directory must be
# the same location if they are to find the table. Also, any old
# routing tables should be removed before starting the sdm. 
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

sub get_node_map {
	my ($node) = @_;
    my $rank;
    my $line;
	# find proc info
	while ($line=<IN>) {
	    #if ($line=~/.*Process rank: $patint/) {
	    if ($line=~/^    rank $patint/) {
	    	$rank = $1;
			$JOB[$rank] = $node;
			$TOTAL_PROCS++;
			print "found proc $rank\n" if ($verbose);
	    } elsif ($line =~ /^$/) {
	    	print "found end of node map\n" if ($verbose);
	    	return;
	    }
	}
}

sub get_job_map {
    my $node;
    my $line;
	# find node/proc info
	while ($line=<IN>) {
	    #if ($line=~/^ Data for node: (Name: )?$patnode\s*Num procs: $patint/) {
	    if ($line=~/^  Process on host: $patnode/) {
			$node = $1;
			print "found host $node \n" if ($verbose);
			get_node_map($node);
		} elsif ($line =~ /^Process Map End/) {
			print "found end of table\n" if ($verbose);
			return;
		}
	}
}

sub generate_routing_file {
	my ($file) = @_;
	open(OUT,"> $file") || die "cannot open file $file";
	printf(OUT "%d\n", $TOTAL_PROCS);
	for (my $count=0; $count < $TOTAL_PROCS; $count++) {
	    printf(OUT "%d %s %d\n",$count,$JOB[$count],$portbase+int(rand($portrange)));
	}
	close(OUT);
}

if ($#ARGV < 1) {
  die " Usage: $0 mpi_cmd [mpi_args ...]\n";
}

my $launchMode = $ENV{'PTP_LAUNCH_MODE'};

my $launchCommand = shift(@ARGV);

if ($launchMode eq 'debug') {
	$debuggerId = $ENV{'PTP_DEBUGGER_ID'};
	$debuggerPath = $ENV{'PTP_DEBUG_EXEC_PATH'};
	@debuggerArgs = shellwords($ENV{'PTP_DEBUG_EXEC_ARGS'});
	$ROUTING_FILE = getcwd() . "/routes_" . $ENV{'PTP_JOBID'};
	#push(@ARGV, "-mca", "orte_show_resolved_nodenames", "1", "-display-map");
	push(@ARGV, "-v");
	push(@debuggerArgs, "--routing_file=$ROUTING_FILE");
}

# Set autoflush to pass output as soon as possble
$|=1;

$pid = fork();
if ( $pid == 0 ) {
	printf("#PTP job_id=%d\n", $$);
	if ($launchMode eq 'debug') {
		my $launchArgs = join(" ", @ARGV);
		my $dbgArgs = join(" ", @debuggerArgs);
		print "running command $launchCommand $launchArgs $debuggerPath $dbgArgs\n" if ($verbose);
		if (open(IN,"$launchCommand $launchArgs $debuggerPath $dbgArgs 2>&1 |")) {
		    while ($line=<IN>) {
				#print "Line: $line";
				chomp($line);
				if ($line=~/^Process Map/) {
					print "found job map\n" if ($verbose);
					get_job_map();
					print "generate_routing_file\n" if ($verbose);
					generate_routing_file($ROUTING_FILE);
				} else {
					print "$line\n";
				}
		    }
		    close(IN);
		    unlink($ROUTING_FILE);
		} 
	} else {
		print "running command $launchCommand @ARGV\n" if ($verbose);
		exec($launchCommand, @ARGV);
		exit(1);
	}
}
push(@child_pids, $pid);

foreach (@child_pids) {
	waitpid($_, 0);
}

exit($? >> 8);



