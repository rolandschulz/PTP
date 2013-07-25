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
package LML_combine_file_obj;

my($debug)=0;

use strict;
use lib "$FindBin::RealBin/lib";
use Data::Dumper;
use Time::Local;
use Time::HiRes qw ( time );


sub new {
    my $self    = {};
    my $proto   = shift;
    my $class   = ref($proto) || $proto;
    my $verbose = shift;
    my $timings = shift;
    printf("\t LML_combine_file_obj: new %s\n",ref($proto)) if($debug>=3);
    $self->{DATA}      = {};
    $self->{VERBOSE}   = $verbose; 
    $self->{TIMINGS}   = $timings; 
    $self->{LASTINFOID} = undef;
    bless $self, $class;
    return $self;
}

sub get_data_ref {
    my($self) = shift;
    return($self->{DATA});
} 

sub get_stat {
    my($self) = shift;
    my($log,$type,%types,$id);
    $log="";
    
    $log.=sprintf("objects: total #%d\n",scalar keys(%{$self->{DATA}->{OBJECT}}));
    foreach $id (keys %{$self->{DATA}->{OBJECT}}) {
	$type=$self->{DATA}->{OBJECT}->{$id}->{type};
	$types{$type}++;
    }
    foreach $type (sort keys %types) {
	$log.=sprintf("        |-- %10d (%s)\n",$types{$type},$type);
    }
    return($log);
} 

