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

my $patint = "([\\+\\-]?[\\d]+)";   # Pattern for Integer number
my $patfp = "([\\+\\-]?[\\d.E]+)";  # Pattern for Floating Point number
my $patwrd = "([\^\\s]+)";          # Pattern for Work (all noblank characters)
my $patbl = "\\s+";                 # Pattern for blank space (variable length)

#####################################################################
# get user info / check system
#####################################################################
my $UserID = getpwuid($<);
my $Hostname = `hostname`;
my $verbose = 1;
my ( $cmd, $line, %jobs, %steps, %jobnr, %midplanes, %nodenr, $nodecardspermidplane,
    $numpsets, $bgp, $bgq, $subblockjobs, @nodes, $node, $key, $value, $count, %notmappedkeys,
    %notfoundkeys );

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
    "JobId"             => "step",
    "Name"              => "name",
    "UserId"            => "owner",
    "GroupId"           => "group",
    "Priority"          => "",
    "Account"           => "account",
    "QOS"               => "",
    "JobState"          => "state",
    "Reason"            => "",
    "Dependency"        => "dependency",
    "Requeue"           => "",
    "Restarts"          => "restart",
    "BatchFlag"         => "",
    "ExitCode"          => "",
    "DerivedExitCode"   => "",
    "RunTime"           => "runtime",
    "TimeLimit"         => "wall",
    "TimeMin"           => "",
    "SubmitTime"        => "queuedate",
    "EligibleTime"      => "",
    "StartTime"         => "dispatchdate",
    "EndTime"           => "",
    "SuspendTime"       => "",
    "SecsPreSuspend"    => "",
    "Partition"         => "queue",
    "AllocNode:Sid"     => "",
    "ReqBP_List"        => "",
    "ExcBP_List"        => "",
    # Change between V2.3 & V2.4 for Blue Gene and Cluster
    "BP_List"           => "nodelist",
    "MidplaneList"      => "nodelist",
    "NodeList"          => "nodelist",
    "BatchHost"         => "",
    "NumNodes"          => "",
    "NumCPUs"           => "totalcores",
    "CPUs/Task"         => "",
    "ReqS:C:T"          => "",
    "CPUs"              => "",
    "Nodes"             => "",
    "CPU_IDs"           => "",
    "Mem"               => "",
    "MinCPUsNode"       => "",
    "MinMemoryNode"     => "",
    "MinTmpDiskNode"    => "",
    "Features"          => "",
    "Gres"              => "",
    "Reservation"       => "",
    "Shared"            => "",
    "Contiguous"        => "",
    "Licenses"          => "",
    "Network"           => "",
    "Command"           => "command",
    "WorkDir"           => "",
    "Block_ID"          => "",
    "Connection"        => "",
    "Reboot"            => "",
    "Rotate"            => "",
    "Geometry"          => "",
    "CnloadImage"       => "",
    "MloaderImage"      => "",
    "IoloadImage"       => "",
    "status"            => "status",
    "detailedstatus"    => "detailedstatus",

    # unknown attributes
);
# BlueGene/Q SLURM to Nodecard mappings
# with no sub-block jobs
# Column order is A-X-Y-Z-E
# Increment order is Y-Z-A-X-E
my %startnc = (
    "00000" => 0,
    "00200" => 1,
    "00020" => 2,
    "00220" => 3,
    "20000" => 4,
    "20200" => 5,
    "20020" => 6,
    "20220" => 7,
    "02000" => 8,
    "02200" => 9,
    "02020" => 10,
    "02220" => 11,
    "22000" => 12,
    "22200" => 13,
    "22020" => 14,
    "22220" => 15,
);

my %endnc = (
    "11111" => 0,
    "11311" => 1,
    "11131" => 2,
    "11331" => 3,
    "31111" => 4,
    "31311" => 5,
    "31131" => 6,
    "31331" => 7,
    "13111" => 8,
    "13311" => 9,
    "13131" => 10,
    "13331" => 11,
    "33111" => 12,
    "33311" => 13,
    "33131" => 14,
    "33331" => 15,
);

