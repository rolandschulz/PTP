#!/usr/bin/perl -w
#*******************************************************************************
#* Copyright (c) 2011 IBM.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*
#* Contributors:
#*    Simon Wail (IBM)
#*******************************************************************************/
use strict;

use FindBin;
use lib "$FindBin::RealBin/../../lib";
use LML_da_util;

my $patint = "([\\+\\-\\d]+)";  # Pattern for Integer number
my $patfp = "([\\+\\-\\d.E]+)"; # Pattern for Floating Point number
my $patwrd = "([\^\\s]+)";      # Pattern for Work (all noblank characters)
my $patbl = "\\s+";             # Pattern for blank space (variable length)

#####################################################################
# get user info / check system
#####################################################################
my $UserID = getpwuid($<);
my $Hostname = `hostname`;
my $verbose = 1;
my ( $cmd, $line, %nodes, %nodenr, $node, $nodeid, $key, $value, $count, $maxx, $maxy,
    $maxz, $nodecardspermidplane, %notmappedkeys, %notfoundkeys );

#####################################################################
# get command line parameter
#####################################################################
if ( $#ARGV != 0 ) {
    die " Usage: $0 <filename> $#ARGV\n";
}

my $filename = $ARGV[0];
my $system_sysprio = -1;
my $maxtopdogs = -1;

my %mapping = (
    "NodeName"        => "id",
    "RackMidplane"	  => "id",
    "Location"		  => "location",
    "CoresPerSocket"  => "",
    "CPUAlloc"        => "",
    "CPUErr"          => "",
    "CPUTot"          => "ncores",
    "Features"        => "",
    "Gres"            => "",
    "RealMemory"      => "physmem",
    "Sockets"         => "",
    "State"           => "state",
    "ThreadsPerCore"  => "",
    "TmpDisk"         => "",
    "Weight"          => "",
    "BootTime"        => "",
    "SlurmdStartTime" => "",
    "Reason"          => "",

    # unknown attributes
);

$cmd = "/usr/bin/scontrol";
$cmd = $ENV{"CMD_NODEINFO"} if ( $ENV{"CMD_NODEINFO"} );

# Get SLURM configuration for Cluster or Blue Gene details
open( IN, "$cmd show config |" );

# Get Cluster or Blue Gene information from configuration
my $nodespermidplane = 0;
my $nodespernodecard = 0;
my $corespernode = 0;
my $numpsets = 0;
my $bgp = "false";
my $bgq = "false";
while ( $line = <IN> ) {
    chomp($line);
    if ( $line =~ /^BasePartitionNodeCnt\s+=\s+([\w]+)$/
        || $line =~ /^MidPlaneNodeCnt\s+=\s+([\w]+)$/ ) {
        $nodespermidplane = $1;
    }
    elsif ( $line =~ /^NodeCPUCnt\s+=\s+([\w]+)$/ ) {
        $corespernode = $1;
    }
    elsif ( $line =~ /^NodeCardNodeCnt\s+=\s+([\w]+)$/ ) {
        $nodespernodecard = $1;
    }
    elsif ($line =~ /^Numpsets\s+=\s+([\w]+)$/
        || $line =~ /^IONodesPerMP\s+=\s+([\w]+)$/ ) {
        $numpsets = $1;
    }
    elsif ( $line =~ /^Bluegene\/P configuration$/ ) {
        $bgp = "true";
    }
    elsif ( $line =~ /^Bluegene\/Q configuration$/ ) {
        $bgq = "true";
    }
}
close(IN);

# Get list of SLURM nodes - on Blue Gene this is midplanes
open( IN, "$cmd show node |" );

# Count Cluster nodes or Blue Gene midplanes
my $name = "";
my $mem  = "";
my $rmd  = "";
my ( $location, $a, $b, $c, $d );
my $row  = 0;
my $rack = 0;
my $mid  = 0;
$count = 0;
while ( $line = <IN> ) {
    chomp($line);
    if ( $line =~ /^NodeName=([\w]+).*$/ ) {
        $name = $1;
        $nodes{$name}{NodeName} = $name;
        if ( $bgq eq "true" ) {
			$nodes{$name}{RackMidplane} = "";
        	if ( $line =~ /.*RackMidplane=([\w]+-[\w]+).*$/ ) {
        		$rmd = $1;
        		$rmd =~ /R(\d)(\d)-M(\d)/s;
        		( $row, $rack, $mid ) = ( $1, $2, $3 );
        		$nodes{$name}{RackMidplane} = sprintf( "R%02d%02d-M%01d", $row, $rack, $mid );
        	}
       		$location = $name;
    		$location =~ /[\w]+(\d)(\d)(\d)(\d)/s;
    		( $a, $b, $c, $d ) = ( $1, $2, $3, $4 );
        	$nodes{$name}{Location} = sprintf( "(%d,%d,%d,%d)", $a, $b, $c, $d );
        }	
        $count++;
    }
    elsif ( $line =~ /^\s+RealMemory=([\w]+).*$/ ) {
        $nodes{$name}{RealMemory} = $1;
        $mem = $1;
    }
    elsif ( $line =~ /^\s+State=([\w]+).*$/ ) {
        $nodes{$name}{State} = $1;
    }
    elsif ( $line =~ /^.*CPUTot=([\w]+).*$/ ) {
    	$nodes{$name}{CPUTot} = $1;
    }
}
close(IN);

