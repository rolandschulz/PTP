#!/usr/bin/perl -w
#*******************************************************************************
#* Copyright (c) 2011-2012 Forschungszentrum Juelich GmbH.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*
#* Contributors:
#*    Wolfgang Frings, Carsten Karbach (Forschungszentrum Juelich GmbH) 
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
my ($line,%apps,%appnr,$key,$value,$count,%notmappedkeys,%notfoundkeys);

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
    "apid"                                   => "apid",
    "batchid"                                => "batchid",
    "info"                                   => "",
    );

my $cmd="/usr/bin/apstat";
$cmd=$ENV{"CMD_APPINFO"} if($ENV{"CMD_APPINFO"}); 

open(IN,"$cmd -r -a -v |");
my $apid="-";
my $lastkey="info";

# skip overview
while($line=<IN>) {
    last if($line=~/Application detail/ or $line=~/ResId/);
}

while($line=<IN>) {
    chomp($line);
	#Slurm alps apstat returns all information about a job in a single line
    if($line=~/^A\s+(\d+)\s+(\d+)\s+batch:(\d+)/){
    	$apid = $1;
    	my $mline = $line;
    	$mline=~s/^A\s*//;
    	$mline=~s/\s*$//;
    	$mline=~s/\s+/;/g;
    	$apps{$apid}{$lastkey}=$mline;
    	next;
    }
    
    #Old checks of TORQUE_ALPS
    if($line=~/Ap\[$patint\]: (.*)$/) {
	$apid=$1;
	$apps{$apid}{$lastkey}=$line;
	$apps{$apid}{$lastkey}.=";";
    } else {
	$line=~s/^\s*//gs;
	$apps{$apid}{$lastkey}.=$line;
	$apps{$apid}{$lastkey}.=";";
    }
}
close(IN);

# check info of apps
foreach $apid (sort(keys(%apps))) {
    my $info=$apps{$apid}{info};
    if($info=~/apid\s+$patint[\s,;]/){
	$apps{$apid}{apid}=$1;
    }
    if($info=~/Batch System ID = $patint[\s,;]/){
	$apps{$apid}{batchid}=$1;
    }
    #Get app id and batch id for slurm alps
    if($info=~/(\d+);(\d+);batch:(\d+)/ ){
    	$apps{$apid}{apid}=$2;
    	$apps{$apid}{batchid}=$3;
    }
}

open(OUT,"> $filename") || die "cannot open file $filename";
printf(OUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
printf(OUT "<lml:lgui xmlns:lml=\"http://eclipse.org/ptp/schemas\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
printf(OUT "	xsi:schemaLocation=\"http://eclipse.org/ptp/schemas http://eclipse.org/ptp/schemas/lgui.xsd\"\n");
printf(OUT "	version=\"0.7\"\>\n");
printf(OUT "<objects>\n");
$count=0;
foreach $apid (sort(keys(%apps))) {
    $count++;$appnr{$apid}=$count;
    printf(OUT "<object id=\"a%06d\" name=\"%s\" type=\"app\"/>\n",$count,$apid);
}
printf(OUT "</objects>\n");
printf(OUT "<information>\n");
foreach $apid (sort(keys(%apps))) {
    printf(OUT "<info oid=\"a%06d\" type=\"short\">\n",$appnr{$apid});
    foreach $key (sort(keys(%{$apps{$apid}}))) {
	if(exists($mapping{$key})) {
	    if($mapping{$key} ne "") {
		$value=&modify($key,$mapping{$key},$apps{$apid}{$key});
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