# BlueGene/Q Computecard mappings
# for sub-block jobs
# Column order is A-X-Y-Z-E
# Increment order is Y-Z-A-X-E
my %computecard = (
	"00000" => 0,
	"00100" => 1,
	"00010" => 2,
	"00110" => 3,
	"10000" => 4,
	"10100" => 5,
	"10010" => 6,
	"10110" => 7,
	"01000" => 8,
	"01100" => 9,
	"01010" => 10,
	"01110" => 11,
	"11000" => 12,
	"11100" => 13,
	"11010" => 14,
	"11110" => 15,
	"00001" => 16,
	"00101" => 17,
	"00011" => 18,
	"00111" => 19,
	"10001" => 20,
	"10101" => 21,
	"10011" => 22,
	"10111" => 23,
	"01001" => 24,
	"01101" => 25,
	"01011" => 26,
	"01111" => 27,
	"11001" => 28,
	"11101" => 29,
	"11011" => 30,
	"11111" => 31,
);

$cmd = "/usr/bin/scontrol";
$cmd = $ENV{"CMD_JOBINFO"} if ( $ENV{"CMD_JOBINFO"} );

# Get BG job information
open( IN, "$cmd show job --detail |" );
my $jobid   = "-";
my $lastkey = "-";
my @pairs;
my $pair = "";
while ( $line = <IN> ) {
    chomp($line);
    if ( $line =~ "No jobs in the system" ) {
        last;
    }
    if ( $line =~ /^JobId=([\w]+).*$/ ) {
        $jobid = $1;
        $jobs{$jobid}{JobId} = $jobid;
    }
    if ( $line =~ /^$/ ) {
        $jobs{$jobid}{$lastkey} .= $line;
    }
    else {
        $line =~ s/^\s+//;
        @pairs = split( /\s+/, $line );
        # For Clusters - get list of nodes and CPUs
        if ( $pairs[0] =~ /^NodeList=.*/ ) {
        	$value = substr( $pairs[0], 9 );
        	if ( $value ne "(null)" ) {
        	   $jobs{$jobid}{NodeList} = &expand_node_list( substr( $pairs[0], 9 ) );
        	}
        } 
        elsif ( $pairs[0] =~ /^Nodes=.*/ ) {
        	@nodes = split( / /, &expand_node_list( substr( $pairs[0], 6 ) ) );
        	foreach $pair (@pairs[1,-1]) {
        		( $key, $value ) = split( /=/, $pair );
        		if ( $key eq "CPU_IDs") {
        			foreach $node (@nodes) {
                        $jobs{$jobid}{$node} = $value;
        			}
        		}
        	}
        }
        else {
            foreach $pair (@pairs) {
                ( $key, $value ) = split( /=/, $pair );
                $lastkey = $key;
                if ( $value ne "(null)" ) {
                    $jobs{$jobid}{$key} = $value;
                }
            }
        }
    }
}
close(IN);

# add unknown but manatory attributes to jobs
foreach $jobid ( sort( keys(%jobs) ) ) {
    # check state
    ( $jobs{$jobid}{status}, $jobs{$jobid}{detailedstatus} ) =
        &get_state( $jobs{$jobid}{JobState}, $jobs{$jobid}{Reason} );
}

