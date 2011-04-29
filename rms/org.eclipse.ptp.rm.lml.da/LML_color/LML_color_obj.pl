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

use LML_file_obj;
use LML_color_manager;

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


#####################################################################
# get command line parameter
#####################################################################

# option handling
my $opt_outfile="./test.lml";
my $opt_colorconfigfile="./default.conf";
my $opt_verbose=0;
my $opt_timings=0;
my $opt_dump=0;
my $opt_dbdir="./";
usage($0) if( ! GetOptions( 
			    'verbose'          => \$opt_verbose,
			    'timings'          => \$opt_timings,
			    'dump'             => \$opt_dump,
			    'colordefs=s'      => \$opt_colorconfigfile,
			    'output=s'         => \$opt_outfile
			    ) );

#print "@ARGV ($opt_outfile)\n";
if ($#ARGV < 0) {
    &usage($0);
}
my $filename = $ARGV[0];

my $filehandler;

$filehandler=LML_file_obj->new($opt_verbose,$opt_timings);

print "reading file: $filename  ...\n" if($opt_verbose); 
$filehandler->read_lml_fast($filename);

my $colormanager=LML_color_manager->new($opt_colorconfigfile);

{
    my ($key,$type,$color);
    foreach $key (keys(%{$filehandler->{DATA}->{OBJECT}})) {
	$type=$filehandler->{DATA}->{OBJECT}->{$key}->{type};
	$color=$colormanager->get_color($type,$key);
	if($color) {
	    $filehandler->{DATA}->{OBJECT}->{$key}->{color}=$color;
	}
    }

}

if($opt_verbose) {
    print $filehandler->get_stat();
}

$filehandler->write_lml($opt_outfile);

exit(0);

sub usage {
    die "Usage: $_[0] <options> <filenames> 
                -output <file>           : LML output filename
                -colordefs <file>        : color definition file
                -verbose                 : verbose
";
}

1;
