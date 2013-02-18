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


################################################################################################################
# insert_job_into_nodedisplay: Insert one job into data tree 
#
#  Parameters:
#   schemeref: Reference to scheme tree 
#   dataref:   Reference to data tree 
#   nodelist:  List of nodes, as comma-separated list 
#   oid:       Object-Id of job in LML description 
# 
#  Description:
#   Insert into the data tree (parameter dataref) a job in that way, that all nodes n which the job is running gets 
#   an oid reference to this job, The nodelist and te oid is given as parameter. The scheme describing the
#   full system structure is also given as parameter.
#  
################################################################################################################
sub insert_job_into_nodedisplay  {
    my($self) = shift;
    my($schemeref) = shift;
    my($dataref) = shift;
    my($nodelist) = shift;
    my($oid) = shift;
    my($data,$node,$listref,@nodelistrefs,@nodelistrefs_reduced,@nodesizelistrefs_reduced, $allcovered, $child);

    # Transfer each node name of the nodelist to a list of ordering number for each level of the tree
    # according to the mask or map attribute in th escheme definition
#    print "insert_job_into_nodedisplay $oid >$nodelist<\n";
    foreach $node (sort(split(/\s*,\s*/,$nodelist))) {
	$listref=$self->get_numbers_from_name($node,$schemeref);
	if(!defined($listref)) {
		#Check if node name is full qualified. Try to map not qualified node name
		my $pointPos = index($node, '.');
		if($pointPos != -1 ){
			$node =~ s/\.[^\-]*//;#Remove domain, but do not remove possible -c00 attachments for core selection
			#Try to map the node again
			$listref=$self->get_numbers_from_name($node,$schemeref);
			if(!defined($listref)) {
				print STDERR "insert_job_into_nodedisplay: Error: could not map node $node\n";
	    		return(0);
			}
		}
	}
	push(@nodelistrefs,$listref);
    }
    
    # Debugging
    if($debug>=2) {
	foreach $listref (@nodelistrefs) {
	    print "insert_job_into_nodedisplay, before: ",join(',',@{$listref}),"\n"; 
	}
    }

    # Find and compress sets of nodenames, which covers a full subtree
    # after this run nodelistrefs_reduced contains only location of full 
    # covered tree nodes which
    # This scan will run recursively on each subtree of the root node   
    foreach $child (@{$schemeref->{_childs}}) {
	$allcovered=$self->_reduce_nodelist($child,\@nodelistrefs,\@nodelistrefs_reduced,\@nodesizelistrefs_reduced);
    }

    print "insert_job_into_nodedisplay, allcovered=$allcovered\n"  if($debug>=2); 

    # Insert job into each node of the reduced list
    my($allsize) ;
    foreach $listref (@nodelistrefs_reduced) {
	$allsize=shift(@nodesizelistrefs_reduced);
	$self->_insert_jobnode_nodedisplay($dataref,$listref,$allsize,$oid);
    }

    # scan all created data tree structure for empty nodes
    

    return(1);
}

