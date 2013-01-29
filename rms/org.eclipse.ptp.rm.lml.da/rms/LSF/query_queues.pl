#!/usr/bin/perl
#*******************************************************************************
#* Copyright (c) 2013 IBM Corporation.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*******************************************************************************/
# Output a sorted list of queue names reported by 'bqueues -w'
# Queue names are in the first column of output and the first
# line of output is a heading line
exec "bqueues -w | tail -n +2 | cut -f1 -d' ' | sort";