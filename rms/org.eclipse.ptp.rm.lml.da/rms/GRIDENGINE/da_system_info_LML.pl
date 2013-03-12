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

if ($#ARGV != 0) {
  die " Usage: $0 <filename> $#ARGV\n";
}
my $filename = $ARGV[0];

my $system_time = &get_current_date();
my $hostname = `hostname`; chomp($hostname);

open(OUT,"> $filename") || die "cannot open file $filename";
printf OUT <<EOF;
<?xml version="1.0" encoding="UTF-8"?>
<lml:lgui xmlns:lml="http://eclipse.org/ptp/schemas" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://eclipse.org/ptp/schemas http://eclipse.org/ptp/schemas/lgui.xsd"
        version="0.7">
  <objects>
    <object id="sys000001" name="cluster" type="system"/>
  </objects>
  <information>
    <info oid="sys000001" type="short">
      <data key="system_time"    value="$system_time"/>
      <data key="hostname"       value="$hostname"/>
      <data key="type"           value="Cluster"/>
    </info>
  </information>
</lml:lgui>
EOF
close(OUT);

sub get_current_date {
    my($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$idst)=localtime(time());
    my($date);
    $year=substr($year,1,2);
    $date=sprintf("%02d/%02d/%02d-%02d:%02d:%02d",$mon+1,$mday,$year,$hour,$min,$sec);
    return($date);
}
