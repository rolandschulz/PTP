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

package LML_da_date_manip;
use Time::Local;
my $debug=0;
use Data::Dumper;

my %monthmap= ('Jan' => 1, 'Feb' => 2, 'Mar' => 3, 'Apr' => 4, 'May'=> 5, 'Jun' => 6, 
	       'Jul' => 7, 'Aug' => 8, 'Sep' => 9, 'Oct' => 10, 'Nov' => 11, 'Dec' => 12 );

sub date_to_stddate {
    my($indate)=@_;
    my($outdate);
    my($sec,$min,$hour,$mday,$mon,$year);

    if($indate=~/^\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d$/) {
	return($indate);
    }
    if($indate=~/^\w\w\w\s+(\w\w\w)\s+(\d+)\s+(\d\d):(\d\d):(\d\d)\s+(\d\d\d\d)$/) {
	($sec,$min,$hour,$mday,$mon,$year)=($5,$4,$3,$2,$1,$6);
	$outdate=sprintf("%04d-%02d-%02d %02d:%02d:%02d",$year,$monthmap{$mon},$mday,$hour,$min,$sec);
	return($outdate);
    } 
    print "ERROR: date_to_stddate: could not convert date  '$indate' -> ?\n";
    return($indate);
}



sub time_to_stdduration {
    my($intime)=@_;
    my($outdate);
    my($sec,$min,$hour,$day);

    if($indate=~/^\d\d\d\d-\d\d-\d\d \d\d:\d\d:\d\d$/) {
	return($indate);
    }
    if($indate=~/^\w\w\w\s+(\w\w\w)\s+(\d+)\s+(\d\d):(\d\d):(\d\d)\s+(\d\d\d\d)$/) {
	($sec,$min,$hour,$mday,$mon,$year)=($5,$4,$3,$2,$1,$6);
	$outdate=sprintf("%02d/%02d/%02d %02d:%02d:%02d",$monthmap{$mon},$mday,$year,$hour,$min,$sec);
	return($outdate);
    } 
    print "ERROR: date_to_stddate: could not convert date  '$indate' -> ?\n";
    return($indate);
}


sub sec_to_date {
    my ($lsec)=@_;
    my($date);
    my ($sec,$min,$hours,$mday,$mon,$year,$rest)=localtime($lsec);
    $year=sprintf("%02d",$year % 100);
    $mon++;
    $date=sprintf("%02d/%02d/%02d-%02d:%02d:%02d",$mon,$mday,$year,$hours,$min,$sec);
    return($date);
}

sub sec_to_date2 {
    my ($lsec)=@_;
    my($date);
    my ($sec,$min,$hours,$mday,$mon,$year,$rest)=localtime($lsec);
    $mon++;
    $year+=1900;
    $date=sprintf("%02d.%02d.%4d",$mday,$mon,$year);
    return($date);
}

sub sec_to_day {
    my ($lsec)=@_;
    my($wdaystr);
    my ($sec,$min,$hours,$mday,$mon,$year,$wday,$rest)=localtime($lsec);
    $wdaystr=("Su","Mo","Tu","We","Th","Fr","Sa")[$wday];
    return($wday);
}

sub sec_to_month {
    my ($lsec)=@_;
    my ($sec,$min,$hours,$mday,$mon,$year,$wday,$rest)=localtime($lsec);
    return($mon);
}

sub timediff {
    my ($date1,$date2)=@_;
    my $timesec1=&date_to_sec($date1);
    my $timesec2=&date_to_sec($date2);
    return($timesec1-$timesec2);
}

sub date_to_sec3 {
    my ($ldate)=@_;
    my ($mday,$mon,$year,$hours,$min,$sec)=split(/[ \.:\/\-\_\.]/,$ldate);
    $hours=$min=$sec=0;
    $mon--;
    my $timesec=timelocal($sec,$min,$hours,$mday,$mon,$year);
    return($timesec);
}

sub date_to_sec {
    my ($ldate)=@_;
    my ($mon,$mday,$year,$hours,$min,$sec)=split(/[ :\/\-\_\.]/,$ldate);
    $mon--;
    my $timesec=timelocal($sec,$min,$hours,$mday,$mon,$year);
    return($timesec);
}

sub date_to_sec_job {
    my ($ldate)=@_;
    my ($mday,$mon,$year,$hours,$min,$sec)=split(/[ :\/\-\_\.]/,$ldate);
    $mon--;
    my $timesec=timelocal($sec,$min,$hours,$mday,$mon,$year);
    return($timesec);
}

sub date_to_sec2 {
    my ($ldate)=@_;
    my ($mday,$mon,$year)=split(/[ :\/\-\.\_]/,$ldate);
    $mon--;
    if($mon<0) {  
	$mon=0;$year=0;$mday=1;
    }
    my $timesec=timelocal(0,0,0,$mday,$mon,$year);
    return($timesec);
}

sub timediff_md {
    my ($timesec1,$timesec2)=@_;
    my @days=(31,28,31,30,31,30,31,31,30,31,30,31);
    my($diffm,$difft);
    my ($sec1,$min1,$hours1,$mday1,$mon1,$year1,$wday1,$rest1)=localtime($timesec1);
    my ($sec2,$min2,$hours2,$mday2,$mon2,$year2,$wday2,$rest2)=localtime($timesec2);
    my($m1,$m2)=($mon1+12*$year1,$mon2+12*$year2);
    if($m1==$m2) {
	$diffm=0;
	$difft=($mday2-$mday1+1) * $dpm/$days[$mon1];
    } else {
	$diffm=$m2-$m1+1;
	if($mday1!=1) {
	    $difft=-($mday1) * $dpm/$days[$mon1];
	} else {$difft=0}

	if($mday2!=$days[$mon2]) {
	    $difft+= -($days[$mon2]-$mday2+1) * $dpm/$days[$mon2];
	}
	if($difft<0) {
	    $diffm+=int($difft/$dpm-1);
	    $difft+= -$dpm*int($difft/$dpm-1);
	}
    }
    return($diffm,$difft);
}

sub date_to_absmonth {
    my ($ldate)=@_;
    my $absmonth=-1;
    my ($mday,$mon,$year);
    if($ldate) {
	($mday,$mon,$year)=split(/[ :\/\-\.\_]/,$ldate);
	$absmonth=$mon-1;
	$year-=2000 if($year>=2000);
	$absmonth+=(12*$year);
    }
    return($absmonth);
}

sub absmonth_to_date {
    my ($absmonth)=@_;
    my($ldate,$mday,$mon,$year);
    $year=int($absmonth/12);
    $mon=$absmonth-$year*12 + 1;
    $year+=2000;
    $mday=01;
    $ldate=sprintf("%02d.%02d.%4d",$mday,$mon,$year);
    return($ldate);
}

sub absmonth_to_mname {
    my ($absmonth)=@_;
    my($mname,$mon,$year);
    $year=int($absmonth/12);
    $mon=$absmonth-$year*12 + 1;
    $mname=("","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")[$mon];
    $mname.=sprintf("%02d",$year);
    return($mname);
}


1;
