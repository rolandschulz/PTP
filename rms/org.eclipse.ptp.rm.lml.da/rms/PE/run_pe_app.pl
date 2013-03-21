#!/usr/bin/perl
#*******************************************************************************
#* Copyright (c) 2012 IBM Corporation and others.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*******************************************************************************/

use strict;
use File::Basename;
use Cwd;
use Sys::Hostname;
use IO::Socket::UNIX;

my $patint  = "([\\+\\-\\d]+)";              # Pattern for Integer number
my $patnode = "([\^\\s]+(\\.[\^\\s]*)*)";    # Pattern for domain name (a.b.c)

my $portbase     = 50000;
my $portrange    = 10000;
my $ROUTING_FILE = "routing_file";
my $TOTAL_PROCS  = 0;

my $pid;
my $hpcrun;
my $launchMode;
my $debuggerId;
my $debuggerLauncher;
my @child_pids;


#####################################################################
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
sub generate_routing_file {
	my ($pid, $file) = @_;
	my $ntasks;
	my $cfg_file = "/tmp/.ppe.$pid.attach.cfg";
	my $hname = hostname;

	# get the location of the socket file
	my $taskinfodir = $ENV{'MP_DBG_TASKINFO_DIR'};
	if ( $taskinfodir == "" ) {
		$taskinfodir = "/tmp";
	}

	my $socket_file = "$taskinfodir/.ppe.$hname.$pid.socket";
	my $socket_addr = sockaddr_un($socket_file);
	my $sb;

	while ( $sb == "" ) {
		# check the socket file
		$sb = stat($socket_file);
	}

	my $clntsocket = IO::Socket::UNIX->new(Type => SOCK_STREAM,
	                                       Peer => $socket_file) || die "IO::Socket->new: $!\n";

	# register for notifications
	my $register_for_notifications = "[TASK:%t,%p,%H]-1\n";
	$clntsocket->syswrite($register_for_notifications) || die "syswrite: $!\n";

	my @msgs;
	my $msg;
	my $chr;
	my $bytesread = 0;
	my $msgdone = 0;

	while ($bytesread = $clntsocket->sysread($chr, 1)) {
		if ( $chr eq "\n" ) {
			if ( $msgdone eq 1 ) {
				last;
			} else {
				$msgdone = 1;
				$msg = $msg . $chr;
				push( @msgs, $msg );
				$msg = "";
			}
		} else {
			$msgdone = 0;
			$msg = $msg . $chr;
		}
	}

	$clntsocket->close;

	# write the task configuration info to the routing file
	open( OUT, ">$file") || die "open: $!\n";

	printf(OUT "%d\n", scalar(@msgs));

	foreach $msg (@msgs) {
		if ( $msg =~ /$patint,$patint,$patint,$patnode/ ) {
			my ( $taskid, $nodeid ) = ( $2, $4 );
			printf(OUT "%d %s %d\n", $taskid, $nodeid, $portbase + int (rand($portrange)));
		}
	}

	close(OUT);
}

sub start_sdm_master {
	my($cmd, @args) = @_;
	my $pid;
	
	$pid = fork();
	if ($pid == 0) {
		exec($cmd, '--master', @args);
		exit(1);
	}
	push(@child_pids, $pid);
}

$launchMode = $ENV{'PTP_LAUNCH_MODE'};
$hpcrun = $ENV{'HPC_USE_HPCRUN'};

if ($launchMode eq 'debug') {
	$debuggerId = $ENV{'PTP_DEBUGGER_ID'};
	$debuggerLauncher = $ENV{'PTP_DEBUGGER_LAUNCHER'};
	
	if ($debuggerId eq 'org.eclipse.ptp.debug.sdm') {
		$ROUTING_FILE = getcwd() . "/route." . $$;
		push(@ARGV, "--routing_file=$ROUTING_FILE");
	} elsif ($debuggerLauncher) {
	    exec($debuggerLauncher, @ARGV); # try running directly first
	    exec('/usr/bin/perl', $debuggerLauncher, @ARGV); # assume perl script if this fails
	    exit(5);	# Not reached
	}
}

$pid = fork();
if ( $pid == 0 ) {
	# set MP_DBG_TASKINFO
	$ENV{'MP_DEBUG_ATTACH'} = "no";
	$ENV{'MP_DBG_TASKINFO'} = "yes";

	printf( "::::PoePid=%d::::\n", $$ );

    if ($hpcrun eq 'yes') {
        exec('/opt/ibmhpc/ppedev.hpct/bin/hpcrun', '/usr/bin/poe', @ARGV);
    }
    else {
        exec('/usr/bin/poe', @ARGV);
    }
	exit(1);
}
push(@child_pids, $pid);

if ($launchMode eq 'debug' && $debuggerId eq 'org.eclipse.ptp.debug.sdm') {
	generate_routing_file($pid, $ROUTING_FILE);
	start_sdm_master(@ARGV);
}

foreach (@child_pids) {
	waitpid($_, 0);
}

if ($launchMode eq 'debug' && $debuggerId eq 'org.eclipse.ptp.debug.sdm') {
	unlink($ROUTING_FILE);
}

exit( $? >> 8 );
