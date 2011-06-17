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
use Getopt::Long;

use FindBin;
use lib "$FindBin::RealBin/";
use lib "$FindBin::RealBin/../lib";

use LML_combine_file_obj;
use LML_combine_obj_check;
use LML_combine_obj_bgp;


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
my ($filename);


#####################################################################
# get command line parameter
#####################################################################

# option handling
my $opt_outfile="./test.lml";
my $opt_verbose=0;
my $opt_timings=0;
my $opt_dump=0;
my $opt_dbdir="./";
usage($0) if( ! GetOptions( 
			    'verbose'          => \$opt_verbose,
			    'timings'          => \$opt_timings,
			    'dump'             => \$opt_dump,
			    'dbdir=s'          => \$opt_dbdir,
			    'output=s'         => \$opt_outfile
			    ) );

#print "@ARGV ($opt_outfile)\n";
if ($#ARGV < 0) {
    &usage($0);
}
my @filenames = @ARGV;

my $system_sysprio=-1;
my $maxtopdogs=-1;
my $filehandler;

$filehandler=LML_combine_file_obj->new($opt_verbose,$opt_timings);

foreach $filename (@filenames) {
    print "reading file: $filename  ...\n" if($opt_verbose); 
    $filehandler->read_lml_fast($filename);
}

# determine system type
my $system_type = "unknown";
my $system_type_ref;
{
    keys(%{$filehandler->{DATA}->{OBJECT}}); # reset iterator
    my($key,$ref);
    while(($key,$ref)=each(%{$filehandler->{DATA}->{OBJECT}})) {
	if($ref->{type} eq 'system') {
	    $system_type_ref=$ref=$filehandler->{DATA}->{INFODATA}->{$key};
	    if($ref->{type}) {
		$system_type=$ref->{type};
		printf("scan system: type is %s\n",$system_type);
	    }
	    last; 
	}
    }
}


if($system_type eq "BG/P") {
    &LML_combine_obj_bgp::update($filehandler->get_data_ref(),$opt_dbdir);
}

# check if Cluster is a PBS controlled Altix SMP Cluster
if($system_type eq "Cluster") {
    keys(%{$filehandler->{DATA}->{OBJECT}}); # reset iterator
    my($key,$ref);
    while(($key,$ref)=each(%{$filehandler->{DATA}->{OBJECT}})) {
	if($ref->{type} eq 'node') {
	    $ref=$filehandler->{DATA}->{INFODATA}->{$key};
	    if(exists($ref->{ntype})) {
		if($ref->{ntype} eq "PBS") {
		    $system_type="PBS";
		    $system_type_ref->{type}="PBS";
		    printf("scan system: type reset to %s\n",$system_type);
		}
	    }
	    last; 
	}
    }
}

&LML_combine_obj_check::check_jobs($filehandler->get_data_ref());

if($opt_verbose) {
    print $filehandler->get_stat();
}

$filehandler->write_lml($opt_outfile);

sub usage {
    die "Usage: $_[0] <options> <filenames> 
                -output <file>           : LML output filename
                -verbose                 : verbose
";
}

1;
