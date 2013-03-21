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
use Storable;
use File::Basename;

my $patint="([\\+\\-\\d]+)";   # Pattern for Integer number
my $patfp ="([\\+\\-\\d.E]+)"; # Pattern for Floating Point number
my $patwrd="([\^\\s]+)";       # Pattern for Work (all noblank characters)
my $patbl ="\\s+";             # Pattern for blank space (variable length)

#####################################################################
# get user info / check system 
#####################################################################
my $UserID = getpwuid($<);
my $Hostname = `hostname -d`;
my $verbose=1;
my ($line,%nodes,%nodenr,$key,$value,$count,%notmappedkeys,%notfoundkeys);

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

my $is_bgq = 0;
if (defined($ENV{'BGQDRV'})) {
    $is_bgq = 1;
}


my %mapping = (
    "availmem"                               => "availmem",
    "id"                                     => "id",
    "name"                                   => "name",
    "idletime"                               => "",
    "jobs"                                   => "",
    "loadave"                                => "",
    "ncores"                                  => "ncores",
    "netload"                                => "",
    "note"                                   => "",
    "np"                                     => "",
    "nsessions"                              => "",
    "ntype"                                  => "",
    "nusers"                                 => "",
    "opsys"                                  => "",
    "physmem"                                => "physmem",
    "properties"                             => "",
    "rectime"                                => "",
    "sessions"                               => "",
    "size"                                   => "",
    "ntype"                                  => "ntype",
    "state"                                  => "state",
    "type"                                   => "type",
    "x_loc"                                  => "x_loc",
    "y_loc"                                  => "y_loc",
    "z_loc"                                  => "z_loc",
    "status"                                 => "",
    "totmem"                                 => "",
    "uname"                                  => "",
    "varattr"                                => "",
    "Mom"                                    => "",
    "Priority"                               => "",
    "comment"                                => "",
    "pcpus"                                  => "",
    "resources_assigned.mem"                 => "",
    "resources_assigned.ncpus"               => "",
    "resources_assigned.netwins"             => "",
    "resources_assigned.vmem"                => "",
    "resources_available.arch"               => "",
    "resources_available.enabled"            => "",
    "resources_available.host"               => "",
    "resources_available.location"           => "",
    "resources_available.mem"                => "",
    "resources_available.node_type"          => "",
    "resources_available.rack"               => "",
    "resources_available.rack2"              => "",
    "resources_available.rank"               => "",
    "resources_available.rank2"              => "",
    "resources_available.router"             => "",
    "resources_available.universe"           => "",
    "resources_available.vmem"               => "",
    "resources_available.vnode"              => "",
    "resv_enable"                            => "",
    "sharing"                                => "",
# unknown attributes
    );


my $cmd="/usr/bin/partlist";
$cmd=$ENV{"CMD_NODEINFO"} if($ENV{"CMD_NODEINFO"}); 

open(IN,"$cmd -a -v |");
my $nodeid="-";
my $lastkey="-";

my $max_row = 0;
my $max_column = 0;
my $max_midplane = 0;
my $max_nodecard = 0;
my $max_computecard = 0;

# for the BG/Q the names map to the torus coordinates.

my $max_torus_x = 0; 
my $max_torus_y = 0;
my $max_torus_z = 0;
my $max_torus_t = 0;
my $max_torus_w = 0;
my $have_bgq = 0;

