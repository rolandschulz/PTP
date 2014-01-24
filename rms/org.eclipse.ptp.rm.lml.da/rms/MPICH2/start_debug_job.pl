#!/usr/bin/perl -w
#*******************************************************************************
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#* 
#* Contributors:
#*     David Wootton - Initial implementation derived from start_job.pl
#*******************************************************************************/ 
use strict;
use threads;
use Text::ParseWords;
use Cwd;
# Use the perl Devel-GDB-2.02 or later package from cpan.org
# The Devel::GDB module requires the perl Expect module to be installed.
use Devel::GDB;

my $portbase=50000;
my $portrange=10000;
my $ROUTING_FILE;
my $ROUTING_PART;
my $debuggerPath;
my @debuggerArgs;
my $gdb;
my $sdmPid;
my $mpiPid;

#####################################################################
#
# Script to start the SDM and generate a routing table. 
#
# The routing table is called routes_<PTP_JOBID> and it is generated in 
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

  # Send a SIGINT to gdb to interrupt it so MPIR_debug_state can be queried
sub doInterrupt {
       my @args = @_;
       sleep 1;
       kill "INT", $args[0];
}

  # Attempt to clean up all application processes when terminating
sub terminate {
       $gdb->end;
       kill "INT", $sdmPid;
       kill "INT", $mpiPid;
       waitpid($sdmPid, 0);
       waitpid($mpiPid, 0);
       die "$_[0]";
}

if ($#ARGV < 1) {
       die " Usage: $0 [mpi_args ...] [debugger_args ...]\n";
}

my $cmd = shift(@ARGV);
my $args = join(" ", @ARGV);

$debuggerPath = $ENV{'PTP_DEBUG_EXEC_PATH'};
@debuggerArgs = shellwords($ENV{'PTP_DEBUG_EXEC_ARGS'});
$ROUTING_FILE = getcwd() . "/routes_" . $ENV{'PTP_JOBID'};
$ROUTING_PART = $ROUTING_FILE . ".part";
push(@debuggerArgs, "--routing_file=$ROUTING_FILE");

$sdmPid = fork();
if ($sdmPid == 0) {
        exec($debuggerPath, "--master", @debuggerArgs);
        exit(1);
}

# Set autoflush to pass output as soon as possble
$|=1;

$mpiPid = fork();
if ( $mpiPid == 0 ) {
	printf("#PTP job_id=%d\n", $$);
        exec($cmd, @ARGV, $debuggerPath, @debuggerArgs);
        exit(1);
}
else {
        my $value;
        my $nTasks;
        my $i;
        my $response;

        $gdb = new Devel::GDB('-params' => "$cmd $mpiPid",
                              '-create-expect' => 1);
          # gdb prints a bunch of messsages about debug symbols at startup.
          # Issue a gdb command only to consume those messages.
        $gdb->get("print 0");
        $response = $gdb->get("print MPIR_debug_state");
        if ($response =~ m/.*= (\d+).*/) {
                $value = $1;
        }
        else {
                terminate "Error reading MPIR_debug_state";
        }
          # When $value (MPIR_debug_state) = 1, task map is available
        while ($value ne "1") {
                my $intThread;

                if ($value eq "2") {
                        terminate "gdb terminating, exit";
                }
                  # Let gdb run briefly then interrupt it
                $gdb->get("continue");
                $intThread = threads->create("doInterrupt", $mpiPid);
                $intThread->join();
                $response = $gdb->get("print MPIR_debug_state");
                if ($response =~ m/.*= (\d+).*/) {
                        $value = $1;
                }
                else {
                        terminate "Error reading MPIR_debug_state";
                }
        }
         # Get application task count
        $response = $gdb->get("print MPIR_proctable_size");
        if ($response =~ m/.*= (\d+).*/) {
                $nTasks = $1;
        }
        else {
                terminate "Error reading MPIR_proctable_size";
        }
          # Write temporary routing file then rename it after file is complete
  	open(OUT,"> $ROUTING_PART") ||
             terminate "cannot open file $ROUTING_PART";
  	printf(OUT "%d\n", $nTasks);
        for ($i = 0; $i < $nTasks; $i++) {
                my $host;

                $response = $gdb->get("print MPIR_proctable[$i].host_name");
                if ($response =~ m/.* = 0[xX][0-9a-fA-F]+ \"(.*)\".*/) {
                        $host = $1;
                }
                else {
                        terminate "Error reading MPIR_proctable";
                }
  	        printf(OUT 
                      "%d %s %d\n", $i, $host, $portbase+int(rand($portrange)));
        }
  	close(OUT);
        rename $ROUTING_PART, $ROUTING_FILE || 
               terminate "Cannot rename $ROUTING_PART to $ROUTING_FILE";
        $gdb->end;
        waitpid($mpiPid, 0);
}
waitpid($sdmPid, 0);
exit($? >> 8);
