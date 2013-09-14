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

package LML_da_util;
use Time::Local;
my $debug=0;
use Data::Dumper;

# SUPPORT FUNCTIONS
###################
sub init_globalvar {
    my($vardefsref,$varhashref)=@_;
    my($pairref,$key,$value);
    
    foreach $pairref (@{$vardefsref->{var}}) {
	$key=$pairref->{key};
	$value=$pairref->{value};
	&substitute(\$value,$varhashref);
	print "init_globalvar: $key -> $value\n" if($debug==1);
	$varhashref->{$key}=$value;
    }
    
    return(1);
}

sub substitute_recursive {
    my($ds,$varhashref)=@_;
    my($i);
#    print "substitute_recursive: ",ref($ds),"\n";
#    print "substitute_recursive: ",Dumper($ds),"\n";
    
    if(ref($ds) eq "HASH") {
	foreach $key (keys(%{$ds})) {
#	    print "substitute_recursive: HASH -> $key\n";
	    if(ref($ds->{$key})) {
		&substitute_recursive($ds->{$key},$varhashref);
	    } else {
		&substitute(\$ds->{$key},$varhashref);
	    }
	}
    } elsif(ref($ds) eq "ARRAY") {
	for($i=0;$i<=$#{$ds};$i++) {
#	    print "substitute_recursive: ARRAY\n";
	    if(ref($ds->[$i])) {
		&substitute_recursive($ds->[$i],$varhashref);
	    } else {
		&substitute(\$ds->[$i],$varhashref);
	    }
	}
    } else {
	    print "substitute_recursive: unknown type ",ref($ds),"\n";
    }
    return(1);
}

