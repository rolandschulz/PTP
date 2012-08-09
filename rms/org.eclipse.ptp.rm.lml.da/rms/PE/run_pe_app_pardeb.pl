#!/usr/bin/perl
#*******************************************************************************
#* Copyright (c) 2012 IBM Corporation and others.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*******************************************************************************/
use strict;
use Switch;

my $DBG_EXE="gdia";
my $DBG_HELPER="pmdhelper";
my @DBG_ARGV;
my @APP_ARGV;

# Debug trace
my $dbglog="/dev/null";
if ( $ENV{'DER_DBG_TRACE'} == 0 )
{
    $dbglog="/tmp/.pardeb.$$";
}

open DBGLOG, ">$dbglog" or die "ERROR: could not open file $dbglog - $!\n";
# Dump the environment
foreach (sort keys %ENV) { 
    printf DBGLOG "$_  =  $ENV{$_}\n"; 
}
# Dump argv[]
for (my $i = 0; $i <= $#ARGV; $i++)
{
    printf DBGLOG "argv[$i] = $ARGV[$i]\n";
}
$ENV{PE_DEBUGGER_ID} = $$;  # Unique number used by application & ParDeb to hook up.
# Do we have a hostfile?
if ( $ENV{'MP_HOSTFILE'} eq "" )
{
    printf DBGLOG "MP_RESD = [$ENV{'MP_RESD'}] pre-MP_SAVEHOSTFILE = [$ENV{'MP_SAVEHOSTFILE'}]\n";
    $ENV{'MP_SAVEHOSTFILE'} = "/tmp/.ppe.savehostfile.$$";
}

if ( $ENV{'PE_DEBUGGER_HELPER'} )
{
    $DBG_HELPER = $ENV{'PE_DEBUGGER_HELPER'};
}
# Separate debugger and application args...
my $i = 0;
if ( $ARGV[0] == /.*gdia/ )
{
    $DBG_EXE=$ARGV[0];
    $i += 1;
}
$DBG_ARGV[0] = $DBG_EXE;
$APP_ARGV[0] = $DBG_HELPER;
my $a = 1;
my $d = 1;
my $postPoe = 0;
for ( ; $i <= $#ARGV ; $i++)
{
    switch ( $ARGV[$i] )
    {
        case ( /^poe/ )
        {
            $postPoe = 1;
        }
        case ( /--port=|--host=|--debugger=|--depth=/ )
        {
            $DBG_ARGV[$d] = $ARGV[$i];
            $d += 1;
        }
        case ( /--debugger_path=/ )
        {
            $DBG_EXE=$ARGV[$i];
            $DBG_EXE=~ s/.*=//;
            $DBG_EXE=~ s/\r|\n//g;
            $DBG_ARGV[0] = $DBG_EXE;
            printf DBGLOG "DBG_EXE=[$DBG_EXE]\n";
        }
        case ( /PE_DEBUG_HELPER=/ )
        {
            $DBG_HELPER=$ARGV[$i];
            $DBG_HELPER=~ s/.*=//;
            $DBG_HELPER=~ s/\r|\n//g;
            $APP_ARGV[0] = $DBG_HELPER;
        }
        case ( /PE_DEBUG_MODE=/ )
        {
            ;
        }
        else
        {
            if ( $postPoe )
            {
                $APP_ARGV[$a] = $ARGV[$i];
                $a += 1;
            }
        }
    }
}
if ( $ENV{'PTP_APP_EXEC_PATH'} )
{
	$APP_ARGV[$a] = $ENV{'PTP_APP_EXEC_PATH'};
	$a += 1;
}
if ( $ENV{'PTP_APP_EXEC_ARGS'} )
{
	my @ARGS = split / /,$ENV{'PTP_APP_EXEC_ARGS'};
	for ($i = 0 ; $i <= $#ARGS ; $i++)
	{
		$APP_ARGV[$a] = $ARGS[$i];
		$a += 1;
	}
}
# DEBUG: Dump argument set for ParDeb and Application
for ($i = 0; $i <= $#DBG_ARGV; $i++)
{
    printf DBGLOG "DBG_ARGV[$i] = $DBG_ARGV[$i]\n";
}
for ($i = 0; $i <= $#APP_ARGV; $i++)
{
    printf DBGLOG "APP_ARGV[$i] = $APP_ARGV[$i]\n";
}

my $pid = fork();
if ( $pid == 0 )
{
    # launch IBM Parallel Debugger
    printf DBGLOG "launching $DBG_EXE MP_PROCS=$ENV{'MP_PROCS'} MP_RESD=[$ENV{'MP_RESD'}] MP_HOSTFILE=$ENV{'MP_HOSTFILE'} MP_SAVEHOSTFILE=$ENV{'MP_SAVEHOSTFILE'}\n";
    close DBGLOG;
    exec(@DBG_ARGV);
    printf "exec($DBG_ARGV[0]) failed! ret $?\n";
    exit(1);
}
$pid = fork();
if ( $pid == 0 )
{
    # launch the application
    printf( "::::PoePid=%d::::\n", $$ );
    printf DBGLOG "launching $APP_ARGV[1], MP_PROCS=$ENV{'MP_PROCS'} MP_RESD=$ENV{'MP_RESD'} MP_HOSTFILE=$ENV{'MP_HOSTFILE'} MP_SAVEHOSTFILE=[$ENV{'MP_SAVEHOSTFILE'}]\n";
    close DBGLOG;
    exec('/usr/bin/poe', @APP_ARGV);
    exit(1);
}

my $ret = 1;
if ( defined($pid) ) {
    printf DBGLOG "Waiting for poe $APP_ARGV[1] to terminate\n";
    wait();
    $ret = $? >> 8;
}
printf DBGLOG "poe $APP_ARGV[1] returns $ret\n";
close DBGLOG;
exit $ret;
