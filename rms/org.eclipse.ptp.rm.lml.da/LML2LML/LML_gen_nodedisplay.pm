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

my $VERSION='1.0';
my($debug)=0;

use strict;
use Data::Dumper;
use Time::Local;
use Time::HiRes qw ( time );
use String::Scanf;

use LML_ndtree;

use LML_gen_nodedisplay_insert_job;

sub new {
    my $self    = {};
    my $proto   = shift;
    my $class   = ref($proto) || $proto;
    my $verbose = shift;
    my $timings = shift;
    printf("\t LML_gen_nodedisplay: new %s\n",ref($proto)) if($debug>=3);
    $self->{VERBOSE}   = $verbose; 
    $self->{TIMINGS}   = $timings; 
    $self->{LMLFH}       = undef; 
    $self->{LAYOUT}      = undef; 
    $self->{NODEMAPPING} = undef; 
    $self->{NODENAMENAMASK}= "n%06d";
    $self->{SCHEMEROOT} =  undef; 
    $self->{DATAROOT}   =  undef; 
    $self->{SCHEMEFROMREQUEST} =  undef; 
    $self->{IDLISTREF}   = undef; 
    bless $self, $class;
    return $self;
}

sub get_ids {
    my($self) = shift;
    return($self->{IDLISTREF});
}

sub process {
    my($self) = shift;
    my $layoutref  = shift;
    my $schemefromrequest  = shift;
    my $filehandler_LML  = shift;
    my ($numids,$gid,$idlistref);
    my ($schemeroot,$dataroot);
    $numids=0;
    $self->{LAYOUT}    = $layoutref; 
    $self->{LMLFH}     = $filehandler_LML; 
    $gid               = $layoutref->{gid};

    $self->{SCHEMEFROMREQUEST} =  $schemefromrequest; 


    # internal structure
    $self->{SCHEMEROOT} = $schemeroot = LML_ndtree->new();
    $self->{DATAROOT}   = $dataroot   = LML_ndtree->new();
    
    # determine scheme of system
    $self->{SYSTEMTYPE}=$self->_get_system_type();    
    if($self->{SYSTEMTYPE} eq "BG/P") {
	my($maxlx,$maxly,$maxlz,$maxpx,$maxpy,$maxpz)=$self->_get_system_size_bg();
	if(!$self->_init_trees_bg($maxlx,$maxly,$maxlz,$maxpx,$maxpy,$maxpz)) {
	    print "ERROR: could not init internal data structures, system type: $self->{SYSTEMTYPE}, aborting ...\n";
	    return(-1);
	}
    } elsif($self->{SYSTEMTYPE} eq "Cluster") {

	# user define scheme given
	if($self->{SCHEMEFROMREQUEST}) {

	    if(!$self->_init_trees_cluster_from_scheme()) {
		print "ERROR: could not init internal data structures, system type: $self->{SYSTEMTYPE}, aborting ...\n";
		return(-1);
	    }
	    
	} else {
	    # standard one-level tree, mapping of node names
	    my $numnodes=$self->_get_system_size_cluster();
	    if(!$self->_init_trees_cluster()) {
		print "ERROR: could not init internal data structures, system type: $self->{SYSTEMTYPE}, aborting ...\n";
		return(-1);
	    }
	    $self->_adjust_layout_cluster($numnodes);
	}
    } else {
	print "ERROR: not supported system type: $self->{SYSTEMTYPE}, aborting ...\n";
	return(-1);
    }
    # add regular expression to each level of node display scheme for fast pattern scan of nodenames
    $self->_add_regexp_to_scheme();
    # adjust min,max attribute if only one is given
    $self->_update_scheme_attr();
    
    # init data tree with empty root nodes
    $self->_add_empty_root_elements();


    $idlistref=[];
    print "LML_gen_nodedisplay::process: gid=$gid\n" if($self->{VERBOSE});
    $idlistref=$self->_insert_run_jobs();

    $self->{IDLISTREF}=$idlistref;
    $numids=scalar @{$idlistref};
    
    # update layout
    

    return($numids);
}

sub _insert_run_jobs {
    my($self) = shift;
    my (@idlist,$key,$ref,$inforef,$nodelist);
    
    keys(%{$self->{LMLFH}->{DATA}->{OBJECT}}); # reset iterator
    while(($key,$ref)=each(%{$self->{LMLFH}->{DATA}->{OBJECT}})) {
#	last; # WF
	next if($ref->{type} ne 'job');
	$inforef=$self->{LMLFH}->{DATA}->{INFODATA}->{$key};
	next if($inforef->{state} ne 'Running');
	$nodelist=$self->_remap_nodes($inforef->{nodelist});
	$self->insert_job_into_nodedisplay($self->{SCHEMEROOT},$self->{DATAROOT},$nodelist,$key);
	push(@idlist,$key);
    }
    return(\@idlist);
}


