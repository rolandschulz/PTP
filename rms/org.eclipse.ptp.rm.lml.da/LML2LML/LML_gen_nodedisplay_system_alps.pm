#*******************************************************************************
#* Copyright (c) 2011-2012 Forschungszentrum Juelich GmbH.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*
#* Contributors:
#*    Wolfgang Frings, Carsten Karbach (Forschungszentrum Juelich GmbH) 
#*******************************************************************************/ 
package LML_gen_nodedisplay;
my($debug)=0;
my($generateusage)=1;
use strict;
use Time::Local;
use Time::HiRes qw ( time );
use Data::Dumper;

###############################################
# ALPS related
############################################### 
sub _adjust_layout_alps  {
    my($self) = shift;
    my($root_layout,$root_scheme,$treenode,$ltreenode,$streenode,$num,$min,$max,$lmin,$lmax);
    my $rc=1;
    my $maxlevel=6;
  
    $root_layout=$self->{LAYOUT}->{tree};
    $root_scheme=$self->{SCHEMEROOT};

    # COLS
    ######
    $streenode=$root_scheme->get_child({_name => "el1" });
    $ltreenode=$root_layout->get_child({_name => "el0" });

    # get number of rows (in el1 of scheme)
    if($streenode) {
	$min=$streenode->{ATTR}->{min};
	$max=$streenode->{ATTR}->{max};
	$num=$max-$min+1;
	
    } else {
	print STDERR "$0: ERROR: inconsistent scheme tree for ALPS system (cols) ...\n";return(0);
    }
    
    if(!$ltreenode) {
	$ltreenode=$root_layout->new_child();
    }
    # set size attributes
    $ltreenode->{ATTR}->{rows}=1;
    $ltreenode->{ATTR}->{cols}=$num;

    # set some default layout attributes
    $ltreenode->{ATTR}->{maxlevel} = 4              if(!exists($ltreenode->{ATTR}->{maxlevel}));
    $ltreenode->{ATTR}->{vgap} = 5                  if(!exists($ltreenode->{ATTR}->{vgap}));
    $ltreenode->{ATTR}->{hgap} = 0                  if(!exists($ltreenode->{ATTR}->{hgap}));       
    $ltreenode->{ATTR}->{fontsize} = 10             if(!exists($ltreenode->{ATTR}->{fontsize}));   
    $ltreenode->{ATTR}->{border}   = 0              if(!exists($ltreenode->{ATTR}->{border}));     
    $ltreenode->{ATTR}->{fontfamily} = "Monospaced" if(!exists($ltreenode->{ATTR}->{fontfamily})); 
    $ltreenode->{ATTR}->{showtitle}  = "false"      if(!exists($ltreenode->{ATTR}->{showtitle}));  
    $ltreenode->{ATTR}->{background} = "#777"       if(!exists($ltreenode->{ATTR}->{background})); 
    $ltreenode->{ATTR}->{mouseborder}= 0            if(!exists($ltreenode->{ATTR}->{mouseborder})); 
    $ltreenode->{ATTR}->{transparent}= "false"      if(!exists($ltreenode->{ATTR}->{transparent}));
    $lmin=$min;$lmax=$max;

    # Rack
    ######
    $streenode=$streenode->get_child({_name => "el2" });

    # get number of rows (in el1 of scheme)
    if($streenode) {
	$min=$streenode->{ATTR}->{min};
	$max=$streenode->{ATTR}->{max};
	$num=$max-$min+1;
    } else {
	print STDERR "$0: ERROR: inconsistent scheme tree for ALPS system (rows) ...\n";return(0);
    }
    
    $treenode=$ltreenode->get_child({_name => "el1" });
    if(!$treenode) {
	$treenode=$ltreenode->new_child();
    } 
    $ltreenode=$treenode;

    # set size attributes
    $ltreenode->{ATTR}->{rows}=$num;    $ltreenode->{ATTR}->{cols}=1;
    $ltreenode->{ATTR}->{min}=$lmin;    $ltreenode->{ATTR}->{max}=$lmax;

    # set some default layout attributes
    $ltreenode->{ATTR}->{maxlevel} = 5              if(!exists($ltreenode->{ATTR}->{maxlevel}));
    $ltreenode->{ATTR}->{vgap} = 5                  if(!exists($ltreenode->{ATTR}->{vgap}));
    $ltreenode->{ATTR}->{hgap} = 0                  if(!exists($ltreenode->{ATTR}->{hgap}));       
#    $ltreenode->{ATTR}->{fontsize} = 10             if(!exists($ltreenode->{ATTR}->{fontsize}));   
#    $ltreenode->{ATTR}->{border}   = 0              if(!exists($ltreenode->{ATTR}->{border}));     
#    $ltreenode->{ATTR}->{fontfamily} = "Monospaced" if(!exists($ltreenode->{ATTR}->{fontfamily})); 
    $ltreenode->{ATTR}->{showtitle}  = "true"       if(!exists($ltreenode->{ATTR}->{showtitle}));  
#    $ltreenode->{ATTR}->{background} = "#777"       if(!exists($ltreenode->{ATTR}->{background})); 
#    $ltreenode->{ATTR}->{mouseborder}= 0            if(!exists($ltreenode->{ATTR}->{mouseborder})); 
#    $ltreenode->{ATTR}->{transparent}= "false"      if(!exists($ltreenode->{ATTR}->{transparent}));
    $lmin=$min;$lmax=$max;

    # Cage
    #######
    $streenode=$streenode->get_child({_name => "el3" });

    # get number of cages (in el2 of scheme)
    if($streenode) {
	$min=$streenode->{ATTR}->{min};
	$max=$streenode->{ATTR}->{max};
	$num=$max-$min+1;
    } else {
	print STDERR "$0: ERROR: inconsistent scheme tree for BG system (racks) ...\n";return(0);
    }
    
    $treenode=$ltreenode->get_child({_name => "el2" });
    if(!$treenode) {
	$treenode=$ltreenode->new_child();
    } 
    $ltreenode=$treenode;

    # set size attributes
    $ltreenode->{ATTR}->{rows}=$num;    $ltreenode->{ATTR}->{cols}=1;
    $ltreenode->{ATTR}->{min}=$lmin;    $ltreenode->{ATTR}->{max}=$lmax;

    # set some default layout attributes
    $ltreenode->{ATTR}->{maxlevel} = 6             if(!exists($ltreenode->{ATTR}->{maxlevel}));
    $ltreenode->{ATTR}->{showtitle}  = "true"      if(!exists($ltreenode->{ATTR}->{showtitle}));  
    $lmin=$min;$lmax=$max;

    # blade
    #######
    $streenode=$streenode->get_child({_name => "el4" });

    # get number of slots (in el3 of scheme)
    if($streenode) {
	$min=$streenode->{ATTR}->{min};
	$max=$streenode->{ATTR}->{max};
	$num=$max-$min+1;
    } else {
	print STDERR "$0: ERROR: inconsistent scheme tree for BG system (midplanes) ...\n";return(0);
    }
    
    $treenode=$ltreenode->get_child({_name => "el3" });
    if(!$treenode) {
	$treenode=$ltreenode->new_child();
    } 
    $ltreenode=$treenode;

    # set size attributes
    $ltreenode->{ATTR}->{rows}=1;       $ltreenode->{ATTR}->{cols}=$num;
    $ltreenode->{ATTR}->{min}=$lmin;    $ltreenode->{ATTR}->{max}=$lmax;

    # set some default layout attributes
    $ltreenode->{ATTR}->{maxlevel}         = 6             if(!exists($ltreenode->{ATTR}->{maxlevel}));
    $ltreenode->{ATTR}->{showtitle}        = "false"        if(!exists($ltreenode->{ATTR}->{showtitle}));  
    $ltreenode->{ATTR}->{highestrowfirst}  = "true"        if(!exists($ltreenode->{ATTR}->{highestrowfirst}));  
    $ltreenode->{ATTR}->{showfulltitle}    = "false"        if(!exists($ltreenode->{ATTR}->{showfulltitle}));  
    $lmin=$min;$lmax=$max;


    # Node
    ######
    $streenode=$streenode->get_child({_name => "el5" });

    # get number of midplanes (in el4 of scheme)
    if($streenode) {
	$min=$streenode->{ATTR}->{min};
	$max=$streenode->{ATTR}->{max};
	$num=$max-$min+1;
    } else {
	print STDERR "$0: ERROR: inconsistent scheme tree for BG system (nodeboards) ...\n";return(0);
    }
    
    $treenode=$ltreenode->get_child({_name => "el4" });
    if(!$treenode) {
	$treenode=$ltreenode->new_child();
    } 
    $ltreenode=$treenode;

    # set size attributes
    $ltreenode->{ATTR}->{rows}=$num;    $ltreenode->{ATTR}->{cols}=1;
    $ltreenode->{ATTR}->{min}=$lmin;    $ltreenode->{ATTR}->{max}=$lmax;

    # set some default layout attributes
    $ltreenode->{ATTR}->{maxlevel}         = $maxlevel     if(!exists($ltreenode->{ATTR}->{maxlevel}));
    $ltreenode->{ATTR}->{showtitle}        = "false"        if(!exists($ltreenode->{ATTR}->{showtitle}));  
    $ltreenode->{ATTR}->{fontsize}         = 8             if(!exists($ltreenode->{ATTR}->{fontsize}));   
    $ltreenode->{ATTR}->{vgap}             = 0             if(!exists($ltreenode->{ATTR}->{vgap}));
    $ltreenode->{ATTR}->{hgap}             = 0             if(!exists($ltreenode->{ATTR}->{hgap}));       
    $ltreenode->{ATTR}->{highestrowfirst}  = "true"        if(!exists($ltreenode->{ATTR}->{highestrowfirst}));  
    $ltreenode->{ATTR}->{showfulltitle}    = "false"       if(!exists($ltreenode->{ATTR}->{showfulltitle}));  
    $lmin=$min;$lmax=$max;

    # cores
    #######
    $streenode=$streenode->get_child({_name => "el6" });

    # get number of cores (in el6 of scheme)
    if($streenode) {
	$min=$streenode->{ATTR}->{min};
	$max=$streenode->{ATTR}->{max};
	$num=$max-$min+1;
    } else {
	print STDERR "$0: ERROR: inconsistent scheme tree for ALPS system (cores) ...\n";return(0);
    }
    
    $treenode=$ltreenode->get_child({_name => "el5" });
    if(!$treenode) {
	$treenode=$ltreenode->new_child();
    } 
    $ltreenode=$treenode;

    # set size attributes
    if($num%8==0) {
		$ltreenode->{ATTR}->{cols}=8;
    } else {
		$ltreenode->{ATTR}->{cols}=12;
    }
    $ltreenode->{ATTR}->{rows}=1;       
    $ltreenode->{ATTR}->{min}=$lmin;    $ltreenode->{ATTR}->{max}=$lmax;

    # set some default layout attributes
    $ltreenode->{ATTR}->{maxlevel}         = $maxlevel     if(!exists($ltreenode->{ATTR}->{maxlevel}));
    $lmin=$min;$lmax=$max;


#    print "$0: LAYOUT: ",Dumper($root_layout);
#    print "$0: SCHEME: ",Dumper($root_scheme);

    return($rc);
}


