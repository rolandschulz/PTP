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
use FindBin;
use lib "$FindBin::RealBin/../LML_specs";
use lib "$FindBin::RealBin/../lib";
use LML_specs;
use LML_da_util;

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

#
# returns a list of LML object ids which have to included in the objects
# and information tag of the resulting LML file
#
sub get_ids {
    my($self) = shift;
    return($self->{IDLISTREF});
}

#
# processes the raw LML file data and extracts objects according to the 
# requested pattern and generates from this a table structure
#
# Parameters:
#    
# filehandler_LML:  handler to raw LML file
#
sub process {
    my($self) = shift;
    my $layoutref  = shift;
    my $filehandler_LML  = shift;
    my ($numids,$gid,$contenttype,$idlistref,$objtype_pattern,$patternsref,$selectsref);
    $numids=0;
    $self->{LAYOUT}    = $layoutref; 
    $self->{LMLFH}     = $filehandler_LML; 
    
    $gid=$layoutref->{gid};
    $contenttype="jobs";#Default content type
    if( exists($layoutref->{contenthint}) ){
    	$contenttype = $layoutref->{contenthint};
    }
    if(!$contenttype) {
	print "LML_gen_table: ERROR no contenttype given for table $gid, skipping ...\n";
	return(-1);
    }

    # check content type
    if(lc($contenttype) eq "jobs") {
	$objtype_pattern="job";
    } elsif(lc($contenttype) eq "nodes") {
 	$objtype_pattern="node";
    } else {
	# contenttype is used directly to check objecttype
 	$objtype_pattern="$contenttype";
    }
    $self->{OBJTYPE_PATTERN}=$objtype_pattern;

    # check pattern
    $patternsref=$self->_extract_patterns($layoutref);

    # check select
    $selectsref=$self->_extract_selects($layoutref);

    $idlistref=[];
    print "LML_gen_table::process: gid=$gid contenttype=$contenttype objtype_pattern=$objtype_pattern\n" 
	if($self->{VERBOSE});

    $idlistref=$self->_select_objs($objtype_pattern, $patternsref, $selectsref);

    $self->{IDLISTREF}=$idlistref;
    $numids=scalar @{$idlistref};
#    print "LML_gen_table::process: idlist=(",join(',',sort(@{$idlistref})),")\n" if($self->{VERBOSE});
    
    $self->{VALIDATTR}=$self->_update_valid_attributes();

    return($numids);
}

sub _extract_patterns {
    my($self) = shift;
    my($layoutref) = @_;
    my(%patterns,$cid,$key,$ptype,$regexp,$ref);
    
    foreach $cid (keys(%{$layoutref->{column}})) {
	if(exists($layoutref->{column}->{$cid}->{key})) {
	    $key=$layoutref->{column}->{$cid}->{key};
	} else {
	    print "LML_gen_table: ERROR, could not find key for column $cid of table $layoutref->{gid}, skipping column ...\n";
	    next;
	}
	$patterns{$key}=".*"; # default
	if(exists($layoutref->{column}->{$cid}->{pattern})) {
	my (@patlist);
	foreach $ref (@{$layoutref->{column}->{$cid}->{pattern}}) {
	    ($ptype,$regexp)=@{$ref};
	    if($ptype eq "include") {
		push(@patlist,"$regexp");
	    }
	    if($ptype eq "exclude") {
		push(@patlist,"($regexp){0}");
	    }
	}
	$patterns{$key}="(".join('|',@patlist).")";
	}
    }
#    print "_extract_patterns $layoutref->{gid} -> ",Dumper(\%patterns),"\n";
    return(\%patterns);
}