# add job step details
foreach $jobid ( sort( keys(%jobs) ) ) {
	open( IN, "$cmd show step " . $jobid . " |" );
	%steps = ();
	my $stepid   = "-";
	$lastkey = "-";
	$pair = "";
	while ( $line = <IN> ) {
    	chomp($line);
    	if ( $line =~ /Job step .* not found/ ) {
        	last;
    	}
	    if ( $line =~ /^StepId=([\w]+\.[\w]+).*$/ ) {
    	    $stepid = $1;
        	$steps{$stepid}{StepId} = $stepid;
    	}
    	if ( $line =~ /^$/ ) {
        	$steps{$stepid}{$lastkey} .= $line;
    	}
    	else {
        	$line =~ s/^\s+//;
        	@pairs = split( /\s+/, $line );
        	# For Clusters - get list of nodes and CPUs
        	if ( $pairs[0] =~ /^NodeList=.*/ ) {
        		$value = substr( $pairs[0], 9 );
        		if ( $value ne "(null)" ) {
        	   		$steps{$stepid}{NodeList} = &expand_node_list( substr( $pairs[0], 9 ) );
        		}
        	} 
        	elsif ( $pairs[0] =~ /^Nodes=.*/ ) {
        		@nodes = split( / /, &expand_node_list( substr( $pairs[0], 6 ) ) );
        		foreach $pair (@pairs[1,-1]) {
        			( $key, $value ) = split( /=/, $pair );
        			if ( $key eq "CPU_IDs") {
        				foreach $node (@nodes) {
                        	$steps{$stepid}{$node} = $value;
        				}
        			}
        		}
        	}
        	else {
            	foreach $pair (@pairs) {
                	( $key, $value ) = split( /=/, $pair );
                	$lastkey = $key;
                	if ( $value ne "(null)" ) {
                    	$steps{$stepid}{$key} = $value;
                	}
            	}
       		}
    	}
	}
	close(IN);
	
	if ( keys( %steps ) > 1 ) {
		# Have job steps so create copies of original job details for each step
		# and modify each for step details
		foreach $stepid ( sort( keys( %steps ) ) ) {
			foreach $key ( sort( keys( % {$jobs{$jobid} } ) ) ) {
				$jobs{$stepid}{$key} = $jobs{$jobid}{$key};
			}
			$jobs{$stepid}{JobId} = $stepid;
			$jobs{$stepid}{StartTime} = $steps{$stepid}{StartTime};
			$jobs{$stepid}{MidplaneList} = $steps{$stepid}{MidplaneList};
			$jobs{$stepid}{Command} = $steps{$stepid}{Name};					
		}
		# Remove original parent job details
		delete $jobs{$jobid};
	}
}

# Get details of nodes
&generate_bgnode_list();

open( OUT, "> $filename" ) || die "cannot open file $filename";
printf( OUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );
printf( OUT "<lml:lgui xmlns:lml=\"http://eclipse.org/ptp/lml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" );
printf( OUT "    xsi:schemaLocation=\"http://eclipse.org/ptp/lml http://eclipse.org/ptp/schemas/v1.1/lgui.xsd\"\n" );
printf( OUT "    version=\"1.1\"\>\n" );
printf( OUT "<objects>\n" );