while($line=<IN>) {
    chomp($line);
    if($line=~/^Name\s+Queue\s+State\s+Backfill\s*$/) {
        # this is the header
    } elsif($line=~/^[=]+$/) {
        # this is the line after headers
# get other data
    } elsif($line=~/^([A-Z0-9\-]+)\s+(\S+)\s+(\S*)\s+.*$/) {
#	printf("%s %s %s\n",$1,$2,$3);
        $nodeid=$1;

# Check for a BG/Q-style blockname
# EAS-xyzwt-xyzwt-size
        if($nodeid=~/^[\w]+\-[\d]+\-(\d)(\d)(\d)(\d)(\d)\-[\d]+$/) {
          $max_torus_x=$1 if(int($1) > $max_torus_x);
          $max_torus_y=$2 if(int($2) > $max_torus_y);
          $max_torus_z=$3 if(int($3) > $max_torus_z);
          $max_torus_w=$4 if(int($4) > $max_torus_w);
          $max_torus_t=$5 if(int($5) > $max_torus_t);
          $have_bgq = 1;
        }

# For blocks 512 nodes in size:
# <machine label>-R<row><column>-M<midplane>-512
        elsif($nodeid=~/^[\w]+\-R(\d)(\d)\-M(\d+)\-512$/) {
          $max_row=$1 if(int($1) > $max_row);
          $max_column=$2 if(int($2) > $max_column);
          $max_midplane=$3 if(int($3) > $max_midplane);
        }

# For blocks 1,024 nodes in size:
# <machine label>-R<row><column>-1024
        elsif($nodeid=~/^[\w]+\-R(\d)(\d)\-1024$/) {
          $max_row=$1 if(int($1) > $max_row);
          $max_column=$2 if(int($2) > $max_column);
        }

# For blocks under 512 nodes:
# <machine label>-R<row><column>-M<midplane>-N<first node card in block>-<block size in compute node cards>
        elsif($nodeid=~/^[\w]+\-R(\d)(\d)\-M(\d+)\-N(\d+)\-[\d]+$/) {
          $max_row=$1 if(int($1) > $max_row);
          $max_column=$2 if(int($2) > $max_column);
          $max_midplane=$3 if(int($3) > $max_midplane);
          $max_nodecard=$4 if(int($4) > $max_nodecard);
        }

# For blocks greater than 1,024 nodes:
# <machine label>-R<starting row><starting column>-R<ending row><ending column>-<blocksize>
        elsif($nodeid=~/^[\w]+\-R(\d)(\d)\-R(\d)(\d)\-[\d]+$/) {
          $max_row=$1 if(int($1) > $max_row);
          $max_column=$2 if(int($2) > $max_column);
          $max_row=$3 if(int($3) > $max_row);
          $max_column=$4 if(int($4) > $max_column);
        }

# On challenger there is one additional addition to block names -T< the
# compute to ION ratio  if not the default 64:1> which comes directly
# before <blocksize>.
        elsif(($nodeid=~/^[\w]+\-R(\d)(\d)\-M(\d+)\-N(\d+)\-[TJ](\d+)\-[\d]+$/) ||
              ($nodeid=~/^[\w]+\-R(\d)(\d)\-R(\d)(\d)\-[TJ](\d+)\-[\d]+$/) ||
              ($nodeid=~/^[\w]+\-R(\d)(\d)\-[TJ](\d+)\-1024$/) ||
              ($nodeid=~/^[\w]+\-R(\d)(\d)\-M(\d+)\-[TJ](\d+)\-512$/)) {
          # print "$nodeid\n"
        }

    } else {
	$line=~s/^\s*//gs;
	$nodes{$nodeid}{$lastkey}.=$line;
    }
}

my $numcores = 4;

