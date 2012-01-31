#!/usr/bin/perl
#*******************************************************************************
#* Copyright (c) 2007 IBM Corporation and others.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*******************************************************************************/

$pid = fork();
if ( $pid == 0 ) {
	printf( "::::PoePid=%d::::\n", $$ );
	exec(@ARGV);
	exit(1);
} else {
	if ( defined($pid) ) {
		wait();
		exit( $? >> 8 );
	} else {
		exit(1);
	}
}
