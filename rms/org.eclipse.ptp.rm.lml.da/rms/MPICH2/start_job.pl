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

if ($#ARGV < 1) {
  die " Usage: $0 [mpi_args ...] [debugger_args ...]\n";
}

my $pid;
my $cmd = shift(@ARGV);

# Set autoflush to pass output as soon as possble
$|=1;

$pid = fork();
if ( $pid == 0 ) {
	printf("#PTP job_id=%d\n", $$);
	exec($cmd, @ARGV);
	exit(1);
}

waitpid($pid, 0);
exit($? >> 8);