# compute the max torus size
if ($have_bgq) {
  my $totalsize = ($max_torus_x+1) * ($max_torus_y+1) * ($max_torus_z+1) * ($max_torus_w+1) * ($max_torus_t+1);
  # print "torus: $max_torus_x, $max_torus_y, $max_torus_z, $max_torus_w, $max_torus_t, \n";
  # get the total number of midplanes
  my $total_midplanes = $totalsize / 512;
  print "midplanes = $totalsize\n";
  if ($total_midplanes > 1) {
    $max_midplane = 1;
  } else {
    $max_midplane = 0;
  }
  # two midplanes per rack
  my $total_columns = $total_midplanes / 2;
  # assume eight racks per row for PTP system monitor view
  if ($total_columns > 7) {
    $max_column = 7;
  } else {
    $max_column = $total_columns - 1;
  }
  my $total_rows = $total_midplanes / 16;
  if ($total_midplanes > 15) {
    $max_row = $total_rows - 1;
  } else {
    $max_row = 0;
  }
  $max_nodecard = 15;
  $max_computecard = 31;
  my $mumcores = 16;

  my $x = 0;
  my $y = 0;
  my $z = 0;
  my $w = 0;
  my $t = 0;
  my $r = 0;
  my $c = 0;
  my $mp = 0;
  my $nc = 0;
  my $cc = 0;
  my %hash = ();
  $hash { "max_x" } = $max_torus_x;
  $hash { "max_y" } = $max_torus_y;
  $hash { "max_z" } = $max_torus_z;
  $hash { "max_w" } = $max_torus_w;
  $hash { "max_t" } = $max_torus_t;

# OK, so mapping the torus to the nodes is a bit tricky.
# The torus is incremented to maximize crossection bandwidth,
# so it isn't straightforward.
#   32 nodes: 00000-11111 (easy!)
#   64 nodes: 00000-11311 (00000-11111, 00200-11311)
#  128 nodes: 00000-11331 (00000-11111, 00200-11311, 00020-11131, 00220-11331)
#  256 nodes: 00000-31331 (and so on...)
#  512 nodes: 00000-33331 (and so on...)
# 1024 nodes: 00000-33371 (whoa!)
# 2048 nodes: 00000-?????

  my $limit = 8;

  my $main_max = 4;
  my $reset_x = 0;
  my $reset_y = 0;
  my $reset_z = 0;
  my $reset_w = 0;
  my $reset_t = 0;
  my $start_x = 0;
  my $start_y = 0;
  my $start_z = 0;
  my $start_w = 0;
  my $start_t = 0;

  while($main_max <= $limit) {
    $start_y = $reset_y;
    while (($start_y < $max_torus_y) && ($start_y < $main_max)) {
      $start_x = $reset_x;
      while (($start_x < $max_torus_x) && ($start_x < $main_max)) {
        $start_w = $reset_w;
        while (($start_w < $max_torus_w) && ($start_w < $main_max)) {
          $start_z = $reset_z;
          while (($start_z < $max_torus_z) && ($start_z < $main_max)) {
            $start_t = $reset_t;
            while (($start_t < $max_torus_t) && ($start_t < $main_max)) {
              for ($y = 0; $y < 2 ; $y++) {
                for ($x = 0; $x < 2 ; $x++) {
                  for ($w = 0; $w < 2 ; $w++) {
                    for ($z = 0; $z < 2 ; $z++) {
                      for ($t = 0; $t < 2 ; $t++) {
                        my $coordinate = sprintf ("%d%d%d%d%d", $x+$start_x, $y+$start_y, $z+$start_z, $w+$start_w, $t+$start_t);
                        my $block = sprintf ("R%02d%02d-M%01d-N%02d-C%02d", $r, $c, $mp, $nc, $cc);
                        $hash{ $coordinate } = $block;
                        $cc++;
                        if ($cc > 31) {
                          $cc = 0;
                          $nc++;
                          if ($nc > 15) {
                            $nc = 0;
                            $mp++;
                            if ($mp > 1) {
                              $mp = 0;
                              $c++;
                              if ($c > 7) {
                                $c = 0;
                                $r++;
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
              $start_t = $start_t + $t;
            }
            $start_z = $start_z + $z;
          }
          $start_w = $start_w + $w;
        }
        $start_x = $start_x + $x;
      }
      $start_y = $start_y + $y;
    }
    $reset_t = ($main_max >= $max_torus_t) ? 0 : $main_max;
    $reset_z = ($main_max >= $max_torus_z) ? 0 : $main_max;
    $reset_w = ($main_max >= $max_torus_w) ? 0 : $main_max;
    $reset_x = ($main_max >= $max_torus_x) ? 0 : $main_max;
    $reset_y = ($main_max >= $max_torus_y) ? 0 : $main_max;
    $main_max = $main_max * 2;
  }
  store \%hash, $hashfile;
}

my $row = 0;
my $column = 0;
my $midplane = 0;
my $nodecard = 0;
my $computecard = 0;
if (defined($ENV{'BGQDRV'})) {
   $computecard = 0;
} else {
   $computecard = 4;
}

# print the first node
$nodeid=sprintf("R%02d%02d-M%01d-N%02d-C%02d", $row, $column, $midplane, $nodecard, $computecard);
($key,$value)=("name",$nodeid);
$nodes{$nodeid}{$key}=$value;
($key,$value)=("id","bgbp-".$nodeid);
$nodes{$nodeid}{$key}=$value;
($key,$value)=("ncores",$numcores);
$nodes{$nodeid}{$key}=$value;
($key,$value)=("type","node");
$nodes{$nodeid}{$key}=$value;
($key,$value)=("x_loc",$row);
$nodes{$nodeid}{$key}=$value;
($key,$value)=("y_loc",$column);
$nodes{$nodeid}{$key}=$value;
($key,$value)=("z_loc",$midplane);
$nodes{$nodeid}{$key}=$value;

# print the last node
$nodeid=sprintf("R%02d%02d-M%01d-N%02d-C%02d", $max_row, $max_column, $max_midplane, $max_nodecard, $computecard+31);
($key,$value)=("name",$nodeid);
$nodes{$nodeid}{$key}=$value;
($key,$value)=("id","bgbp-".$nodeid);
$nodes{$nodeid}{$key}=$value;
($key,$value)=("ncores",$numcores);
$nodes{$nodeid}{$key}=$value;
($key,$value)=("type","node");
$nodes{$nodeid}{$key}=$value;
($key,$value)=("x_loc",$max_row);
$nodes{$nodeid}{$key}=$value;
($key,$value)=("y_loc",$max_column);
$nodes{$nodeid}{$key}=$value;
($key,$value)=("z_loc",$max_midplane);
$nodes{$nodeid}{$key}=$value;

# add unknown but manatory attributes to nodes
foreach $nodeid (keys(%nodes)) {
    my($key,$value,$pair);
#    if(exists($nodes{$nodeid}{status})) {
#	foreach $pair (split(/,/,$nodes{$nodeid}{status})) {
#	    ($key,$value)=split(/=/,$pair);
#	    $nodes{$nodeid}{$key}=$value;
#	}
#    } 

}

open(OUT,"> $filename") || die "cannot open file $filename";
printf(OUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
printf(OUT "<lml:lgui xmlns:lml=\"http://eclipse.org/ptp/lml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
printf(OUT "	xsi:schemaLocation=\"http://eclipse.org/ptp/lml http://eclipse.org/ptp/schemas/v1.1/lgui.xsd\"\n");
printf(OUT "	version=\"1.1\"\>\n");
printf(OUT "<objects>\n");
$count=0;
foreach $nodeid (sort(keys(%nodes))) {
    $count++;$nodenr{$nodeid}=$count;
    printf(OUT "<object id=\"bgbp%06d\" name=\"%s\" type=\"node\"/>\n",$count,$nodeid);
}
printf(OUT "</objects>\n");
printf(OUT "<information>\n");
foreach $nodeid (sort(keys(%nodes))) {
    printf(OUT "<info oid=\"bgbp%06d\" type=\"short\">\n",$nodenr{$nodeid});
    foreach $key (sort(keys(%{$nodes{$nodeid}}))) {
	if(exists($mapping{$key})) {
	    if($mapping{$key} ne "") {
		$value=&modify($key,$mapping{$key},$nodes{$nodeid}{$key});
		#if($value) {
		    printf(OUT " <data %-20s value=\"%s\"/>\n","key=\"".$mapping{$key}."\"",$value);
		#}
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
	$ret="Running"  if ($value eq "busy");
	$ret="Down"     if ($value eq "down");
	$ret="Idle"     if ($value eq "idle");
	$ret="Blocked"  if ($value eq "blocked");
	$ret="Blocked"  if ($value eq "blocked-wiring");
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

    if($mkey eq "nodelist") {
	if($ret ne "-") {
	    $ret=~s/\//,/gs;
	    my @nodes = split(/\+/,$ret);
	    $ret="(".join(')(',@nodes).")";
	}
    }

    if($mkey eq "totalcores") {
	if($ret=~/$patint[:]ppn=$patint/) {
	    $ret=$1*$2;
	}
    }
    if($mkey eq "totaltasks") {
	if($ret=~/$patint[:]ppn=$patint/) {
	    $ret=$1*$2;
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
