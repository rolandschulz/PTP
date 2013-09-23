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
package LML_da_step;

my $VERSION='1.0';
my($debug)=0;

use strict;
use FindBin;
use lib "$FindBin::RealBin/.";
use Data::Dumper;
use Time::Local;
use Time::HiRes qw ( time );
use LML_da_util;
use LML_da_step_execute;

sub new {
    my $self    = {};
    my $proto   = shift;
    my $class   = ref($proto) || $proto;
    my $stepdefs     = shift;
    my $globalvarref = shift;
    my $verbose = shift;
    printf("\t LML_da_step: new %s\n",ref($proto)) if($debug>=3);
    $self->{STEPDEFS}  = $stepdefs;
    $self->{GLOBALVARS}= $globalvarref;
    $self->{VERBOSE}   = $globalvarref->{verbose};
    $self->{TMPDIR}    = $globalvarref->{"tmpdir"};
    $self->{PERMDIR}   = $globalvarref->{"permdir"};
    $self->{LASTSTEP}  = "__init__";
    bless $self, $class;
    return $self;
}

sub process {
    my($self) = shift;
    my($step,$steprefs,@steplist,%stepstate,$depsteps,$depstep,$stepref,$dostep);
    my $rc=0;
    my $loop_done=0;
    $steprefs=$self->{STEPDEFS};
    foreach $step (keys(%{$steprefs})) {
	next if (!exists($steprefs->{$step}->{active}));
	next if ($steprefs->{$step}->{active}==0);
	print "LML_da_step: scan step $step active=$steprefs->{$step}->{active}\n" if($self->{VERBOSE});
	push(@steplist,$step);
	$stepstate{$step}="do";
    }
    push(@steplist,"___endloop___");

    while(@steplist) {
	$step=shift(@steplist);
	if($step eq "___endloop___") {
	    # check if smething happens in this loop
	    if($loop_done==0) {
		if(!@steplist) {
		    print "LML_da_step: all steps processed, leaving\n" if($self->{VERBOSE});
		    return(1);
		}
		print "LML_da_step: something went wrong --> noc action in current cyle of loop, leaving ...\n";
		return(0);
	    } else {
		$loop_done=0;	    
		push(@steplist,$step);
	    }
	    next;
	}
	$stepref=$self->{STEPDEFS}->{$step};
	if($stepref->{exec_after} eq "") {
	    # no dependency, execute
	    $rc=$self->execute_step($step);
	    if($rc) {
		$stepstate{$step}="failed";
	    } else {
		$stepstate{$step}="done";
	    }
	    $loop_done++;
	    next;
	} 
	$depsteps=$stepref->{exec_after};
	$dostep=1;
	foreach $depstep (split('\s*,\s*',$depsteps)) {
	    if(!exists($stepstate{$depstep})) {
		print "LML_da_step: dependency to unknown step ($depstep), leaving ...\n";
		return(0);
	    }
	    print "LML_da_step: step check dependency to step $depstep (=$stepstate{$depstep})\n" if($debug==1);
	    $dostep=0 if($stepstate{$depstep} ne "done");
	}
	if($dostep) {
	    # dependencies fulfilled, execute
	    $rc=$self->execute_step($step);
	    if($rc) {
		$stepstate{$step}="failed";
	    } else {
		$stepstate{$step}="done";
	    }
	    $loop_done++;
	    next;
	} else {
	    print "LML_da_step: step $step dependencies not fulfilled ($depsteps) dostep=$dostep loop_done=$loop_done\n"  if($debug==1);
	    push(@steplist,$step);
	} 
	
    }
    
    return($rc);

}

sub execute_step {
    my($self) = shift;
    my($step) = shift;
    my ($stepref);
    my $rc=0;
    my $laststep=$self->{LASTSTEP};
    my $stepinfile=$self->{GLOBALVARS}->{tmpdir}."/datastep_$laststep.xml";
    my $stepoutfile=$self->{GLOBALVARS}->{tmpdir}."/datastep_$step.xml";
    printf("EXECUTE_STEP: %-10s\n",$step) if($self->{VERBOSE});
    printf("              (%-30s->%-30s) ...\n",$stepinfile,$stepoutfile) if($self->{VERBOSE});

    $stepref=$self->{STEPDEFS}->{$step};
    $self->{GLOBALVARS}->{stepinfile}=$stepinfile;
    $self->{GLOBALVARS}->{stepoutfile}=$stepoutfile;
    &LML_da_util::substitute_recursive($stepref,$self->{GLOBALVARS}); 


    if(!-f $stepinfile) {
	printf("execute_step: input file for step not found %-30s ...\n",$stepinfile);
	if($laststep eq "__init__") {
	    printf("execute_step: --> generating empty %-30s ...\n",$stepinfile);
	    system("touch $stepinfile");
	}
    }

    if(-f $stepoutfile) {
	printf("execute_step: unlink output file from previous run %-30s ...\n",$stepoutfile);
	unlink($stepoutfile);
    }

    if($stepref->{type} eq "execute") {
	my $execobj=LML_da_step_execute->new($stepref,$self->{GLOBALVARS});
	$rc=$execobj->execute();
    }

    if($stepref->{type} eq "driver_bgp") {
	my $execobj=LML_da_driver_bgp->new($stepref,$self->{GLOBALVARS});
	$rc=$execobj->generate_xmlfile();
    }

    if($stepref->{type} eq "usagedb") {
	my $execobj=LML_da_usageDB->new($stepref,$self->{GLOBALVARS});
	$rc=$execobj->update_usagedb();
    }

    if($stepref->{type} eq "forecast") {
	my $execobj=LML_da_forecastcaller->new($stepref,$self->{GLOBALVARS});
	$rc=$execobj->runforecast();
    }

    if($stepref->{type} eq "historyMGR") {
	my $execobj=LML_da_historyMGR->new($stepref,$self->{GLOBALVARS});
	$rc=$execobj->store();
    }

    if($stepref->{type} eq "put") {
	my $execobj=LML_da_put->new($stepref,$self->{GLOBALVARS});
	$rc=$execobj->put();
    }

    if($stepref->{type} eq "LMLconvert") {
	my $execobj=LML_da_convert2LMLcaller->new($stepref,$self->{GLOBALVARS});
	$rc=$execobj->runconvert();
    }

    if(! -f $stepoutfile) {
	printf("execute_step: output file not generated by step, renaming input file to %-30s ...\n",$stepoutfile);
	rename($stepinfile,$stepoutfile);
    }

    $self->{GLOBALVARS}->{stepinfile}="";
    $self->{GLOBALVARS}->{stepoutfile}="";

    $self->{LASTSTEP} = $step;
    return($rc);
}

1;