sub read_lml_fast {
    my($self) = shift;
    my $infile  = shift;
    my $type    = shift;
    my($xmlin);
    my $rc=0;

    my $tstart=time;
    if(!open(IN,$infile)) {
	print "could not open $infile, leaving ...\n";return(0);
    }
    while(<IN>) {
	$xmlin.=$_;
    }
    close(IN);
    my $tdiff=time-$tstart;
    printf("LML_file_obj: read  XML in %6.4f sec\n",$tdiff) if($self->{VERBOSE});

    $self->{DATA}->{SEARCHTYPE}=$type;
    $tstart=time;

    # light-weight self written xml parser, only working for simple XML files  
    $xmlin=~s/\n/ /gs;
    $xmlin=~s/\s\s+/ /gs;
    my ($tag,$tagname,$rest,$ctag,$nrc);
    foreach $tag (split(/\>\s*/,$xmlin)) {
	$ctag.=$tag;
	$nrc=($ctag=~ tr/\"/\"/);
	if($nrc%2==0) {
	    $tag=$ctag;
	    $ctag="";
	} else {
	    next;
	}
	
#	print "TAG: '$tag'\n";
	if($tag=~/^<[\/\?](.*)[^\s\>]/) {
	    $tagname=$1;
	    $self->lml_end($self->{DATA},$tagname,());
	} elsif($tag=~/<([^\s]+)\s*$/) {
	    $tagname=$1;
#	    print "TAG0: '$tagname'\n";
	    $self->lml_start($self->{DATA},$tagname,());
	} elsif($tag=~/<([^\s]+)(\s(.*)[^\/])$/) {
	    $tagname=$1;
	    $rest=$2;$rest=~s/^\s*//gs;$rest=~s/\s*$//gs;
#	    print "TAG1: '$tagname' rest='$rest'\n";
	    $self->lml_start($self->{DATA},$tagname,split(/=?\"\s*/,$rest));
	} elsif($tag=~/<([^\s]+)(\s(.*)\s?)\/$/) {
	    $tagname=$1;
	    $rest=$2;$rest=~s/^\s*//gs;$rest=~s/\s*$//gs;
#	    print "TAG2: '$tagname' rest='$rest' closed\n";
	    $self->lml_start($self->{DATA},$tagname,split(/=?\"\s*/,$rest));
	    $self->lml_end($self->{DATA},$tagname,());
	}
    }

    $tdiff=time-$tstart;
    printf("LML_file_obj: parse XML in %6.4f sec\n",$tdiff) if($self->{VERBOSE});

#    print Dumper($self->{DATA});
    return($rc);

}

sub lml_start {
    my $self=shift; # object reference
    my $o   =shift;
    my $name=shift;
#    print "WF: >",ref($o),"< >$name<\n";
    my($k,$v,$actnodename,$id);
    my %attr=(@_);

    if($name eq "lml:lgui") {
	foreach $k (sort keys %attr) {
	    $o->{LMLLGUI}->{$k}=$attr{$k};
	}
	return(1);
    }
    if($name eq "objects") {
	return(1);
    }
    if($name eq "information") {
	return(1);
    }
    if($name eq "object") {
	$id=$attr{id};
	if(exists($o->{OBJECT}->{$id})) {
	    print "LML_file_obj: WARNING objects with id >$id< exists, skipping\n";
	    return(0);
    	}
	foreach $k (sort keys %attr) {
#	    print "$k: $attr{$k}\n";
	    $o->{OBJECT}->{$id}->{$k}=$attr{$k};
	}
	return(1);
    }
    if($name eq "info") {
	$id=$attr{oid};
	$o->{LASTINFOID}=$id;
	if(exists($o->{INFO}->{$id})) {
	    print "LML_file_obj: WARNING info with id >$id< exists, skipping\n";
	    return(0);
    	}
	foreach $k (sort keys %attr) {
#	    print "$k: $attr{$k}\n";
	    $o->{INFO}->{$id}->{$k}=$attr{$k};
	}
	return(1);
    }
    if($name eq "data") {
	$id=$o->{LASTINFOID};
	$k=$attr{key};
	$v=$attr{value};

	if(exists($o->{INFODATA}->{$id}->{$k})) {
	    print "LML_file_obj: WARNING infodata with id >$id< and key >$k< exists, skipping\n";
	    return(0);
    	}
	$o->{INFODATA}->{$id}->{$k}=$v;
	return(1);
    }
    print "LML_file_obj: WARNING unknown tag >$name< \n";
   
}

sub lml_end {
    my $self=shift; # object reference
    my $name=shift;
}


sub write_lml {
    my($self) = shift;
    my $outfile  = shift;
    my($k,$rc,$id);
    my $tstart=time;
    $rc=1;

    open(OUT,"> $outfile") || die "cannot open file $outfile";
    printf(OUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

    printf(OUT "<lml:lgui ");
    foreach $k (sort keys %{$self->{DATA}->{LMLLGUI}}) {
	printf(OUT "%s=\"%s\"\n ",$k,$self->{DATA}->{LMLLGUI}->{$k});
    }
    printf(OUT "     \>\n");


    printf(OUT "<objects>\n");
    foreach $id (sort keys %{$self->{DATA}->{OBJECT}}) {
	printf(OUT "<object");
	foreach $k (sort keys %{$self->{DATA}->{OBJECT}->{$id}}) {
	    printf(OUT " %s=\"%s\"",$k,$self->{DATA}->{OBJECT}->{$id}->{$k});
	}
	printf(OUT "/>\n");
    }
    printf(OUT "</objects>\n");

    printf(OUT "<information>\n");
    foreach $id (sort keys %{$self->{DATA}->{INFO}}) {
	printf(OUT "<info");
	foreach $k (sort keys %{$self->{DATA}->{INFO}->{$id}}) {
	    printf(OUT " %s=\"%s\"",$k,$self->{DATA}->{INFO}->{$id}->{$k});
	}
	printf(OUT ">\n");
	foreach $k (sort keys %{$self->{DATA}->{INFODATA}->{$id}}) {
	    my $value = escapeForXML($self->{DATA}->{INFODATA}->{$id}->{$k});
	    printf(OUT "<data key=\"%s\" value=\"%s\"/>\n",$k,$value);
	}
	printf(OUT "</info>\n");
    }
    printf(OUT "</information>\n");

    printf(OUT "</lml:lgui>\n");

    close(OUT);

    my $tdiff=time-$tstart;
    printf("LML_file_obj: wrote  XML in %6.4f sec to %s\n",$tdiff,$outfile) if($self->{TIMINGS});

    return($rc);

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
	#Unescape characters, which were escaped in a previous step
	#This makes sure, that &amp; is not replaced by &amp;amp;
	$result =~ s/&amp;/&/g;
	$result =~ s/&lt;/</g;
	$result =~ s/&gt;/>/g;
	$result =~ s/&quot;/"/g;
	$result =~ s/&apos;/'/g;
	#Now escape all special characters in the result string
	$result =~ s/&/&amp;/g;
	$result =~ s/</&lt;/g;
	$result =~ s/>/&gt;/g;
	$result =~ s/"/&quot;/g;
	$result =~ s/'/&apos;/g;
	
	return $result;
}

1;
