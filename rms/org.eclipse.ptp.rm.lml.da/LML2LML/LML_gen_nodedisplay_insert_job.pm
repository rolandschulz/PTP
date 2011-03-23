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
use strict;
use Time::Local;
use Time::HiRes qw ( time );
use Data::Dumper;


################################################################################################################
# job insertion
################################################################################################################
sub insert_job_into_nodedisplay  {
    my($self) = shift;
    my($schemeref) = shift;
    my($dataref) = shift;
    my($nodelist) = shift;
    my($oid) = shift;
    my($data,$node,$listref,@nodelistrefs,@nodelistrefs_reduced, $allcovered, $child);

    foreach $node (sort(split(/\s*,\s*/,$nodelist))) {
	$listref=$self->get_numbers_from_name($node,$schemeref);
	push(@nodelistrefs,$listref);
    }
    
    foreach $listref (@nodelistrefs) {
	print "insert_job_into_nodedisplay, before: ",join(',',@{$listref}),"\n"  if($debug>=2); 
    }

    # for each system part
    foreach $child (@{$schemeref->{_childs}}) {
	$allcovered=$self->_reduce_nodelist($child,\@nodelistrefs,\@nodelistrefs_reduced);
    }

    print "insert_job_into_nodedisplay, allcovered=$allcovered\n"  if($debug>=2); 

    foreach $listref (@nodelistrefs_reduced) {
	$self->_insert_jobnode_nodedisplay($dataref,$listref,$oid);
    }


    return(1);
}

sub _insert_jobnode_nodedisplay {
    my($self) = shift;
    my($dataref)=shift;
    my($nodelistref)=shift;
    my($oid) = shift;
    my($myspec,$min,$max,$subspec,$child);
    my($lmin,$lmax,$subchilds);
    my $level=$dataref->{_level};
    my $xspace=" "x$level;
    my (@covered, @childlist, $newchild, @newlist, $isleaf, $i, @notcoveredlist, $newlist, $updatechild);
    
    print "_insert_jobnode_nodedisplay: $xspace # START level=$level nodelistref=>",join(',',@{$nodelistref}),"<\n"  if($debug>=2); 
    
    $myspec=$nodelistref->[0];
    if($myspec=~/^\((.*)\)$/) {
	my $list=$1;
	for $subspec (split(',',$list)) {
	    my @list = @{$nodelistref};
	    $list[0]=$subspec;
	    print "_insert_jobnode_nodedisplay: $xspace  -> re-calling with top subspec >$subspec<\n"  if($debug>=2); 
	    $self->_insert_jobnode_nodedisplay($dataref,\@list, $oid);
	}
	print "_insert_jobnode_nodedisplay: $xspace # END   level=$level nodelistref=>",join(',',@{$nodelistref}),"<\n" if($debug>=2); 
	return();
    } elsif($myspec=~/(\d+)\-(\d+)/) {
	$min=$1;$max=$2;
    } else {
	$min=$max=$myspec;
    }
    for($i=$min;$i<=$max;$i++) {$covered[$i]=1;}

    @newlist = @{$nodelistref};shift(@newlist);
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
		    print "_insert_jobnode_nodedisplay: $xspace  -> duplicate child   with (",($max+1),"..",($max),")\n" if($debug>=2); 
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
		$self->_insert_jobnode_nodedisplay($updatechild,\@newlist, $oid) ;
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
		$self->_insert_jobnode_nodedisplay($child,\@newlist, $oid) ;
	    }
 	    for($i=$min;$i<=$lmin;$i++) {$covered[$i]=0}
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
		$self->_insert_jobnode_nodedisplay($child,\@newlist, $oid) ;
	    }
 	    for($i=$min;$i<=$lmax;$i++) {$covered[$i]=0}
	}
    }

    # build entries for not already covered childs
    for($i=$min;$i<=$max;$i++) {
	push(@notcoveredlist,$i) if($covered[$i]==1);
    }
    if(@notcoveredlist) {
	$newlist=reduce_list(@notcoveredlist);$newlist=~s/\(//gs;$newlist=~s/\)//gs;
	print "_insert_jobnode_nodedisplay: $xspace  -> notcoveredlist= >$newlist<\n" if($debug>=2); 
	for $subspec (split(',',$newlist)) {
	    if($subspec=~/(\d+)\-(\d+)/) {$min=$1;$max=$2;  } 
	    else                         {$min=$max=$subspec;}
	}
	$newchild=$dataref->new_child();
	if($isleaf) {
	    print "_insert_jobnode_nodedisplay: $xspace  -> insert new child with (",($min),"..",($max),")\n" if($debug>=2); 
	    $newchild->add_attr({ oid => $oid, min => $min, max => $max });
	} else {
	    $newchild->add_attr({ min => $min, max => $max, oid => $dataref->{ATTR}->{oid} });
	    print "_insert_jobnode_nodedisplay: $xspace  -> insert new child with (",($min),"..",($max),"), inserting subnodes ...\n" if($debug>=2); 
	    $self->_insert_jobnode_nodedisplay($newchild,\@newlist, $oid) ;
	}
	
    }

    print "_insert_jobnode_nodedisplay: $xspace # END   level=$level nodelistref=>",join(',',@{$nodelistref}),"<\n" if($debug>=2); 

}    