################################################################################################################
# _insert_jobnode_nodedisplay: Insert one jobnode into data tree
#
#  Parameters:
#   dataref:   Reference to data tree 
#   nodelistref:  Location of node  
#   oid:       Object-Id of job in LML description 
# 
#  Description:
#   Insert into the data tree (parameter dataref) a job in that way, that all nodes n which the job is running gets 
#   an oid reference to this job, The nodelist and te oid is given as parameter. The scheme describing the
#   full system structure is also given as parameter.
#  
################################################################################################################
sub _insert_jobnode_nodedisplay {
    my($self) = shift;
    my($dataref)=shift;
    my($nodelistref)=shift;
    my($sizelistref)=shift;
    my($oid) = shift;
    my($myspec,$mysizespec,$min,$max,$subspec,$child);
    my($allsize);
    my($lmin,$lmax,$subchilds);
    my $level=$dataref->{_level};
    my $xspace=" "x$level;
    my (@covered, @childlist, $newchild, @newlist, @newsizelist, $isleaf, $i, @notcoveredlist, $updatechild);
    
    print "_insert_jobnode_nodedisplay: $xspace # START level=$level nodelistref=>",join(',',@{$nodelistref}),
          "< sizelistref=>",join(',',@{$sizelistref}),"<\n"  if($debug>=2); 
    
    $myspec=$nodelistref->[0];
    if($myspec=~/^\((.*)\)$/) {
	my($list,$mysizespec,$tmplist,@sizespec);

	$list      = $1;
	$mysizespec=$sizelistref->[0];
	$mysizespec=~/^\((.*)\)$/;
	$tmplist   =$1;
	@sizespec  =(split(',',$tmplist));

	for $subspec (split(',',$list)) {
	    my @list     = @{$nodelistref};
	    my @sizelist = @{$sizelistref};
	    $list[0]=$subspec;
	    $sizelist[0]=shift(@sizespec);
	    print "_insert_jobnode_nodedisplay: $xspace  -> re-calling with top subspec >$subspec< subsize>$sizelist[0]<\n"  if($debug>=2); 
	    $self->_insert_jobnode_nodedisplay($dataref,\@list, \@sizelist, $oid);
	}
	print "_insert_jobnode_nodedisplay: $xspace # END   level=$level nodelistref=>",join(',',@{$nodelistref}),"<\n" if($debug>=2); 
	return();
    } elsif($myspec=~/(\d+)\-(\d+)/) {
	$min=$1;$max=$2;
    } else {
	$min=$max=$myspec;
    }
    for($i=$min;$i<=$max;$i++) {$covered[$i]=1;}

    @newlist     = @{$nodelistref};shift(@newlist);
    @newsizelist = @{$sizelistref};
    $allsize     = shift(@newsizelist);
    $isleaf=($#newlist == -1)?1:0;
    # scan child
    @childlist=@{$dataref->{_childs}};
    foreach $child (@childlist) {
	$lmin=$child->{ATTR}->{min};
	$lmax=$child->{ATTR}->{max};
	$subchilds=$#{$child->{_childs}}+1;
	print "_insert_jobnode_nodedisplay: $xspace ==> level=$level [$min..$max] found child:  ($lmin..$lmax) #subchilds=$subchilds\n"  if($debug>=2); 

	if(($max<$lmin) || ($min>$lmax)) {
	    print "_insert_jobnode_nodedisplay: $xspace  -> child full outside range, skip\n"  if($debug>=2); 
	    next;
	}
	# case 2: full covered
	if(($min>=$lmin) && ($max<=$lmax)) {
	    print "_insert_jobnode_nodedisplay: $xspace  -> child full covered, isleaf=$isleaf\n"  if($debug>=2); 
	    
	    if( ($min==$lmin) && ($max==$lmax) ) { # exact match
		$updatechild=$child;
	    } else {
		if($min>$lmin) { 	           # overlap before
		    print "_insert_jobnode_nodedisplay: $xspace  -> duplicate child   with (",($lmin),"..",($min-1),"), inserting subnodes ...\n" if($debug>=2); 
		    $newchild=$dataref->duplicate_child($child);
		    $newchild->{ATTR}->{min}=$lmin;
		    $newchild->{ATTR}->{max}=$min-1;
		    print "_insert_jobnode_nodedisplay: $xspace  -> adjust min of child to (",$min,"..",($lmax),")\n" if($debug>=2); 
		    $child->{ATTR}->{min}=$min;
		} 
		if($max<$lmax) {                   # overlap after, copy tree
		    print "_insert_jobnode_nodedisplay: $xspace  -> duplicate child   with (",($max+1),"..",($lmax),")\n" if($debug>=2); 
		    $newchild=$dataref->duplicate_child($child);
		    $newchild->{ATTR}->{min}=$max+1;
		    $newchild->{ATTR}->{max}=$lmax;
		    print "_insert_jobnode_nodedisplay: $xspace  -> adjust max of child to (",$min,"..",($max),")\n" if($debug>=2); 
		    $child->{ATTR}->{max}=$max;
		}
		$updatechild=$child;
	    }

	    if($isleaf) {
		# insert new child
		print "_insert_jobnode_nodedisplay: $xspace     remove old child\n" if($debug>=2); 
		$dataref->remove_child($updatechild);
		print "_insert_jobnode_nodedisplay: $xspace  -> add new child   with (",($min),"..",($max),")\n" if($debug>=2); 
		$updatechild=$dataref->new_child();
		$updatechild->add_attr({ oid => $oid, min => $min, max => $max });
	    } else {
		# change tree of existing child
#		$updatechild->{ATTR}->{min}=$min;
#		$updatechild->{ATTR}->{max}=$max;
		print "_insert_jobnode_nodedisplay: $xspace  -> duplicate child   with (",($min),"..",($max),"), inserting subnodes ...\n" if($debug>=2); 
		$self->_insert_jobnode_nodedisplay($updatechild,\@newlist, \@newsizelist, $oid) ;
	    }
 	    for($i=$min;$i<=$max;$i++) {$covered[$i]=0}

	} elsif(($min<$lmin) && ($max<=$lmax)) {
	    print "_insert_jobnode_nodedisplay: $xspace  -> child not full covered: low overlap #subchilds=$subchilds\n" if($debug>=2); 

	    if($max<$lmax) {             # overlap high
		$newchild=$dataref->duplicate_child($child);
		$newchild->{ATTR}->{min}=$max+1;
		$newchild->{ATTR}->{max}=$lmax;
		print "_insert_jobnode_nodedisplay: $xspace  -> duplicate child   with (",($max+1),"..",($lmax),")\n" if($debug>=2); 
	    } 
	    if($isleaf) {
		$dataref->remove_child($child); # replace subtree
		print "_insert_jobnode_nodedisplay: $xspace     remove old child\n" if($debug>=2); 
		$newchild=$dataref->new_child();
		$newchild->add_attr({ oid => $oid, min => $lmin, max => $max });
		print "_insert_jobnode_nodedisplay: $xspace  -> insert new child  with (",($lmin),"..",($max),")\n" if($debug>=2); 
	    } else {
		$child->{ATTR}->{min}=$lmin;
		$child->{ATTR}->{max}=$max;
		print "_insert_jobnode_nodedisplay: $xspace  -> adjust range      with (",($lmin),"..",($max),"), inserting subnodes ...\n" if($debug>=2); 
		$self->_insert_jobnode_nodedisplay($child,\@newlist, \@newsizelist, $oid) ;
	    }
 	    for($i=$lmin;$i<=$max;$i++) {$covered[$i]=0}
	} elsif(($min>=$lmin) && ($max>$lmax)) {
	    print "_insert_jobnode_nodedisplay: $xspace  -> child not full covered: high overlap #subchilds=$subchilds\n" if($debug>=2); 
	    if($min>$lmin) {             # overlap low
		$newchild=$dataref->duplicate_child($child);
		$newchild->{ATTR}->{min}=$lmin;
		$newchild->{ATTR}->{max}=$min-1;
		print "_insert_jobnode_nodedisplay: $xspace  -> duplicate child   with (",($lmin),"..",($min-1),")\n" if($debug>=2); 
	    } 
	    if($isleaf) {
		$dataref->remove_child($child); # replace subtree
		print "_insert_jobnode_nodedisplay: $xspace     remove old child\n" if($debug>=2); 
		$newchild=$dataref->new_child();
		$newchild->add_attr({ oid => $oid, min => $min, max => $lmax });
		print "_insert_jobnode_nodedisplay: $xspace  -> insert new child  with (",($min),"..",($lmax),")\n" if($debug>=2); 
	    } else {
		$child->{ATTR}->{min}=$min;
		$child->{ATTR}->{max}=$lmax;
		print "_insert_jobnode_nodedisplay: $xspace  -> adjust range      with (",($min),"..",($lmax),"), inserting subnodes ...\n" if($debug>=2); 
		$self->_insert_jobnode_nodedisplay($child,\@newlist, \@newsizelist, $oid) ;
	    }
 	    for($i=$min;$i<=$lmax;$i++) {$covered[$i]=0}
	} elsif(($min<$lmin) && ($max>$lmax)) {
	    print "_insert_jobnode_nodedisplay: $xspace  -> child not full covered: middle overlap #subchilds=$subchilds\n" if($debug>=2); 
	    if($isleaf) {
		$dataref->remove_child($child); # replace subtree
		print "_insert_jobnode_nodedisplay: $xspace     remove old child\n" if($debug>=2); 
		$newchild=$dataref->new_child();
		$newchild->add_attr({ oid => $oid, min => $lmin, max => $lmax });
		print "_insert_jobnode_nodedisplay: $xspace  -> insert new child  with (",($lmin),"..",($lmax),")\n" if($debug>=2); 
	    } else {
		print "_insert_jobnode_nodedisplay: $xspace  -> adjust range      with (",($lmin),"..",($lmax),"), inserting subnodes ...\n" if($debug>=2); 
		$self->_insert_jobnode_nodedisplay($child,\@newlist, \@newsizelist, $oid) ;
	    }
 	    for($i=$lmin;$i<=$lmax;$i++) {$covered[$i]=0}
    }
    }

    # build entries for not already covered childs
    for($i=$min;$i<=$max;$i++) {
	push(@notcoveredlist,$i) if($covered[$i]==1);
    }
    if(@notcoveredlist) {
	my $newlist=reduce_list(@notcoveredlist);$newlist=~s/\(//gs;$newlist=~s/\)//gs;
	print "_insert_jobnode_nodedisplay: $xspace  -> notcoveredlist= >$newlist<\n" if($debug>=2); 
	for $subspec (split(',',$newlist)) {
	    if($subspec=~/(\d+)\-(\d+)/) {$min=$1;$max=$2;  } 
	    else                         {$min=$max=$subspec;}
	$newchild=$dataref->new_child();
	if($isleaf) {
	    print "_insert_jobnode_nodedisplay: $xspace  -> insert new child with (",($min),"..",($max),")\n" if($debug>=2); 
	    $newchild->add_attr({ oid => $oid, min => $min, max => $max });
	} else {
	    $newchild->add_attr({ min => $min, max => $max, oid => $dataref->{ATTR}->{oid} });
	    print "_insert_jobnode_nodedisplay: $xspace  -> insert new child with (",($min),"..",($max),"), inserting subnodes ...\n" if($debug>=2); 
	    $self->_insert_jobnode_nodedisplay($newchild,\@newlist, \@newsizelist, $oid) ;
	}
	}
	
    }

    # adjust usage information
    if($generateusage) {
	if(!exists($dataref->{ATTR}->{_JOBUSAGE})) {
	    $dataref->{ATTR}->{_JOBUSAGE}={};
	} 
	$dataref->{ATTR}->{_JOBUSAGE}->{$oid}+=$allsize;
    }

    print "_insert_jobnode_nodedisplay: $xspace # END   level=$level allsize=$allsize nodelistref=>",join(',',@{$nodelistref}),"<\n" if($debug>=2); 

}    


################################################################################################################
# _reduce_nodelist: reduce list of nodes, covering only full subtrees
#
#  Parameters:
#   schemeref:        Reference to scheme tree 
#   nodelistrefs:     Reference to list of locations 
#   newnodelistrefs:  Reference to reduced list of locations 
# 
#  Description:
#   Find and compress sets of nodenames, which covers a full subtree.
#   This scan will run recursively on each subtree of the root node.   
#
#  Return value:
#
################################################################################################################
sub _reduce_nodelist {
    my($self) = shift;
    my($schemeref)=shift;
    my($nodelistrefs)=shift;
    my($newnodelistrefs)=shift;
    my($newsizelistrefs)=shift;
    my($rg,$child, $listref, @covered, $allcovered, $allsize, @allsize, $nodenum,@shortlists,@newshortlists,@newshortsizelists, $lastfound, $allsizelist);
    my $level=$schemeref->{_level};
    my $xspace=" "x$level;

    print "_reduce_nodelist: $xspace # START level=$level scheme min..max=$schemeref->{ATTR}->{min}..$schemeref->{ATTR}->{max}\n" if($debug>=2); 
    print "_reduce_nodelist: $xspace #       ",&print_nodelists($nodelistrefs),"\n" if($debug>=3); 

    # Initialization
    for($nodenum=$schemeref->{ATTR}->{min};$nodenum<=$schemeref->{ATTR}->{max};$nodenum++) {
	$covered[$nodenum]=0;
    }

    # check which treenodes are given in nodelist, 
    # for each node: 
    #  covered[.] = {0 no nodes, 1 all nodes, 2 some nodes}  
    foreach $listref (@{$nodelistrefs}) {
	$nodenum=$listref->[0];

	# check only nodes which are described by this subtree of the scheme
	next if($nodenum<$schemeref->{ATTR}->{min});
	next if($nodenum>$schemeref->{ATTR}->{max});
	if($#{$listref}==0) {
	    $covered[$nodenum]=1; # node full covered
	    print "_reduce_nodelist: $xspace # nodenum=$nodenum full covered (",join(',',@{$listref}),")\n" if($debug>=2); 
	} else {
	    my(@list);
	    $covered[$nodenum]=2 if($covered[$nodenum]==0);
	    print "_reduce_nodelist: $xspace # nodenum=$nodenum not full covered, rescan on sub-level (",join(',',@{$listref}),")\n" if($debug>=2); 

	    # remove top elem and add it to sublist
	    @list=@{$listref};shift(@list);push(@{$shortlists[$nodenum]},\@list);
	}
    }

    # check childs of all nodes which are not fully covered
    $allcovered=1;
    for($nodenum=$schemeref->{ATTR}->{min};$nodenum<=$schemeref->{ATTR}->{max};$nodenum++) { 
	# partly covered
	if($covered[$nodenum]==2) {
	    # partly covered, go down
	    my $allcovered_=1;
	    foreach $child (@{$schemeref->{_childs}}) {
		my(@newsublist,@newsizelist,$allcovered__,$allsize__);
		($allcovered__,$allsize__)=$self->_reduce_nodelist($child,$shortlists[$nodenum],\@newsublist,\@newsizelist);
		$allcovered_=0 if(!$allcovered__);
		$allsize[$nodenum]+=$allsize__;
		push(@{$newshortlists[$nodenum]},@newsublist);
		push(@{$newshortsizelists[$nodenum]},@newsizelist);
		print "_reduce_nodelist: $xspace # covered[$nodenum]==2 allsize[$nodenum]=$allsize[$nodenum] allsize__=$allsize__\n" if($debug>=3); 
	    }
	    # reduce if all covered
	    if($allcovered_) {
		$covered[$nodenum]=1;
		$newshortlists[$nodenum]=[];
		$newshortsizelists[$nodenum]=[];
	    } else {
		$allcovered=0;
	    }
	} elsif($covered[$nodenum]==1) {
	    # full covered, get size
	    $allsize[$nodenum]=$self->_get_size_for_node($schemeref,$nodenum);
	    
	} elsif($covered[$nodenum]==0) {
	    $allcovered=0;
	    $allsize[$nodenum]=0;
	}
    }

    # compute total size of job in this subtree
    for($allsize=0,$nodenum=$schemeref->{ATTR}->{min};$nodenum<=$schemeref->{ATTR}->{max};$nodenum++) { 
	$allsize+=$allsize[$nodenum];
    }
    
    # build new node list
    if($allcovered) {
	
	push(@{$newnodelistrefs},[$schemeref->{ATTR}->{min}."-".$schemeref->{ATTR}->{max}]);
	push(@{$newsizelistrefs},[$allsize]);

    } else {
	# build list of allcovered subnodes
	my(@nlist,$newlist);
	for($nodenum=$schemeref->{ATTR}->{min};$nodenum<=$schemeref->{ATTR}->{max};$nodenum++) { 
	    if($covered[$nodenum]==1) {
		push(@nlist, $nodenum); 
	    }
	}
	if(@nlist) {
	    $newlist=reduce_list(@nlist);
	    push(@{$newnodelistrefs},[$newlist]); 
	    
	    $allsizelist=&create_size_list( $newlist,\@allsize);
	    push(@{$newsizelistrefs},[$allsizelist]);
	}


	# build list of partly covered nodes
	for($nodenum=$schemeref->{ATTR}->{min};$nodenum<=$schemeref->{ATTR}->{max};$nodenum++) { 
	    if($covered[$nodenum]==2) {
		my(@nlist,$newlist,$nd);
		for($nd=$nodenum;$nd<=$schemeref->{ATTR}->{max};$nd++) { 
		    if($covered[$nd]==2) {
			if(&twolist_compare($newshortlists[$nodenum],$newshortlists[$nd])) {
			    push(@nlist,$nd);
			    $covered[$nd]=-1;
			}
		    }
		}
		$newlist     = reduce_list(@nlist);
		$allsizelist = &create_size_list( $newlist,\@allsize);
	
		foreach $listref (@{$newshortsizelists[$nodenum]}) {
		    unshift(@$listref,  $allsizelist );
		    push(@{$newsizelistrefs},$listref);
		}
		foreach $listref (@{$newshortlists[$nodenum]}) {
		    unshift(@$listref,  $newlist );
		    push(@{$newnodelistrefs},$listref);
		}
	    }
	}
	
    }

    print "_reduce_nodelist: $xspace # END   level=$level scheme min..max=$schemeref->{ATTR}->{min}..$schemeref->{ATTR}->{max} allsize=$allsize\n" if($debug>=2); 
    print "_reduce_nodelist: $xspace #       nodes ",&print_nodelists($newnodelistrefs),"\n" if($debug>=3); 
    print "_reduce_nodelist: $xspace #       sizes ",&print_nodelists($newsizelistrefs),"\n" if($debug>=3); 
    return($allcovered,$allsize);
}

# create list of sizes, according to given reduced nodelist 
sub create_size_list {
    my($list,$allsizeref) = @_;
    my($allsize,$myspec,@allsizelist,$nodenum);

    
    print "create_size_list >$list<\n" if($debug>=3); 
    if($list=~/^\((.*)\)$/) {
	my $sublist=$1;
	my $newlist=&create_size_list($sublist,$allsizeref);
	push(@allsizelist,$newlist);
    } else {
        foreach $myspec (split(',',$list)) { 
	    print "create_size_list myspec=>$myspec<\n" if($debug>=3); 
	    if($myspec=~/(\d+)\-(\d+)/) {
		$allsize=0;
		for($nodenum=$1;$nodenum<=$2;$nodenum++) {
		    $allsize+=$allsizeref->[$nodenum];
		}
		push(@allsizelist,$allsize);
	    } else {
		$nodenum=$myspec;
		push(@allsizelist,$allsizeref->[$nodenum]);
	    }
	}
    }
    if($#allsizelist>0) {
	return("(" . (join(',',@allsizelist)) .")");
    } else {
	return($allsizelist[0]);
    }
    
}

sub twolist_compare  {
    my($listref1) = shift;
    my($listref2) = shift;
    my($l1,$l2, $rc);
    
    $rc=1;
#    print "Compare1:", Dumper($listref1),"\n"; 
#    print "Compare2:", Dumper($listref2),"\n"; 
    if($#{$listref1} ne $#{$listref2}) {
	return(0);
    }
    for($l1=0;$l1<=$#{$listref1};$l1++) {
	if($#{$listref1->[$l1]} ne $#{$listref2->[$l1]}) {
	    return(0);
	}
	for($l2=0;$l2<=$#{$listref1->[$l1]};$l2++) {
	    $rc=0 if($listref1->[$l1]->[$l2] ne $listref2->[$l1]->[$l2]);
	}
    }
    return($rc);
}

sub reduce_list  {
    my(@list) = @_;
    my(@newlist,$i,$start,$lastfound);

    return("") if(!@list);
    $lastfound=$list[0];
    $start=$list[0];
    for($i=1;$i<=$#list;$i++) {
	if(($list[$i]-1) == $lastfound) {
	    $lastfound++;
	} else {
	    if($start != $lastfound) {push(@newlist, $start."-".$lastfound)} 
	    else                     {push(@newlist, $start)};
	    $lastfound=$list[$i];
	    $start=$lastfound;
	}
    }
    if($start != $lastfound) {push(@newlist, $start."-".$lastfound)} 
    else                     {push(@newlist, $start)};

    if($#newlist>0) {
	return("(" . (join(',',@newlist)) .")");
    } else {
	return($newlist[0]);
    }
}


sub _get_size_for_node  {
    my($self) = shift;
    my($schemeref,$nodenum) = @_;
    my($allsize,$child);
    my $subchilds=$#{$schemeref->{_childs}}+1;
    my $level=$schemeref->{_level};
    my $xspace=" "x$level;

    print "_get_size_for_node: $xspace # START nodenum=$nodenum subchilds=$subchilds\n" if($debug>=2); 

    $allsize=0;
    if($subchilds>0) {
	foreach $child (@{$schemeref->{_childs}}) {
	    next if($nodenum<$schemeref->{ATTR}->{min});
	    next if($nodenum>$schemeref->{ATTR}->{max});
	    $allsize+=$self->_get_size_of_node($child);
	}
    } else {
	$allsize=1;
    }
    
    print "_get_size_for_node: $xspace # END  nodenum=$nodenum allsize=$allsize\n" if($debug>=2); 


    return($allsize);
}

sub _get_size_of_node  {
    my($self) = shift;
    my($schemeref) = @_;
    my($allsize,$child);
    my $subchilds=$#{$schemeref->{_childs}}+1;
    my $level=$schemeref->{_level};
    my $xspace=" "x$level;

    print "_get_size_of_node:   $xspace # START level=$level subchilds=$subchilds\n" if($debug>=2); 

    $allsize=0;
    if($subchilds>0) {
	foreach $child (@{$schemeref->{_childs}}) {
	    $allsize+=$self->_get_size_of_node($child);
	}
    } else {
	$allsize=$schemeref->{ATTR}->{max}-$schemeref->{ATTR}->{min}+1;
    }
    
    print "_get_size_of_node:   $xspace # START allsize=$allsize\n" if($debug>=2); 

    return($allsize);
}


sub print_nodelists  {
    my($nodelistrefs) = @_;
    my($listref);
    my $result="";
    foreach $listref (@{$nodelistrefs}) {
	$result.="(".print_nodelist($listref).")";
    }
    return($result);
}

sub print_nodelist  {
    my($nodelistref) = @_;
    return("".join(",",@{$nodelistref}));
}

################################################################################################################
# get_numbers_from_name: nodename to location numbers (main)
#
#  Parameters:
#   name:       node name 
#   schemeref:  Reference to scheme tree 
# 
#  Description:
#   Maps nodename to a list of numbers, giving for each level of the scheme tree
#   the ordering number of the node on that level
#
#  Return value:
#   List of numbers
#
################################################################################################################
sub get_numbers_from_name  {
    my($self) = shift;
    my($name) = shift;
    my($schemeref) = shift;
    my($id,$listref,$format,$child);
   
    # check for each child recursively if nodename matches to this tree
    foreach $child (@{$schemeref->{_childs}}) {
	$listref=$self->_get_numbers_from_name($child,$name);
	last if(defined($listref)); 
    }
    if(!defined($listref)) {
	print "get_numbers_from_name: not found >$name<\n";
    }
    return($listref);
}

################################################################################################################
# _get_numbers_from_name: nodename to location numbers (recursive)
#
#  Parameters:
#   name:       node name 
#   schemeref:  Reference to scheme tree 
# 
#  Description:
#   Maps nodename to a list of numbers, giving for each level of the scheme tree
#   the ordering number of the node on that level
#
#  Return value:
#   List of numbers
#  
################################################################################################################
sub _get_numbers_from_name {
    my($self) = shift;
    my($schemeref)=shift;
    my($name)=shift;
    my($rg,$child,@list,$listref);
    
    $listref=undef;

    # get regexp match full node names to this level  
    $rg=$schemeref->{ATTR}->{_maskregall};

    # ready if nodename matches to this level 
#    print "get_numbers_from_name: check on level ",$schemeref->{_level}+1," $name -> $rg subcheck ",$name=~/^$rg/,"\n" if($debug>=2); 
    if($name=~/^$rg$/) {
	@list=$name=~/^$rg$/;
	print "get_numbers_from_name: found on level ",$schemeref->{_level}+1," $name -> ",join(',',@list),"\n" if($debug>=2); 
	$listref=\@list;
	return(\@list);
    } else {
	# search if nodename matches for one of the child nodes
	foreach $child (@{$schemeref->{_childs}}) {
	    $listref=$self->_get_numbers_from_name($child,$name);
	    last if(defined($listref)); 
	}
    }

    # remap strings to number on that level if map attribute is used instead of mask
    # mapping is given as an hash attached to this tree node 
    if(defined($listref)) {
	if(exists($schemeref->{ATTR}->{_map})) {
	    if(exists($schemeref->{ATTR}->{_map}->{$listref->[$schemeref->{_level}-1]})) {
		$listref->[$schemeref->{_level}-1]=$schemeref->{ATTR}->{_map}->{$listref->[$schemeref->{_level}-1]};
	    }
	    
	}
    }
    
    return($listref);
}

1;
