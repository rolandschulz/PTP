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

use lib ".";

my $patint="([\\+\\-\\d]+)";   # Pattern for Integer number
my $patfp ="([\\+\\-\\d.E]+)"; # Pattern for Floating Point number
my $patwrd="([\^\\s]+)";       # Pattern for Work (all noblank characters)
my $patbl ="\\s+";             # Pattern for blank space (variable length)

#####################################################################
# get user sysinfo / check system 
#####################################################################
my $UserID = getpwuid($<);
my $Hostname = `hostname`;
chomp($Hostname);
my $verbose=1;

my $filename = $ARGV[0];

my %mapping = (
    "hostname"                                  => "hostname",
    "date"                                      => "system_time",
    "type"                                      => "type",
    "version"                                   => "version"
    );

my ($sysinfoid,$line,%sysinfo,%sysinfonr,$key,$value,$count,%notmappedkeys,%notfoundkeys);

$sysinfoid="cluster";
$sysinfo{$sysinfoid}{hostname}=$Hostname;
$sysinfo{$sysinfoid}{date}=&get_current_date();
$sysinfo{$sysinfoid}{type}="Cluster";

my $cmd="/usr/bin/lsid";
$cmd=$ENV{"CMD_SYSINFO"} if($ENV{"CMD_SYSINFO"}); 

open(IN,"$cmd |");

$line = <IN>;
chomp($line);
$sysinfo{$sysinfoid}{version} = $line;

open(OUT,"> $filename") || die "cannot open file $filename";
printf(OUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
printf(OUT "<lml:lgui xmlns:lml=\"http://eclipse.org/ptp/lml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
printf(OUT "	xsi:schemaLocation=\"http://eclipse.org/ptp/lml http://eclipse.org/ptp/schemas/v1.1/lgui.xsd\"\n");
printf(OUT "	version=\"1.1\"\>\n");
printf(OUT "<objects>\n");
$count=0;
foreach $sysinfoid (sort(keys(%sysinfo))) {
    $count++;$sysinfonr{$sysinfoid}=$count;
    printf(OUT "<object id=\"sys%06d\" name=\"%s\" type=\"system\"/>\n",$count,$sysinfoid);
}
printf(OUT "</objects>\n");
printf(OUT "<information>\n");
foreach $sysinfoid (sort(keys(%sysinfo))) {
    printf(OUT "<info oid=\"sys%06d\" type=\"short\">\n",$sysinfonr{$sysinfoid});
    foreach $key (sort(keys(%{$sysinfo{$sysinfoid}}))) {
	if(exists($mapping{$key})) {
	    if($mapping{$key} ne "") {

		$value=&modify($key,$mapping{$key},$sysinfo{$sysinfoid}{$key});
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
    printf("%-40s => \"\"\n","\"".$key."\"",$notfoundkeys{$key});
}

1;

#***********************
# @return current date in a default format
#***********************
sub get_current_date {
    my($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$idst)=localtime(time());
    my($date);
    $year=substr($year,1,2);
    $date=sprintf("%02d/%02d/%02d-%02d:%02d:%02d",$mon+1,$mday,$year,$hour,$min,$sec);
    return($date);
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

    $ret = &LML_da_util::escapeForXML($ret);

    return($ret);
}

