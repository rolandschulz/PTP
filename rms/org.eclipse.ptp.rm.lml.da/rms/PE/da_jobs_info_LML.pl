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

my $patint="([\\+\\-\\d]+)";   # Pattern for Integer number
my $patfp ="([\\+\\-\\d.E]+)"; # Pattern for Floating Point number
my $patwrd="([\^\\s]+)";       # Pattern for Work (all noblank characters)
my $patbl ="\\s+";             # Pattern for blank space (variable length)
my $patnode="([\^\\s]+(\\.[\^\\s]*)*)";       # Pattern for domain name (a.b.c)

#####################################################################
# get user info / check system 
#####################################################################
my $UserID = getpwuid($<);
my $Hostname = `hostname`;
my $verbose=1;
my ($line,%jobs,%jobnr,$jobid,$key,$value,$count,%notmappedkeys,%notfoundkeys);
my (%nodes,$nodeid,%nodenr);

#unless( ($Hostname =~ /jugenes\d/) && ($UserID =~ /llstat/) ) {
#  die "da_jobs_info_LML.pl can only be used as llstat on jugenesX!";
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

my %mapping_node = (
    "id"                                     => "id",
    "slots"                                  => "",
    "slotsinuse"                             => "ncores",
    "slotsmax"                               => "",
    "state"                                  => "state",
    "arch"                                   => "arch",
    "_taskcounter"                           => "",

    );

my %mapping_job = (
    "step"                                   => "step",
    "nodelist"                               => "nodelist",
    "totaltasks"                             => "totaltasks",
    "totalcores"                             => "totalcores",
    "job_state"                              => "state",

    "status"                                 => "status",
    "detailedstatus"                         => "detailedstatus",
    "user"                                   => "owner",
    "group"                                  => "group",
    "queue"                                  => "queue",
    "etime"                                  => "wall",
    "id"                              => "",
    "spec"                            => "",

# unknown attributes
    );

my $cmd="ps | grep poe | grep -v grep";
$cmd=$ENV{"CMD_JOBINFO"} if($ENV{"CMD_JOBINFO"}); 

if (open(IN,"$cmd 2>&1 |")) {
    while($line=<IN>) {
		chomp($line);
		if ($line=~/$patbl$patint$patbl/) {
			my($jobid)=($2);
		    $jobs{$jobid}{step}=$jobid;
			$jobs{$jobid}{job_state}="RUNNING";
			&check_attach_cfg($jobid);
	    }
    }
    close(IN);
} 

open(OUT,"> $filename") || die "cannot open file $filename";
printf(OUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
printf(OUT "<lml:lgui xmlns:lml=\"http://www.llview.de\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
printf(OUT "	xsi:schemaLocation=\"http://www.llview.de lgui.xsd\"\n");
printf(OUT "	version=\"0.7\"\>\n");
printf(OUT "<objects>\n");

# job objs
$count=0;
foreach $jobid (sort(keys(%jobs))) {
    $count++;
    $jobnr{$jobid}=$count;
    printf(OUT "<object id=\"j%06d\" name=\"%s\" type=\"job\"/>\n",$count,$jobid);
}

# node objs
$count=0;
foreach $nodeid (sort(keys(%nodes))) {
    $count++;
    $nodenr{$nodeid}=$count;
    printf(OUT "<object id=\"nd%06d\" name=\"%s\" type=\"node\"/>\n",$count,$nodeid);
}

printf(OUT "</objects>\n");
printf(OUT "<information>\n");

# job info
foreach $jobid (sort(keys(%jobs))) {
    printf(OUT "<info oid=\"j%06d\" type=\"short\">\n",$jobnr{$jobid});
    foreach $key (sort(keys(%{$jobs{$jobid}}))) {
	if (exists($mapping_job{$key})) {
	    if ($mapping_job{$key} ne "") {
			$value=&modify($key,$mapping_job{$key},$jobs{$jobid}{$key});
			if ($value) {
			    printf(OUT " <data %-20s value=\"%s\"/>\n","key=\"".$mapping_job{$key}."\"",$value);
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

# node info
foreach $nodeid (sort(keys(%nodes))) {
    printf(OUT "<info oid=\"nd%06d\" type=\"short\">\n",$nodenr{$nodeid});
    foreach $key (sort(keys(%{$nodes{$nodeid}}))) {
		if(exists($mapping_node{$key})) {
		    if($mapping_node{$key} ne "") {
				$value=&modify($key,$mapping_node{$key},$nodes{$nodeid}{$key});
				if($value) {
				    printf(OUT " <data %-20s value=\"%s\"/>\n","key=\"".$mapping_node{$key}."\"",$value);
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

sub check_attach_cfg {
    my($jobid)=@_;
	if (open(CFG, "/tmp/.ppe.$jobid.attach.cfg")) {
		# Ignore first line
		$line=<CFG>;
		# Second line has number of tasks
		chomp($line=<CFG>);
		if ($line=~/$patint$patbl;/) {
			$jobs{$jobid}{totaltasks}=$1;
		}
		while($line=<CFG>) {
			my($taskid,$nodeid);
			chomp($line);
			if ($line=~/$patint$patbl$patnode/) {
				($taskid,$nodeid)=($1,$2);
		    } elsif ($line=~/$patint$patbl$patint$patbl$patnode/) {
				($taskid,$nodeid)=($1,$3);
		    }
		    $nodes{$nodeid}{id}=$nodeid;
		    $nodes{$nodeid}{state}="Up";
	    }
	    close(CFG);
	}
}

sub modify {
    my($key,$mkey,$value)=@_;
    my $ret=$value;

    if(!$ret) {
		return(undef);
    }

    if($mkey eq "owner") {
		$ret=~s/\@.*//gs;
    }

    if($mkey eq "state") {
		$ret="Completed"   if ($value eq "C");
		$ret="Removed"     if ($value eq "E");
		$ret="System Hold" if ($value eq "H");
		$ret="Idle"        if ($value eq "Q");
		$ret="Idle"        if ($value eq "W");
		$ret="Idle"        if ($value eq "T");
		$ret="Running"     if ($value eq "R");
		$ret="System Hold" if ($value eq "S");
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

    if($mkey eq "totalcores") {
		my $numcores=0;
		my ($spec);
		foreach $spec (split(/\s*\+\s*/,$ret)) {
		    # std job
		    if($ret=~/^$patint[:]ppn=$patint/) {
				$numcores+=$1*$2;
		    } elsif($ret=~/^$patwrd[:]ppn=$patint/) {
				$numcores+=1*$2;
		    }
		}
		$ret=$numcores if($numcores>0);
    }
    if($mkey eq "totaltasks") {
		my $numcores=0;
		my ($spec);
		foreach $spec (split(/\s*\+\s*/,$ret)) {
		    # std job
		    if($ret=~/^$patint[:]ppn=$patint/) {
				$numcores+=$1*$2;
		    } elsif($ret=~/^$patwrd[:]ppn=$patint/) {
				$numcores+=1*$2;
		    }
		}
		$ret=$numcores if($numcores>0);
    }

    if(($mkey eq "comment")) {
		$ret=~s/\"//gs;
    }

    # mask & in user input
    if($ret=~/\&/) {
		$ret=~s/\&/\&amp\;/gs;
    } 

    return($ret);
}
