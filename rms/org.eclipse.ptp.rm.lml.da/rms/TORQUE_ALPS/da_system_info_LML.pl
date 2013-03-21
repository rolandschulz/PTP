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
    "motd"                                      => "motd",
    );

my ($sysinfoid,$line,%sysinfo,%sysinfonr,$key,$value,$count,%notmappedkeys,%notfoundkeys);

$sysinfoid="cluster";
$sysinfo{$sysinfoid}{hostname}=$Hostname;
$sysinfo{$sysinfoid}{date}=&get_current_date();
$sysinfo{$sysinfoid}{type}="ALPS";

# checking system message of today 
my $motd = "/etc/motd";
if(-e $motd) {
    if(open(MOTD,'<',$motd)) {
        while(<MOTD>) {
        $sysinfo{$sysinfoid}{motd}.="$_";
        }
        close MOTD;
    }
}

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

sub get_current_date {
    my($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$idst)=localtime(time());
    my($date);
    $year=substr($year,1,2);
    $date=sprintf("%02d/%02d/%02d-%02d:%02d:%02d",$mon+1,$mday,$year,$hour,$min,$sec);
    return($date);
}


sub modify {
    my($key,$mkey,$value)=@_;
    my $ret=$value;

    if($mkey eq "motd") {
        $ret=~s/</\&lt;/gs;
        $ret=~s/>/\&gt;/gs;
        $ret=~s/\&/\&amp;/gs;
        $ret=~s/'/\&apos;/gs;
        $ret=~s/"/\&quot;/gs;
        $ret=~s/\n/\&\#10;/gs;
    }

    return($ret);
}

