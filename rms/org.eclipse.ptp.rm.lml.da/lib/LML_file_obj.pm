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
package LML_file_obj;

my($debug)=0;

use strict;
use Data::Dumper;
use Time::Local;
use Time::HiRes qw ( time );

use LML_ndtree;

sub new {
    my $self    = {};
    my $proto   = shift;
    my $class   = ref($proto) || $proto;
    my $verbose = shift;
    my $timings = shift;
    printf("\t LML_file_obj: new %s\n",ref($proto)) if($debug>=3);
    $self->{DATA}      = {};
    $self->{VERBOSE}   = $verbose; 
    $self->{TIMINGS}   = $timings; 
    $self->{LASTINFOID} = undef;
    bless $self, $class;
    return $self;
}

# internal data structures:
# $self->{DATA}->                                    # structure corresponds to LML scheme
#                 {OBJECT}->{$id}->{id}    
#                                ->{name}
#                                ->{type}
#                 {INFO}  ->{$oid}->{oid}  
#                                 ->{type}
#                 {INFODATA}->{$oid}->{$key}

#                 {REQUEST}->{$key}
#
#                 {TABLELAYOUT}->{$id}->{id}
#                                     ->{gid}
#                                     ->{column}->{$cid}->{cid}
#                                                       ->{key}
#                                                       ->{pos}
#                                                       ->{width}
#                                                       ->{active}
#                 {TABLE}->{$id}->{id}
#                               ->{title}
#                               ->{column}->{$cid}->{id}
#                                                ->{name}
#                                                ->{sort}
#                               ->{row}->{$id}->{cell}->[value,value,...]
#
#                 {NODEDISPLAYLAYOUT}->{$id}->{id}
#                                           ->{gid}
#                                           ->{elements}->[elref, elref, ...]
#                                   ... elref->{elname}  
#                                            ->{key}
#                                            ->{elements}->[elref, elref, ...]
#
#                 {NODEDISPLAY}->{$id}->{id}
#                                            ->{elements}->[elref, elref, ...]
#                                               ... elref->{elname}  
#                                                        ->{key}
#                                                        ->{elements}->[elref, elref, ...]
#
#
# derived:
#                 {INFOATTR}->{obj_type}->{$key}  # of occurrences
# 
sub get_data_ref {
    my($self) = shift;
    return($self->{DATA});
} 


sub init_file_obj {
    my($self) = shift;
    $self->{DATA}->{LMLLGUI}={
	'xmlns:xsi' => 'http://www.w3.org/2001/XMLSchema-instance',
	'xmlns:lml' => 'http://www.llview.de',
	'version' => '1.0',
	'xsi:schemaLocation' => 'http://www.llview.de lgui.xsd '
	};
    return(1);
} 

