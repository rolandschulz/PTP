#!/usr/bin/perl -w
#*******************************************************************************
#* Copyright (c) 2011-2012 IBM Corporation and others.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*
#* Contributors:
#*    Simon Wail (IBM)
#*	  Carsten Karbach (Forschungszentrum Juelich GmbH) 
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
my ( $cmd, $line, %jobs, %jobnr, %midplanes, %nodenr, $nodecardspermidplane,
    $numpsets, $bgp, $bgq, @nodes, $node, $key, $value, $count, %notmappedkeys,
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
    "Account"           => "",
    "QOS"               => "",
    "JobState"          => "state",
    "Reason"            => "",
    "Dependency"        => "dependency",
    "Requeue"           => "",
    "Restarts"          => "restart",
    "BatchFlag"         => "",
    "ExitCode"          => "",
    "DerivedExitCode"   => "",
    "RunTime"           => "",
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
    "Command"           => "",
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
        
        removeInvalidPairs( \@pairs );
        
        # For Clusters - get list of nodes and CPUs
        if ( $pairs[0] =~ /^NodeList=.*/ ) {
        	$value = substr( $pairs[0], 9 );
        	if ( $value ne "(null)" ) {
        	   $jobs{$jobid}{NodeList} = &expand_node_list( substr( $pairs[0], 9 ) );
        	}
        } 
        elsif ( $pairs[0] =~ /^Nodes=.*/ ) {
        	@nodes = split( / /, &expand_node_list( substr( $pairs[0], 6 ) ) );
			if($#pairs >=2 ){
	        	foreach $pair (@pairs[1,-1]) {
	        		( $key, $value ) = split( /=/, $pair );
	        		if ( $key eq "CPU_IDs") {
	        			foreach $node (@nodes) {
	                        $jobs{$jobid}{$node} = $value;
	        			}
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

# Get details of nodes
&generate_bgnode_list();

open( OUT, "> $filename" ) || die "cannot open file $filename";
printf( OUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );
printf( OUT "<lml:lgui xmlns:lml=\"http://eclipse.org/ptp/schemas\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" );
printf( OUT "    xsi:schemaLocation=\"http://eclipse.org/ptp/schemas http://eclipse.org/ptp/schemas/lgui.xsd\"\n" );
printf( OUT "    version=\"0.7\"\>\n" );
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
        	#if ( exists( $jobs{$jobid}{NodeList}{$key} ) ) {
        	   # Found $key in node list - this value defines CPUs allocated for node
        	   # Do nothing as already handled by "modify" routine for "nodelist" above
        	#}
        	#else {
                $notfoundkeys{$key}++;
        	#}
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
    $detailed_state = "";

    if ( $job_state eq "PENDING" || $job_state eq "SUSPENDED" ) {
        $state = "SUBMITTED";
        $detailed_state = "USER_ON_HOLD" if ( $Hold_types eq "JobHeldUser" );
        $detailed_state = "SYSTEM_ON_HOLD" if ( $Hold_types ne "None" && $Hold_types ne "JobHeldUser" );
    }
    if ( $job_state eq "CONFIGURING" ) {
        $state = "SUBMITTED";
        $detailed_state = "";
    }
    if ( $job_state eq "RUNNING" || $job_state eq "COMPLETING" ) {
        $state = "RUNNING";
        $detailed_state = "";
    }
    if ( $job_state eq "COMPLETED"
        || $job_state eq "CANCELLED"
        || $job_state eq "FAILED"
        || $job_state eq "NODE_FAIL"
        || $job_state eq "TIMEOUT" ) {
        $state = "COMPLETED";
        $detailed_state = "JOB_OUTERR_READY";
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

    if ( $mkey eq "wall" ) {
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
        	if ( $bgp eq "true" || $bgq eq "true" ) {
                $ret = &get_bgnode_list($ret);
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
		if( defined($jobs{$jobid}{$node}) ){
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
    }
    close(IN);

    if ( $bgp eq "true" || $bgq eq "true" ) {
        # Get list of SLURM nodes - on Blue Gene this is midplanes
        open( IN, "$cmd show node |" );

        # Count Blue Gene midplanes
        my $name = "";
        my ( $y, $z, $maxy, $maxz );
        $maxy = 0;
        $maxz = 0;
        while ( $line = <IN> ) {
            chomp($line);
            if ( $line =~ /^NodeName=([\w]+).*$/ ) {
                $name = $1;
                $midplanes{$name}{NodeName} = $name;
                $y = substr $name, -2, 1;
                $z = substr $name, -1, 1;
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

        my $row = 0;
        my $rack = 0;
        my $mid = 0;
        my $nodecard = 0;
        my $nodeid = "";
        my $midplane = "";
        foreach $midplane ( sort( keys(%midplanes) ) ) {
            for ( $nodecard = 0 ; $nodecard < $nodecardspermidplane ; $nodecard++ ) {
                if ( $bgp eq "true" ) {
                    $nodeid = sprintf( "R%01d%01d-M%01d-N%02d", $row, $rack, $mid, $nodecard );
                }
                if ( $bgq eq "true" ) {
                    $nodeid = sprintf( "R%02d%02d-M%01d-N%02d", $row, $rack, $mid, $nodecard );
                }
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

sub get_bgnode_list {
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
        # sub-block partition
        my ( $n, $n1, $n2, $cnr1, $cnr2 );
        $n1 = 0;
        $n2 = 0;
        if ( $bgq eq "true" ) {
            ( $cnr1, $cnr2 ) = split( /x/, $suffix );
            #$n1 = $startnc{$cnr1};
            #$n2 = $endnc{$cnr2};
            $n1 = &floor_nc( $cnr1 );
            $n2 = &ceil_nc( $cnr2 );
        }
        elsif ( $bgp eq "true" ) {
            ( $n1, $n2 ) = split( /-/, $suffix );
        }
        $n2 = $n1 if ( $n2 == 0 );
        if ( ( $bgp eq "true" ) && ( $nodecardspermidplane > $numpsets ) ) {
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
        my ( $cnr1, $cnr2, @dim1, @dim2, $i, $j, $k, $l, $midplane );
        ( $cnr1, $cnr2 ) = split( /x/, $suffix );
        for ( $i = 0 ; $i < length($cnr1) ; $i++ ) {
            $dim1[$i] = substr( $cnr1, $i, 1 );
        }
        for ( $i = 0 ; $i < length($cnr2) ; $i++ ) {
            $dim2[$i] = substr( $cnr2, $i, 1 );
        }
        for ( $i = $dim1[0] ; $i <= $dim2[0] ; $i++ ) {
            for ( $j = $dim1[1] ; $j <= $dim2[1] ; $j++ ) {
                for ( $k = $dim1[2] ; $k <= $dim2[2] ; $k++ ) {
                    if ( $bgq eq "true" ) {
                        for ( $l = $dim1[3] ; $l <= $dim2[3] ; $l++ ) {
                            $midplane = $prefix . $i . $j . $k . $l;
                            for ( $n = 0 ; $n < $nodecardspermidplane ; $n++ ) {
                                $ret .= "," . $nodenr{$midplane}[$n];
                            }
                        }
                    }
                    elsif ( $bgp eq "true" ) {
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
    
    return ( $startnc{$newnc} );
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
    
    return ( $endnc{$newnc} );
}

#********************************
# Removes pairs in the passed pairs array, which do not contain real pairs.
# A pair should contain a key, the equals operator and the value.
# Invalid pairs are attached to previous pairs.
#
# @param $pairsRef reference to the array of pair-strings, each string looks like <key>=<value>,
#	the referenced array is adjusted by this function 
#********************************
sub removeInvalidPairs {
	
	my $pairsRef = shift(@_);
	# Search for invalid pairs, and attach them to valid values
	my $lastReal = -1;
    for (my $i=0; $i<=$#{$pairsRef}; $i++){
      	if( $pairsRef->[$i] =~ /\w+=.+/ ){
       		$lastReal = $i;
       	}
       	else{
       		if($lastReal != -1 ){
       			$pairsRef->[$lastReal].=" ".$pairsRef->[$i];
       			$pairsRef->[$i] = "";
       		}
       	}
    }
    # Delete the invalid pairs from the array
    for (my $i=0;$i<=$#{$pairsRef};$i++){
      	if($pairsRef->[$i] eq "" ){
       		splice(@{$pairsRef}, $i, 1);
       		$i--;
       	}
    }
	
}
