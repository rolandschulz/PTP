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
package LML_combine_obj_alps;
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

    my $nodemapref=&get_mapping_info($dataptr);

    &update_node_info($dataptr,$nodemapref);

    my $appmapref=&get_app_mapping_info($dataptr,$nodemapref);

    &update_job_info($dataptr,$appmapref);
    return(1);
} 

# returns hash:  NID -> nodename
sub get_mapping_info {
    my($dataptr) = shift;
    my($id,$nid,$nodename,%nodemap);

    # scan for partition names
    foreach $id (keys(%{$dataptr->{OBJECT}})) {
	if($dataptr->{OBJECT}->{$id}->{type} eq "nodemap") {
	    $nid=$dataptr->{OBJECT}->{$id}->{name};
	    $nodename=$dataptr->{INFODATA}->{$id}->{name};
	    $nodemap{$nid}=$nodename;
	    delete($dataptr->{OBJECT}->{$id});
	    delete($dataptr->{INFO}->{$id});
	    delete($dataptr->{INFODATA}->{$id});
	}
    }
    return(\%nodemap);
}

# returns hash:  apid-> vnodelist
sub get_app_mapping_info {
    my($dataptr) = shift;
    my($nodemapref) = shift;
    my($id,$apid,$nodename,%appmap,$numtasks);

    # scan for partition names
    foreach $id (keys(%{$dataptr->{OBJECT}})) {
	if($dataptr->{OBJECT}->{$id}->{type} eq "node") {
	    next if(!(exists($dataptr->{INFODATA}->{$id}->{Apids})));
	    next if($dataptr->{INFODATA}->{$id}->{Apids} eq "-");
	    $apid=$dataptr->{INFODATA}->{$id}->{Apids};  # todo: more apid's possible?
	    $numtasks=1;
	    if(exists($dataptr->{INFODATA}->{$id}->{Pl})) {
		$numtasks=$dataptr->{INFODATA}->{$id}->{Pl};
	    }
	    $appmap{$apid}.="(".$dataptr->{OBJECT}->{$id}->{name}.",$numtasks)";
	}
    }
    return(\%appmap);
}


sub update_node_info {
    my($dataptr) = shift;
    my($nodemapref) = shift;
    my($id,$nid,$noderef);
    
    # update node info
    foreach $id (keys(%{$dataptr->{OBJECT}})) {
	if($dataptr->{OBJECT}->{$id}->{type} eq "node") {
	    $noderef=$dataptr->{INFODATA}->{$id};
	    $nid=$dataptr->{OBJECT}->{$id}->{name};
	    if(exists($nodemapref->{$nid})) {
		$dataptr->{OBJECT}->{$id}->{name}=$nodemapref->{$nid};
	    } else {
		print "update_node_info(ALPS): warning unknown nid: '$nid'\n";
	    }
	    
	    # update attribute: memory
	    my $memfactor=1;
	    if(exists($noderef->{PgSz})) {
		if($noderef->{PgSz}=~/$patint[K]/) {
		    $memfactor=$1 / 1024.0; # --> MB
		}
		delete($noderef->{PgSz});
	    }
	    if(exists($noderef->{Conf})) {
		$noderef->{physmem}=int($memfactor*$noderef->{Conf});
		delete($noderef->{Conf});
	    }else {
		$noderef->{physmem}="0";
	    }

	    if(exists($noderef->{Placed})) {
		$noderef->{availmem}=$noderef->{physmem}-int($memfactor*$noderef->{Placed});
		$noderef->{availmem}=int($memfactor*$noderef->{Placed});
		delete($noderef->{Placed});
	    } else {
		$noderef->{availmem}="0";
	    }

	    if(exists($noderef->{State})) {
		$noderef->{state}="Running" if ($noderef->{State} eq "UP");
		$noderef->{state}="Down"    if ($noderef->{State} eq "DN");
		delete($noderef->{State});
	    } else {
		$noderef->{state}="Unknown";
	    }
	}
    }

    return(1);
}


sub update_job_info {
    my($dataptr) = shift;
    my($appmapnodemapref) = shift;
    my($id,$jobref,$apid,$batchid,%batchtoappid);
    
    # scan for app
    foreach $id (keys(%{$dataptr->{OBJECT}})) {
	if($dataptr->{OBJECT}->{$id}->{type} eq "app") {
	    $apid=$dataptr->{INFODATA}->{$id}->{apid};
	    $batchid=$dataptr->{INFODATA}->{$id}->{batchid};
	    $batchtoappid{$batchid}=$apid;
	    delete($dataptr->{OBJECT}->{$id});
	    delete($dataptr->{INFO}->{$id});
	    delete($dataptr->{INFODATA}->{$id});
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
		if(!exists($jobref->{vnodelist})) {
		    $jobref->{vnodelist}="-";
		}
	    }
	    # update totalcores
	    if(!exists($jobref->{totalcores})) {
		print "update_job_info: could not find attributes for job $id to compute totalcores\n";
	    }

	    if($jobref->{state} eq "Running") {
		$batchid=$dataptr->{OBJECT}->{$id}->{name};
		$batchid=~s/\..*$//gs;
		if(exists($batchtoappid{$batchid})) {
		    $apid=$batchtoappid{$batchid};
		} else {
		    print "update_job_info: could not find apid for batch job '$batchid'\n";
		}
		if(exists($appmapnodemapref->{$apid})) {
		    $jobref->{vnodelist}=$appmapnodemapref->{$apid};
		} else {
		    print "update_job_info: could not find nodelist for app '$apid' of batch job '$batchid'\n";
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
	    $classes{$class}{include}=$dataptr->{INFODATA}->{$id}->{alps_incl_bg};
	    $classes{$class}{exclude}=$dataptr->{INFODATA}->{$id}->{alps_excl_bg};
	}
    }

    return(1);
} 

1;