sub _extract_selects {
    my($self) = shift;
    my($layoutref) = @_;
    my(%selects,$cid,$key,$ptype,$rel,$value,$ref);
    
    foreach $cid (keys(%{$layoutref->{column}})) {
		if(exists($layoutref->{column}->{$cid}->{key})) {
		    $key=$layoutref->{column}->{$cid}->{key};
		} else {
		    print "LML_gen_table: ERROR, could not find key for column $cid of table $layoutref->{gid}, skipping column ...\n";
		    next;
		}
		$selects{$key}=undef; # default
		if(exists($layoutref->{column}->{$cid}->{pattern})) {
			my (@selectlist);
			foreach $ref (@{$layoutref->{column}->{$cid}->{pattern}}) {
			    if($ref->[0] eq "select") {
				($ptype,$rel,$value)=@{$ref};
				$rel=&LML_da_util::unescape_special_characters($rel);
				push(@selectlist,[$rel,$value]);
			    }
			}
			if($#selectlist>=0) { 
			    $selects{$key}=[@selectlist];
			}
		}
    }
    return(\%selects);
}


sub _select_objs {
    my($self) = shift;
    my($objtype_pattern,$patternsref,$selectsref)=@_;
    my (@idlist,$key,$ref,$sref,$skey,$found,$regexp,$selects,$sort,$specref);
    

    keys(%{$self->{LMLFH}->{DATA}->{OBJECT}}); # reset iterator
    while(($key,$ref)=each(%{$self->{LMLFH}->{DATA}->{OBJECT}})) {
	$found=1;

	# check against contenttype
	next if($ref->{type} ne $objtype_pattern);

	# checking against include/exclude pattern
	foreach $skey (keys(%{$patternsref})) {
	    $regexp=$patternsref->{$skey};

	    if(exists($self->{LMLFH}->{DATA}->{INFODATA}->{$key}->{$skey})) {
		my $val=$self->{LMLFH}->{DATA}->{INFODATA}->{$key}->{$skey};

		if($val!~/$regexp/) {
		    $found=0; # not matched
#		    print "not matched for $key $skey $regexp\n";
		}
	    } else {
		if($regexp ne ".*") {
		    $found=0; # attribute not available, but user defined a pattern
		}
#		print "not found for $key $skey $regexp\n";
	    }
	}
	

	# checking against select pattern


	foreach $skey (keys(%{$selectsref})) {
	    $selects=$selectsref->{$skey};
	    $specref = $LML_specs::LMLattributes->{$objtype_pattern}->{$skey};

	    $sort="alpha"; # default
	    if(defined($specref->[0]) ){
		    $sort="alpha"   if ( ($specref->[0] eq "s") || ($specref->[0] eq "k") );
		    $sort="numeric" if ( ($specref->[0] eq "d") || ($specref->[0] eq "f") );
		    $sort="date"    if ( ($specref->[0] eq "D") );
		}
	    

	    if(exists($self->{LMLFH}->{DATA}->{INFODATA}->{$key}->{$skey})) {
		my $val=$self->{LMLFH}->{DATA}->{INFODATA}->{$key}->{$skey};

		if($selects) {
		    foreach $sref (@{$selects}) {
			my($rel,$refval)=(@{$sref});
			
			# equal
			$found=0 if ($sort eq "numeric") && ($rel eq "=") && (!($val == $refval));
			$found=0 if ($sort eq "alpha")   && ($rel eq "=") && (!($val eq $refval));
			$found=0 if ($sort eq "date")    && ($rel eq "=") && (!($val eq $refval));

			# not equal
			$found=0 if ($sort eq "numeric") && ($rel eq "!=") && (!($val != $refval));
			$found=0 if ($sort eq "alpha")   && ($rel eq "!=") && (!($val ne $refval));
			$found=0 if ($sort eq "date")    && ($rel eq "!=") && (!($val ne $refval));

			# less (equal)
			$found=0 if ($sort eq "numeric") && ($rel eq "lt")   && (!($val <  $refval));
			$found=0 if ($sort eq "numeric") && ($rel eq "le")  && (!($val <= $refval));
			$found=0 if ($sort eq "date") && ($rel eq "lt")   && (!($val lt  $refval));
			$found=0 if ($sort eq "date") && ($rel eq "le")  && (!($val le $refval));

			# greater (equal)
			$found=0 if ($sort eq "numeric") && ($rel eq "gt")   && (!($val >  $refval));
			$found=0 if ($sort eq "numeric") && ($rel eq "ge")  && (!($val >= $refval));
			$found=0 if ($sort eq "date") && ($rel eq "gt")   && (!($val gt  $refval));
			$found=0 if ($sort eq "date") && ($rel eq "ge")  && (!($val ge $refval));

			# (not) match reqexp
			$found=0 if ($sort eq "alpha") && ($rel eq "=~")   && (!($val=~/$refval/));
			$found=0 if ($sort eq "alpha") && ($rel eq "!~")   && (!($val!~/$refval/));
			
		    }
		}

	    } else {
		if($selects) {
		    $found=0; # attribute not available, but user defined a select
		}
#		print "skey not defined not found for $key $skey (select)\n";
	    } 
	}

	if($found) {
	    push(@idlist,$key);
	}
    }
    return(\@idlist);
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

sub get_lml_table {
    my($self) = shift;
    my($ds,$rc,$id,$cid);
    my $layoutref  = $self->{LAYOUT};
    my $tableref   = $self->{TABLE};
    my(%keylist,$key,$value,$ref, $objtype_pattern, $specref, $active, $lastcid);

    $objtype_pattern=$self->{OBJTYPE_PATTERN};

    # top level attributes of table
    $ds->{id}         = $layoutref->{gid};
    $ds->{title}      = $layoutref->{gid};
    $ds->{contenttype}= $tableref->{contenttype};
    if(!defined($ds->{contenttype})){
    	$ds->{contenttype} = $layoutref->{contenthint};
    }
    $ds->{description}= $tableref->{description} if(exists($tableref->{description}));
    
    # define columns
    foreach $cid (sort {$a <=> $b} (keys(%{$layoutref->{column}}))) {
	$key     = $layoutref->{column}->{$cid}->{key};
	$active  = $layoutref->{column}->{$cid}->{active};
	$specref = $LML_specs::LMLattributes->{$objtype_pattern}->{$key};
	if(!$specref) {
	    print STDERR "unknown table column requested $key, data may be corrupted ...";
	    $specref = ["s", "O", undef, "unknown column"];
	}

	next if ( ($active eq "false") and ($specref->[1] ne "M") );

	$ds->{column}->{$cid}->{name}          = $layoutref->{column}->{$cid}->{key};
	$ds->{column}->{$cid}->{id}            = $cid;
	$ds->{column}->{$cid}->{description}   = $specref->[3];
	$ds->{column}->{$cid}->{type}          = "mandatory" if($specref->[1] eq "M");
	$ds->{column}->{$cid}->{type}          = "optional"  if($specref->[1] ne "M");
	$ds->{column}->{$cid}->{sort}          = "alpha"     if($specref->[0] eq "s");
	$ds->{column}->{$cid}->{sort}          = "alpha"     if($specref->[0] eq "k");
	$ds->{column}->{$cid}->{sort}          = "date"      if($specref->[0] eq "D");
	$ds->{column}->{$cid}->{sort}          = "numeric"   if($specref->[0] eq "d");
	$ds->{column}->{$cid}->{sort}          = "numeric"   if($specref->[0] eq "f");
	if(exists($layoutref->{column}->{$cid})) {
	    if(exists($layoutref->{column}->{$cid}->{pattern})) {
		foreach $ref (@{$layoutref->{column}->{$cid}->{pattern}}) {
		    push(@{$ds->{column}->{$cid}->{pattern}},$ref);
		}
	    }
	}
	$keylist{$cid} = $layoutref->{column}->{$cid}->{key};
	#push(@keylist,$layoutref->{column}->{$cid}->{key});
    }
    
    # add data to table
    foreach $id (@{$self->{IDLISTREF}}) {
	$lastcid = 0;
	foreach $cid (sort {$a <=> $b} (keys %keylist)) {
		$key = $keylist{$cid};
	    if(exists($self->{LMLFH}->{DATA}->{INFODATA}->{$id}->{$key})) {
		$value=$self->{LMLFH}->{DATA}->{INFODATA}->{$id}->{$key};
	    } else {
		$value="?";
	    }
	    
	    $ds->{row}->{$id}->{cell}->{$cid}->{value} = $value;
	    if (($cid - $lastcid) > 1) {
	    	$ds->{row}->{$id}->{cell}->{$cid}->{cid} = $cid;
	    }
	    $lastcid = $cid;
	}
    }
    
    return($ds);

}


sub get_lml_tablelayout {
    my($self) = shift;
    my($ds,$rc,$id,$cid);
    my $layoutref  = $self->{LAYOUT};
    my($key,$value,%activekeys,$lastcid,$wsum,$wsumweight,$numcolumns);

    $ds->{id}=$layoutref->{id};
    $ds->{gid}=$layoutref->{gid};
    $ds->{contenthint} = $layoutref->{contenthint};
    $numcolumns=scalar keys(%{$layoutref->{column}});

    if($numcolumns>0) {

	# check width
	$wsum=0.0;
	foreach $cid (sort {$a <=> $b} (keys(%{$layoutref->{column}}))) {
	    next if($layoutref->{column}->{$cid}->{active} eq "false");
	    $layoutref->{column}->{$cid}->{width}=1.0 if(!exists($layoutref->{column}->{$cid}->{width}));
	    $layoutref->{column}->{$cid}->{width}=1.0 if($layoutref->{column}->{$cid}->{width}<=0);
	    $wsum+=$layoutref->{column}->{$cid}->{width};
	}
	if($wsum>0)  {$wsumweight=1.0/$wsum;}
	else         {$wsumweight=1.0;}
	foreach $cid (sort {$a <=> $b} (keys(%{$layoutref->{column}}))) {
	    next if($layoutref->{column}->{$cid}->{active} eq "false");
	    $layoutref->{column}->{$cid}->{width}*=$wsumweight;
	}

	# generate columns
	$lastcid=0;
	foreach $cid (sort {$a <=> $b} (keys(%{$layoutref->{column}}))) {
	    $ds->{column}->{$cid}->{cid}=$cid;
	    $ds->{column}->{$cid}->{key}=$layoutref->{column}->{$cid}->{key};
	    $ds->{column}->{$cid}->{pos}=$layoutref->{column}->{$cid}->{pos};
	    $ds->{column}->{$cid}->{width}=$layoutref->{column}->{$cid}->{width};
	    $ds->{column}->{$cid}->{active}=$layoutref->{column}->{$cid}->{active};
	    $ds->{column}->{$cid}->{sorted}=$layoutref->{column}->{$cid}->{sorted} if (exists ($layoutref->{column}->{$cid}->{sorted}));
	    if(exists($layoutref->{column}->{$cid})) {
		    if(exists($layoutref->{column}->{$cid}->{pattern})) {
			foreach my $ref (@{$layoutref->{column}->{$cid}->{pattern}}) {
			    push(@{$ds->{column}->{$cid}->{pattern}},$ref);
			}
		    }
		}
	    $lastcid=$cid if($cid>$lastcid);
	    $activekeys{$ds->{column}->{$cid}->{key}}++;
	}

	# add alterative columns (as inactive)
	foreach $key (@{$self->{VALIDATTR}}) {
	    next if($activekeys{$key});
	    $lastcid++;$cid=$lastcid;
	    $ds->{column}->{$cid}->{cid}=$cid;
	    $ds->{column}->{$cid}->{pos}=$cid - 1;
	    $ds->{column}->{$cid}->{key}=$key;
	    $ds->{column}->{$cid}->{width}="1";
	    $ds->{column}->{$cid}->{active}="false";
	    $lastcid=$cid if($cid>$lastcid);
	}
    }
    return($ds);

}

1;