open( OUT, "> $filename" ) || die "cannot open file $filename";
printf( OUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );
printf( OUT "<lml:lgui xmlns:lml=\"http://eclipse.org/ptp/lml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" );
printf( OUT "    xsi:schemaLocation=\"http://eclipse.org/ptp/lml http://eclipse.org/ptp/schemas/v1.1/lgui.xsd\"\n" );
printf( OUT "    version=\"1.1\"\>\n" );
printf( OUT "<objects>\n" );

if ( $bgp eq "true" ) {
    # Calculate number of node cards or psets (resolution of Blue Gene display)
    # and cores per node card
    $nodecardspermidplane = $nodespermidplane / $nodespernodecard;
    if ( $nodecardspermidplane < $numpsets ) {
        # I/O rich system - more I/O nodes than nodes cards
        # Represent each pset as a "node card"
        $nodecardspermidplane = $numpsets;
        $nodespernodecard = $nodespermidplane / $numpsets;
    }
    my $nodecards = $count * $nodecardspermidplane;
    my $corespernodecard = $corespernode * $nodespernodecard;
    my $mempernodecard = $mem / $nodecards;

    foreach $node ( sort( keys(%nodes) ) ) {
        $nodes{$node}{CPUTot} = $corespernodecard;
        $nodes{$node}{RealMemory} = $mempernodecard;
    }
}
if ( $bgp eq "true" || $bgq eq "true" ) {
    # Ouptut Blue Gene partition (midplane) information
    # And determine dimensions of the system
    $count = 0;
    my ( $a, $x, $y, $z );
    $maxx = 0;
    $maxy = 0;
    $maxz = 0;
    foreach $node ( sort( keys(%nodes) ) ) {
    	if ( $bgp eq "true" ) {
        	printf( OUT "<object id=\"bgbp%06d\" name=\"%s\" type=\"partition\"/>\n", $count, &LML_da_util::escapeForXML($node) );
    	}
        $count++;
        $x = substr $node, -3, 1;
        $y = substr $node, -2, 1;
        $z = substr $node, -1, 1;
        $x = 10 + ( ord($x) - ord('A') ) if ( $x =~ /[A-Z]/ );
        $y = 10 + ( ord($y) - ord('A') ) if ( $y =~ /[A-Z]/ );
        $z = 10 + ( ord($z) - ord('A') ) if ( $z =~ /[A-Z]/ );
        $maxx = $x if ( $x > $maxx );
        $maxy = $y if ( $y > $maxy );
        $maxz = $z if ( $z > $maxz );
    }

    # Update maxy if maxz > 2 midplanes per rack
    if ( $maxz > 1 ) {
        $maxy = ( ( $maxy + 1 ) * ( ( $maxz + 1 ) / 2 ) ) - 1;
        $maxz = 1;
    }
}

# Output Cluster or Blue Gene nodes (node cards) information
$count = 0;
$row = 0;
$rack = 0;
$mid = 0;
my $nodecard = 0;
foreach $node ( sort( keys(%nodes) ) ) {
	if ( $bgp eq "true" || $bgq eq "true" ) {
		if ( $bgp eq "true" ) {
			for ( $nodecard = 0 ; $nodecard < $nodecardspermidplane ; $nodecard++ ) {
	        	$nodeid = sprintf( "R%02d%02d-M%01d-N%02d", $row, $rack, $mid, $nodecard );
            	$nodenr{$nodeid}{num} = $count;
            	$nodenr{$nodeid}{midplane} = $node;
            	printf( OUT "<object id=\"nd%06d\" name=\"%s\" type=\"node\"/>\n", $count, &LML_da_util::escapeForXML($nodeid) );
            	$count++;
        	}
		}
		if ( $bgq eq "true" ) {
			# Blue Gene/Q defines each midplane as a node
			$nodeid = $nodes{$node}{RackMidplane};
			if ( $nodeid eq "" ) {
				$nodeid = sprintf( "R%02d%02d-M%01d", $row, $rack, $mid );
				$nodes{$node}{RackMidplane} = $nodeid;
			}
       		printf( OUT "<object id=\"%s\" name=\"%s\" type=\"node\"/>\n", &LML_da_util::escapeForXML($node), &LML_da_util::escapeForXML($nodeid) );    		
        	$nodenr{$node}{node} = $node;
		}
        $mid++;
        if ( $mid > $maxz ) {
           	$mid = 0;
           	$rack++;
        }
        if ( $rack > $maxy ) {
           	$rack = 0;
           	$row++;
        }
	}
	else {
        $nodenr{$node}{num} = $count;
        $nodenr{$node}{node} = $node;
        printf( OUT "<object id=\"nd%06d\" name=\"%s\" type=\"node\"/>\n", $count, &LML_da_util::escapeForXML($node) );
        $count++;		
	}
}

