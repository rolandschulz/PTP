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
#use String::Scanf;
use LML_da_util;

use LML_ndtree;

# system dependent functions
use LML_gen_nodedisplay_system_cluster;
use LML_gen_nodedisplay_system_bgp;
use LML_gen_nodedisplay_system_bgq;
use LML_gen_nodedisplay_system_alps;
use LML_gen_nodedisplay_system_pbs;

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
    my ($schemeroot,$dataroot,$usescheme);
    $numids=0;
    $self->{LAYOUT}    = $layoutref; 
    $self->{LMLFH}     = $filehandler_LML; 
    $gid               = $layoutref->{gid};

    # check if schme is given
    $self->{SCHEMEFROMREQUEST} =  $schemefromrequest; 

    # internal structure
    $self->{SCHEMEROOT} = $schemeroot = LML_ndtree->new();
    $self->{DATAROOT}   = $dataroot   = LML_ndtree->new();
    
    # determine scheme of system
    ($self->{SYSTEMTYPE},$self->{SYSTEMNAME})=$self->_get_system_type();    
    if($self->{SYSTEMTYPE} eq "BG/P") {
	my($maxlx,$maxly,$maxlz,$maxpx,$maxpy,$maxpz)=$self->_get_system_size_bgp();
	if(!$self->_init_trees_bgp($maxlx,$maxly,$maxlz,$maxpx,$maxpy,$maxpz)) {
	    print "ERROR: could not init internal data structures, system type: $self->{SYSTEMTYPE}, aborting ...\n";
	    return(-1);
	}
	# init data tree with empty root nodes
	$self->_add_empty_root_elements();
	
	$self->_adjust_layout_bgp();


    } elsif($self->{SYSTEMTYPE} eq "BG/Q") {
	my($maxla,$maxlb,$maxlc,$maxld,$maxpx,$maxpy,$maxpz)=$self->_get_system_size_bgq();
	if(!$self->_init_trees_bgq($maxla,$maxlb,$maxlc,$maxld,$maxpx,$maxpy,$maxpz)) {
	    print "ERROR: could not init internal data structures, system type: $self->{SYSTEMTYPE}, aborting ...\n";
	    return(-1);
	}
	# init data tree with empty root nodes
	$self->_add_empty_root_elements();
	
	$self->_adjust_layout_bgq();


    } elsif($self->{SYSTEMTYPE} eq "ALPS") {
	my($maxpcol,$maxprow,$maxpcage,$maxpslot,$maxpnode,$maxpcore)=$self->_get_system_size_alps();
	if(!$self->_init_trees_alps($maxpcol,$maxprow,$maxpcage,$maxpslot,$maxpnode,$maxpcore)) {
	    print "ERROR: could not init internal data structures, system type: $self->{SYSTEMTYPE}, aborting ...\n";
	    return(-1);
	}
	# init data tree with empty root nodes
	$self->_add_empty_root_elements();
	
	$self->_adjust_layout_alps();



    } elsif($self->{SYSTEMTYPE} eq "Cluster") {

	# user define scheme given
	if($self->{SCHEMEFROMREQUEST}) {

	    if(!$self->_init_trees_cluster_from_scheme()) {
		print "ERROR: could not init internal data structures, system type: $self->{SYSTEMTYPE}, aborting ...\n";
		return(-1);
	    }

	    # init data tree with empty root nodes
	    $self->_add_empty_root_elements();
	    
	} else {
	    # standard one-level tree, mapping of node names
	    my $numnodes=$self->_get_system_size_cluster();
	    if(!$self->_init_trees_cluster()) {
		print "ERROR: could not init internal data structures, system type: $self->{SYSTEMTYPE}, aborting ...\n";
		return(-1);
	    }
	    $self->_adjust_layout_cluster($numnodes);
	}
    } elsif($self->{SYSTEMTYPE} eq "PBS") {

	# user define scheme given
	if($self->{SCHEMEFROMREQUEST}) {

	    if(!$self->_init_trees_cluster_from_scheme()) {
		print "ERROR: could not init internal data structures, system type: $self->{SYSTEMTYPE}, aborting ...\n";
		return(-1);
	    }

	    # init data tree with empty root nodes
	    $self->_add_empty_root_elements();
	    
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


    $idlistref=[];
    print "LML_gen_nodedisplay::process: gid=$gid\n" if($self->{VERBOSE});
    $idlistref=$self->_insert_run_jobs();

    $self->{IDLISTREF}=$idlistref;
    $numids=scalar @{$idlistref};
    
    # update layout

#    print Dumper($self->{DATAROOT});
    

    return($numids);
}

sub _insert_run_jobs {
    my($self) = shift;
    my (@idlist,$key,$ref,$inforef,$nodelist);
    my($tstart,$tdiff,$jcount);

    $tstart=time;$jcount=0;
    keys(%{$self->{LMLFH}->{DATA}->{OBJECT}}); # reset iterator
    while(($key,$ref)=each(%{$self->{LMLFH}->{DATA}->{OBJECT}})) {
	next if($ref->{type} ne 'job');
	$inforef=$self->{LMLFH}->{DATA}->{INFODATA}->{$key};
	next if($inforef->{status} ne 'RUNNING');
	if(exists($inforef->{vnodelist})) {
	    $nodelist=$self->_remap_nodes_vnode($inforef->{vnodelist});
	} else {
	    $nodelist=$self->_remap_nodes($inforef->{nodelist});
	}
#	print "_insert_run_jobs job $key \n" if($self->{VERBOSE});
	$self->insert_job_into_nodedisplay($self->{SCHEMEROOT},$self->{DATAROOT},$nodelist,$key);
	push(@idlist,$key);
	$jcount++;
	if($jcount%10==0) {
	    $tdiff=time-$tstart;
	    printf("$0: inserted %d jobs in %6.4f sec\n",$jcount,$tdiff) if($self->{VERBOSE});
	}

#	last; # WF
    }
    $tdiff=time-$tstart;
    printf("$0: inserted %d jobs in %6.4f sec\n",$jcount,$tdiff) if($self->{VERBOSE});
    return(\@idlist);
}


sub get_lml_nodedisplay {
    my($self) = shift;
    my($ds,$rc,$id,$cid);
    my $layoutref  = $self->{LAYOUT};
    my(@keylist,$key,$value);

    $ds->{id}=$layoutref->{gid};
    $ds->{title}="system: ".$self->{SYSTEMNAME};
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
    my $system_name = "unknown";
    my($key,$ref);
    keys(%{$self->{LMLFH}->{DATA}->{OBJECT}}); # reset iterator
    while(($key,$ref)=each(%{$self->{LMLFH}->{DATA}->{OBJECT}})) {
	if($ref->{type} eq 'system') {
	    $ref=$self->{LMLFH}->{DATA}->{INFODATA}->{$key};
	    if($ref->{type}) {
		$system_type=$ref->{type};
		printf("_get_system_type: type is '%s'\n",$system_type) if($self->{VERBOSE});
	    }
	    if($ref->{hostname}) {
		$system_name=$ref->{hostname};
		printf("_get_system_type: name is '%s'\n",$system_name) if($self->{VERBOSE});
	    }
	    last; 
	}
    }
    return($system_type,$system_name);
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
#	$rg=String::Scanf::format_to_re($schemeref->{ATTR}->{mask});
	$rg=LML_da_util::mask_to_regexp($schemeref->{ATTR}->{mask});
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
    if(($self->{SYSTEMTYPE} eq "BG/P") || ($self->{SYSTEMTYPE} eq "BG/Q") ) {
	return($nodelist);
    }
    foreach $spec (split(/\),?\(/,$nodelist)) {
	# change form '(node,node num)' to (node-c<num>)
	if($spec=~/\(?([^,]+),(\d+)\)?/) {
	    $node=$1;$num=$2;
	} elsif($spec=~/^([^,]+)$/) {
	    $node=$1;$num=0;	
	} else {
	    print "ERROR: _remap_nodes: unknown node in spec '$spec', skipping\n";
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

sub _remap_nodes_vnode {
    my($self) = shift;
    my($nodelist)=shift;
    my($newnodelist,$spec,$node,$num,$number,$newnode,$start,$generatelist);
    if(($self->{SYSTEMTYPE} eq "BG/P") || ($self->{SYSTEMTYPE} eq "BG/Q") ){
	return($nodelist);
    }
    foreach $spec (split(/\),?\(/,$nodelist)) {
	# change form '(node,number tasks)' to (node-c<num>, ...)
	if($spec=~/\(?([^,]+),(\d+)\)?/) {
	    $node=$1;$number=$2;
	} elsif($spec=~/^([^,]+)$/) {
	    $node=$1;$number=0;	
	} else {
	    print "ERROR: _remap_nodes: unknown node in spec '$spec', skipping\n";
	}
	
	if(exists($self->{NODEMAPPING}->{$node})) {
	    $newnode=$self->{NODEMAPPING}->{$node}; 
	} else {
	    $newnode=$node;
	}
	
	if(!exists($self->{NODELASTTASKNUMBER}->{$node})) {
	    $self->{NODELASTTASKNUMBER}->{$node}=-1;
	}
	$start=$self->{NODELASTTASKNUMBER}->{$node}+1;
	# Store last task number
	$self->{NODELASTTASKNUMBER}->{$node} = $start+$number-1;
	 
	$generatelist=1;
	if(exists($self->{MAXCORESCHECK})) {
	    if($number==$self->{MAXCORESCHECK}+1) {
		$newnodelist.="," if($newnodelist);
		$newnodelist.=$newnode;
		$generatelist=0;
	    } 
	}
	if($generatelist) {
	    for($num=$start;$num<$start+$number;$num++) {
		$newnodelist.="," if($newnodelist);
		$newnodelist.=sprintf("%s-c%02d",$newnode,$num);
	    }
	}
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


1;