sub get_stat {
    my($self) = shift;
    my($log,$type,%types,$id);
    $log="";
    
    {
	my($type,%types,$id);
	$log.=sprintf("objects: total #%d\n",scalar keys(%{$self->{DATA}->{OBJECT}}));
	foreach $id (keys %{$self->{DATA}->{OBJECT}}) {
	    $type=$self->{DATA}->{OBJECT}->{$id}->{type};
	    $types{$type}++ if($type);
	}
	foreach $type (sort keys %types) {
	    $log.=sprintf("        |-- %10d (%s)\n",$types{$type},$type);
	}
    }

    {
	my($type,%types,$id);
	if($self->{DATA}->{TABLELAYOUT}) {
	    $log.=sprintf("tablelayout: total #%d\n",scalar keys(%{$self->{DATA}->{TABLELAYOUT}}));
	    foreach $id (keys %{$self->{DATA}->{TABLELAYOUT}}) {
		$log.=sprintf("        |--        1x%d (%s)\n",
			      scalar keys(%{$self->{DATA}->{TABLELAYOUT}->{$id}->{column}}),
			      $id);
	    }
	}
    }

    {
	my($type,%types,$id);
	if($self->{DATA}->{TABLE}) {
	    $log.=sprintf("table: total #%d\n",scalar keys(%{$self->{DATA}->{TABLE}}));
	    foreach $id (keys %{$self->{DATA}->{TABLE}}) {
		$log.=sprintf("        |--     %4dx%d (%s)\n",
			      scalar keys(%{$self->{DATA}->{TABLE}->{$id}->{row}}),
			      scalar keys(%{$self->{DATA}->{TABLE}->{$id}->{column}}),
			      $id);
	    }
	}
    }

    {
	my($type,%types,$id);
	if($self->{DATA}->{NODEDISPLAYLAYOUT}) {
	    $log.=sprintf("nodedisplaylayout: total #%d\n",scalar keys(%{$self->{DATA}->{NODEDISPLAYLAYOUT}}));
	}
    }

    {
	my($type,%types,$id);
	if($self->{DATA}->{NODEDISPLAY}) {
	    $log.=sprintf("nodedisplay: total #%d\n",scalar keys(%{$self->{DATA}->{NODEDISPLAY}}));
	}
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
	print STDERR "$0: ERROR: could not open $infile, leaving ...\n";return(0);
    }
    while(<IN>) {
	$xmlin.=$_;
    }
    close(IN);
    my $tdiff=time-$tstart;
    printf("LML_file_obj: read  XML in %6.4f sec\n",$tdiff) if($self->{VERBOSE});

    if(!$xmlin) {
	print STDERR "$0: ERROR: empty file $infile, leaving ...\n";return(0);
    }


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
	if($tag=~/^<[\/\?](.*[^\s\>])/) {
	    $tagname=$1;
#	    print "TAGE: '$tagname'\n";
	    $self->lml_end($self->{DATA},$tagname,());
	} elsif($tag=~/<([^\s]+)\s*$/) {
	    $tagname=$1;
#	    print "TAG0: '$tagname'\n";
	    $self->lml_start($self->{DATA},$tagname,());
	} elsif($tag=~/<([^\s]+)(\s(.*)[^\/])$/) {
	    $tagname=$1;
	    $rest=$2;$rest=~s/^\s*//gs;$rest=~s/\s*$//gs;$rest=~s/\=\s+\"/\=\"/gs;
#	    print "TAG1: '$tagname' rest='$rest'\n";
	    $self->lml_start($self->{DATA},$tagname,split(/=?\"\s*/,$rest));
	} elsif($tag=~/<([^\s]+)(\s(.*)\s?)\/$/) {
	    $tagname=$1;
	    $rest=$2;$rest=~s/^\s*//gs;$rest=~s/\s*$//gs;$rest=~s/\=\s+\"/\=\"/gs;
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


# from lib/LLview_parse_xml.pm
sub lml_start {
    my $self=shift; # object reference
    my $o   =shift;
    my $name=shift;
    my($k,$v,$actnodename,$id,$cid,$oid);

#    print "LML_file_obj: lml_start >$name< \n";

    if($name eq "!--") {
	# a comment
	return(1);
    }
    my %attr=(@_);

    if($name eq "lml:lgui") {
	foreach $k (sort keys %attr) {
	    $o->{LMLLGUI}->{$k}=$attr{$k};
	}
	return(1);
    }
    # Objects
    if($name eq "objects") {
	return(1);
    }
    if($name eq "object") {
	$id=$attr{id};
	if(exists($o->{OBJECT}->{$id})) {
	    print "LML_file_obj: WARNING objects with id >$id< exists, skipping\n";
	    return(0);
    	}
	foreach $k (sort keys %attr) {
	    $o->{OBJECT}->{$id}->{$k}=$attr{$k};
	}
	return(1);
    }

    # Request
    if($name eq "request") {
	foreach $k (sort keys %attr) {
	    $o->{OBJECT}->{request}->{$k}=$attr{$k};
	}
	return(1);
    }

    # Information
    if($name eq "information") {
	return(1);
    }
    if($name eq "info") {
	$oid=$attr{oid};
	$o->{LASTINFOID}=$oid;
	$o->{LASTINFOTYPE}=$o->{OBJECT}->{$oid}->{type};
	if(exists($o->{INFO}->{$oid})) {
	    print "LML_file_obj: WARNING info with id >$id< exists, skipping\n";
	    return(0);
    	}
	foreach $k (sort keys %attr) {
#	    print "$k: $attr{$k}\n";
	    $o->{INFO}->{$oid}->{$k}=$attr{$k};
	}
	return(1);
    }
    if($name eq "data") {
	if($o->{LASTINFOID}) {
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
	if($o->{LASTNODEDISPLAYID}) {
	    $id=$o->{LASTNODEDISPLAYID};
	    $o->{NODEDISPLAY}->{$id}->{dataroot}=LML_ndtree->new("dataroot");
	    $o->{NODEDISPLAY}->{$id}->{dataroot}->{_level}=-1;
	    push(@{$o->{NODEDISPLAYSTACK}},$o->{NODEDISPLAY}->{$id}->{dataroot});
	}
    }
    if($name eq "scheme") {
	if($o->{LASTNODEDISPLAYID}) {
	    $id=$o->{LASTNODEDISPLAYID};
	    $o->{NODEDISPLAY}->{$id}->{schemeroot}=LML_ndtree->new("schemeroot");
	    $o->{NODEDISPLAY}->{$id}->{schemeroot}->{_level}=-1;
	    push(@{$o->{NODEDISPLAYSTACK}},$o->{NODEDISPLAY}->{$id}->{schemeroot});
	}
    }
    # Tablelayout
    if($name eq "tablelayout") {
	$id=$attr{id};
	$o->{LASTTABLELAYOUTID}=$id;
	if(exists($o->{TABLELAYOUT}->{$id})) {
	    print "LML_file_obj: WARNING Tablelayout with id >$id< exists, skipping\n";
	    return(0);
    	}
	foreach $k (sort keys %attr) {
#	    print "$k: $attr{$k}\n";
	    $o->{TABLELAYOUT}->{$id}->{$k}=$attr{$k};
	}
	return(1);
    }

    if($name eq "column") {
	$id=$o->{LASTTABLELAYOUTID};
	$cid=$attr{cid};
	$v=$attr{value};

	if(exists($o->{TABLELAYOUT}->{$id}->{column}->{$cid})) {
	    print "LML_file_obj: WARNING column in tablelayout with id >$cid<  exists, skipping\n";
	    return(0);
    	}
	foreach $k (sort keys %attr) {
#	    print "$k: $attr{$k}\n";
	    $o->{TABLELAYOUT}->{$id}->{column}->{$cid}->{$k}=$attr{$k};
	}
	return(1);
    }

    # nodedisplaylayout
    if($name eq "nodedisplay") {
	$id=$attr{id};
	$o->{LASTNODEDISPLAYID}=$id;
	if(exists($o->{NODEDISPLAY}->{$id})) {
	    print "LML_file_obj: WARNING Nodedisplay with id >$id< exists, skipping\n";
	    return(0);
    	}
	foreach $k (sort keys %attr) {
	    $o->{NODEDISPLAY}->{$id}->{$k}=$attr{$k};
	}
	return(1);
    }
    # nodedisplaylayout
    if($name eq "nodedisplaylayout") {
	$id=$attr{id};
	if(exists($o->{NODEDISPLAYLAYOUT}->{$id})) {
	    print "LML_file_obj: WARNING Nodedisplaylayout with id >$id< exists, skipping\n";
	    return(0);
    	}
	foreach $k (sort keys %attr) {
	    $o->{NODEDISPLAYLAYOUT}->{$id}->{$k}=$attr{$k};
	}
	$o->{NODEDISPLAYLAYOUT}->{$id}->{tree}=LML_ndtree->new("ndlytree");
	$o->{NODEDISPLAYLAYOUT}->{$id}->{tree}->{_level}=-1;
	push(@{$o->{NODEDISPLAYSTACK}},$o->{NODEDISPLAYLAYOUT}->{$id}->{tree});
	return(1);
    }
    if(($name=~/el\d/) || ($name eq 'img') ) {
	my $lastelem=$o->{NODEDISPLAYSTACK}->[-1];
	my $treenode=$lastelem->new_child(\%attr,$name);
	push(@{$o->{NODEDISPLAYSTACK}},$treenode);
	return(1);
    }
    print "LML_file_obj: WARNING unknown tag >$name< \n";
   
}

sub lml_end {
    my $self=shift; # object reference
    my $o   =shift;
    my $name=shift;
#    print "LML_file_obj: lml_end >$name< \n";

    if($name=~/data/) {
	if(!$self->{LASTINFOID}) {
	    pop(@{$o->{NODEDISPLAYSTACK}});
	}
    }

    if(($name=~/el\d/) || ($name eq 'img') || ($name eq 'scheme')) {
	pop(@{$o->{NODEDISPLAYSTACK}});
    }
    if($name=~/nodedisplaylayout\d/) {
	pop(@{$o->{NODEDISPLAYSTACK}});
    }
    if($name=~/nodedisplay\d/) {
	pop(@{$o->{NODEDISPLAYSTACK}});
	$o->{LASTNODEDISPLAYID} = undef;
    }
    if($name=~/info/) {
	$o->{LASTINFOID} = undef;
    }

#    print Dumper($o->{NODEDISPLAYSTACK});
}


sub write_lml {
    my($self) = shift;
    my $outfile  = shift;
    my($k,$rc,$id,$c,$key);
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
	    printf(OUT "<data key=\"%s\" value=\"%s\"/>\n",$k,$self->{DATA}->{INFODATA}->{$id}->{$k});
	}
	printf(OUT "</info>\n");
    }
    printf(OUT "</information>\n");

    if(exists($self->{DATA}->{TABLE})) {

	foreach $id (sort keys %{$self->{DATA}->{TABLE}}) {
	    my $table=$self->{DATA}->{TABLE}->{$id};
	    printf(OUT "<table title=\"%s\" id=\"%s\">\n", $table->{title}, $table->{id});
 	    foreach $k (sort keys %{$table->{column}}) {
		printf(OUT "<column");
		for $key ("id","name","sort") {
		    printf(OUT " %s=\"%s\"",$key,  $table->{column}->{$k}->{$key});
		}
		printf(OUT "/>\n");
	    }
 	    foreach $k (sort keys %{$table->{row}}) {
		printf(OUT "<row  %s=\"%s\">\n","oid",$k);
		foreach $c (@{$table->{row}->{$k}->{cell}}) {
		    printf(OUT "<cell %s=\"%s\"/>\n","value",$c);
		}
		printf(OUT "</row>\n");
	    }
	    printf(OUT "</table>\n");
	}
    }

    if(exists($self->{DATA}->{TABLELAYOUT})) {

	foreach $id (sort keys %{$self->{DATA}->{TABLELAYOUT}}) {
	    my $tablelayout=$self->{DATA}->{TABLELAYOUT}->{$id};
	    printf(OUT "<tablelayout id=\"%s\" gid=\"%s\">\n", $tablelayout->{id}, $tablelayout->{gid});
 	    foreach $k (sort {$a <=> $b} keys %{$tablelayout->{column}}) {
		printf(OUT "<column");
		for $key ("cid","pos","width","active","key") {
		    if(exists($tablelayout->{column}->{$k}->{$key})) {
			printf(OUT " %s=\"%s\"",$key,  $tablelayout->{column}->{$k}->{$key});
		    }
		}
		printf(OUT "/>\n");
	    }
	    printf(OUT "</tablelayout>\n");
	}
    }


    if(exists($self->{DATA}->{NODEDISPLAYLAYOUT})) {

	foreach $id (sort keys %{$self->{DATA}->{NODEDISPLAYLAYOUT}}) {
	    my $ndlayout=$self->{DATA}->{NODEDISPLAYLAYOUT}->{$id};
	    printf(OUT "<nodedisplaylayout id=\"%s\" gid=\"%s\">\n", $ndlayout->{id}, $ndlayout->{gid});
	    print OUT $ndlayout->{tree}->get_xml_tree(0);
	    printf(OUT "</nodedisplaylayout>\n");
	}
    }

    if(exists($self->{DATA}->{NODEDISPLAY})) {

	foreach $id (sort keys %{$self->{DATA}->{NODEDISPLAY}}) {
	    my $nd=$self->{DATA}->{NODEDISPLAY}->{$id};
	    printf(OUT "<nodedisplay id=\"%s\" title=\"%s\">\n", $nd->{id}, $nd->{title});
	    print OUT "<scheme>\n";
	    print OUT $nd->{schemeroot}->get_xml_tree(1);
	    print OUT "</scheme>\n";
	    print OUT "<data>\n";
	    print OUT $nd->{dataroot}->get_xml_tree(1);
	    print OUT "</data>\n";
	    printf(OUT "</nodedisplay>\n");
	}
    }
    
    printf(OUT "</lml:lgui>\n");
    
    close(OUT);

    my $tdiff=time-$tstart;
    printf("LML_file_obj: wrote  XML in %6.4f sec to %s\n",$tdiff,$outfile) if($self->{TIMINGS});
    
    return($rc);
    
}
    
1;
