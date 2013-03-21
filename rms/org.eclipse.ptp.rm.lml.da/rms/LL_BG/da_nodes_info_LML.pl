#!/usr/bin/perl -w
#*******************************************************************************
#* Copyright (c) 2012 Forschungszentrum Juelich GmbH.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*
#* Contributors:
#*    Wolfgang Frings (Forschungszentrum Juelich GmbH) 
#*******************************************************************************/ 
use strict;

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
my ($line,%nodes,%nodenr,$key,$value,$count,%notmappedkeys,%notfoundkeys);

#unless( ($Hostname =~ /jugenes\d/) && ($UserID =~ /llstat/) ) {
#  die "da_nodes_info_LML.pl can only be used as llstat on jugenesX!";
#}

#####################################################################
# get command line parameter
#####################################################################
if ($#ARGV != 0) {
  die " Usage: $0 <filename> $#ARGV\n";
}
my $filename = $ARGV[0];

my $system_sysprio=-1;
my $maxtopdogs=-1;

my %mapping = (
    "C-node_Memory"                          => "physmem",
    "ComputeBlock"                           => "partition",
    "In_Use"                                 => "inuse",
    "Location"                               => "location",
    "Midplane_State"                         => "state",
    "Number_of_IOLinks"                      => "",
    "Cpus"                                   => "ncores",
    "State"                                  => "",
    "id"                                     => "id",
    "Node_Board_List"                        => "",
# unknown attributes
    );


my $cmd="/usr/bin/llbgstatus";
$cmd=$ENV{"CMD_NODEINFO"} if($ENV{"CMD_NODEINFO"}); 

open(IN," $cmd -M all |");
my $nodeid="-";
my $lastkey="-";
my $hwnodeid="";

while($line=<IN>) {
    chomp($line);
    next if ($line=~/^\-+$/);
    next if ($line=~/^\=+$/);

    if($line=~/^\s*Midplane Location\s*[:]\s*$patwrd/) {
	$hwnodeid=$1;
	if($hwnodeid=~/^R(.)(.)-(.*)$/) {
	    $nodeid=sprintf("R%02d%02d-%s",&Rack_ord($1),&Rack_ord($2),$3);
	}
	$nodes{$nodeid}{id}=$nodeid;
#	print "line $line\n";
    } elsif($line=~/^\s*([^\:]+)\s*\:\s+(.*)$/) {
	($key,$value)=($1,$2);
	$key=~s/\s/_/gs;
	$lastkey=$key;
	$nodes{$nodeid}{$key}=$value;
    } elsif($line=~/^\s*([^\:]+)\s*\:\s*$/) {
	($key)=($1);
	$key=~s/\s/_/gs;
	$lastkey=$key;
	$nodes{$nodeid}{$key}="";
     } else {
	$line=~s/^\s*//gs;
	$nodes{$nodeid}{$lastkey}.=$line;
    }
}
close(IN);

# add unknown but manatory attributes to nodes
foreach $nodeid (keys(%nodes)) {
    my($key,$value,$pair);
    if(exists($nodes{$nodeid}{status})) {
	foreach $pair (split(/,/,$nodes{$nodeid}{status})) {
	    ($key,$value)=split(/=/,$pair);
	    $nodes{$nodeid}{$key}=$value;
	}
    } 

}

open(OUT,"> $filename") || die "cannot open file $filename";
printf(OUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
printf(OUT "<lml:lgui xmlns:lml=\"http://eclipse.org/ptp/lml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
printf(OUT "	xsi:schemaLocation=\"http://eclipse.org/ptp/lml http://eclipse.org/ptp/schemas/lgui.xsd\"\n");
printf(OUT "	version=\"1.0\"\>\n");
printf(OUT "<objects>\n");
$count=0;
foreach $nodeid (sort(keys(%nodes))) {
    $count++;$nodenr{$nodeid}=$count;
    printf(OUT "<object id=\"nd%06d\" name=\"%s\" type=\"node\"/>\n",$count,$nodeid);
}
printf(OUT "</objects>\n");
printf(OUT "<information>\n");
foreach $nodeid (sort(keys(%nodes))) {
    printf(OUT "<info oid=\"nd%06d\" type=\"short\">\n",$nodenr{$nodeid});
    foreach $key (sort(keys(%{$nodes{$nodeid}}))) {
	if(exists($mapping{$key})) {
	    if($mapping{$key} ne "") {
		$value=&modify($key,$mapping{$key},$nodes{$nodeid}{$key});
		if($value) {
		    printf(OUT " <data %-20s value=\"%s\"/>\n","key=\"".$mapping{$key}."\"",$value);
		}
	    } else {
		$notmappedkeys{$key}++;
	    }
	} else {
	    $notfoundkeys{$key}++;
	}
    }
    printf(OUT "</info>\n");
}
printf(OUT "</information>\n");
 
printf(OUT "</lml:lgui>\n");

close(OUT);

foreach $key (sort(keys(%notfoundkeys))) {
    printf("%-40s => \"\",\n","\"".$key."\"",$notfoundkeys{$key});
}



sub modify {
    my($key,$mkey,$value)=@_;
    my $ret=$value;

    if($mkey eq "owner") {
	$ret=~s/\@.*//gs;
    }

    if($mkey eq "state") {
	$ret="Running"  if ($value eq "Running");
	$ret="Down"     if ($value eq "Down");
	$ret="Idle"     if ($value eq "Idle");
	$ret="unknown"  if ($value eq "unknown");
    }

    if(($mkey eq "wall") || ($mkey eq "wallsoft")) {
	if($value=~/\($patint seconds\)/) {
	    $ret=$1;
	}
	if($value=~/$patint minutes/) {
	    $ret=$1*60;
	}
	if($value=~/^$patint[:]$patint[:]$patint$/) {
	    $ret=$1*60*60+$2*60+$3;
	}
    }

    if(($mkey eq "comment")) {
	$ret=~s/\"//gs;
    }
    if(($mkey eq "bgp_state")) {
	$ret=~s/\<unknown\>/unknown/gs;
    }

    return($ret);
}

sub Rack_ord {
    my($spec)=@_;
    my$ret=0;
    if($spec=~/\d+/) {
	$ret=$spec;
    } elsif($spec=~/[a-z]/) {
	$ret=ord($spec)-ord('a')+10;
    }elsif($spec=~/[A-Z]/) {
	$ret=ord($spec)-ord('A')+10;
    }
    return($ret);
}
