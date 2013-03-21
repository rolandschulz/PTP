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
use LML_da_util;

sub new {
    my $self    = {};
    my $proto   = shift;
    my $class   = ref($proto) || $proto;
    my $verbose = shift;
    my $timings = shift;
    printf(STDERR "\t LML_file_obj: new %s\n",ref($proto)) if($debug>=3);
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
#
#                 {REQUEST}->{attr}->{$key}
#                          ->{driver}->{attr}->{name}
#                                    ->{command}->{$name}->{exec}
#                                                        ->{input}
#                          ->{layoutManagement}->{attr}->{$key}              
#
#                 {TABLELAYOUT}->{$id}->{id}
#                                     ->{gid}
#                                     ->{column}->{$cid}->{cid}
#                                                       ->{key}
#                                                       ->{pos}
#                                                       ->{width}
#                                                       ->{active}
#														->{pattern}=[ [include, regexp]|
#                                                               [exclude, regexp]|                
#                                                               [select,rel,value]  ...]
#                 {TABLE}->{$id}->{id}
#                               ->{title}
#                               ->{column}->{$cid}->{id}
#                                                 ->{name}
#                                                 ->{sort}
#                                                 
#                               ->{row}->{$id}->{cell}->{$cid}->{value}
#                                                             ->{cid}
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
#                 {SPLITLAYOUT}->{$id}->{id}
#                                     ->{elements}->[elref, elref, ...]
#                                       ... elref->{elname}  
#                                                ->{key}
#                                                ->{elements}->[elref, elref, ...]
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
	'xmlns:lml' => 'http://eclipse.org/ptp/lml',
	'version' => '1.0',
	'xsi:schemaLocation' => 'http://eclipse.org/ptp/lml http://eclipse.org/ptp/schemas/lgui.xsd'
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

    {
	my($type,%types,$id);
	if($self->{DATA}->{SPLITLAYOUT}) {
	    $log.=sprintf("splitlayout: total #%d\n",scalar keys(%{$self->{DATA}->{SPLITLAYOUT}}));
	}
    }

    return($log);
} 

sub read_lml_fast {
    my($self) = shift;
    my $infile  = shift;
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
    printf(STDERR "LML_file_obj: read  XML in %6.4f sec\n",$tdiff) if($self->{VERBOSE});

    if(!$xmlin) {
	print STDERR "$0: ERROR: empty file $infile, leaving ...\n";return(0);
    }


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
	    $ctag.="\>";
	    next;
	}
	

	# comment
	next if($tag =~ /\!\-\-/);

