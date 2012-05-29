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
package LML_combine_obj_cluster;
use strict;
use Data::Dumper;

my($debug)=0;

my $patint="([\\+\\-\\d]+)";   # Pattern for Integer number
my $patfp ="([\\+\\-\\d.E]+)"; # Pattern for Floating Point number
my $patwrd="([\^\\s]+)";       # Pattern for Work (all noblank characters)
my $patbl ="\\s+";             # Pattern for blank space (variable length)

sub update {
    my($dataptr) = shift;
    my($dbdir) = shift;
    my(%gpuoffsets);

    &get_node_info($dataptr,\%gpuoffsets);

    &update_job_info($dataptr,\%gpuoffsets);

    return(1);
} 

# returns hash:  NID -> nodename
sub get_node_info {
    my($dataptr) = shift;
    my($gpuoffsetref) = shift;
    my($id,$ncores,$ngpus,$name);

    # scan for gpus
    foreach $id (keys(%{$dataptr->{OBJECT}})) {
	if($dataptr->{OBJECT}->{$id}->{type} eq "node") {
	    $name=$dataptr->{OBJECT}->{$id}->{name};
	    $ncores=$dataptr->{INFODATA}->{$id}->{ncores};
	    if(exists($dataptr->{INFODATA}->{$id}->{gpus})) {
		$gpuoffsetref->{$name}=$ncores;
	    }
	}
    }
    return();
}

sub update_job_info {
    my($dataptr) = shift;
    my($gpuoffsetref) = shift;
    my($id,$jobref,$spec,$node,$pos,$newnode,$newpos);
    
    # update job info
    foreach $id (keys(%{$dataptr->{OBJECT}})) {
	if($dataptr->{OBJECT}->{$id}->{type} eq "job") {
	    $jobref=$dataptr->{INFODATA}->{$id};
	    
	    # update nodelist 
	    if($jobref->{state} ne "Running") {
		if(!exists($jobref->{nodelist})) {
		    $jobref->{nodelist}="-";
		    $jobref->{totaltasks}=0;
		}
		if(!exists($jobref->{vnodelist})) {
		    $jobref->{vnodelist}="-";
		}
	    }
	    # update totalcores
	    if(!exists($jobref->{totalcores})) {
		print "update_job_info: could not find attributes for job $id to compute totalcores\n";
	    }
	    
	    if($jobref->{state} eq "Running") {
		
		if(exists($jobref->{gpulist})) {
		    foreach $spec (split(/\),?\(/,$jobref->{gpulist})) {
			if($spec=~/\(?([^,]+),(\d+)\)?/) {
			    $node=$1;$pos=$2;
			    $newnode=$node;$newnode=~s/\-gpu$//s;
			    $newpos=$gpuoffsetref->{$newnode}+$pos;
			    $jobref->{nodelist}.="($newnode,$newpos)";
#			    $jobref->{nodelist}.="($node,$pos)";
			}
		    }
		}
	    }
	}
    }
    return(1);
}

1;
