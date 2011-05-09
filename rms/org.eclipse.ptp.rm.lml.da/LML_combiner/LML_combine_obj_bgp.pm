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
package LML_combine_obj_bgp;
use strict;
use Data::Dumper;

my($debug)=0;

sub update {
    my($dataptr) = shift;
    my($dbdir) = shift;
#    print Dumper($dataptr);
    &update_job_info($dataptr);
    &update_class_info($dataptr);
    return(1);
} 

sub update_job_info {
    my($dataptr) = shift;
    my($id,$jobref,%partitions);
    
    # scan for partition names
    foreach $id (keys(%{$dataptr->{OBJECT}})) {
	if($dataptr->{OBJECT}->{$id}->{type} eq "partition") {
	    $partitions{$dataptr->{OBJECT}->{$id}->{name}}=$dataptr->{INFODATA}->{$id};
	}
    }
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
	    }
	    # update totalcores
	    if(!exists($jobref->{totalcores})) {
		if(exists($jobref->{bgp_size_alloc})) {
		    $jobref->{totalcores}=$jobref->{bgp_size_alloc}*4;
		} elsif (exists($jobref->{bgp_size_req})) {
		    $jobref->{totalcores}=$jobref->{bgp_size_req}*4;
		} elsif (exists($jobref->{bgp_shape_alloc})) {
		    my @shape=split(/x/,$jobref->{bgp_shape_alloc});
		    $jobref->{totalcores}=$shape[0]*$shape[1]*$shape[2]*512*4;
		} elsif (exists($jobref->{bgp_shape_req})) {
		    my @shape=split(/x/,$jobref->{bgp_shape_req});
		    $jobref->{totalcores}=$shape[0]*$shape[1]*$shape[2]*512*4;
		} elsif(exists($jobref->{queue})) {
		    if($jobref->{queue} eq "serial") {
			# job will not executed on a partition
			$jobref->{totalcores}=0;
			$jobref->{totaltasks}=1;
		    }
		} else {
		    print "update_job_info: could not find attributes for job $id to compute totalcores\n";
		}
	    }
	    # update totaltasks and other attributes for BG/P running jobs
	    if(!exists($jobref->{totaltasks})) {
		if(exists($jobref->{bgp_partalloc})) {
		    my $partitionref=$partitions{$jobref->{bgp_partalloc}};
		    if(exists($partitionref->{bgp_mode})) {
			$jobref->{totaltasks}=$jobref->{bgp_size_alloc}*4;
		    } else {
			$jobref->{totaltasks}=-1;
		    }
 		    if(exists($partitionref->{bgp_executable})) {
			$jobref->{executable}=$partitionref->{bgp_executable};
		    }   
		}
	    }

	}
    }
    return(1);
}

sub update_class_info {
    my($dataptr) = shift;
    my($id,$class);
    my(%classes);

    # get class info
    foreach $id (keys(%{$dataptr->{OBJECT}})) {
	if($dataptr->{OBJECT}->{$id}->{type} eq "class") {
	    $class=$dataptr->{INFODATA}->{$id}->{name};
#	    print "class=$class\n";
	    $classes{$class}{include}=$dataptr->{INFODATA}->{$id}->{bgp_incl_bg};
	    $classes{$class}{exclude}=$dataptr->{INFODATA}->{$id}->{bgp_excl_bg};
	}
    }

    return(1);
} 

1;
