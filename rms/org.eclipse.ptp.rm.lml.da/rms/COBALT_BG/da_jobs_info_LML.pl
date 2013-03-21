#!/usr/bin/perl -w
#*******************************************************************************
#* Copyright (c) 2012 ParaTools, Inc.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*
#* Contributors:
#*    Kevin A. Huck (ParaTools, Inc.)
#*******************************************************************************/ 
use strict;
use Time::Local;
use File::Basename;
use Storable;

my $patint="([\\+\\-\\d]+)";   # Pattern for Integer number
my $patfp ="([\\+\\-\\d.E]+)"; # Pattern for Floating Point number
my $patwrd="([\^\\s]+)";       # Pattern for Work (all noblank characters)
my $patbl ="\\s+";             # Pattern for blank space (variable length)
my $cores_per_node = 4;        # for BG/P, override for BG/Q to 16

#####################################################################
# get user info / check system 
#####################################################################
my $UserID = getpwuid($<);
my $Hostname = `hostname -d`;
my $verbose=1;
my ($line,%jobs,%jobnr,$key,$value,$count,%notmappedkeys,%notfoundkeys);

#####################################################################
# get command line parameter
#####################################################################
if ($#ARGV != 0) {
  die " Usage: $0 <filename> $#ARGV\n";
}
my $filename = $ARGV[0];
my $hashfile = sprintf("%s/%s", dirname($ARGV[0]), 'hash.file');

my $system_sysprio=-1;
my $maxtopdogs=-1;

my %months = (
    "Jan" => "01",
    "Feb" => "02",
    "Mar" => "03",
    "Apr" => "04",
    "May" => "05",
    "Jun" => "06",
    "Jul" => "07",
    "Aug" => "08",
    "Sep" => "09",
    "Oct" => "10",
    "Nov" => "11",
    "Dec" => "12"
);

my %mapping = (
    "JobName"                                => "name",
    "User"                                   => "owner",
    "Rerunable"                              => "restart",
    "Dependencies"                           => "dependency",
    "Nodes"                                  => "totalcores",
    "WallTime"                               => "wall",
    "Location"                               => "nodelist",
    "S"                                      => "job_state",
    "job_state"                              => "state",
    "QueuedTime"                             => "queuedate",
    "Queue"                                  => "queue",
    "StartTime"                              => "dispatchdate",
    "step"                                   => "step",
    "spec"                                   => "spec",
    "status"                                 => "status",
    "detailedstatus"                         => "detailedstatus",
    "Procs"                                  => "totaltasks",

    "Checkpoint"                             => "",
    "Error_Path"                             => "",
    "Hold_Types"                             => "",
    "Join_Path"                              => "",
    "Keep_Files"                             => "",
    "Mail_Points"                            => "",
    "Mail_Users"                             => "",
    "Output_Path"                            => "",
    "Priority"                               => "",
    "Resource_List.cput"                     => "",
    "Resource_List.mem"                      => "",
    "Resource_List.nodect"                   => "",
    "Resource_List.pmem"                     => "",
    "Resource_List.vnodelist"                => "vnodelist",
    "Shell_Path_List"                        => "",
    "Walltime.Remaining"                     => "",
    "comment"                                => "",
    "ctime"                                  => "",
    "depend"                                 => "",
    "etime"                                  => "",
    "exit_status"                            => "",
    "fault_tolerant"                         => "",
    "interactive"                            => "",
    "mtime"                                  => "",
    "resources_used.cput"                    => "",
    "resources_used.mem"                     => "",
    "resources_used.vmem"                    => "",
    "resources_used.walltime"                => "",
    "server"                                 => "",
    "session_id"                             => "",
    "start_count"                            => "",
    "submit_args"                            => "",
    "Resource_List.backfill"                 => "",
    "Resource_List.bandwidth"                => "",
    "Resource_List.enabled"                  => "",
    "Resource_List.job_type"                 => "",
    "Resource_List.ncpus"                    => "",
    "Resource_List.nightrun"                 => "",
    "Resource_List.node_type"                => "",
    "Resource_List.operational"              => "",
    "Resource_List.place"                    => "",
    "Resource_List.select"                   => "",
    "Submit_arguments"                       => "",
    "Variable_List"                          => "",
    "alt_id"                                 => "",
    "jobdir"                                 => "",
    "pset"                                   => "",
    "resources_used.cpupercent"              => "",
    "resources_used.ncpus"                   => "",
    "stime"                                  => "",
    "substate"                               => "",
    
# unknown attributes
    "group"                                  => "group",
    );

