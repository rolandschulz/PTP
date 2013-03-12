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
my ($line,%node,%nodenr,$key,$value,$count,%notmappedkeys,%notfoundkeys);

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

my %mapping = (
    "Apids"                                  => "Apids",
    "Arch"                                   => "",
    "Avl"                                    => "",
    "Conf"                                   => "Conf",
    "HW"                                     => "HW",
    "PEs"                                    => "",
    "PgSz"                                   => "PgSz",
    "Pl"                                     => "Pl",
    "Placed"                                 => "Placed",
    "Rv"                                     => "",
    "State"                                  => "State",
    "Alloc"                                  => "A",
    );

my $cmd="/usr/bin/apstat";
$cmd=$ENV{"CMD_NODEINFO"} if($ENV{"CMD_NODEINFO"}); 

open(IN,"$cmd -n -v |");
my $nid="-";


# skip header
$line=<IN>;

while($line=<IN>) {
    chomp($line);
    
    if($line=~/^\s*$patint\s+$patwrd\s+$patwrd\s+$patwrd\s+$patint\s+$patint\s+$patint\s+$patwrd\s+$patint\s+$patint\s+$patint\s+$patint\s+$patint\s*$/) {
	$nid=$1;
	$node{$nid}{Arch}=$2,
	$node{$nid}{State}=$3;
	$node{$nid}{Alloc}=$4;
	$node{$nid}{HW}=$5;
	$node{$nid}{Rv}=$6;
	$node{$nid}{Pl}=$7;
	$node{$nid}{PgSz}=$8;
	$node{$nid}{Avl}=$9;
	$node{$nid}{Conf}=$10;
	$node{$nid}{Placed}=$11;
	$node{$nid}{PEs}=$12;
	$node{$nid}{Apids}=$13;
    }
    if($line=~/^\s*$patint\s+$patwrd\s+$patwrd\s+$patwrd\s+$patint\s+$patwrd\s+$patwrd\s+$patwrd\s+$patint\s+$patint\s+$patint\s+$patint\s*$/) {
	$nid=$1;
	$node{$nid}{Arch}=$2,
	$node{$nid}{State}=$3;
	$node{$nid}{Alloc}=$4;
	$node{$nid}{HW}=$5;
	$node{$nid}{Rv}=$6;
	$node{$nid}{Pl}=$7;
	$node{$nid}{PgSz}=$8;
	$node{$nid}{Avl}=$9;
	$node{$nid}{Conf}=$10;
	$node{$nid}{Placed}=$11;
	$node{$nid}{PEs}=$12;
	$node{$nid}{Apids}="-";
    }
} 
close(IN);

open(OUT,"> $filename") || die "cannot open file $filename";
printf(OUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
printf(OUT "<lml:lgui xmlns:lml=\"http://eclipse.org/ptp/schemas\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
printf(OUT "	xsi:schemaLocation=\"http://eclipse.org/ptp/schemas http://eclipse.org/ptp/schemas/lgui.xsd\"\n");
printf(OUT "	version=\"0.7\"\>\n");
printf(OUT "<objects>\n");
$count=0;
foreach $nid (sort(keys(%node))) {
    $count++;$nodenr{$nid}=$count;
    printf(OUT "<object id=\"nd%06d\" name=\"%s\" type=\"node\"/>\n",$count,$nid);
}
printf(OUT "</objects>\n");
printf(OUT "<information>\n");
foreach $nid (sort(keys(%node))) {
    printf(OUT "<info oid=\"nd%06d\" type=\"short\">\n",$nodenr{$nid});
    foreach $key (sort(keys(%{$node{$nid}}))) {
	if(exists($mapping{$key})) {
	    if($mapping{$key} ne "") {
		$value=&modify($key,$mapping{$key},$node{$nid}{$key});
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

    if(!$ret) {
	return(undef);
    }

    # mask & in user input
    if($ret=~/\&/) {
	$ret=~s/\&/\&amp\;/gs;
    } 


    return($ret);
}
