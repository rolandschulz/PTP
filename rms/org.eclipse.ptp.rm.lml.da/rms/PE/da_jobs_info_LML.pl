#!/usr/bin/perl -w
#*******************************************************************************
#* Copyright (c) 2007 IBM Corporation and others.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*******************************************************************************/
use strict;

my $patint = "([\\+\\-\\d]+)";      # Pattern for Integer number
my $patfp  = "([\\+\\-\\d.E]+)";    # Pattern for Floating Point number
my $patwrd = "([\^\\s]+)";          # Pattern for Word (all noblank characters)
my $patbl  = "\\s*";                # Pattern for blank space (variable length)
my $patnode = "([\^\\s]+(\\.[\^\\s]*)*)";    # Pattern for domain name (a.b.c)

#####################################################################
# get user info / check system
#####################################################################
my $verbose = 1;
my ( $line, $key, $value, $count, $arch, %notmappedkeys, %notfoundkeys );
my ( %jobs,  %jobnr,  $jobid );
my ( %nodes, $nodeid, %nodenr );

#####################################################################
# get command line parameter
#####################################################################
if ( $#ARGV != 0 ) {
	die " Usage: $0 <filename> $#ARGV\n";
}
my $filename = $ARGV[0];

my %mapping_node = (
	"id"           => "id",
	"ncores"       => "ncores",
	"state"        => "state",
	"arch"         => "arch",
	"_taskcounter" => "",

);

my %mapping_job = (
	"step"       => "step",
	"nodelist"   => "nodelist",
	"totaltasks" => "totaltasks",
	"totalcores" => "totalcores",
	"job_state"  => "state",

	"status"         => "status",
	"detailedstatus" => "detailedstatus",
	"user"           => "owner",
	"group"          => "group",
	"queue"          => "queue",
	"etime"          => "wall",
	"id"             => "",
	"spec"           => "",
	"queuedate"      => "queuedate",
	"dispatchdate"   => "dispatchdate",

	# unknown attributes
);

my $is_aix = `uname -s` =~ /aix/i;
my $cmd;

if ($is_aix) {
	$cmd = "ps -u $ENV{LOGNAME} -o pid,user,group,etime,command | grep poe | grep -v grep";
	}
else {
	$cmd = "ps x -o pid,user,group,etime,command | grep poe | grep -v grep";
}

my $datecmd = "date '+%D %T'";

if ( open( IN, "$cmd 2>&1 |" ) ) {
	while ( $line = <IN> ) {
		chomp($line);
		if ( $line =~
			/$patbl$patint$patbl$patwrd$patbl$patwrd$patbl$patwrd$patbl$patwrd/
		  )
		{
			my ( $jobid, $user, $group, $etime, $command ) =
			  ( $1, $2, $3, $4, $5 );
			if ( $command eq "poe" ) {
				$jobs{$jobid}{step}   = $jobid;
				$jobs{$jobid}{status} = "RUNNING";
				$jobs{$jobid}{user}   = $user;
				$jobs{$jobid}{group}  = $group;
				$jobs{$jobid}{etime}  = $etime;
				&check_attach_cfg($jobid);
			}
		}
	}
	close(IN);
}

# determine number of cores
my $numcpu;
if ( $is_aix ) {
	$numcpu = `lscfg | grep proc | wc -l`;
} else {
	$numcpu = `grep -c ^processor /proc/cpuinfo`;
}

if ( $? != 0 ) {
	$numcpu = "-1";
}

# add manatory attributes to jobs
foreach $jobid ( sort( keys(%jobs) ) ) {
	my $jobdate = `$datecmd`;
	if ( $numcpu eq "-1" ) {
		$jobs{$jobid}{totalcores} = $jobs{$jobid}{totaltasks};
	} else {
		$jobs{$jobid}{totalcores} = $numcpu;
	}
	$jobs{$jobid}{queue}          = "local";
	$jobs{$jobid}{detailedstatus} = "";
	$jobs{$jobid}{queuedate}      = $jobdate;
	$jobs{$jobid}{dispatchdate}   = $jobdate;
}

# add default node that is required for LML schema
if ( !keys(%nodes) ) {

	# set default
	$nodeid = `uname -n`;
	chomp($nodeid);
	$nodes{$nodeid}{id}    = $nodeid;
	$nodes{$nodeid}{state} = "Running";
}

# add mandatory attributes for nodes. This should be obtained from the nodes
$arch = `uname -p`;
chomp($arch);
foreach $nodeid ( sort( keys(%nodes) ) ) {
	$nodes{$nodeid}{arch}   = $arch;
	$nodes{$nodeid}{ncores} = 1;
}