sub substitute {
    my($strref,$hashref)=@_;    my($found,$c,@varlist1,@varlist2,$var);
    my($SUBSTITUTE_NOTFOUND);
    $c=0;
    $found=0;

    return(0) if($$strref eq "");

    # search normal variables
    @varlist1=($$strref=~/\$([^\{\[\$\\\s\.\,\*\/\+\-\\\`\(\)\'\?\:\;\}]+)/g);
    foreach $var (sort {length($b) <=> length($a)} (@varlist1)) {
	if(exists($hashref->{$var})) {
	    my $val=$hashref->{$var};
	    $$strref=~s/\$$var/$val/egs;
	    printf("                      substitute var1: %s = %s\n",$var,$val) if ($debug==1);
	    $found=1;
	}
    }

    # search variables in following form: ${name}
    @varlist2=($$strref=~/\$\{([^\{\[\$\\\s\.\,\*\/\+\-\\\`\(\)\'\?\:\;\}]+)\}/g);
    foreach $var (sort {length($b) <=> length($a)} (@varlist2)) {
	if(exists($hashref->{$var})) {
	    my $val=$hashref->{$var};
	    $$strref=~s/\$\{$var\}/$val/egs;
	    printf("                      substitute var2: %s = %s\n",$var,$val) if ($debug==1);
	    $found=1;
	} 
    }

    # search eval strings (`...`)
    while($$strref=~/^(.*)(\`(.*?)\`)(.*)$/) {
	my ($before,$evalall,$evalstr,$after)=($1,$2,$3,$4);
	my($val,$executeval);
        $val=undef;

        if($evalstr=~/^\s*getstdout\((.*)\)\s*$/) {
            $executeval=$1;
	    eval("{\$val=`$executeval`}");
            $val=~s/\n/ /gs;
        } 
	if(!defined($val)) {
	    eval("{\$val=$evalstr;}");
	}
	if(!defined($val)) {
	    $val=eval("{$evalstr;}");
	}
	$val="" if(!defined($val));
	if($val ne "") {
	    $$strref=$before.$val.$after;
	} else {
	    last;
	}
	printf("                      eval %s -> %s >%s<\n",$val,$$strref,$evalall) if ($debug==1);
    }

    # search for variables which could not be substitute
    @varlist1=($$strref=~/\$([^\{\[\$\\\s\.\,\*\/\+\-\\\`\(\)\'\?\:\;\}]+)/g);
    @varlist2=($$strref=~/\$\{([^\{\[\$\\\s\.\,\*\/\+\-\\\`\(\)\'\?\:\;\}]+)\}/g);
    if ( (@varlist1) || (@varlist2) ) {
	$SUBSTITUTE_NOTFOUND=join(',',@varlist1,@varlist2);
	$found=-1;
	printf("                      unknown vars in %s: %s\n",$$strref,$SUBSTITUTE_NOTFOUND);
    }
    return($found);
}

# just a replacement if perl module function is missing
sub mask_to_regexp {
    my($mask)=@_;
    my($regexp);
    my($pat,$repl);
    $regexp=$mask;
    # substitution typical patterns

    # %d
    $regexp=~s/%d/\\s*\([-+]?\\d+(?:_\\d+)*\)/gs;

    # %03d
    $regexp=~s/\%(\d+)d/\\s*\([-+]?\\d{$1}(?:_\\d+)*\)/gs;

    # %s
    $regexp=~s/\%s/\\s*\(\\S*\)/gs;

    $regexp=~s/\%(\d+)s/\\s*\(\\S\{0,$1\}\)/gs;

    return($regexp);
}

# UTILITY FUNCTIONS
###################

sub cp_file {
    my($from,$to,$verbose)=@_;
    my $cmd="/bin/cp $from $to";
    printf STDERR "executing: %s\n",$cmd if($verbose);
    system($cmd);$rc=$?;
    if($rc) {     printf STDERR "failed executing: %s rc=%d\n",$cmd,$rc; exit(-1);}
    return($rc);
}

sub sec_to_date {
    my ($lsec)=@_;
    my($date);
    my ($sec,$min,$hours,$mday,$mon,$year,$rest)=localtime($lsec);
    $year=sprintf("%02d",$year % 100);
    $mon++;
    $date=sprintf("%02d/%02d/%02d-%02d:%02d:%02d",$mon,$mday,$year,$hours,$min,$sec);
#    print "WF: sec_to_date $lsec -> sec=$sec,min=$min,hours=$hours,mday=$mday,mon=$mon,year=$year -> $date\n";
    return($date);
}

sub sec_to_date2 {
    my ($lsec)=@_;
    my($date);
    my ($sec,$min,$hours,$mday,$mon,$year,$rest)=localtime($lsec);
    $mon++;
    $year+=1900;
    $date=sprintf("%02d.%02d.%4d",$mday,$mon,$year);
#    print "WF: sec_to_date $lsec -> sec=$sec,min=$min,hours=$hours,mday=$mday,mon=$mon,year=$year -> $date\n";
    return($date);
}

sub sec_to_day {
    my ($lsec)=@_;
    my($wdaystr);
    my ($sec,$min,$hours,$mday,$mon,$year,$wday,$rest)=localtime($lsec);
    $wdaystr=("Su","Mo","Tu","We","Th","Fr","Sa")[$wday];
#    print "WF: sec_to_day $lsec -> sec=$sec,min=$min,hours=$hours,mday=$mday,mon=$mon,year=$year -> wday=$wday wdaystr=$wdaystr\n";
    return($wday);
}

sub sec_to_month {
    my ($lsec)=@_;
    my ($sec,$min,$hours,$mday,$mon,$year,$wday,$rest)=localtime($lsec);
    return($mon);
}

sub timediff {
    my ($date1,$date2)=@_;
#    print"WF: timediff $date1 $date2\n";
    my $timesec1=&date_to_sec($date1);
    my $timesec2=&date_to_sec($date2);
    return($timesec1-$timesec2);
}

sub date_to_sec3 {
    my ($ldate)=@_;
    my ($mday,$mon,$year,$hours,$min,$sec)=split(/[ \.:\/\-\_\.]/,$ldate);
    $hours=$min=$sec=0;
    $mon--;
#    print caller(),"\n";
    my $timesec=timelocal($sec,$min,$hours,$mday,$mon,$year);
#    print "WF: date_to_sec3 $date -> sec=$sec,min=$min,hours=$hours,mday=$mday,mon=$mon,year=$year -> $timesec\n";
    return($timesec);
}

sub date_to_sec {
    my ($ldate)=@_;
    my ($mon,$mday,$year,$hours,$min,$sec)=split(/[ :\/\-\_\.]/,$ldate);
    $mon--;
    my $timesec=timelocal($sec,$min,$hours,$mday,$mon,$year);
#   print "WF: date_to_sec $ldate -> sec=$sec,min=$min,hours=$hours,mday=$mday,mon=$mon,year=$year -> $timesec\n";
    return($timesec);
}

sub date_to_sec_job {
    my ($ldate)=@_;
    my ($mday,$mon,$year,$hours,$min,$sec)=split(/[ :\/\-\_\.]/,$ldate);
    $mon--;
    my $timesec=timelocal($sec,$min,$hours,$mday,$mon,$year);
#   print "WF: date_to_sec $ldate -> sec=$sec,min=$min,hours=$hours,mday=$mday,mon=$mon,year=$year -> $timesec\n";
    return($timesec);
}

sub date_to_sec2 {
    my ($ldate)=@_;
    my ($mday,$mon,$year)=split(/[ :\/\-\.\_]/,$ldate);
    $mon--;
    if($mon<0) {  
	$mon=0;$year=0;$mday=1;
#	print "WF: date_to_sec >$ldate< -> sec=$sec,min=$min,hours=$hours,mday=$mday,mon=$mon,year=$year -> $timesec\n";
#	print caller();
    }
    my $timesec=timelocal(0,0,0,$mday,$mon,$year);
#    print "WF: date_to_sec $ldate -> sec=$sec,min=$min,hours=$hours,mday=$mday,mon=$mon,year=$year -> $timesec\n";
    return($timesec);
}

# $dpm Tage pro Monat
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
#	    print "WF: mday1 $difft\n";
	} else {$difft=0}

	if($mday2!=$days[$mon2]) {
	    $difft+= -($days[$mon2]-$mday2+1) * $dpm/$days[$mon2];
#	    print "WF: mday2 $difft\n";
	}
	if($difft<0) {
	    $diffm+=int($difft/$dpm-1);
	    $difft+= -$dpm*int($difft/$dpm-1);
	}
    }
#   print "WF timediff_md: $m1,$m2 $year1,$year2 $mday1,$mday2 ($diffm,$difft)\n";
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

sub unify_string {
    my($lineref)=@_;
    my $data=$$lineref;
    $data=~s/\xe1/a/gs;
    $data=~s/\xe4/ae/gs;
    $data=~s/\xfc/ue/gs;
    $data=~s/\xf6/oe/gs;
    $data=~s/\xf3/o/gs;
    $data=~s/\xc1/A/gs;
    $data=~s/\xd6/Oe/gs;
    $data=~s/\xdf/ss/gs;
#   print "unify_line: '$$lineref' -> '$data'\n" if($data=~/Ir/);
    $$lineref=$data;
}

sub unescape_special_characters {
    my($str)=@_;
    my $newstr=$str;
    $newstr=~s/\&eq;/=/gs;
    $newstr=~s/\&ne;/!=/gs;
    $newstr=~s/\&lt;/</gs;
    $newstr=~s/\&le;/<=/gs;
    $newstr=~s/\&gt;/>/gs;
    $newstr=~s/\&ge;/>=/gs;
    return($newstr);
}


sub escape_special_characters {
    my($str)=@_;
    my $newstr=$str;
    $newstr=~s/"="/"&eq;"/gs;
    $newstr=~s/"!="/"&ne;"/gs;
    $newstr=~s/"<"/"&lt;"/gs;
    $newstr=~s/"<="/"&le;"/gs;
    $newstr=~s/">"/"&gt;"/gs;
    $newstr=~s/">="/"&ge;"/gs;
    return($newstr);
}

#***************************************************************************
# Takes a string and escapes all special characters for usage in XML data.
# The returned string can be used as attribute value in a valid XML file.
#
# @param $_[0] string with special characters
#
# @return passed string with escaped XML special characters
#***************************************************************************
sub escapeForXML{
	my $result = shift;
	$result =~ s/&/&amp;/g;
	$result =~ s/</&lt;/g;
	$result =~ s/>/&gt;/g;
	$result =~ s/"/&quot;/g;
	$result =~ s/'/&apos;/g;
	
	return $result;
}

1;