###############################################
# ALPS related
############################################### 
sub _get_system_size_alps {
    my($self) = shift;
    my($indataref) = $self->{INDATA};
    my($nodename,$pcol,$prow,$pcage,$pslot,$pnode,$pcore);
    my($maxpcol,$maxprow,$maxpcage,$maxpslot,$maxpnode,$maxpcore);

    my ($key,$ref);
    
    $maxpcol=    $maxprow=    $maxpcage=    $maxpslot=    $maxpnode= $maxpcore = 0;
    keys(%{$self->{LMLFH}->{DATA}->{OBJECT}}); # reset iterator
    while(($key,$ref)=each(%{$self->{LMLFH}->{DATA}->{OBJECT}})) {
	next if($ref->{type} ne 'node');
	$nodename=$self->{LMLFH}->{DATA}->{OBJECT}->{$key}->{name};
	if($nodename=~/^c(\d+)-(\d+)c(\d+)s(\d+)n(\d+)$/) {
	    ($pcol,$prow,$pcage,$pslot,$pnode)=($1,$2,$3,$4,$5);
	} else {
	    print "_get_system_size_alps: node name could not be parsed: $nodename\n";
	}
	if(exists($self->{LMLFH}->{DATA}->{INFODATA}->{$key}->{HW})) {
	    $pcore=$self->{LMLFH}->{DATA}->{INFODATA}->{$key}->{HW}-1;
	} else {
	    $pcore=0;
	}

	$maxpcol=$pcol if($pcol>$maxpcol);
	$maxprow=$prow if($prow>$maxprow);
	$maxpcage=$pcage if($pcage>$maxpcage);
	$maxpslot=$pslot if($pslot>$maxpslot);
	$maxpnode=$pnode if($pnode>$maxpnode);
	$maxpcore=$pcore if($pcore>$maxpcore);
    }

    $self->{MAXCORESCHECK}=$maxpcore;

    printf("_get_system_size_alps: ALPS system found of size: col=%d row=%d cage=%d slot=%d node=%d core=%d\n",
	   $maxpcol+1,$maxprow+1,$maxpcage+1,$maxpslot+1,$maxpnode+1,$maxpcore+1 ) if($self->{VERBOSE});

    return($maxpcol,$maxprow,$maxpcage,$maxpslot,$maxpnode,$maxpcore);
}