my $cmd="/usr/bin/qstat";
$cmd=$ENV{"CMD_JOBINFO"} if($ENV{"CMD_JOBINFO"}); 

open(IN,"$cmd -f -l |");
my $jobid="-";
my $lastkey="-";


while($line=<IN>) {
    chomp($line);
    if($line=~/^JobID\:\s+(\S*)$/) {
	$jobid=$1;
	$jobs{$jobid}{step}=$jobid;
    } elsif($line=~/^\s+(\S*)\s+\:\s+(.*)$/) {
	($key,$value)=($1,$2);
	$key=~s/\s/_/gs;
	$lastkey=$key;
	$jobs{$jobid}{$key}=$value;
    } else {
	$line=~s/^\s*//gs;
	$jobs{$jobid}{$lastkey}.=$line;
    }
}
close(IN);

# add unknown but manatory attributes to jobs
foreach $jobid (sort(keys(%jobs))) {
    $jobs{$jobid}{group}      = "unknown" if(!exists($jobs{$jobid}{group}));
    $jobs{$jobid}{exec_host}  = "-" if(!exists($jobs{$jobid}{exec_host}));
    $jobs{$jobid}{totaltasks} = $jobs{$jobid}{"Procs"};
    $jobs{$jobid}{spec}       = $jobs{$jobid}{"Resource_List.nodes"} if(!exists($jobs{$jobid}{spec}));
    $jobs{$jobid}{group}      = "unknown" if(!exists($jobs{$jobid}{group}));
    $jobs{$jobid}{Hold_Types} = "";
    $jobs{$jobid}{Hold_Types} = "u" if($jobs{$jobid}{"User_Hold"} eq "True");
    $jobs{$jobid}{Hold_Types} = "s" if($jobs{$jobid}{"Admin_Hold"} eq "True");
    # check state
    $jobs{$jobid}{job_state} = $jobs{$jobid}{"S"};
    ($jobs{$jobid}{status},$jobs{$jobid}{detailedstatus}) = &get_state($jobs{$jobid}{"S"},
								       $jobs{$jobid}{Hold_Types}); 
    ($jobs{$jobid}{StartTime},$jobs{$jobid}{QueuedTime}) = &fixdates($jobs{$jobid}{StartTime}, $jobs{$jobid}{QueuedTime}); 
}

