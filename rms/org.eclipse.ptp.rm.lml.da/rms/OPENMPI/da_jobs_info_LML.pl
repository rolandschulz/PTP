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

my $cmd="orte-ps";
$cmd=$ENV{"CMD_JOBINFO"} if($ENV{"CMD_JOBINFO"}); 

if(open(IN,"$cmd -n |")) {
    my $firstrankpid=undef;
    $jobid="-";
    while($line=<IN>) {
	chomp($line);
	if($line=~/Information from $patwrd \[$patint,$patint\]$/) {
	    my($call,$pid,$num)=($1,$2,$3);
	    print "found job $pid\n";
	    $jobid=$pid;
	    $jobs{$jobid}{step}=$jobid;
	} elsif($line=~/^\s*Node Name \|/) {
	    # scan node table
	    print "line 1: $line\n";
	    $line=<IN>;
	    while($line=<IN>) {
		if($line=~/^\s*$patwrd\s*\|\s*$patwrd\s*\|\s*$patwrd\s*\|\s*$patwrd\s*\|\s*$patwrd\s*\|\s*$patwrd\s*\|/) {
		    my($nodeid,$arch,$state,$slots,$slotsmax,$slotsinuse)=($1,$2,$3,$4,$5,$6);
		    $nodes{$nodeid}{id}=$nodeid;
		    $nodes{$nodeid}{arch}=$arch;
		    $nodes{$nodeid}{state}=$state;
		    $nodes{$nodeid}{slots}+=$slots;
		    $nodes{$nodeid}{slotsmax}+=$slotsmax;
		    $nodes{$nodeid}{slotsinuse}+=$slotsinuse;
		    $nodes{$nodeid}{_taskcounter}=0 if(!exists($nodes{$nodeid}{_taskcounter}));
		    print "found node $nodeid $arch $state $slots,$slotsmax,$slotsinuse\n";
		}
		if($line=~/^\s*$/) {
		    last;
		}
	    }
	} elsif($line=~/^\s+JobID \|/) {
	    # scan job table
	    print "line 2: $line\n";
	    $line=<IN>;
	    $line=<IN>;
	    # job line
	    print "line 3: $line\n";
	    if($line=~/^\s*\[$patint,$patint\]\s*\|\s*$patwrd\s*\|\s*$patwrd\s*\|\s*$patwrd\s*\|/) {
		my($ppid,$num,$state,$slots,$numproc)=($1,$2,$3,$4,$5);
		print "found joblist $ppid,$num,$state,$slots,$numproc\n";
		$jobs{$jobid}{id}              = $ppid;
		$jobs{$jobid}{totaltasks}      = $numproc;
		$jobs{$jobid}{job_state}       = $state;
		$line=<IN>;
		if($line=~/^\s+Process Name \|/) {
		    $line=<IN>;
		    while($line=<IN>) {
			if($line=~/^\s*([^\|\s]+)\s*\|\s*\[\[$patint,$patint\],$patint\]\s*\|\s*$patwrd\s*\|\s*$patwrd\s*\|\s*$patwrd\s*\|\s*$patwrd\s*\|/) {
			    my($pname,$ortename1,$ortename2,$ortename3,$rank,$pid,$nodeid,$pstate)=($1,$2,$3,$4,$5,$6,$7,$8);
			    print "found process $pname,$ortename1,$ortename2,$ortename3,$rank,$pid,$nodeid,$pstate\n";
			    $jobs{$jobid}{nodelist}.="($nodeid,".$nodes{$nodeid}{_taskcounter}.")";
			    $nodes{$nodeid}{_taskcounter}++;
			    $firstrankpid=$pid if($rank==0);
#			print "jobs{$jobid}{nodelist}=$jobs{$jobid}{nodelist}\n";
			}
			if($line=~/^\s*$/) {
			    last;
			}
		    }
		}
		if($firstrankpid) {
		    my($pline);
		    print "WF: ps -p $firstrankpid -o pid,user,group,vsz,etime\n";
		    open(PSIN,"ps -p $firstrankpid -o pid,user,group,vsz,etime |");
		    $pline=<PSIN>;
		    chomp($pline);
		    print "PS1: $pline\n";
		    $pline=<PSIN>;
		    if(defined($pline)) {
			chomp($pline);
			print "PS2: $pline\n";
			if($pline=~/^\s*$patint\s*$patwrd\s*$patwrd\s*$patint\s*$patwrd/) {
			my($pppid,$user,$group,$vsz,$etime)=($1,$2,$3,$4,$5);
			print "found process info $pppid,$user,$group,$vsz,$etime\n";
			$jobs{$jobid}{user}=$user;
			$jobs{$jobid}{group}=$group;
			$jobs{$jobid}{etime}=$etime;
			}
		    }
		    close(PSIN);
		}
	    }
	}
    }
    close(IN);
} 
if(!keys(%nodes)) {
    # set default
    $nodeid=`uname -n`;chomp($nodeid);
    $nodes{$nodeid}{id}       = $nodeid;
    $nodes{$nodeid}{arch}     = `uname -p`;
    $nodes{$nodeid}{state}    = "Up";
    $nodes{$nodeid}{slots}    = `grep family /proc/cpuinfo | wc -l`;
    foreach $key (keys(%{$nodes{$nodeid}})) {
	chomp($nodes{$nodeid}{$key});
    }
}


