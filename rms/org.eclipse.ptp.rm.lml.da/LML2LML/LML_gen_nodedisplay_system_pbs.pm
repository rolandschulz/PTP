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
package LML_gen_nodedisplay;
my($debug)=0;
my($generateusage)=1;
use strict;
use Time::Local;
use Time::HiRes qw ( time );
use Data::Dumper;

###############################################
# PBS related
############################################### 
sub _get_system_size_pbs  {
    my($self) = shift;
    my($indataref) = $self->{INDATA};
    my($numnodes);
    my ($key,$ref,$name,$ncores);

    keys(%{$self->{LMLFH}->{DATA}->{OBJECT}}); # reset iterator
    while(($key,$ref)=each(%{$self->{LMLFH}->{DATA}->{OBJECT}})) {
	next if($ref->{type} ne 'node');
	$name=$ref->{name};
	$ncores=$self->{LMLFH}->{DATA}->{INFODATA}->{$key}->{ncores};
	if(!defined($ncores)) {
	    print "_get_system_size_cluster: suspect node: $name, assuming 1 cores\n"  if($self->{VERBOSE});
	    $ncores=1;
	}
	if($ncores<0) {
	    print "_get_system_size_cluster: suspect node: $name negative number of cores, assuming 1 cores\n"  if($self->{VERBOSE});
	    $ncores=1;
	}
	push(@{$self->{NODESIZES}->{$ncores}},$name);
    }


    $numnodes=0;
    foreach $ncores (sort {$a <=> $b} keys %{$self->{NODESIZES}}) {
	foreach $name (@{$self->{NODESIZES}->{$ncores}}) {
	    # register new node 
	    if(!exists($self->{NODEMAPPING}->{$name})) {
		$self->{NODEMAPPING}->{$name}=sprintf($self->{NODENAMENAMASK},$numnodes);
#		print "_get_system_size_cluster: remap '$name' -> '$self->{NODEMAPPING}->{$name}'\n";
		$numnodes++;
	    } else {
		print "ERROR: _get_system_size_cluster: duplicate node '$name' -> '$self->{NODEMAPPING}->{$name}'\n";
	    }
	}
	printf("_get_system_size_cluster: found %4d nodes of size: %d\n", scalar @{$self->{NODESIZES}->{$ncores}},$ncores) 
	    if($self->{VERBOSE});
    }
    printf("_get_system_size_cluster: Cluster found of size: %d\n",$numnodes) if($self->{VERBOSE});
    
    return($numnodes);
}

sub _init_trees_pbs_from_scheme  {
    my($self) = shift;
    my($treenode, $child);

    my $schemeroot=$self->{SCHEMEROOT};

    $schemeroot->copy_tree($self->{SCHEMEFROMREQUEST});

    return(1);
}

sub _init_trees_pbs  {
    my($self) = shift;
    my($id,$subid,$treenode,$schemeroot,$ncores,$numnodes,$start);

    $schemeroot=$self->{SCHEMEROOT};
    $start=0;
    foreach $ncores (sort {$a <=> $b} keys %{$self->{NODESIZES}}) {
	$numnodes=scalar @{$self->{NODESIZES}->{$ncores}};
	$treenode=$schemeroot;
	$treenode=$treenode->new_child();
	$treenode->add_attr({ tagname => 'node',
			      min     => $start,
			      max     => $start+$numnodes-1,
			      mask    => $self->{NODENAMENAMASK} });
	$treenode=$treenode->new_child();
	$treenode->add_attr({ tagname => 'core',
			      min     => 0,
			      max     => $ncores-1,
			      mask    => '-c%02d' });
	
	# insert first element in data section
	$treenode=$self->{DATAROOT};
	$treenode=$treenode->new_child();
	$treenode->add_attr({ min     => $start,
			      max     => $start+$numnodes-1,
			      oid     => 'empty' });
	$start+=$numnodes;
    }

    return(1);
}

sub _adjust_layout_pbs  {
    my($self) = shift;
    my($numnodes)=@_;
    my($id,$subid,$treenode,$child,$ncores,$start,$numchilds);
    my $rc=1;
    my $default_nodes_per_row=8;
  
    $treenode=$self->{LAYOUT}->{tree};

    $numchilds=scalar @{$treenode->{_childs}};
    if($numchilds==1) {
	$child=$treenode->{_childs}->[0];
	if( ($child->{ATTR}->{rows} eq 0) && ($child->{ATTR}->{cols} eq 0)) {
	    $child->{ATTR}->{rows}=$default_nodes_per_row;
	    $child->{ATTR}->{cols}=int($numnodes/$default_nodes_per_row)+1;
	} elsif($child->{ATTR}->{cols} eq 0) {
	    $child->{ATTR}->{cols}=int($numnodes/$child->{ATTR}->{rows})+1;
	} elsif($child->{ATTR}->{rows} eq 0) {
	    $child->{ATTR}->{rows}=int($numnodes/$child->{ATTR}->{cols})+1;
	}

	if(!exists($child->{ATTR}->{maxlevel})) {
	    $child->{ATTR}->{maxlevel}=2;
	}
    } else {
	# more sophisticated layout, tbd
    }

    return($rc);
}

1;