sub get_lml_nodedisplay {
    my($self) = shift;
    my($ds,$rc,$id,$cid);
    my $layoutref  = $self->{LAYOUT};
    my(@keylist,$key,$value);

    $ds->{id}=$layoutref->{gid};
    $ds->{title}=$layoutref->{id};
    $ds->{schemeroot}=$self->{SCHEMEROOT};
    $ds->{dataroot}=$self->{DATAROOT};
    
    return($ds);

}


sub get_lml_nodedisplaylayout {
    my($self) = shift;
    my($ds,$rc,$id,$cid);
    my $layoutref  = $self->{LAYOUT};

    return($layoutref);

}

sub _get_system_type {
    my($self) = shift;
    my $system_type = "unknown";
    my($key,$ref);
    keys(%{$self->{LMLFH}->{DATA}->{OBJECT}}); # reset iterator
    while(($key,$ref)=each(%{$self->{LMLFH}->{DATA}->{OBJECT}})) {
	if($ref->{type} eq 'system') {
	    $ref=$self->{LMLFH}->{DATA}->{INFODATA}->{$key};
	    if($ref->{type}) {
		$system_type=$ref->{type};
		printf("_get_system_type: type is '%s'\n",$system_type) if($self->{VERBOSE});
	    }
	    last; 
	}
    }
    return($system_type);
}


sub _add_regexp_to_scheme  {
    my($self) = shift;
    
    my $schemeref=$self->{SCHEMEROOT};
    my($id,@numbers,$format,$regexp, $child);


    $regexp="";

    foreach $child (@{$schemeref->{_childs}}) {
	$self->__add_regexp_to_scheme($child,$regexp);
    }

    return(1);
}

sub __add_regexp_to_scheme {
    my($self) = shift;
    my($schemeref)=shift;
    my($regexp)=shift;
    my($rg,$child,$key);

    if(exists($schemeref->{ATTR}->{mask})) {
	$rg=String::Scanf::format_to_re($schemeref->{ATTR}->{mask});
    } elsif(exists($schemeref->{ATTR}->{map})) {
	$rg="\(".join("\|",split('\s*,\s*',$schemeref->{ATTR}->{map}))."\)";
	my $num=$schemeref->{ATTR}->{min};
	foreach $key (split('\s*,\s*',$schemeref->{ATTR}->{map})) {
	    $schemeref->{ATTR}->{_map}->{$key}=$num;
	    $num++;
	}
    } else {
	$rg="";
    }
    $schemeref->{ATTR}->{_maskreg}=$rg;
    $regexp.=$rg;
    $schemeref->{ATTR}->{_maskregall}=$regexp;
    foreach $child (@{$schemeref->{_childs}}) {
	$self->__add_regexp_to_scheme($child,$regexp);
    }
    return(1);
}

sub _update_scheme_attr  {
    my($self) = shift;
    my($child);

    my $schemeref=$self->{SCHEMEROOT};

    foreach $child (@{$schemeref->{_childs}}) {
	$self->__update_scheme_attr($child);
    }
    return(1);
}

sub __update_scheme_attr {
    my($self) = shift;
    my($schemeref)=shift;
    my($child);
    
    foreach $child (@{$schemeref->{_childs}}) {
	$self->__update_scheme_attr($child);
    }
    
    if(!exists($schemeref->{ATTR}->{min})) {
	$schemeref->{ATTR}->{min} = $schemeref->{ATTR}->{max} if(exists($schemeref->{ATTR}->{max}));
    }
    if(!exists($schemeref->{ATTR}->{max})) {
	$schemeref->{ATTR}->{max} = $schemeref->{ATTR}->{min} if(exists($schemeref->{ATTR}->{min}));
    }

    return(1);
}

sub _remap_nodes {
    my($self) = shift;
    my($nodelist)=shift;
    my($newnodelist,$spec,$node,$num,$newnode);
    foreach $spec (split(/\),?\(/,$nodelist)) {
	# change form '(node,node num)' to (node-c<num>)
	if($spec=~/\(?([^,]+),(\d+)\)?/) {
	    $node=$1;$num=$2;
	} elsif($spec=~/^([^,]+)$/) {
	    $node=$1;$num=0;	
	} else {
	    print "ERROR: _remap_nodes: unknown node '$node', skipping\n";
	}
	
	if(exists($self->{NODEMAPPING}->{$node})) {
	    $newnode=$self->{NODEMAPPING}->{$node}; 
	} else {
	    $newnode=$node;
	}
	$newnodelist.="," if($newnodelist);
	$newnodelist.=sprintf("%s-c%02d",$newnode,$num);
    }
    return($newnodelist);
}