#	print "TAG: '$tag'\n";
	if($tag=~/^<[\/\?](.*[^\s\>])/) {
	    $tagname=$1;
#	    print "TAGE: '$tagname'\n";
	    $self->lml_end($self->{DATA},$tagname,());
	} elsif($tag=~/<([^\s\/]+)\s*$/) {
	    $tagname=$1;
#	    print "TAG0: '$tagname'\n";
	    $self->lml_start($self->{DATA},$tagname,());
	} elsif($tag=~/<([^\s]+)(\s(.*)[^\/])$/) {
	    $tagname=$1;
	    $rest=$2;$rest=~s/^\s*//gs;$rest=~s/\s*$//gs;$rest=~s/\=\s+\"/\=\"/gs;$rest=~s/\s+\=\"/\=\"/gs;
	    $rest=&LML_da_util::escape_special_characters($rest) if($tagname=~/(select)/);
#	    print "TAG1: '$tagname' rest='$rest'\n";
	    $self->lml_start($self->{DATA},$tagname,split(/=?\"\s*/,$rest));
	} elsif($tag=~/<([^\s\/]+)(\s(.*)\s?)\/$/) {
	    $tagname=$1;
	    $rest=$2;$rest=~s/^\s*//gs;$rest=~s/\s*$//gs;$rest=~s/\=\s+\"/\=\"/gs;$rest=~s/\s+\=\"/\=\"/gs;
#	    print "TAG2: '$tagname' rest='$rest' closed\n";
	    $rest=&LML_da_util::escape_special_characters($rest) if($tagname=~/(select)/);
	    $self->lml_start($self->{DATA},$tagname,split(/=?\"\s*/,$rest));
	    $self->lml_end($self->{DATA},$tagname,());
	} elsif($tag=~/<([^\s\/]+)\/$/) {
	    $tagname=$1;
	    $rest="";
#	    print "TAG2e: '$tagname' rest='$rest' closed\n";
	    $self->lml_start($self->{DATA},$tagname,split(/=?\"\s*/,$rest));
	    $self->lml_end($self->{DATA},$tagname,());
	}
    }

    $tdiff=time-$tstart;
    printf(STDERR "LML_file_obj: parse XML in %6.4f sec\n",$tdiff) if($self->{VERBOSE});

#    print Dumper($self->{DATA});
    return($rc);

}

sub lml_start {
    my $self=shift; # object reference
    my $o   =shift;
    my $name=shift;
    my($k,$v,$actnodename,$id,$cid,$oid);


    if($name eq "!--") {
	# a comment
	return(1);
    }
    my %attr=(@_);

#   print "LML_file_obj: lml_start >$name< ",Dumper(\%attr),"\n";

	if($name =~ /(.*):layout/) {
	foreach $k (sort keys %attr) {
	    $o->{LMLLGUI}->{$k}=$attr{$k};
	}
	return(1);
    }

    if($name eq "lml:lgui") {
	foreach $k (sort keys %attr) {
	    $o->{LMLLGUI}->{$k}=$attr{$k};
	}
	return(1);
    }

    if($name =~/(.*):lgui/) {
	foreach $k (sort keys %attr) {
	    $o->{LMLLGUI}->{$k}=$attr{$k};
	}
    }

    if($name eq "ns2:lguiType") {
	foreach $k (sort keys %attr) {
	    $o->{LMLLGUI}->{$k}=$attr{$k};
	}
	return(1);
    }

    if($name eq "ns2:lgui") {
	foreach $k (sort keys %attr) {
	    $o->{LMLLGUI}->{$k}=$attr{$k};
	}
	return(1);
    }


# general tags, used in more than one tags
###########################################################################################
    if($name eq "data") {
	if($o->{LASTINFOID}) {
	    $id=$o->{LASTINFOID};
	    $k=$attr{key};
	    $v=$attr{value};
	    if(exists($o->{INFODATA}->{$id}->{$k})) {
		print STDERR "LML_file_obj: WARNING infodata with id >$id< and key >$k< exists, skipping\n";
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
	return(1);
    }

# handling request tags
###########################################################################################
    if($name eq "request") {
	foreach $k (sort keys %attr) {
	    $o->{REQUEST}->{attr}->{$k}=$attr{$k};
	}
	return(1);
    }
    if($name eq "layoutManagement") {
	foreach $k (sort keys %attr) {
	    $o->{REQUEST}->{layoutManagement}->{attr}->{$k}=$attr{$k};
	}
	return(1);
    }
    if($name eq "driver") {
	foreach $k (sort keys %attr) {
	    $o->{REQUEST}->{driver}->{attr}->{$k}=$attr{$k};
	}
	return(1);
    }
    if($name eq "command") {
	if(exists($attr{name})) {
	    my $cmdname=$attr{name};
	    foreach $k (sort keys %attr) {
		$o->{REQUEST}->{driver}->{command}->{$cmdname}->{$k}=$attr{$k};
	    }
	}
	return(1);
    }

# handling objects tags
###########################################################################################
    if($name eq "objects") {
	return(1);
    }
    if($name eq "object") {
	$id=$attr{id};
	if(exists($o->{OBJECT}->{$id})) {
	    print STDERR "LML_file_obj: WARNING objects with id >$id< exists, skipping\n";
	    return(0);
    	}
	foreach $k (sort keys %attr) {
	    $o->{OBJECT}->{$id}->{$k}=$attr{$k};
	}
	return(1);
    }

# handling information tags
###########################################################################################
    if($name eq "information") {
	return(1);
    }
    if($name eq "info") {
	$oid=$attr{oid};
	$o->{LASTINFOID}=$oid;
	$o->{LASTINFOTYPE}=$o->{OBJECT}->{$oid}->{type};
	if(exists($o->{INFO}->{$oid})) {
	    print STDERR "LML_file_obj: WARNING info with id >$id< exists, skipping\n";
	    return(0);
    	}
	foreach $k (sort keys %attr) {
#	    print "$k: $attr{$k}\n";
	    $o->{INFO}->{$oid}->{$k}=$attr{$k};
	}
	return(1);
    }

# handling tables
###########################################################################################
    if($name eq "table") {
	$id=$attr{id};
	$o->{LASTTABLEID}=$id;
	if(exists($o->{TABLE}->{$id})) {
	    print STDERR "LML_file_obj: WARNING Table with id >$id< exists, skipping\n";
	    return(0);
    	}
	foreach $k (sort keys %attr) {
	    $o->{TABLE}->{$id}->{$k}=$attr{$k};
	}
	return(1);
    }
    if($name eq "tablelayout") {
	$id=$attr{id};
	$o->{LASTTABLELAYOUTID}=$id;
	if(exists($o->{TABLELAYOUT}->{$id})) {
	    print STDERR "LML_file_obj: WARNING Tablelayout with id >$id< exists, skipping\n";
	    return(0);
    	}
	foreach $k (sort keys %attr) {
	    $o->{TABLELAYOUT}->{$id}->{$k}=$attr{$k};
	}
	return(1);
    }
    if($name eq "column") {
	if($o->{LASTTABLEID}) {
	    $id=$o->{LASTTABLEID};
	    $cid=$attr{id};
	    $o->{LASTCOLUMNID}=$cid;
	    $v=$attr{value};
	    if(exists($o->{TABLE}->{$id}->{column}->{$cid})) {
		print STDERR "LML_file_obj: WARNING column in table with id >$cid<  exists, skipping\n";
		return(0);
	    }
	    foreach $k (sort keys %attr) {
		$o->{TABLE}->{$id}->{column}->{$cid}->{$k}=$attr{$k};
	    }
	}
	if($o->{LASTTABLELAYOUTID}) {
	    $id=$o->{LASTTABLELAYOUTID};
	    $cid=$attr{cid};
	    $o->{LASTCOLUMNID}=$cid;
	    $v=$attr{value};
	    if(exists($o->{TABLELAYOUT}->{$id}->{column}->{$cid})) {
		print STDERR "LML_file_obj: WARNING column in tablelayout with id >$cid<  exists, skipping\n";
		return(0);
	    }
	    foreach $k (sort keys %attr) {
		$o->{TABLELAYOUT}->{$id}->{column}->{$cid}->{$k}=$attr{$k};
	    }
	}
	return(1);
    }
    if($name eq "pattern") {
	if($o->{LASTTABLELAYOUTID}) {
	    $id=$o->{LASTTABLELAYOUTID};
	    if($o->{LASTCOLUMNID}) {
		$cid=$o->{LASTCOLUMNID};
		$o->{LASTPATTERNLIST}=$o->{TABLELAYOUT}->{$id}->{column}->{$cid}->{pattern}=[];
	    }
    	}
	return(1);
    }
    if($name eq "exclude") {
	if($o->{LASTPATTERNLIST}) {
	    if(exists($attr{'regexp'})) {
		push(@{$o->{LASTPATTERNLIST}},['exclude',$attr{'regexp'}]);
	    }
	}
	return(1);
    }
    if($name eq "include") {
	if($o->{LASTPATTERNLIST}) {
	    if(exists($attr{'regexp'})) {
		push(@{$o->{LASTPATTERNLIST}},['include',$attr{'regexp'}]);
	    }
	}
	return(1);
    }
    if($name eq "select") {
	if($o->{LASTPATTERNLIST}) {
	    if(exists($attr{'rel'}) && (exists($attr{'value'}))) {
		push(@{$o->{LASTPATTERNLIST}},['select',$attr{'rel'},$attr{'value'}]);
	    }
	}
	return(1);
    }
    
# handling nodedisplays
###########################################################################################
    if($name eq "nodedisplay") {
	$id=$attr{id};
	$o->{LASTNODEDISPLAYID}=$id;
	if(exists($o->{NODEDISPLAY}->{$id})) {
	    print STDERR "LML_file_obj: WARNING Nodedisplay with id >$id< exists, skipping\n";
	    return(0);
    	}
	foreach $k (sort keys %attr) {
	    $o->{NODEDISPLAY}->{$id}->{$k}=$attr{$k};
	}
	return(1);
    }
    if($name eq "nodedisplaylayout") {
	$id=$attr{id};
	$o->{LASTNODEDISPLAYLAYOUTID}=$id;
	if(exists($o->{NODEDISPLAYLAYOUT}->{$id})) {
	    print STDERR "LML_file_obj: WARNING Nodedisplaylayout with id >$id< exists, skipping\n";
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
    if($name eq "scheme") {
	if($o->{LASTNODEDISPLAYID}) {
	    $id=$o->{LASTNODEDISPLAYID};
	    $o->{NODEDISPLAY}->{$id}->{schemeroot}=LML_ndtree->new("schemeroot");
	    $o->{NODEDISPLAY}->{$id}->{schemeroot}->{_level}=-1;
	    push(@{$o->{NODEDISPLAYSTACK}},$o->{NODEDISPLAY}->{$id}->{schemeroot});
	}
	return(1);
    }
    #Read scheme hint within nodedisplaylayout
    if($name eq "schemehint") {
	if($o->{LASTNODEDISPLAYLAYOUTID}) {
	    $id=$o->{LASTNODEDISPLAYLAYOUTID};
	    $o->{NODEDISPLAYLAYOUT}->{$id}->{schemehint}=LML_ndtree->new("schemeroot");
	    $o->{NODEDISPLAYLAYOUT}->{$id}->{schemehint}->{_level}=-1;
	    push(@{$o->{NODEDISPLAYSTACK}},$o->{NODEDISPLAYLAYOUT}->{$id}->{schemehint});
	}
	return(1);
    }
    if(($name=~/el\d/) || ($name eq 'img') ) {
	my $lastelem=$o->{NODEDISPLAYSTACK}->[-1];
	my $treenode=$lastelem->new_child(\%attr,$name);
	push(@{$o->{NODEDISPLAYSTACK}},$treenode);
	return(1);
    }

# handling splitlayout (needed at least for java appl.)
###########################################################################################
    if($name eq "splitlayout") {
	$id=$attr{id};
	$o->{LASTSPLITLAYOUTID}=$id;
	if(exists($o->{SPLITLAYOUT}->{$id})) {
	    print STDERR "LML_file_obj: WARNING splitlayout with id >$id< exists, skipping\n";
	    return(0);
    	}
	foreach $k (sort keys %attr) {
	    $o->{SPLITLAYOUT}->{$id}->{$k}=$attr{$k};
	}
	$o->{SPLITLAYOUT}->{$id}->{tree}=LML_ndtree->new("splitlayout");
	$o->{SPLITLAYOUT}->{$id}->{tree}->{_level}=-1;
	push(@{$o->{SPLITLAYOUTSTACK}},$o->{SPLITLAYOUT}->{$id}->{tree});
	return(1);
    }
    if(($name=~/top/) || ($name eq 'bottom') || ($name=~/left/) || ($name eq 'right') ) {
	my $lastelem=$o->{SPLITLAYOUTSTACK}->[-1];
	my $treenode=$lastelem->new_child(\%attr,$name);
	push(@{$o->{SPLITLAYOUTSTACK}},$treenode);
	return(1);
    }

# handling unused / not needed tags
###########################################################################################
    if($name eq "abslayout") {
	return(1);
    }
    if($name eq "comp") {
	return(1);
    }

    # unknown element
    print STDERR "LML_file_obj: WARNING unknown tag >$name< \n";
   
}

sub lml_end {
    my $self=shift; # object reference
    my $o   =shift;
    my $name=shift;
#    print STDERR "LML_file_obj: lml_end >$name< \n";

    if($name=~/data/) {
	if(!$self->{LASTINFOID}) {
	    pop(@{$o->{NODEDISPLAYSTACK}});
	}
    }

    if(($name=~/el\d/) || ($name eq 'img') || ($name eq 'scheme')) {
	pop(@{$o->{NODEDISPLAYSTACK}});
    }
    if($name=~/nodedisplaylayout/) {
	pop(@{$o->{NODEDISPLAYSTACK}});
    }
    if($name=~/schemehint/) {
	pop(@{$o->{NODEDISPLAYSTACK}});
    }
    if($name=~/nodedisplay/) {
	pop(@{$o->{NODEDISPLAYSTACK}});
	$o->{LASTNODEDISPLAYID} = undef;
    }
    if($name=~/table/) {
	$o->{LASTTABLEID} = undef;
    }
    if($name=~/tablelayout/) {
	$o->{LASTTABLELAYOUTID} = undef;
    }
    if($name=~/column/) {
	$o->{LASTCOLUMNID} = undef;
    }
    if($name=~/pattern/) {
	$o->{LASTPATTERNLIST} = undef;
    }
    if($name=~/info/) {
	$o->{LASTINFOID} = undef;
    }
    if($name=~/splitlayout/) {
	pop(@{$o->{SPLITLAYOUTSTACK}});
	$o->{LASTSPLITLAYOUTID} = undef;
#	print STDERR Dumper($o->{SPLITLAYOUT});
    }
    if( ($name=~/top/) || ($name eq 'bottom') || ($name=~/left/) || ($name eq 'right') ) {
	pop(@{$o->{SPLITLAYOUTSTACK}});
    }

}


sub write_lml {
    my($self) = shift;
    my $outfile  = shift;
    my($k,$rc,$id,$c,$key,$ref,$t);
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
	$t=0;
	foreach $k (sort keys %{$self->{DATA}->{OBJECT}->{$id}}) {
	    printf(OUT " %s=\"%s\"",$k,$self->{DATA}->{OBJECT}->{$id}->{$k});$t=1;
	}
	printf(OUT "/>\n");
	print "write_lml: WARNING object with id '$id' has no attributes\n" if($t==0);
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
	    printf(OUT "<table ");
	    for $key ("id", "gid","name","contenttype","description","title") {
		printf(OUT " %s=\"%s\"",$key,  $table->{$key}) if (exists($table->{$key}) && defined($table->{$key}));
	    }
	    printf(OUT ">\n");

 	    foreach $k (sort keys %{$table->{column}}) {
#		print STDERR "$id $k ",Dumper($table->{column}->{$k});
		printf(OUT "<column");
		for $key ("id","name","sort","description","type") {
		    printf(OUT " %s=\"%s\"",$key,  $table->{column}->{$k}->{$key}) if (exists($table->{column}->{$k}->{$key}));
		}
		printf(OUT "/>\n");
	    }
 	    foreach $k (sort keys %{$table->{row}}) {
		printf(OUT "<row  %s=\"%s\">\n","oid",$k);
		foreach $c (sort {$a <=> $b} keys %{$table->{row}->{$k}->{cell}}) {
			if (exists($table->{row}->{$k}->{cell}->{$c}->{cid})) {
				printf(OUT "<cell %s=\"%s\" %s=\"%s\"/>\n","value",$table->{row}->{$k}->{cell}->{$c}->{value},"cid",$table->{row}->{$k}->{cell}->{$c}->{cid});
			} else {
				printf(OUT "<cell %s=\"%s\"/>\n","value",$table->{row}->{$k}->{cell}->{$c}->{value});
			}
		}
		printf(OUT "</row>\n");
	    }
	    printf(OUT "</table>\n");
	}
    }

    if(exists($self->{DATA}->{TABLELAYOUT})) {

	foreach $id (sort keys %{$self->{DATA}->{TABLELAYOUT}}) {
	    my $tablelayout=$self->{DATA}->{TABLELAYOUT}->{$id};
	    printf(OUT "<tablelayout ");
	    for $key ("id","gid","active","contenthint") {
		printf(OUT " %s=\"%s\"",$key,  $tablelayout->{$key}) if (exists($tablelayout->{$key}) && defined($tablelayout->{$key}));
	    }
	    printf(OUT ">\n");
 	    foreach $k (sort {$a <=> $b} keys %{$tablelayout->{column}}) {
		printf(OUT "<column");
		for $key ("cid","pos","sorted","width","active","key") {
		    if(exists($tablelayout->{column}->{$k}->{$key})) {
			printf(OUT " %s=\"%s\"",$key,  $tablelayout->{column}->{$k}->{$key});
		    }
		}
		if(exists($tablelayout->{column}->{$k}->{pattern})) {
		    printf(OUT ">\n");
		    printf(OUT " <pattern>\n");
		    foreach $ref (@{$tablelayout->{column}->{$k}->{pattern}}) {
			printf(OUT " <%s regexp=\"%s\"/>\n",$ref->[0],$ref->[1]) if (($ref->[0] eq "include") || ($ref->[0] eq "exclude") );
			printf(OUT " <%s rel=\"%s\" value=\"%s\"/>\n",
			       $ref->[0],
			       &LML_da_util::unescape_special_characters($ref->[1]),
			       ,$ref->[2]) if (($ref->[0] eq "select") );
		    }
		    
		    printf(OUT " </pattern>\n");
		    printf(OUT "</column>\n");
		} else {
		    printf(OUT "/>\n");
		}
	    }
	    printf(OUT "</tablelayout>\n");
	}
    }


    if(exists($self->{DATA}->{NODEDISPLAYLAYOUT})) {

	foreach $id (sort keys %{$self->{DATA}->{NODEDISPLAYLAYOUT}}) {
	    my $ndlayout=$self->{DATA}->{NODEDISPLAYLAYOUT}->{$id};
	    printf(OUT "<nodedisplaylayout ");
	    for $key ("id","gid","active") {
		printf(OUT " %s=\"%s\"",$key,  $ndlayout->{$key}) if (exists($ndlayout->{$key}));
	    }
	    printf(OUT ">\n");
	    if(defined($ndlayout->{schemehint}) ){
	    	print OUT "<schemehint>\n";
	    	print OUT $ndlayout->{schemehint}->get_xml_tree(1);
	    	print OUT "</schemehint>\n";
	    }
	    print OUT $ndlayout->{tree}->get_xml_tree(0);
	    printf(OUT "</nodedisplaylayout>\n");
	}
    }

    if(exists($self->{DATA}->{NODEDISPLAY})) {

	foreach $id (sort keys %{$self->{DATA}->{NODEDISPLAY}}) {
	    my $nd=$self->{DATA}->{NODEDISPLAY}->{$id};
	    printf(OUT "<nodedisplay id=\"%s\" title=\"%s\">\n", $nd->{id}, $nd->{title});
	    if(exists($nd->{schemeroot})) {
		print OUT "<scheme>\n";
		print OUT $nd->{schemeroot}->get_xml_tree(1);
		print OUT "</scheme>\n";
	    }
	    if(exists($nd->{dataroot})) {
		print OUT "<data>\n";
		print OUT $nd->{dataroot}->get_xml_tree(1);
		print OUT "</data>\n";
	    }
	    printf(OUT "</nodedisplay>\n");
	}
    }

    if(exists($self->{DATA}->{SPLITLAYOUT})) {

	foreach $id (sort keys %{$self->{DATA}->{SPLITLAYOUT}}) {
	    my $sl=$self->{DATA}->{SPLITLAYOUT}->{$id};
	    my $attr="";
	    if (exists($sl->{divpos})) {
		$attr.="divpos=\"$sl->{divpos}\"";
	    };
	    printf(OUT "<splitlayout id=\"%s\" %s>\n", $sl->{id}, $attr);
	    if(exists($sl->{tree})) {
		print OUT $sl->{tree}->get_xml_tree(1);
	    }
	    printf(OUT "</splitlayout>\n");

	}
    }
    
    printf(OUT "</lml:lgui>\n");
    
    close(OUT);

    my $tdiff=time-$tstart;
    printf(STDERR "LML_file_obj: wrote  XML in %6.4f sec to %s\n",$tdiff,$outfile) if($self->{VERBOSE});
    
    return($rc);
    
}

sub check_lml {
    my($self) = shift;
   
    {
	my($type,%types,$id);
	if($self->{DATA}->{TABLELAYOUT}) {
	    foreach $id (keys %{$self->{DATA}->{TABLELAYOUT}}) {
		$self->_check_lml_tablelayout_width($self->{DATA}->{TABLELAYOUT}->{$id});
		$self->_check_lml_tablelayout_pos($self->{DATA}->{TABLELAYOUT}->{$id});
	    }
	}
    }
    return(1);
} 

sub _check_lml_tablelayout_width {
    my($self) = shift;
    my($tlayoutref) = @_;
    my($cid, $numcolumns, $wsum, $wsumweight);

    $numcolumns=scalar keys(%{$tlayoutref->{column}});
    
    $wsum=0.0;
    foreach $cid (sort {$a <=> $b} (keys(%{$tlayoutref->{column}}))) {
	next if($tlayoutref->{column}->{$cid}->{active} eq "false");
	$tlayoutref->{column}->{$cid}->{width}=1.0 if(!exists($tlayoutref->{column}->{$cid}->{width}));
	$tlayoutref->{column}->{$cid}->{width}=1.0 if($tlayoutref->{column}->{$cid}->{width}<=0);
	$wsum+=$tlayoutref->{column}->{$cid}->{width};
    }
    if($wsum>0)  {$wsumweight=1.0/$wsum;}
    else         {$wsumweight=1.0;}
    foreach $cid (sort {$a <=> $b} (keys(%{$tlayoutref->{column}}))) {
	next if($tlayoutref->{column}->{$cid}->{active} eq "false");
	$tlayoutref->{column}->{$cid}->{width}*=$wsumweight;
    }
    
    return(1);
} 

sub _check_lml_tablelayout_pos {
    my($self) = shift;
    my($tlayoutref) = @_;
    my($cid, $numcolumns, $pos);

    $numcolumns=scalar keys(%{$tlayoutref->{column}});
    
    $pos=0;
    foreach $cid (sort {&_sort_tlayout_pos($tlayoutref,$a,$b)} (keys(%{$tlayoutref->{column}}))) {
	$tlayoutref->{column}->{$cid}->{pos}=$pos;
	$pos++;
    }
   
    return(1);
} 


sub _sort_tlayout_pos {
    my($tlayoutref,$aa,$bb)=@_;

    # pos attribute
    my $apos=1e20;
    my $bpos=1e20;
    $apos=$tlayoutref->{column}->{$aa}->{pos} if(exists($tlayoutref->{column}->{$aa}->{pos}));
    $bpos=$tlayoutref->{column}->{$bb}->{pos} if(exists($tlayoutref->{column}->{$bb}->{pos}));

    # active attribute
    my $aactive="false";
    my $bactive="false";
    $aactive=$tlayoutref->{column}->{$aa}->{active} if(exists($tlayoutref->{column}->{$aa}->{active}));
    $bactive=$tlayoutref->{column}->{$bb}->{active} if(exists($tlayoutref->{column}->{$bb}->{active}));

    if($apos != $bpos) {
	return($apos <=> $bpos);
    } else {
	if($aactive ne $bactive) {
	    return($aactive cmp $bactive);	    
	} else {
	    return($aa <=> $bb);	    
	}
    }
}
    
1;
