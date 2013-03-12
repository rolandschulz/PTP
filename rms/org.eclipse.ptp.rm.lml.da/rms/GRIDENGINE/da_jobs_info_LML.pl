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
use GEHelper;               # Now look for our GEHelper module

my $patint="([\\+\\-\\d]+)";   # Pattern for Integer number
my ($line,%jobs,%jobnr,$key,$value,$count,%notmappedkeys,%notfoundkeys);

if ($#ARGV != 0) {
  die " Usage: $0 <filename> $#ARGV\n";
}
my $filename = $ARGV[0];

my $system_sysprio=-1;
my $maxtopdogs=-1;

my %mapping = map { $_ => $_ } (
    "name",
    "owner",
    "restart",
    "dependency",
    "totalcores",
    "wall",
    "nodelist",
    "vnodelist",
    "state",
    "queuedate",
    "queue",
    "dispatchdate",
    "step",
    "totaltasks",
    "spec",
    "status",
    "detailedstatus",
    "group"
    );

%jobs = get_jobs();

my %nodes = get_nodes();
my ($node_groups, $node_index, $nodes_per_group) = group_nodes();
my %job_nodes = get_job_nodes();
for my $jobid (keys %job_nodes) {
    my $nodelist = $job_nodes{$jobid};
    if (should_group_nodes()) {
        # Determine what groups this 
        my %groups = ();
        for my $nodeid (split(/,/, $nodelist)) {
            my $groupid = $node_groups->{$nodeid};
            my $groupidx = $node_index->{$nodeid};
            $groups{"($groupid,$groupidx)"} = 1;
        }
        $nodelist = join("", sort(keys(%groups)));
    } else {
        my @nodes_in_list = split(/,/, $nodelist);
        $nodelist = '';
        for my $nd (@nodes_in_list) {
            if (defined($nodes{$nd}{ncores}) && $nodes{$nd}{ncores} =~ /[0-9]+/) {
                my $ncores = $nodes{$nd}{ncores};
                for (my $i = 0; $i < $ncores; $i++) {
                    $nodelist .= "($nd,$i)";
                }
            } else {
                $nodelist .= "($nd,0)";
            }
        }
    }

    $jobs{$jobid}{nodelist} = $nodelist;
}

# add unknown but manatory attributes to jobs
for my $jobid (sort(keys(%jobs))) {
    $jobs{$jobid}{group}      = "unknown" if(!exists($jobs{$jobid}{group}));
    $jobs{$jobid}{nodelist}  = "-" if(!exists($jobs{$jobid}{nodelist}));
    $jobs{$jobid}{totaltasks} = $jobs{$jobid}{totalcores} if(!exists($jobs{$jobid}{totaltasks}));
    $jobs{$jobid}{spec}       = $jobs{$jobid}{totalcores} if(!exists($jobs{$jobid}{spec}));
    # check state
    ($jobs{$jobid}{status},$jobs{$jobid}{detailedstatus}) = &get_state($jobs{$jobid}{state}); 
}

open(OUT,"> $filename") || die "cannot open file $filename";
printf(OUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
printf(OUT "<lml:lgui xmlns:lml=\"http://eclipse.org/ptp/schemas\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
printf(OUT "        xsi:schemaLocation=\"http://eclipse.org/ptp/schemas http://eclipse.org/ptp/schemas/lgui.xsd\"\n");
printf(OUT "        version=\"0.7\"\>\n");
printf(OUT "<objects>\n");
$count=0;
for my $jobid (sort(keys(%jobs))) {
    $count++;$jobnr{$jobid}=$count;
    printf(OUT "<object id=\"j%06d\" name=\"%s\" type=\"job\"/>\n",$count,$jobid);
}
printf(OUT "</objects>\n");
printf(OUT "<information>\n");
for my $jobid (sort(keys(%jobs))) {
    printf(OUT "<info oid=\"j%06d\" type=\"short\">\n",$jobnr{$jobid});
    foreach $key (sort(keys(%{$jobs{$jobid}}))) {
        if(exists($mapping{$key})) {
            if($mapping{$key} ne "") {
                $value=&modify($key,$mapping{$key},$jobs{$jobid}{$key});
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


sub get_state {
    my($job_state)=@_;
    my($state,$detailed_state);

    $state="UNDETERMINED";$detailed_state="";

    if($job_state eq "zombie") {
        $state="COMPLETED";$detailed_state="JOB_OUTERR_READY";
    }
    if($job_state eq "pending") {
        $state="SUBMITTED";
        $detailed_state="";
    }
    if($job_state eq "running") {
        $state="RUNNING";$detailed_state="";
    }    

    return($state,$detailed_state);
}

sub modify {
    my($key,$mkey,$value)=@_;
    my $ret=$value;

    if(!$ret) {
        return(undef);
    }

    if($mkey eq "state") {
        $ret="Completed"   if ($value eq "zombie");
        $ret="Idle"        if ($value eq "pending");
        $ret="Running"     if ($value eq "running");
    }

    if(($mkey eq "wall") || ($mkey eq "wallsoft")) {
        if($value=~/^$patint[:]$patint[:]$patint$/) {
            $ret=$1*60*60+$2*60+$3;
        }
    }

    # mask & in user input
    if($ret=~/\&/) {
        $ret=~s/\&/\&amp\;/gs;
    } 

    return($ret);
}