# add unknown but manatory attributes to jobs
foreach $jobid (sort(keys(%jobs))) {
    $jobs{$jobid}{totalcores} = $jobs{$jobid}{totaltasks};
    $jobs{$jobid}{spec}       = $jobs{$jobid}{"totaltasks"};
    $jobs{$jobid}{queue}      = "local";
    # check state
    ($jobs{$jobid}{status},$jobs{$jobid}{detailedstatus}) = &get_state($jobs{$jobid}{job_state}); 
}

open(OUT,"> $filename") || die "cannot open file $filename";
printf(OUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
printf(OUT "<lml:lgui xmlns:lml=\"http://eclipse.org/ptp/lml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
printf(OUT "	xsi:schemaLocation=\"http://eclipse.org/ptp/lml http://eclipse.org/ptp/schemas/v1.1/lgui.xsd\"\n");
printf(OUT "	version=\"1.1\"\>\n");
printf(OUT "<objects>\n");
# job objs
$count=0;
foreach $jobid (sort(keys(%jobs))) {
    $count++;$jobnr{$jobid}=$count;
    printf(OUT "<object id=\"j%06d\" name=\"%s\" type=\"job\"/>\n",$count,$jobid);
}
# node objs
$count=0;
foreach $nodeid (sort(keys(%nodes))) {
    $count++;$nodenr{$nodeid}=$count;
    printf(OUT "<object id=\"nd%06d\" name=\"%s\" type=\"node\"/>\n",$count,$nodeid);
}
printf(OUT "</objects>\n");
printf(OUT "<information>\n");
# job info
foreach $jobid (sort(keys(%jobs))) {
    printf(OUT "<info oid=\"j%06d\" type=\"short\">\n",$jobnr{$jobid});
    foreach $key (sort(keys(%{$jobs{$jobid}}))) {
	if(exists($mapping_job{$key})) {
	    if($mapping_job{$key} ne "") {
		$value=&modify($key,$mapping_job{$key},$jobs{$jobid}{$key});
		if($value) {
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


sub get_state {
    my($job_state,$Hold_types)=@_;
    my($state,$detailed_state);

    $state="UNDETERMINED";$detailed_state="";

    if($job_state eq "C") {
	$state="COMPLETED";$detailed_state="JOB_OUTERR_READY";
    }
    if($job_state eq "H") {
	$state="SUBMITTED";
	$detailed_state="USER_ON_HOLD"   if($Hold_types eq "u");
	$detailed_state="SYSTEM_ON_HOLD" if($Hold_types eq "s");
	$detailed_state="USER_SYSTEM_ON_HOLD" if($Hold_types=~"(us|su)");
	$detailed_state="SYSTEM_ON_HOLD" if($Hold_types eq "o");
    }
    if($job_state eq "E") {
	$state="COMPLETED";$detailed_state="JOB_OUTERR_READY";
    }    
    if($job_state eq "Q") {
	$state="SUBMITTED";$detailed_state="";
    }    
    if($job_state eq "W") {
	$state="SUBMITTED";$detailed_state="";
    }    
    if($job_state eq "T") {
	$state="SUBMITTED";$detailed_state="";
    }    
    if($job_state eq "Running") {
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
