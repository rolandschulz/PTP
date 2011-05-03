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
package LML_ndtree;
use strict;
use Time::Local;
use Time::HiRes qw ( time );
use Storable qw(dclone); 
use Data::Dumper;


sub new  {
    my $self  = {};
    my $proto = shift;
    my $class = ref($proto) || $proto;
    my($name) = shift;

    $self->{ATTR} = {};

    # init root elem
    $self->{_level}  = 0;
    $self->{_childs} = [];
    $name="el".$self->{_level} if(!$name);
    $self->{_name}  = $name;

    bless $self, $class;
    return $self;
}




sub add_attr  {
    my($self) = shift;
    my($attrhashref) = shift;
    my($id);

#    print "LML_ndtree: add_attr: ",join(",",%{$attrhashref}),"->",caller(),"\n"; 
    
    foreach $id (keys(%{$attrhashref})) {
	$self->{ATTR}->{$id}=$attrhashref->{$id};
    }
       
    return(1);
}


sub new_child  {
    my($self) = shift;
    my($attrhashref) = shift;
    my($name) = shift;
    my $treenode = LML_ndtree->new();

    $treenode->{_level}=$self->{_level}+1;
    $name="el".$treenode->{_level} if(!$name);
    $treenode->{_name}=$name;
    
    if($attrhashref) {
	$treenode->add_attr($attrhashref);
    }

    push(@{$self->{_childs}},$treenode);

    return($treenode);
}

sub duplicate_child  {
    my($self) = shift;
    my($child) = shift;
    my($newchild) = dclone($child);
  
    push(@{$self->{_childs}},$newchild);

    return($newchild);
}



sub insert_attr_into_tree  {
    my($self) = shift;
    my($tree) = shift;
    my($spec) = shift;
    my($attrhashref) = shift;
    my($id);

    print "WF: insert_attr_into_tree: $spec, ",join(",",%{$attrhashref}),"\n"; 
    
    return(1);
}


sub remove_child  {
    my($self) = shift;
    my($child) = shift;
    my($childnr);

    
    for ($childnr=0; $childnr<=$#{$self->{_childs}};$childnr++) {
	if($self->{_childs}->[$childnr] eq $child) {
	    last;
	}
    }
    splice(@{$self->{_childs}},$childnr,1);
    return(1);
}


sub copy_tree {
    my($self) = shift;
    my($sourcetree) = @_;
    my($child,$subtree);
    my $rc=1;

    # copy name
    $self->{_name}=$sourcetree->{_name};

    # copy attributes
    $self->add_attr($sourcetree->{ATTR});
    
    # dive in
    foreach $child (@{$sourcetree->{_childs}}) {
	$subtree=$self->new_child();
	$subtree->copy_tree($child);
    }
	
    return($rc);
}

sub get_xml_tree {
    my($self) = shift;
    my($fromlevel,$tolevel) = @_;
    my($id,$subid,$elname,$xmldata,$level,$child);

    $fromlevel = -1     if(!defined($fromlevel));
    $tolevel   = 100000 if(!defined($tolevel));
    $xmldata="";

    $level=$self->{_level};
    $elname=$self->{_name};

    if($level<=$tolevel) {
	
	# prolog
	if($level>=$fromlevel) {
	    $xmldata.="    "x$level;
	    $xmldata.="<$elname";
	    foreach $subid (sort {$b cmp $a} (keys(%{$self->{ATTR}}))) {
		next if($subid=~/\_/);
		$xmldata.=" $subid=\"".$self->{ATTR}->{$subid}."\"";
	    }
	    $xmldata.=">\n";
	}
	
	# dive in
	foreach $child (@{$self->{_childs}}) {
	    $xmldata.=$child->get_xml_tree();
	}
	
	# epilog
	if($level>=$fromlevel) {
	    $xmldata.="    "x$level;
	    $xmldata.="</$elname>\n";
	}
	
    }

    return($xmldata);
}


1;
