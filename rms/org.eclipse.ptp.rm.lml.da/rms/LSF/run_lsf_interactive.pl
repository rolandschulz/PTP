#!/usr/bin/perl 
#******************************************************************************* 
#* Copyright (c) 2013 IBM Corporation. 
#* 
#* All rights reserved. This program and the accompanying materials 
#* are made available under the terms of the Eclipse Public License v1.0 
#* which accompanies this distribution, and is available at 
#* http://www.eclipse.org/legal/epl-v10.html 
#*******************************************************************************/ 
use strict;
use Cwd;

my $MPI_Command = $ENV{'LSF_MPI_COMMAND'};
my $PTP_Directory = $ENV{'PTP_DIRECTORY'};
my @runArgs;
my @MPIArgs;

# Set autoflush to pass output as soon as possble
$|=1;

if ($ENV{'PTP_LAUNCH_MODE'} eq 'debug') {
    my @debugArgs;
    if ($MPI_Command eq 'Parallel Environment') {
        $ENV{'MP_PROCS'} = $ENV{'LSF_MPI_TASK_COUNT'};
        @debugArgs= split(/ +/, $ENV{'PTP_DEBUG_EXEC_ARGS'});
        exec("/usr/bin/perl", "$PTP_Directory/rms/PE/run_pe_app.pl",
             $ENV{'PTP_DEBUG_EXEC_PATH'}, @debugArgs);
    }
    elsif ($MPI_Command eq 'Open MPI') {
        exec("/usr/bin/perl", "$PTP_Directory/rms/OPENMPI/start_job.pl",
             "mpirun", "-np", $ENV{'LSF_MPI_TASK_COUNT'});
    }
    elsif ($MPI_Command eq 'MPICH2') {
        printf("Debugging MPICH programs not supported\n");
        exit(1);
    }
    else {
        exec($ENV{'SHELL'}, '-l');
    }
}
else {
    if ($MPI_Command eq 'Parallel Environment') {
        $ENV{'MP_PROCS'} = $ENV{'LSF_MPI_TASK_COUNT'};
        @runArgs = split(/ +/, $ENV{'PTP_APP_EXEC_ARGS'});
        exec("/usr/bin/perl", "$PTP_Directory/rms/PE/run_pe_app.pl",
             $ENV{'PTP_APP_EXEC_PATH'}, @runArgs);
    }
    elsif ($MPI_Command eq 'Open MPI') {
        @runArgs = split(/ +/, $ENV{'PTP_APP_EXEC_ARGS'});
        @MPIArgs = split(/ +/, $ENV{'LSF_MPI_OPTIONS'});
        exec("/usr/bin/perl", "$PTP_Directory/rms/OPENMPI/start_job.pl",
             "mpirun", "-np", $ENV{'LSF_MPI_TASK_COUNT'}, @MPIArgs,
             $ENV{'PTP_APP_EXEC_PATH'}, @runArgs);
    }
    elsif ($MPI_Command eq 'MPICH2') {
        @runArgs = split(/ +/, $ENV{'PTP_APP_EXEC_ARGS'});
        @MPIArgs = split(/ +/, $ENV{'LSF_MPI_OPTIONS'});
        exec("/usr/bin/perl", "$PTP_Directory/rms/MPICH2/start_job.pl",
             "mpirun", "-np", $ENV{'LSF_MPI_TASK_COUNT'}, @MPIArgs,
             $ENV{'PTP_APP_EXEC_PATH'}, @runArgs);
    }
    else {
        exec($ENV{'SHELL'}, '-l');
    }
}
