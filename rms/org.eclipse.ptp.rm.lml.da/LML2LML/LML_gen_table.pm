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
package LML_gen_table;

my($debug)=0;

use strict;
use Data::Dumper;
use Time::Local;
use Time::HiRes qw ( time );

sub new {
    my $self    = {};
    my $proto   = shift;
    my $class   = ref($proto) || $proto;
    my $verbose = shift;
    my $timings = shift;
    printf("\t LML_gen_table: new %s\n",ref($proto)) if($debug>=3);
    $self->{VERBOSE}   = $verbose; 
    $self->{TIMINGS}   = $timings; 
    $self->{LMLFH}     = undef; 
    $self->{LAYOUT}    = undef; 
    $self->{IDLISTREF} = undef; 
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
    my $filehandler_LML  = shift;
    my ($numids,$gid,$idlistref);
    $numids=0;
    $self->{LAYOUT}    = $layoutref; 
    $self->{LMLFH}     = $filehandler_LML; 
    
    $gid=$layoutref->{gid};
    
    $idlistref=[];
    print "LML_gen_table::process: gid=$gid\n" if($self->{VERBOSE});
    if(uc($gid) eq "JOBLIST_RUN") {
	$idlistref=$self->_select_run_jobs();
    }

    if(uc($gid) eq "JOBLIST_WAIT") {
	$idlistref=$self->_select_wait_jobs();
    }

    $self->{IDLISTREF}=$idlistref;
    $numids=scalar @{$idlistref};
#    print "LML_gen_table::process: idlist=(",join(',',sort(@{$idlistref})),")\n" if($self->{VERBOSE});
    
    $self->{VALIDATTR}=$self->_update_valid_attributes();

    return($numids);
}

sub _update_valid_attributes {
    my($self) = shift;
    my ($key,$k,%validattr,@validattr);
    
    foreach $key (@{$self->{IDLISTREF}}) {
	foreach $k (keys %{$self->{LMLFH}->{DATA}->{INFODATA}->{$key}}) {
	    $validattr{$k}++;
	}
    }
    @validattr=(sort keys(%validattr));
    return(\@validattr);
}

sub _select_run_jobs {
    my($self) = shift;
    my (@idlist,$key,$ref);
    
    while(($key,$ref)=each(%{$self->{LMLFH}->{DATA}->{OBJECT}})) {
	next if($ref->{type} ne 'job');
	next if($self->{LMLFH}->{DATA}->{INFODATA}->{$key}->{state} ne 'Running');
	push(@idlist,$key);
    }
    return(\@idlist);
}

sub _select_wait_jobs {
    my($self) = shift;
    my (@idlist,$key,$ref);
    
    while(($key,$ref)=each(%{$self->{LMLFH}->{DATA}->{OBJECT}})) {
	next if($ref->{type} ne 'job');
	next if($self->{LMLFH}->{DATA}->{INFODATA}->{$key}->{state} ne 'Idle');
	push(@idlist,$key);
    }
    
    return(\@idlist);
}


sub get_lml_table {
    my($self) = shift;
    my($ds,$rc,$id,$cid);
    my $layoutref  = $self->{LAYOUT};
    my(@keylist,$key,$value);

    $ds->{id}=$layoutref->{gid};
    $ds->{title}=$layoutref->{gid};
    
    foreach $cid (sort {$a <=> $b} (keys(%{$layoutref->{column}}))) {
	$ds->{column}->{$cid}->{id}=$cid;
	$ds->{column}->{$cid}->{name}=$layoutref->{column}->{$cid}->{key};
	$ds->{column}->{$cid}->{sort}="alpha";
	push(@keylist,$layoutref->{column}->{$cid}->{key});
    }
    
    foreach $id (@{$self->{IDLISTREF}}) {
	foreach $key (@keylist) {
	    if(exists($self->{LMLFH}->{DATA}->{INFODATA}->{$id}->{$key})) {
		$value=$self->{LMLFH}->{DATA}->{INFODATA}->{$id}->{$key};
	    } else {
		$value="?";
	    }
	    push(@{$ds->{row}->{$id}->{cell}},$value);
	}
    }
    
    return($ds);

}


sub get_lml_tablelayout {
    my($self) = shift;
    my($ds,$rc,$id,$cid);
    my $layoutref  = $self->{LAYOUT};
    my($key,$value,%activekeys,$lastcid);

    $ds->{id}=$layoutref->{id};
    $ds->{gid}=$layoutref->{gid};
    
    $lastcid=0;
    foreach $cid (sort {$a <=> $b} (keys(%{$layoutref->{column}}))) {
	$ds->{column}->{$cid}->{cid}=$cid;
	$ds->{column}->{$cid}->{key}=$layoutref->{column}->{$cid}->{key};
	$ds->{column}->{$cid}->{pos}=$layoutref->{column}->{$cid}->{pos};
	$ds->{column}->{$cid}->{width}=$layoutref->{column}->{$cid}->{width};
	$ds->{column}->{$cid}->{active}=$layoutref->{column}->{$cid}->{active};
	$lastcid=$cid if($cid>$lastcid);
	$activekeys{$ds->{column}->{$cid}->{key}}++;
    }

    foreach $key (@{$self->{VALIDATTR}}) {
	next if($activekeys{$key});
	$lastcid++;$cid=$lastcid;
	$ds->{column}->{$cid}->{cid}=$cid;
	$ds->{column}->{$cid}->{key}=$key;
	$ds->{column}->{$cid}->{width}="1";
	$ds->{column}->{$cid}->{active}="false";
	$lastcid=$cid if($cid>$lastcid);
    }

    return($ds);

}

1;