printf( OUT "</objects>\n" );
printf( OUT "<information>\n" );

if ( $bgp eq "true" ) {
    # Output Blue Gene partition (midplane) details
    $count = 0;
    my $row   = 0;
    my $rack  = 0;
    my $mid   = 0;
    foreach $node ( sort( keys(%nodes) ) ) {
        printf( OUT "<info oid=\"bgbp%06d\" type=\"short\">\n", $count );
        printf( OUT "  <data %-20s value=\"%s\"/>\n", "key=\"" . "bgp_partitionid" . "\"", &LML_da_util::escapeForXML($node) );
        printf( OUT "  <data %-20s value=\"%s\"/>\n", "key=\"" . "x_loc" . "\"", &LML_da_util::escapeForXML($row) );
        printf( OUT "  <data %-20s value=\"%s\"/>\n", "key=\"" . "y_loc" . "\"", &LML_da_util::escapeForXML($rack) );
        printf( OUT "  <data %-20s value=\"%s\"/>\n", "key=\"" . "z_loc" . "\"", &LML_da_util::escapeForXML($mid) );
        printf( OUT "</info>\n" );
        $count++;

        $mid++;
        if ( $mid > $maxz ) {
            $mid = 0;
            $rack++;
        }
        if ( $rack > $maxy ) {
            $rack = 0;
            $row++;
        }
    }
}

# Output Cluster or Blue Gene node (node card) details
my $nodenum = "";
foreach $nodeid ( sort( keys(%nodenr) ) ) {
	if ( $bgq eq "true") {
		$nodenum = $nodenr{$nodeid}{node};	
	}
	else {
		$nodenum = sprintf( "nd%06d", $nodenr{$nodeid}{num} );
	}
   	printf( OUT "<info oid=\"%s\" type=\"short\">\n", &LML_da_util::escapeForXML($nodenum) );
   	if ( $bgp eq "true" ) {
       	$node = $nodenr{$nodeid}{midplane};
   	}
   	else {
    	$node = $nodenr{$nodeid}{node};
    }
    foreach $key ( sort( keys( %{ $nodes{$node} } ) ) ) {
        if ( exists( $mapping{$key} ) ) {
          	if ( $mapping{$key} ne "" ) {
                $value = &modify( $key, $mapping{$key}, $nodes{$node}{$key}, $nodeid );
                if ($value) {
                    printf( OUT " <data %-20s value=\"%s\"/>\n", "key=\"" . $mapping{$key} . "\"", &LML_da_util::escapeForXML($value) );
                }
            }
            else {
                $notmappedkeys{$key}++;
            }
        }
        else {
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

sub modify {
    my ( $key, $mkey, $value, $node ) = @_;
    my $ret = $value;

    if ( $mkey eq "id" ) {
    	if ( $bgq eq "true" ) {
    		if ( $key ne "RackMidplane" ) {
    			$ret = "";
    		}
    	}
    	else {
        	$ret = $node;
    	}
    }

    if ( $mkey eq "state" ) {
        $ret = "Down"    if ( $value eq "NoResp" );
        $ret = "Running" if ( $value eq "ALLOC" );
        $ret = "Running" if ( $value eq "ALLOCATED" );
        $ret = "Down"    if ( $value eq "DOWN" );
        $ret = "Drained" if ( $value eq "DRAIN" );
        $ret = "Down"    if ( $value eq "FAIL" );
        $ret = "Down"    if ( $value eq "FAILING" );
        $ret = "Idle"    if ( $value eq "IDLE" );
        $ret = "Running" if ( $value eq "MIXED" );
        $ret = "Down"    if ( $value eq "MAINT" );
        $ret = "Down"    if ( $value eq "POWER_DOWN" );
        $ret = "Down"    if ( $value eq "POWER_UP" );
        $ret = "Down"    if ( $value eq "RESUME" );
    }

    if ( $mkey eq "physmem" ) {
        $ret = $value . "mb";
    }

    return ($ret);
}
