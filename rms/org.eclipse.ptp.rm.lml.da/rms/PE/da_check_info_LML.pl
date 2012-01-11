#!/usr/bin/perl -w
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
use strict;

sub check_rms_PE {
    my($rmsref,$cmdsref,$verbose)=@_;
    my($key,$cmd);
    my $rc=1;
    
    my %cmdname=(
		"job"  => "poe",
	);
    
    my %cmdpath=(
	"job"  => "/usr/bin/poe",
	);
    
    foreach $key (keys(%cmdname)) {
	if (exists($cmdsref->{"cmd_${key}info"})) {
	    $cmd=$cmdsref->{"cmd_${key}info"};
	} else {
	    $cmd=$cmdpath{$key};
	}
	if (! -f $cmd) {
	    my $cmdpath=`which $cmdname{$key} 2>/dev/null`; 	# last try: which 
	    if(!$?) {
		chomp($cmdpath);
		$cmd=$cmdpath;
		&report_if_verbose("%s","$0: check_rms_PE: found $cmdname{$key} by which ($cmd)\n");
	    }
	}
	if (-f $cmd) {
	    $cmdsref->{"cmd_${key}info"}=$cmd;
	} else {
	    &report_if_verbose("%s","$0: check_rms_PE: no cmd found for $cmdname{$key}\n");
	    $rc=0;
	}
    }
    if ($rc==1) {
	$$rmsref="PE";
	&report_if_verbose("%s%s%s", "$0: check_rms_PE: found PE commands (",
			   join(",",(values(%{$cmdsref}))),
			   ")\n");
    } else {
	&report_if_verbose("%s","$0: check_rms_PE: seems not to be a PE system\n");
    }
    
    return($rc);
}


sub generate_step_rms_PE {
    my($workflowxml, $laststep, $cmdsref)=@_;
    my($step,$envs,$key,$ukey);

    $envs="";
    foreach $key (keys(%{$cmdsref})) {
		$ukey=uc($key);
		$envs.="$ukey=$cmdsref->{$key} ";
    }

    $step="getdata";
    &add_exec_step_to_workflow($workflowxml,$step, $laststep, 
			       "$envs $^X rms/PE/da_jobs_info_LML.pl   \$tmpdir/jobs_nodes_LML.xml");
    $laststep=$step;

    $step="combineLML";
    &add_exec_step_to_workflow($workflowxml,$step, $laststep, 
			       "$^X \$instdir/LML_combiner/LML_combine_obj.pl  -v -o \$stepoutfile ".
			       "\$tmpdir/jobs_nodes_LML.xml");
    $laststep=$step;

    return($laststep);
}

$main::check_functions->{PE}   =\&check_rms_PE;
$main::generate_functions->{PE}=\&generate_step_rms_PE;

1;