open( OUT, "> $filename" ) || die "cannot open file $filename";
printf( OUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );
printf( OUT
"<lml:lgui xmlns:lml=\"http://eclipse.org/ptp/lml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
);
printf( OUT "	xsi:schemaLocation=\"http://eclipse.org/ptp/lml http://eclipse.org/ptp/schemas/lgui.xsd\"\n" );
printf( OUT "	version=\"1.0\"\>\n" );
printf( OUT "<objects>\n" );

# job objs
$count = 0;
foreach $jobid ( sort( keys(%jobs) ) ) {
	$count++;
	$jobnr{$jobid} = $count;
	printf( OUT "<object id=\"j%06d\" name=\"%s\" type=\"job\"/>\n",
		$count, $jobid );
}

# node objs
$count = 0;
foreach $nodeid ( sort( keys(%nodes) ) ) {
	$count++;
	$nodenr{$nodeid} = $count;
	printf( OUT "<object id=\"nd%06d\" name=\"%s\" type=\"node\"/>\n",
		$count, $nodeid );
}

printf( OUT "</objects>\n" );
printf( OUT "<information>\n" );

# job info
foreach $jobid ( sort( keys(%jobs) ) ) {
	printf( OUT "<info oid=\"j%06d\" type=\"short\">\n", $jobnr{$jobid} );
	foreach $key ( sort( keys( %{ $jobs{$jobid} } ) ) ) {
		if ( exists( $mapping_job{$key} ) ) {
			if ( $mapping_job{$key} ne "" ) {
				$value =
				  &modify( $key, $mapping_job{$key}, $jobs{$jobid}{$key} );
				if ($value) {
					printf( OUT " <data %-20s value=\"%s\"/>\n",
						"key=\"" . $mapping_job{$key} . "\"", $value );
				}
			} else {
				$notmappedkeys{$key}++;
			}
		} else {
			$notfoundkeys{$key}++;
		}

	}
	printf( OUT "</info>\n" );
}

# node info
foreach $nodeid ( sort( keys(%nodes) ) ) {
	printf( OUT "<info oid=\"nd%06d\" type=\"short\">\n", $nodenr{$nodeid} );
	foreach $key ( sort( keys( %{ $nodes{$nodeid} } ) ) ) {
		if ( exists( $mapping_node{$key} ) ) {
			if ( $mapping_node{$key} ne "" ) {
				$value =
				  &modify( $key, $mapping_node{$key}, $nodes{$nodeid}{$key} );
				if ($value) {
					printf( OUT " <data %-20s value=\"%s\"/>\n",
						"key=\"" . $mapping_node{$key} . "\"", $value );
				}
			} else {
				$notmappedkeys{$key}++;
			}
		} else {
			$notfoundkeys{$key}++;
		}
	}
	printf( OUT "</info>\n" );
}

printf( OUT "</information>\n" );
printf( OUT "</lml:lgui>\n" );

close(OUT);

foreach $key ( sort( keys(%notfoundkeys) ) ) {
	printf( "%-40s => \"\",\n", "\"" . $key . "\"", $notfoundkeys{$key} );
}

sub check_attach_cfg {
	my ($jobid) = @_;
	if ( open( CFG, "/tmp/.ppe.$jobid.attach.cfg" ) ) {

		# Ignore first line
		$line = <CFG>;

		# Second line has number of tasks
		chomp( $line = <CFG> );
		if ( $line =~ /$patint$patbl;/ ) {
			$jobs{$jobid}{totaltasks} = $1;
		}
		while ( $line = <CFG> ) {
			my ( $taskid, $nodeid );
			chomp($line);
			if ( $line =~ /$patint$patbl$patint$patbl$patnode/ ) {
				( $taskid, $nodeid ) = ( $1, $3 );
			} elsif ( $line =~ /$patint$patbl$patnode/ ) {
				( $taskid, $nodeid ) = ( $1, $2 );
			}
			$nodes{$nodeid}{id}           = $nodeid;
			$nodes{$nodeid}{state}        = "Running";
			$nodes{$nodeid}{_taskcounter} = 0
			  if ( !exists( $nodes{$nodeid}{_taskcounter} ) );
			$jobs{$jobid}{nodelist} .=
			  "($nodeid," . $nodes{$nodeid}{_taskcounter} . ")";
			$nodes{$nodeid}{_taskcounter}++;
		}
		close(CFG);
	}
}

sub modify {
	my ( $key, $mkey, $value ) = @_;
	my $ret = $value;

	if ( !$ret ) {
		return (undef);
	}

	if ( $mkey eq "owner" ) {
		$ret =~ s/\@.*//gs;
	}

	if ( ( $mkey eq "comment" ) ) {
		$ret =~ s/\"//gs;
	}

	# mask & in user input
	if ( $ret =~ /\&/ ) {
		$ret =~ s/\&/\&amp\;/gs;
	}

	return ($ret);
}
