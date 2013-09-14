#!/usr/bin/perl -w
#*******************************************************************************
#* Copyright (c) 2011 Forschungszentrum Juelich GmbH and others.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*
#* Contributors:
#*    Wolfgang Frings (Forschungszentrum Juelich GmbH)
#*    Jeff Overbey (Illinois/NCSA) - Grid Engine support
#*******************************************************************************/ 
use strict;

use FindBin;                # Find the directory containing this script
use lib "$FindBin::Bin";    # Search for modules in this script's directory
use lib "$FindBin::RealBin/../../lib";
use LML_da_util;
use GEHelper;               # Now look for our GEHelper module

my $patint="([\\+\\-\\d]+)";   # Pattern for Integer number
my ($line,%nodes,%nodenr,$key,$value,$count,%notmappedkeys,%notfoundkeys);

if ($#ARGV != 0) {
  die " Usage: $0 <filename> $#ARGV\n";
}
my $filename = $ARGV[0];

my $system_sysprio=-1;
my $maxtopdogs=-1;

my %mapping = map { $_ => $_ } (
    "availmem",
    "id",
    "ncores",
    "physmem",
    "ntype",
    "state"
    );


%nodes = get_nodes();

if (should_group_nodes()) {
    my ($node_groups, $node_index, $nodes_per_group) = group_nodes();
    my %groups = ();
    for my $nodeid (keys %nodes) {
        my $groupid = $node_groups->{$nodeid};
        if (!defined($groups{$groupid})) {
            $groups{$groupid}{id}     = $groupid; #$nodes{$nodeid}{id};
            $groups{$groupid}{ntype}  = $nodes{$nodeid}{ntype};
            $groups{$groupid}{ncores} = 1;
        } else {
            # $groups{$groupid}{id} .= "," . $nodes{$nodeid}{id};
            if ($nodes{$nodeid}{ntype} ne '-' && $nodes{$nodeid}{ntype} ne $groups{$groupid}{ntype}) {
                $groups{$groupid}{ntype} = "(mixed)";
            }
            $groups{$groupid}{ncores} = $nodes_per_group;
        }
    }
    %nodes = %groups;
}

# add unknown but manatory attributes to nodes
for my $nodeid (keys(%nodes)) {
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
printf(OUT "        xsi:schemaLocation=\"http://eclipse.org/ptp/lml http://eclipse.org/ptp/schemas/v1.1/lgui.xsd\"\n");
printf(OUT "        version=\"1.1\"\>\n");
printf(OUT "<objects>\n");
$count=0;
for my $nodeid (sort(keys(%nodes))) {
    $count++;$nodenr{$nodeid}=$count;
    printf(OUT "<object id=\"nd%06d\" name=\"%s\" type=\"node\"/>\n",$count,&LML_da_util::escapeForXML($nodeid));
}
printf(OUT "</objects>\n");
printf(OUT "<information>\n");
for my $nodeid (sort(keys(%nodes))) {
    printf(OUT "<info oid=\"nd%06d\" type=\"short\">\n",$nodenr{$nodeid});
    foreach $key (sort(keys(%{$nodes{$nodeid}}))) {
        if(exists($mapping{$key})) {
            if($mapping{$key} ne "") {
                $value=&modify($key,$mapping{$key},$nodes{$nodeid}{$key});
                if($value) {
                    printf(OUT " <data %-20s value=\"%s\"/>\n","key=\"".$mapping{$key}."\"",&LML_da_util::escapeForXML($value));
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

    if(!$ret) {
        return(undef);
    }

    # mask & in user input
    if($ret=~/\&/) {
        $ret=~s/\&/\&amp\;/gs;
    } 

    return($ret);
}