open(OUT,"> $filename") || die "cannot open file $filename";
printf(OUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
printf(OUT "<lml:lgui xmlns:lml=\"http://eclipse.org/ptp/lml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
printf(OUT "	xsi:schemaLocation=\"http://eclipse.org/ptp/lml http://eclipse.org/ptp/schemas/v1.1/lgui.xsd\"\n");
printf(OUT "	version=\"1.1\"\>\n");
printf(OUT "<objects>\n");
$count=0;
foreach $jobid (sort(keys(%jobs))) {
    $count++;$jobnr{$jobid}=$count;
    printf(OUT "<object id=\"j%06d\" name=\"%s\" type=\"job\"/>\n",$count,$jobid);
}
printf(OUT "</objects>\n");
printf(OUT "<information>\n");
foreach $jobid (sort(keys(%jobs))) {
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
    if($job_state eq "R") {
	$state="RUNNING";$detailed_state="";
    }    

    return($state,$detailed_state);
}

sub fixdates {
    my($dispatchdate,$queuedate)=@_;
    my $month = 0;
    my $day = 0;
    my $hour = 0;
    my $minute = 0;
    my $second = 0;
    my $year = 0;
    my $wday = 0;
    my $yday = 0;
    my $isdst = 0;
    my $modified_queuedate=$queuedate;
    my $modified_dispatchdate=$dispatchdate;
    my $been_dispatched = 0;

    # printf ( "Dispatch Date: %s\n", $dispatchdate);
    # printf ( "Queue Date: %s\n", $queuedate);

#   parse this: Wed Jan 11 01:26:31 2012 +0000 (UTC)
    if($dispatchdate=~/^(\S+) (\S+) (\S+) (\S+)\:(\S+)\:(\S+) (\S+) +(\S+) \(UTC\)$/) {
      $been_dispatched = 1;
      $month = $months{$2};
      $day = $3;
      $hour = $4;
      $minute = $5;
      $second = $6;
      $year = $7;
      my $tmpvar = sprintf("%04d-%02d-%02d %02d:%02d:%02d", $year, $month, $day, $hour, $minute, $second);
      $modified_dispatchdate=$tmpvar;
    }

    if($queuedate=~/^(\S+)\:(\S+)\:(\S+)$/) {
        my $seconds = (3600*int($1)) + (60*int($2)) + $3;
        my $now = 0;
        if ($been_dispatched == 1) {
          # "now" is the dispatched date/time
          $now = timelocal($second, $minute, $hour, $day, $month, $year);
        } else {
          # if the dispatch date is N/A, then compute the queue date based on now
          ($second, $minute, $hour, $day, $month, $year, $wday, $yday, $isdst) = localtime;
          $now = timelocal($second, $minute, $hour, $day, $month, $year);
        }
        # printf ( "%s is %d seconds\n", $queuedate, $seconds);
        my $then = $now - $seconds;
        # printf ( "then is %d, now is %d\n", $then, $now);
        ($second, $minute, $hour, $day, $month, $year, $wday, $yday, $isdst) = localtime($then);
        my $tmpvar = sprintf("%04d-%02d-%02d %02d:%02d:%02d", $year+1900, $month, $day, $hour, $minute, $second);
        # printf ( "then was %s\n", $tmpvar);
        $modified_queuedate=$tmpvar;
    }

    return($modified_dispatchdate, $modified_queuedate);
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

    if($mkey eq "nodelist") {
        if($ret=~/^[\w]+\-(\d)(\d)(\d)(\d)(\d)\-(\d)(\d)(\d)(\d)(\d)\-[\d]+$/) {
            $ret = map_torus_to_nodes($ret);
            $cores_per_node = 16;
        } else {
            $ret = map_block_to_nodes($ret);
        }
	if($ret ne "-") {
	    $ret=~s/\//,/gs;
	    my @nodes = split(/\+/,$ret);
	    #$ret="(".join(')(',@nodes).")";
	    $ret="".join(')(',@nodes)."";
	}
    }

    if($mkey eq "vnodelist") {
	if($ret ne "-") {
	    my @nodes = split(/\+/,$ret);
	    my ($c,$nd,$num);
	    for($c=0;$c<$#nodes;$c++) {
		if($nodes[$c]=~/([^\(\)\:]+)\:.*ncpus=(\d+)/s) {
		    $nd=$1;$num=$2;
		    $nodes[$c]="$nd,$num";
		} elsif($nodes[$c]=~/([^\(\)\:]+)\:.*mpiprocs=(\d+)/s) {
		    $nd=$1;$num=$2;
		    $nodes[$c]="$nd,$num";
		} elsif($nodes[$c]=~/([^\(\)\:]+)/s) {
		    $nd=$1;$num=1;
		    $nodes[$c]="$nd,$num";
		} else {
		    print STDERR "Error in job node list: $nodes[$c]\n";
		}
	    }
	    $ret="(".join(')(',@nodes).")";
	}
    }

    if($mkey eq "totalcores") {
        my $numcores = $ret * $cores_per_node;
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

sub map_block_to_nodes {
    my($blockname)=@_;

    my $start_row = 0;
    my $start_column = 0;
    my $start_midplane = 0;
    my $start_nodecard = 0;
    my $start_computecard = 0;
    my $max_row = 0;
    my $max_column = 0;
    my $max_midplane = 2;
    my $max_nodecard = 16;
    my $max_computecard = 32;
    my $blocksize = 0;

# For blocks 512 nodes in size:
# <machine label>-R<row><column>-M<midplane>-512
    if(($blockname=~/^[\w]+\-R(\d)(\d)\-M(\d+)\-512$/) ||
       ($blockname=~/^[\w]+\-R(\d)(\d)\-M(\d+)\-[TJ]\d+\-512$/)) {
      $start_row=$1;
      $max_row=$1;
      $start_column=$2;
      $max_column=$2;
      $start_midplane=$3;
      $max_midplane=$3+1;
      $start_nodecard=0;
      $blocksize = 512;
    }

# For blocks 1,024 nodes in size:
# <machine label>-R<row><column>-1024
    elsif(($blockname=~/^[\w]+\-R(\d)(\d)\-1024$/) ||
          ($blockname=~/^[\w]+\-R(\d)(\d)\-[TJ]\d+\-1024$/)) {
      $start_row=$1;
      $max_row=$1;
      $start_column=$2;
      $max_column=$2;
      $start_midplane=0;
      $start_nodecard=0;
      $blocksize = 1024;
    }

# For blocks under 512 nodes:
# <machine label>-R<row><column>-M<midplane>-N<first node card in block>-<block size in compute node cards>
    elsif(($blockname=~/^[\w]+\-R(\d)(\d)\-M(\d+)\-N(\d+)\-(\d+)$/) ||
          ($blockname=~/^[\w]+\-R(\d)(\d)\-M(\d+)\-N(\d+)\-[TJ]\d+\-(\d+)$/)) {
      $start_row=$1;
      $start_column=$2;
      $start_midplane=$3;
      $start_nodecard=$4;
      $blocksize = $5;
    }

# For blocks greater than 1,024 nodes:
# <machine label>-R<starting row><starting column>-R<ending row><ending column>-<blocksize>
    elsif(($blockname=~/^[\w]+\-R(\d)(\d)\-R(\d)(\d)\-(\d+)$/) ||
          ($blockname=~/^[\w]+\-R(\d)(\d)\-R(\d)(\d)\-[TJ]\d+\-(\d+)$/)) {
      $start_row=$1;
      $start_column=$2;
      $max_row=$3;
      $max_column = $4;
      $blocksize = $5;
    }


#    print " $blockname R $start_row .. $max_row\n";
#    print " $blockname C $start_column .. $max_column\n";
#    print " $blockname M $start_midplane .. $max_midplane\n";
#    print " $blockname N $start_nodecard .. $max_nodecard\n";
#    print " $blockname c $start_computecard .. $max_computecard\n";

    my $row = 0;
    my $column = 0;
    my $midplane = 0;
    my $nodecard = 0;
    my $computecard = 0;
    my $node_count = 0;
    my $node_string = "";
    for ($row = $start_row; $row <= $max_row; $row++) {
      for ($column = $start_column; $column <= $max_column; $column++) {
        for ($midplane = $start_midplane; $midplane < $max_midplane; $midplane++) {
          for ($nodecard = $start_nodecard; $nodecard < $max_nodecard; $nodecard++) {
            for ($computecard = $start_computecard; $computecard < $max_computecard; $computecard++) {
              if (length($node_string) > 0) {
                $node_string=sprintf("%s,R%01d%01d-M%01d-N%02d-C%02d", $node_string, $row, $column, $midplane, $nodecard, $computecard+4);
              } else {
                $node_string=sprintf("R%01d%01d-M%01d-N%02d-C%02d", $row, $column, $midplane, $nodecard, $computecard+4);
              }
              $node_count = $node_count + 1;
              last if ($node_count >= $blocksize);
            }
            last if ($node_count >= $blocksize);
          }
          last if ($node_count >= $blocksize);
        }
        last if ($node_count >= $blocksize);
      }
      last if ($node_count >= $blocksize);
    }
    # printf ("%d node block %s maps to %s\n", $blocksize, $blockname, $node_string);
    return ($node_string);
}

sub map_torus_to_nodes {
    my($torus_name)=@_;

    printf("\nMapping %s\n", $torus_name);

    my $begin_torus_x = 0;
    my $begin_torus_y = 0;
    my $begin_torus_z = 0;
    my $begin_torus_w = 0;
    my $begin_torus_t = 0;
    my $end_torus_x = 0;
    my $end_torus_y = 0;
    my $end_torus_z = 0;
    my $end_torus_w = 0;
    my $end_torus_t = 0;

# Check for a BG/Q-style blockname
# EAS-xyzwt-xyzwt-size
    if($torus_name=~/^[\w]+\-(\d)(\d)(\d)(\d)(\d)\-(\d)(\d)(\d)(\d)(\d)\-[\d]+$/) {
      $begin_torus_x=$1;
      $begin_torus_y=$2;
      $begin_torus_z=$3;
      $begin_torus_w=$4;
      $begin_torus_t=$5;
      $end_torus_x=$6;
      $end_torus_y=$7;
      $end_torus_z=$8;
      $end_torus_w=$9;
      $end_torus_t=$10;
    }

    my $x = 0;
    my $y = 0;
    my $z = 0;
    my $w = 0;
    my $t = 0;
    my %hash = %{retrieve($hashfile)};
    #my $max_torus_x = $hash { "max_x" };
    #my $max_torus_y = $hash { "max_y" };
    #my $max_torus_z = $hash { "max_z" };
    #my $max_torus_w = $hash { "max_w" };
    #my $max_torus_t = $hash { "max_t" };
    my $node_string = "";
    #my $stop_x = ($max_torus_x < $end_torus_x ? $max_torus_x : $end_torus_x);
    #for ($x = $begin_torus_x; $x <= $stop_x; $x++) {
      #my $stop_y = ($x < $stop_x ? $max_torus_y : $end_torus_y);
      #for ($y = $begin_torus_y; $y <= $stop_y; $y++) {
        #my $stop_z = ($y < $stop_y ? $max_torus_z : $end_torus_z);
        #for ($z = $begin_torus_z; $z <= $stop_z; $z++) {
          #my $stop_w = ($z < $stop_z ? $max_torus_w : $end_torus_w);
          #for ($w = $begin_torus_w; $w <= $stop_w; $w++) {
            #my $stop_t = ($w < $stop_w ? $max_torus_t : $end_torus_t);
            #for ($t = $begin_torus_t; $t <= $stop_t ; $t++) {
    for ($x = $begin_torus_x; $x <= $end_torus_x; $x++) {
      for ($y = $begin_torus_y; $y <= $end_torus_y; $y++) {
        for ($z = $begin_torus_z; $z <= $end_torus_z; $z++) {
          for ($w = $begin_torus_w; $w <= $end_torus_w; $w++) {
            for ($t = $begin_torus_t; $t <= $end_torus_t ; $t++) {
              my $coordinate = sprintf ("%d%d%d%d%d", $x, $y, $z, $w, $t);
              my $block = $hash{ $coordinate };
              # printf("%s %s\n", $coordinate, $block);
              if (length($node_string) > 0) {
                $node_string=sprintf("%s,%s", $node_string, $block);
              } else {
                $node_string=sprintf("%s", $block);
              }
            }
          }
        }
      }
    }
    return ($node_string);
}