sub _reduce_nodelist {
    my($self) = shift;
    my($schemeref)=shift;
    my($nodelistrefs)=shift;
    my($newnodelistrefs)=shift;
    my($rg,$child, $listref, @covered, $allcovered, $nodenum,@shortlists,@newshortlists, $lastfound);
    my $level=$schemeref->{_level};
    my $xspace=" "x$level;
    

    print "_reduce_nodelist: $xspace # START level=$level scheme min..max=$schemeref->{ATTR}->{min}..$schemeref->{ATTR}->{max}\n" if($debug>=2); 
    for($nodenum=$schemeref->{ATTR}->{min};$nodenum<=$schemeref->{ATTR}->{max};$nodenum++) {
	$covered[$nodenum]=0;
    }

    # check which treenodes are given in nodelist, covered = {0 no nodes, 1 all nodes, 2 some nodes}  
    foreach $listref (@{$nodelistrefs}) {
	$nodenum=$listref->[0];
	# check only nodes which are described by this subtree of the scheme
	next if($nodenum<$schemeref->{ATTR}->{min});
	next if($nodenum>$schemeref->{ATTR}->{max});
	print "_reduce_nodelist: $xspace # CHECK nodenum=$nodenum\n" if($debug>=2); 
	if($#{$listref}==0) {
	    # node full covered
	    $covered[$nodenum]=1;
	} else {
	    my(@list);
	    $covered[$nodenum]=2 if($covered[$nodenum]==0);
	    # remove top elem and add it to sublist
	    @list=@{$listref};shift(@list);push(@{$shortlists[$nodenum]},\@list);
	}
    }

    # check childs of all nodes which are not fully covered
    $allcovered=1;
    for($nodenum=$schemeref->{ATTR}->{min};$nodenum<=$schemeref->{ATTR}->{max};$nodenum++) { 
	if($covered[$nodenum]==2) {
	    my $allcovered_=1;
	    foreach $child (@{$schemeref->{_childs}}) {
		my(@newlist,$allcovered__);
		$allcovered__=$self->_reduce_nodelist($child,$shortlists[$nodenum],\@newlist);
		$allcovered_=0 if(!$allcovered__);
		push(@{$newshortlists[$nodenum]},@newlist);
	    }
	    # reduce if all covered
	    if($allcovered_) {
		$covered[$nodenum]=1;
		$newshortlists[$nodenum]=[];
	    } else {
		$allcovered=0;
	    }

	} elsif($covered[$nodenum]==0) {
	    $allcovered=0;
	}
    }
    
    # build new node list
    if($allcovered) {
	push(@{$newnodelistrefs},[$schemeref->{ATTR}->{min}."-".$schemeref->{ATTR}->{max}]);

    } else {
	# build list of allcovered subnodes
	my(@nlist,$newlist);
	for($nodenum=$schemeref->{ATTR}->{min};$nodenum<=$schemeref->{ATTR}->{max};$nodenum++) { 
	    push(@nlist, $nodenum) if($covered[$nodenum]==1);
	}
	if(@nlist) {
	    $newlist=reduce_list(@nlist);
	    push(@{$newnodelistrefs},[$newlist]); 
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
		$newlist=reduce_list(@nlist);
	
		foreach $listref (@{$newshortlists[$nodenum]}) {
		    unshift(@$listref,  $newlist );
		    push(@{$newnodelistrefs},$listref);
		}
	    }
	}
	
    }

    return($allcovered);
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



################################################################################################################
# help function
################################################################################################################
sub get_numbers_from_name  {
    my($self) = shift;
    my($name) = shift;
    my($schemeref) = shift;
    my($id,$listref,$format,$child);
   
    foreach $child (@{$schemeref->{_childs}}) {
	$listref=$self->_get_numbers_from_name($child,$name);
    }

    return($listref);
}

sub _get_numbers_from_name {
    my($self) = shift;
    my($schemeref)=shift;
    my($name)=shift;
    my($rg,$child,@list,$listref);


    $rg=$schemeref->{ATTR}->{_maskregall};
    if($name=~/^$rg$/) {
	@list=$name=~/^$rg$/;
	print "get_numbers_from_name: found on level ",$schemeref->{_level}+1," $name -> ",join(',',@list),"\n" if($debug>=2); 
	return(\@list);
    } else {
	foreach $child (@{$schemeref->{_childs}}) {
	    $listref=$self->_get_numbers_from_name($child,$name);
	    if(defined($listref)) {
		return($listref);
	    }
	}
    }
    return(undef);
}


1;