$count = 0;
foreach $jobid ( sort( keys(%jobs) ) ) {
    $count++;
    $jobnr{$jobid} = $count;
    printf( OUT "<object id=\"j%06d\" name=\"%s\" type=\"job\"/>\n", $count, $jobid );
}
printf( OUT "</objects>\n" );
printf( OUT "<information>\n" );
foreach $jobid ( sort( keys(%jobs) ) ) {
    printf( OUT "<info oid=\"j%06d\" type=\"short\">\n", $jobnr{$jobid} );
    foreach $key ( sort( keys( %{ $jobs{$jobid} } ) ) ) {
        if ( exists( $mapping{$key} ) ) {
            if ( $mapping{$key} ne "" ) {
                $value = &modify( $jobid, $key, $mapping{$key}, $jobs{$jobid}{$key} );
                if ($value) {
                    printf( OUT " <data %-20s value=\"%s\"/>\n", "key=\"" . $mapping{$key} . "\"", $value );
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

sub get_state {
    my ( $job_state, $Hold_types ) = @_;
    my ( $state, $detailed_state );

    $state = "UNDETERMINED";
    $detailed_state = "QUEUED_ACTIVE";

    if ( $job_state eq "PENDING" || $job_state eq "SUSPENDED" ) {
        $state = "SUBMITTED";
        $detailed_state = "USER_ON_HOLD" if ( $Hold_types eq "JobHeldUser" );
        $detailed_state = "SYSTEM_ON_HOLD" if ( $Hold_types ne "None" && $Hold_types ne "JobHeldUser" );
    }
    if ( $job_state eq "CONFIGURING" ) {
        $state = "SUBMITTED";
    }
    if ( $job_state eq "RUNNING" || $job_state eq "COMPLETING" ) {
        $state = "RUNNING";
    }
    if ( $job_state eq "COMPLETED" ) {
        $state = "COMPLETED";
        $detailed_state = "JOB_OUTERR_READY";
    }
    if ( $job_state eq "CANCELLED" ) {
        $state = "COMPLETED";
        $detailed_state = "CANCELED";
    }    
    if ( $job_state eq "FAILED"
        || $job_state eq "NODE_FAIL"
        || $job_state eq "TIMEOUT" ) {
        $state = "COMPLETED";
        $detailed_state = "FAILED";
    }

    return ( $state, $detailed_state );
}

sub modify {
    my ( $jobid, $key, $mkey, $value ) = @_;
    my $ret = $value;

    if ( !$ret ) {
        return (undef);
    }

    if ( $mkey eq "state" ) {
        $ret = "Cancelled" if ( $value eq "CANCELLED" );
        $ret = "Completed" if ( $value eq "COMPLETED" );
        $ret = "Pending"   if ( $value eq "CONFIGURING" );
        $ret = "Running"   if ( $value eq "COMPLETING" );
        $ret = "Failed"    if ( $value eq "FAILED" );
        $ret = "Failed"    if ( $value eq "NODE_FAIL" );
        $ret = "Pending"   if ( $value eq "PENDING" );
        $ret = "Running"   if ( $value eq "RUNNING" );
        $ret = "Suspended" if ( $value eq "SUSPENDED" );
        $ret = "Failed"    if ( $value eq "TIMEOUT" );
    }

    if ( $mkey eq "wall" || $mkey eq "runtime" ) {
        if ( $value =~ /\($patint seconds\)/ ) {
            $ret = $1;
        }
        if ( $value =~ /$patint minutes/ ) {
            $ret = $1 * 60;
        }
        if ( $value =~ /^$patint[:]$patint[:]$patint$/ ) {
            $ret = $1 * 60 * 60 + $2 * 60 + $3;
        }
        if ( $value =~ /^$patint[-]$patint[:]$patint[:]$patint$/ ) {
            $ret = $1 * 24 * 60 * 60 + $2 * 60 * 60 + $3 * 60 + $4;
        }
    }

    if ( $mkey =~ ".*date" ) {
        if ( $ret eq "Unknown" ) {
            $ret = "";
        }
        else {
            $ret =~ s/T/ /;
        }
    }

    if ( $mkey eq "nodelist" ) {
        if ( $ret ne "-" ) {
        	if ( $bgp eq "true" ) {
                $ret = &get_bgpnode_list($ret);
        	}
        	elsif ( $bgq eq "true" ) {
                $ret = &get_bgqnode_list($ret);
        	}
        	else {
        		$ret = &get_node_list($jobid, $ret);
        	}
        }
    }

    if ( $mkey eq "totalcores" ) {
        $ret =~ s/-[\d]+//;
    }

    return ($ret);
}

sub expand_node_list() {
    my ( $nodelist ) = @_;
    my ( $ret, $nodes, $prefix, $suffix, $num, $n, $x, $y );

    # Split node list by commas only between groups of nodes, not within groups
    # Regex notes:
    #   \w+ - matches one or more "word" characters
    #   (?:\[[\d,-]*\])? - matches zero or one (optional) node groupings like [1,3,5-8,10]
    #                      (?:..) means don't capture matching characters for return
    #   (...) - means capture word characters and optional grouping for return to split 
    #   ,? - matches zero or one comma between groups of nodes.  Need zero option to handle no commas in node list
    foreach $nodes ( split( /(\w+(?:\[[\d,-]*\])?),?/, $nodelist ) ) {
        next if length( $nodes ) == 0;
        ( $prefix, $suffix ) = split( /\[(.*)\]/, $nodes );

        if ( $prefix eq $nodes ) {
            # single node
            $ret .= " " . $nodes;
        }
        else {
            # multiple nodes in node list as in "node[a,b,m-n,x-y]"
            foreach $num ( split( /,/, $suffix ) ) {
                ( $x, $y ) = split( /-/, $num );
                if ( $x eq $num ) {
                    # single node
                    my $format = sprintf( "%%s%%0%dd", length( $num ) );
                    $ret .= " " . sprintf( $format, $prefix, $num );
                }
                else {
                    # multiple nodes
                    my $format = sprintf( "%%s%%0%dd", length( $x ) );
                    for ( $n = $x; $n <= $y; $n++ ) {
        	           $ret .= " " . sprintf( $format, $prefix, $n );
                    }
                }
            }
        }
    }

    return ( substr( $ret, 1 ) );
}

sub get_node_list() {
	my ( $jobid, $nodes ) = @_;
	my ( $ret, $cpulist, $n, $x, $y );
	
	foreach $node ( split( / /, $nodes ) ) {
		foreach $cpulist ( split( /,/, $jobs{$jobid}{$node} ) ) {
		    $x = 0;
		    $y = 0;
		    ( $x, $y ) = split( /-/, $cpulist );
            $y = $x if ( $y == 0 );
            for ( $n = $x; $n <= $y; $n++ ) {
        	    $ret .= "(" . $node . "," . $n . ")"
            }
		}
	}

	return ($ret);
}

sub generate_bgnode_list() {

    # Get SLURM configuration for Blue Gene details
    open( IN, "$cmd show config |" );

    # Get Blue Gene information from configuration
    $numpsets = 0;
    $bgp = "false";
    $bgq = "false";
    $subblockjobs = "false";
    my $nodespermidplane = 0;
    my $nodespernodecard = 0;
    while ( $line = <IN> ) {
        chomp($line);
        if ( $line =~ /^BasePartitionNodeCnt\s+=\s+([\w]+)$/
            || $line =~ /^MidPlaneNodeCnt\s+=\s+([\w]+)$/ ) {
            $nodespermidplane = $1;
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
        elsif ( $line =~ /^AllowSubBlockAllocations\s+=\s+Yes$/ ) {
        	$subblockjobs = "true";
        }
    }
    close(IN);

    if ( $bgp eq "true" || $bgq eq "true" ) {
        # Get list of SLURM nodes - on Blue Gene this is midplanes
        open( IN, "$cmd show node |" );

        # Count Blue Gene midplanes
        my $name = "";
        my ( $y, $z, $maxy, $maxz, $rmd, $row, $rack, $mid );
        $maxy = 0;
        $maxz = 0;
        while ( $line = <IN> ) {
            chomp($line);
            if ( $line =~ /^NodeName=([\w]+).*$/ ) {
                $name = $1;
                $midplanes{$name}{NodeName} = $name;
        		if ( $bgq eq "true" && $line =~ /.*RackMidplane=([\w]+-[\w]+).*$/ ) {
        			$rmd = $1;
        			$rmd =~ /R(\d)(\d)-M(\d)/s;
        			( $row, $rack, $mid ) = ( $1, $2, $3 );
        			$midplanes{$name}{RackMidplane} = sprintf( "R%02d%02d-M%01d", $row, $rack, $mid );
        			$y = $rack;
        			$z = $mid;
        		}
        		else {
                	$y = substr $name, -2, 1;
                	$z = substr $name, -1, 1;
        		}
                $y = 10 + ( ord($y) - ord('A') ) if ( $y =~ /[A-Z]/ );
                $z = 10 + ( ord($z) - ord('A') ) if ( $y =~ /[A-Z]/ );
                $maxy = $y if ( $y > $maxy );
                $maxz = $z if ( $z > $maxz );
            }
        }
        close(IN);

        # Update maxy if maxz > 2 midplanes per rack
        if ( $maxz > 1 ) {
            $maxy = ( ( $maxy + 1 ) * ( ( $maxz + 1 ) / 2 ) ) - 1;
            $maxz = 1;
        }

        # Calculate number of node cards (resolution of Blue Gene display)
        $nodecardspermidplane = $nodespermidplane / $nodespernodecard;
        if ( $nodecardspermidplane < $numpsets ) {
            # I/O rich system - more I/O nodes than nodes cards
            # Represent each pset as a "node card"
            $nodecardspermidplane = $numpsets;
        }

        $row = 0;
        $rack = 0;
        $mid = 0;
        my $nodecard = 0;
        my $nodeid = "";
        my $midplane = "";
        foreach $midplane ( sort( keys(%midplanes) ) ) {
            for ( $nodecard = 0 ; $nodecard < $nodecardspermidplane ; $nodecard++ ) {
                $nodeid = sprintf( "R%02d%02d-M%01d-N%02d", $row, $rack, $mid, $nodecard );
                $nodenr{$midplane}[$nodecard] = $nodeid;
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
    }
}

sub get_bgpnode_list {
    my ( $nodes ) = @_;
    my ( $ret, $prefix, $suffix, $n );

    ( $prefix, $suffix ) = split( /\[(.*)\]/, $nodes );

    if ( $prefix eq $nodes ) {
        # single midplane
        for ( $n = 0 ; $n < $nodecardspermidplane ; $n++ ) {
            $ret .= "," . $nodenr{$prefix}[$n];
        }
    }
    elsif ( exists $midplanes{$prefix} ) {
        # sub-midplane partition
        my ( $n1, $n2 );
        $n1 = 0;
        $n2 = 0;
        ( $n1, $n2 ) = split( /-/, $suffix );
        $n2 = $n1 if ( $n2 == 0 );
        if ( $nodecardspermidplane > $numpsets ) {
            my $factor = $nodecardspermidplane / $numpsets;
            $n1 *= $factor;
            $n2 = ( ( $n2 + 1 ) * $factor ) - 1;
        }
        for ( $n = $n1 ; $n <= $n2 ; $n++ ) {
            $ret .= "," . $nodenr{$prefix}[$n];
        }
    }
    else {
        # more than one midplane
        my ( $mdlist, $cnr1, $cnr2, @dim1, @dim2, $i, $j, $k, $midplane );
        foreach $mdlist ( split( /,/, $suffix ) ) {
            ( $cnr1, $cnr2 ) = split( /x/, $mdlist );
            for ( $i = 0 ; $i < length($cnr1) ; $i++ ) {
                $dim1[$i] = substr( $cnr1, $i, 1 );
            }
            for ( $i = 0 ; $i < length($cnr2) ; $i++ ) {
                $dim2[$i] = substr( $cnr2, $i, 1 );
            }
            for ( $i = $dim1[0] ; $i <= $dim2[0] ; $i++ ) {
                for ( $j = $dim1[1] ; $j <= $dim2[1] ; $j++ ) {
                    for ( $k = $dim1[2] ; $k <= $dim2[2] ; $k++ ) {
                        $midplane = $prefix . $i . $j . $k;
                        for ( $n = 0 ; $n < $nodecardspermidplane ; $n++ ) {
                            $ret .= "," . $nodenr{$midplane}[$n];
                        }
                    }
                }
            }
        }
    }

    return ( substr( $ret, 1 ) );
}

sub get_bgqnode_list {
    my ( $nodes ) = @_;
    my ( $ret, $prefix, $suffix, $n );

    ( $prefix, $suffix ) = split( /\[(.*)\]/, $nodes );

    if ( $prefix eq $nodes ) {
        # single midplane
        for ( $n = 0 ; $n < $nodecardspermidplane ; $n++ ) {
            $ret .= "," . $nodenr{$prefix}[$n];
        }
    }
    elsif ( exists $midplanes{$prefix} ) {
        # sub-midplane partition
        my ( $n1, $n2, $cnr1, $cnr2, $startcnr, $endcnr );
        $n1 = 0;
        $n2 = 0;
        $cnr1 = "";
        $cnr2 = "";
        ( $cnr1, $cnr2 ) = split( /x/, $suffix );
        $cnr2 = $cnr1 if ( $cnr2 eq "" );
        $startcnr = &floor_nc( $cnr1 );
        $endcnr = &ceil_nc( $cnr2 );
        $n1 = $startnc{$startcnr};
        $n2 = $endnc{$endcnr};
        $n2 = $n1 if ( $n2 == 0 );
        #print "cnr1=" . $cnr1 . " cnr2=" . $cnr2 . " startcnr=" . $startcnr . " endcnr=" . $endcnr . " n1=" . $n1 . " n2=" . $n2 . "\n";
        if ( $subblockjobs eq "true" ) {
        	my ( $geom, $a, $x, $y, $z, $e, $adim, $xdim, $ydim, $zdim, $edim, $c, $cc, $snc, $nc );
        	$geom = sprintf( "%05d", $cnr2 - $cnr1 );
        	$geom =~ /(\d)(\d)(\d)(\d)(\d)/s;
        	# Column order of Geometry is A-X-Y-Z-E
			# Increment order is Y-Z-A-X-E
        	( $a, $x, $y, $z, $e ) = ( $1, $2, $3, $4, $5 );
        	#print "geom=" . $geom . " a=" . $a . " x=" . $x . " y=" . $y . " z=" . $z . " e=" . $e . "\n";
        	for ( $edim = 0; $edim <= $e; $edim++ ) {
        		for ( $xdim = 0; $xdim <= $x; $xdim++ ) {
        			for ( $adim = 0; $adim <= $a; $adim++ ) {
        				for ( $zdim = 0; $zdim <= $z; $zdim++ ) {
        					for ( $ydim = 0; $ydim <= $y; $ydim++ ) {
        						$c = sprintf( "%05d", $cnr1 + ( $adim . $xdim . $ydim . $zdim . $edim ) );
        						$snc = &floor_nc( $c );
        						$cc = $computecard{sprintf( "%05d", $c - $snc)};
        						$nc = $startnc{$snc};
        						#print "nc=" . $nc . " snc=" . $snc . " c=" . $c . " cc=" . $cc . "\n";
        						$ret .= "," . $nodenr{$prefix}[$nc] . sprintf( "-C%02d", $cc );        						
        					}
        				}
        			}
        		}
        	}
        }
        else {
        	# Whole nodecard partition
        	for ( $n = $n1 ; $n <= $n2 ; $n++ ) {
        		$ret .= "," . $nodenr{$prefix}[$n];
        	}
        }
    }
    else {
        # more than one midplane
        my ( $mdlist, $cnr1, $cnr2, @dim1, @dim2, $i, $j, $k, $l, $midplane );
        foreach $mdlist ( split( /,/, $suffix ) ) {
            ( $cnr1, $cnr2 ) = split( /x/, $mdlist );
            for ( $i = 0 ; $i < length($cnr1) ; $i++ ) {
                $dim1[$i] = substr( $cnr1, $i, 1 );
            }
            for ( $i = 0 ; $i < length($cnr2) ; $i++ ) {
                $dim2[$i] = substr( $cnr2, $i, 1 );
            }
        	# Column order of midplane dimensions is A-X-Y-Z
			# Increment order is Z-Y-A-X
            for ( $i = $dim1[1] ; $i <= $dim2[1] ; $i++ ) {
                for ( $j = $dim1[0] ; $j <= $dim2[0] ; $j++ ) {
                    for ( $k = $dim1[2] ; $k <= $dim2[2] ; $k++ ) {
                        for ( $l = $dim1[3] ; $l <= $dim2[3] ; $l++ ) {
                            $midplane = $prefix . $j . $i . $k . $l;
                            for ( $n = 0 ; $n < $nodecardspermidplane ; $n++ ) {
                                $ret .= "," . $nodenr{$midplane}[$n];
                            }
                        }
                    }
                }
            }
        }
    }

    return ( substr( $ret, 1 ) );
}

sub floor_nc {
    my ( $nc ) = @_;
    my ( $i, $n, $newnc );
    
    for ($i = 0; $i < length($nc); $i++ ) {
        $n = substr( $nc, $i, 1 );
        if ( $n == 1 || $n == 3 ) {
            $newnc .= $n - 1;
        }
        else {
            $newnc .= $n;
        }
    }
    
    return ( $newnc );
}

sub ceil_nc {
    my ( $nc ) = @_;
    my ( $i, $n, $newnc );

    for ($i = 0; $i < length($nc); $i++ ) {
        $n = substr( $nc, $i, 1 );
        if ( $n == 0 || $n == 2 ) {
            $newnc .= $n + 1;
        }
        else {
            $newnc .= $n;
        }
    }
    
    return ( $newnc );
}