sub _add_empty_root_elements  {
    my($self) = shift;
    my($treenode, $child);

    my $schemeroot=$self->{SCHEMEROOT};

    # insert first element in data section
    $treenode=$self->{DATAROOT};
    foreach $child (@{$schemeroot->{_childs}}) {
	my $subnode=$treenode->new_child();
	$subnode->add_attr({ min     => $child->{ATTR}->{min},
			     max     => $child->{ATTR}->{max},
			     oid     => 'empty' });
    }
	
    return(1);
}

###############################################
# Cluster related
############################################### 
sub _get_system_size_cluster  {
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

sub _init_trees_cluster_from_scheme  {
    my($self) = shift;
    my($treenode, $child);

    my $schemeroot=$self->{SCHEMEROOT};

    $schemeroot->copy_tree($self->{SCHEMEFROMREQUEST});

    return(1);
}

sub _init_trees_cluster  {
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

sub _adjust_layout_cluster  {
    my($self) = shift;
    my($numnodes)=@_;
    my($id,$subid,$treenode,$child,$ncores,$start,$numchilds);
    my $rc=1;
    my $default_nodes_per_row=32;
   

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
	    $child->{ATTR}->{maxlevel}=1;
	}
    } else {
	# more sophisticated layout, tbd
    }

    return($rc);
}

###############################################
# BG/P related
############################################### 
sub _get_system_size_bg  {
    my($self) = shift;
    my($indataref) = $self->{INDATA};
    my($partref,$part,$lx,$ly,$lz,$px,$py,$pz);
    my($maxlx,$maxly,$maxlz,$maxpx,$maxpy,$maxpz);

    my ($key,$ref);
    
    $maxlx=$maxly=$maxlz=0;
    $maxpx=$maxpy=$maxpz=0;
    keys(%{$self->{LMLFH}->{DATA}->{OBJECT}}); # reset iterator
    while(($key,$ref)=each(%{$self->{LMLFH}->{DATA}->{OBJECT}})) {
	next if($ref->{type} ne 'partition');
	next if($ref->{id}!~/^bgbp/s);
	$partref=$self->{LMLFH}->{DATA}->{INFODATA}->{$key};
	$part=$partref->{bgp_partitionid};
	$px=$partref->{x_loc};
	$py=$partref->{y_loc};
	$pz=$partref->{z_loc};
	$maxpx=$px if($px>$maxpx);
	$maxpy=$py if($py>$maxpy);
	$maxpz=$pz if($pz>$maxpz);
        # currently not supported for BG/P
	# data could only get from LL over LL C-API
	$lx=$ly=$lz=0; 
	$maxlx=$lx if($lx>$maxlx);
	$maxly=$ly if($ly>$maxly);
	$maxlz=$lz if($lz>$maxlz);
    }

    printf("_get_system_size_bg: Blue Gene System found of size: %dx%dx%d (logical: %dx%dx%d)\n",
	   $maxpx+1,$maxpy+1,$maxpz+1,
	   $maxlx+1,$maxly+1,$maxlz+1,
	) if($self->{VERBOSE});
    
    return($maxlx,$maxly,$maxlz,$maxpx,$maxpy,$maxpz);
}


sub _init_trees_bg  {
    my($self) = shift;
    my($maxlx,$maxly,$maxlz,$maxpx,$maxpy,$maxpz)=@_;
    my($id,$subid,$treenode,$schemeroot,$bgsystem);

    $schemeroot=$self->{SCHEMEROOT};
    $treenode=$schemeroot;
    $bgsystem=$treenode=$treenode->new_child();
    $treenode->add_attr({ tagname => 'row',
			  min     => 0,
			  max     => $maxpx,
			  mask    => 'R%01d' });

    $treenode=$treenode->new_child();
    $treenode->add_attr({ tagname => 'rack',
			  min     => 0,
			  max     => $maxpy,
			  mask    => '%01d' });

    $treenode=$treenode->new_child();
    $treenode->add_attr({ tagname => 'midplane',
			  min     => 0,
			  max     => $maxpz,
			  mask    => '-M%01d' });

    $treenode=$treenode->new_child();
    $treenode->add_attr({ tagname => 'nodecard',
			  min     => 0,
			  max     => 15,
			  mask    => '-N%02d' });

    $treenode=$treenode->new_child();
    $treenode->add_attr({ tagname => 'computecard',
			  min     => 4,
			  max     => 35,
			  mask    => '-C%02d' });

    $treenode=$treenode->new_child();
    $treenode->add_attr({ tagname => 'core',
			  min     => 0,
			  max     => 3,
			  mask    => '-%01d' });


    return(1);
}


1;