sub _init_trees_alps  {
    my($self) = shift;
    my($maxpcol,$maxprow,$maxpcage,$maxpslot,$maxpnode,$maxpcore)=@_;
    my($id,$subid,$treenode,$schemeroot,$bgsystem);

    $schemeroot=$self->{SCHEMEROOT};
    $treenode=$schemeroot;
    $bgsystem=$treenode=$treenode->new_child();
    $treenode->add_attr({ tagname => 'col',
			  min     => 0,
			  max     => $maxpcol,
			  mask    => 'c%d-' });

    $treenode=$treenode->new_child();
    $treenode->add_attr({ tagname => 'rack',
			  min     => 0,
			  max     => $maxprow,
			  mask    => '%01d' });

    $treenode=$treenode->new_child();
    $treenode->add_attr({ tagname => 'cage',
			  min     => 0,
			  max     => $maxpcage,
			  mask    => 'c%01d' });

    $treenode=$treenode->new_child();
    $treenode->add_attr({ tagname => 'blade',
			  min     => 0,
			  max     => $maxpslot,
			  mask    => 's%01d' });

    $treenode=$treenode->new_child();
    $treenode->add_attr({ tagname => 'node',
			  min     => 0,
			  max     => $maxpnode,
			  mask    => 'n%01d' });


    $maxpcore=15 if ($maxpcore<=0);
    $treenode=$treenode->new_child();
    $treenode->add_attr({ tagname => 'core',
			  min     => 0,
			  max     => $maxpcore,
			  mask    => '-c%02d' });

    return(1);
}

1;
