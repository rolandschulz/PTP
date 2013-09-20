#!/usr/bin/perl -w
#*******************************************************************************
#* Copyright (c) 2012 Forschungszentrum Juelich GmbH.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*
#* Contributors:
#*    Wolfgang Frings, Carsten Karbach (Forschungszentrum Juelich GmbH) 
#*******************************************************************************/ 
use strict;

use FindBin;
use lib "$FindBin::RealBin/../../lib";
use LML_da_util;

my $srcDir = $0;
if($srcDir =~ /\// ){
	$srcDir =~ s/[^\/]+\.pl$//;
}
else{
	$srcDir = "./";
}

require $srcDir."helper_functions.pl";#Include shared helper functions

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
    "HOST_NAME"                            => "id",
    "MAX"                                  => "ncores",
    "NJOBS"                                => "njobs",
    "RUN"                                  => "run",
    "SSUSP"                                => "ssusp",
    "USUSP"                                => "ususp",
    "RSV"                                  => "rsv",
    "STATUS"                               => "state",
    "JL/U"                                 => ""
    );


my $cmd="/usr/bin/bhosts";
$cmd=$ENV{"CMD_NODEINFO"} if($ENV{"CMD_NODEINFO"}); 

open(IN,"$cmd |");
my $nodeid="-";


#Parse first line with attribute names (HOST_NAME          STATUS       JL/U    MAX  NJOBS    RUN  SSUSP  USUSP    RSV)
$line = <IN>;
chomp($line);

my @keyNames = split(/\s+/, $line);
my $keyCount = scalar(@keyNames);
my $keyCountMinusOne = $keyCount-1;

while($line=<IN>) {
    chomp($line);
    #Check for a valid line with $keyCount attributes
	if($line =~ /^((\S+)\s+){$keyCountMinusOne}((\S+))\s*$/ ){
		my @attributes = split(/\s+/, $line);
		$nodeid = $attributes[0];
		
		for(my $i=0; $i<= $keyCountMinusOne; $i++){
			$nodes{$nodeid}{$keyNames[$i]}=$attributes[$i];
		}
	}
}
close(IN);


# add unknown but mandatory attributes to nodes
foreach $nodeid (keys(%nodes)) {
	#Set number of node to at least 1
	if($nodes{$nodeid}{MAX} == 0){
		$nodes{$nodeid}{MAX} = 1;
	}	
}

#Generate LML file
open(OUT,"> $filename") || die "cannot open file $filename";
printf(OUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
printf(OUT "<lml:lgui xmlns:lml=\"http://eclipse.org/ptp/lml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
printf(OUT "	xsi:schemaLocation=\"http://eclipse.org/ptp/lml http://eclipse.org/ptp/schemas/v1.1/lgui.xsd\"\n");
printf(OUT "	version=\"1.1\"\>\n");
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
		if(defined($value)) {
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

#**********************************
# Adjusts value for a node's attribute.
# Especially makes sure, that special characters are escaped in attribute values.
#
# @param $_[0] the original key name parsed from the command's output
# @param $_[1] the mapped key name provided by the global mapping hash
# @param $_[2] the raw value parsed from the command's output
# @return the adjusted value, which can be printed into the XML file
#**********************************
sub modify {
    my($key,$mkey,$value)=@_;
    my $ret=$value;

    if($mkey eq "owner") {
	$ret=~s/\@.*//gs;
    }

    if($mkey eq "state") {
	$ret="Running"  if ($value eq "ok");
	$ret="Running"  if ($value eq "closed");
	$ret="Down"     if ($value eq "unavail");
	$ret="Down"     if ($value eq "unreach");
	$ret="Down"     if ($value eq "unlicensed");
	$ret="unknown"  if ($value eq "unknown");
    }

	$ret = &LML_da_util::escapeForXML($ret);

    return($ret);
}
