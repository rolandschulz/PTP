#!/usr/bin/perl -w
#*******************************************************************************
#* Copyright (c) 2011 Forschungszentrum Juelich GmbH.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*
#* Contributors:
#*    Wolfgang Frings, Carsten Karbach (Forschungszentrum Juelich GmbH) 
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
my $opt_dbdir="";
my $db_file;
usage($0) if( ! GetOptions( 
			    'verbose'          => \$opt_verbose,
			    'timings'          => \$opt_timings,
			    'dump'             => \$opt_dump,
			    'colordefs=s'      => \$opt_colorconfigfile,
			    'dbdir=s'          => \$opt_dbdir,
			    'output=s'         => \$opt_outfile
			    ) );

#print "@ARGV ($opt_outfile)\n";
if ($#ARGV < 0) {
    &usage($0);
}
my $filename = $ARGV[0];

my $filehandler;

$filehandler=LML_file_obj->new($opt_verbose,$opt_timings);

print STDERR "reading file: $filename  ...\n" if($opt_verbose); 
$filehandler->read_lml_fast($filename);

my $colormanager=LML_color_manager->new($opt_colorconfigfile);

my %knownids;

# load db file
if( $opt_dbdir ne "" ) {
    $db_file=sprintf("%s/colormap.db",$opt_dbdir);
    if(-f $db_file) {
	print STDERR "reading db file: $db_file  ...\n" if($opt_verbose); 
	$colormanager->load_db($db_file);
    }
}


print STDERR "processing objs  ...\n" if($opt_verbose); 
{
    my ($key,$type,$color,$colorkey);
    foreach $key (sort(keys(%{$filehandler->{DATA}->{OBJECT}}))) {
	next if(!exists($filehandler->{DATA}->{OBJECT}->{$key}->{type}));
	$colorkey=$key;
	if(exists( $filehandler->{DATA}->{OBJECT}->{$key}->{name} )){
		$colorkey=$filehandler->{DATA}->{OBJECT}->{$key}->{name};
	}
	$type=getType($key, $filehandler);
	$color=$colormanager->get_color($type,$colorkey);
	if($color) {
	    $filehandler->{DATA}->{OBJECT}->{$key}->{color}=$color;
	}
	push(@{$knownids{$type}},$colorkey);
    }

}

if( $opt_dbdir ne "" ) {
    my ($key,$type);
    foreach $type (keys(%knownids)) {
	$colormanager->free_unused($type,@{$knownids{$type}});
    }
    $colormanager->save_db($db_file);
}

if($opt_verbose) {
    print $filehandler->get_stat();
}

$filehandler->write_lml($opt_outfile);

exit(0);

#*********************************************
#
# Get the type corresponding to the id of an
# object. In general, this function returns the 
# value of the type attribute for the object with
# the passed id.
# This functions is especially needed for detecting, if 
# a job is a running or waiting job. This allows two 
# separate these two job classes.
#
# @param id the ID of the object, whose type is requested
#
# @param filehandler LML file handler providing all the 
#	data necessary for retrieving object types
#
#
#*********************************************
sub getType{
	my ($id,$filehandler) = @_;
	if(!exists($filehandler->{DATA}->{OBJECT}->{$id}->{type})){
		return "";
	}
	
	my $type = $filehandler->{DATA}->{OBJECT}->{$id}->{type};
	
	#Detect if this is a running/waiting job
	if($type eq "job"){
		if( exists($filehandler->{DATA}->{INFODATA}->{$id}->{status}) ){
			my $status = $filehandler->{DATA}->{INFODATA}->{$id}->{status};
			if($status eq "RUNNING"){
				return "runjob";
			}
			else{
				return "waitjob";
			}
		}
		else{#Cannot determine if running or waiting job
			return "job";
		}
	}
	else{
		return $type;
	}
}

sub usage {
    die "Usage: $_[0] <options> <filenames> 
                -output <file>           : LML output filename
                -colordefs <file>        : color definition file
                -verbose                 : verbose
";
}

1;
