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

use FindBin;
use lib "$FindBin::RealBin/lib";
use Data::Dumper;
use Getopt::Long;
use Time::Local;
use Time::HiRes qw ( time );
use LML_da_workflow_obj;

use strict;

my $patint="([\\+\\-\\d]+)";   # Pattern for Integer number
my $patfp ="([\\+\\-\\d.E]+)"; # Pattern for Floating Point number
my $patwrd="([\^\\s]+)";       # Pattern for Work (all noblank characters)
my $patbl ="\\s+";             # Pattern for blank space (variable length)

use LML_da_util;
use LML_da_step;

my $version="1.0";

# time measurement
my ($tstart,$tdiff,$rc);

# option handling
my $opt_configfile="./LML_da_workflow.conf";
my $opt_verbose=0;
my $opt_dump=0;
usage($0) if( ! GetOptions( 
			    'verbose'          => \$opt_verbose,
			    'configfile=s'     => \$opt_configfile,
			    'dump'             => \$opt_dump
			    ) );
my $date=`date`;
chomp($date);

print STDERR "-"x90,"\n" if($opt_verbose);
print STDERR "  LML Data Access Workflow Manager $version, starting at ($date)\n"; 
print STDERR "-"x90,"\n" if($opt_verbose);

# read config file
print STDERR "$0: configfile=$opt_configfile\n" if($opt_verbose);
$tstart=time;
my $workflow_obj = LML_da_workflow_obj->new($opt_verbose,0);
$workflow_obj->read_xml_fast($opt_configfile);
my $confxml=$workflow_obj->{DATA};
$tdiff=time-$tstart;
printf(STDERR "$0: parsing XML configfile in %6.4f sec\n",$tdiff) if($opt_verbose);



# init global vars
my $vardefs=$confxml->{'vardefs'}->[0];
my $globalvarref;
my $pwd=`pwd`;
chomp($pwd);
$globalvarref->{instdir} = $FindBin::RealBin;
$globalvarref->{pwd}     = $pwd;
$globalvarref->{permdir} = "./perm_default";
$globalvarref->{tmpdir}  = "./tmp_default";
$globalvarref->{verbose} = $opt_verbose;
&LML_da_util::init_globalvar($vardefs,$globalvarref); 

# substitute vars in steps
my $stepdefs=$confxml->{'step'};
#&LML_da_util::substitute_recursive($stepdefs,$globalvarref); 

if($opt_dump) {
    print STDERR Dumper($confxml);
    exit(1);
}

my $permdir=$globalvarref->{permdir};
my $tmpdir=$globalvarref->{tmpdir};


# check permament snd temporary directory
if(! -d $permdir) {
	printf(STDERR "LML_da.pl: permament directory not found, create new directory $permdir ...\n");
	if(!mkdir($permdir,0755)) {
	    die "LML_da.pl: could not create $permdir ...$!\n";
	}
}

if(! -d $tmpdir) {
	printf(STDERR "LML_da.pl: temporary directory not found, create new directory $tmpdir ...\n");
	if(!mkdir($tmpdir,0755)) {
	    die "LML_da.pl: could not create $tmpdir ...$!\n";
	}
}


# check if another LML_da.pl is running
if (-f "$permdir/RUNNING") {
	printf(STDERR "LML_da.pl: another LML_da.pl process is running ... exiting, please remove $permdir/RUNNING\n");
} else {
    # touch RUNNING stamp
    open(RUNNING,"> $permdir/RUNNING");
    print RUNNING "$$\n";
    close(RUNNING);

    # processing steps
    my $stepobj=LML_da_step->new($stepdefs,$globalvarref);
    $stepobj->process();

    unlink("$permdir/RUNNING");

}



print STDERR "-"x90,"\n" if($opt_verbose);
print STDERR "  LML Data Access Workflow Manager $version, ending at   ($date)\n"; 
print STDERR "-"x90,"\n" if($opt_verbose);


sub mydie {
    my ($message)=@_;
    unlink("$permdir/RUNNING");
    die "$message";
}

sub usage {
    die "Usage: $_[0] <options> 
                -configfile <configfile> : configfile (default: ./LML_da.conf)
                -verbose                 : verbose

";
}
