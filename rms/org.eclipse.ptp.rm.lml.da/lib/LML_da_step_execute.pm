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
package LML_da_step_execute;

my($debug)=0;

use strict;
use FindBin;
use lib "$FindBin::RealBin/.";
use Data::Dumper;
use Time::Local;
use Time::HiRes qw ( time );
use LML_da_util;

sub new {
    my $self    = {};
    my $proto   = shift;
    my $class   = ref($proto) || $proto;
    my $stepdef      = shift;
    my $globalvarref = shift;
    printf("\t LML_da_step: new %s\n",ref($proto)) if($debug>=3);
    $self->{STEPDEF}   = $stepdef;
    $self->{GLOBALVARS}= $globalvarref;
    $self->{VERBOSE}   = $globalvarref->{verbose};
    $self->{TMPDIR}    = $globalvarref->{"tmpdir"};
    $self->{PERMDIR}   = $globalvarref->{"permdir"};
    bless $self, $class;
    return $self;
}

sub execute {
    my($self) = shift;
    my($file,$newfile)=@_;
    my($cmd,$cmdref);
    my($tstart,$tdiff);
    my $rc=0;
    my $step=$self->{STEPDEF}->{id};
    my $stepref=$self->{STEPDEF};

#    print Dumper($stepref);

    foreach $cmdref (@{$self->{STEPDEF}->{cmd}}) {
	$cmd=$cmdref->{exec};
	printf  "LML_da_step_execute: executing: %s ...\n",$cmd if($self->{VERBOSE});
	$tstart=time;
        system($cmd);$rc=$?;
	$tdiff=time-$tstart;
	printf  "LML_da_step_execute:            %60s -> ready, time used %10.4ss\n","",$tdiff if($self->{VERBOSE});
        if($rc) {     printf STDERR "failed executing: %s rc=%d\n",$cmd,$rc; return(-1);}
    }
    return($rc);
}



1;
