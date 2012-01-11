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

sub check_rms_PBS {
    my($rmsref,$cmdsref,$verbose)=@_;
    my($key, $cmd);
    my $rc=1;
    
    my %cmdname=(
		"job"  => "qstat",
		"node" => "pbsnodes",
	);
	
    my %cmdpath=(
		"job" => "/usr/bin/qstat",
		"node" => "/usr/bin/pbsnodes",
	);
    
    foreach $key (keys(%cmdname)) {
	# check for job query cmd
	if (exists($cmdsref->{"cmd_${key}info"})) {
	    $cmd=$cmdsref->{"cmd_${key}info"};
	} else {
	    $cmd=$cmdpath{$key};
	}
	if (! -f $cmd) {
	    my $cmdpath=`which $cmdname{$key} 2>/dev/null`; 	# last try: which 
	    if (!$?) {
		chomp($cmdpath);
		$cmd=$cmdpath;
		&report_if_verbose("%s","$0: check_rms_PBS: found $cmdname{$key} by which ($cmd)\n");
	    }
	}
	if (-f $cmd) {
	    $cmdsref->{"cmd_${key}info"}=$cmd;
	} else {
	    &report_if_verbose("%s","$0: check_rms_PBS: no cmd found for $cmdname{$key}\n");
	    $rc=0;
	}
    }
    
    # Ensure it is a PBSpro system
    if (exists($cmdsref->{"cmd_jobinfo"})) {
	$cmd=$cmdsref->{"cmd_jobinfo"}." --version";
	    my $cmdversion=`$cmd 2>/dev/null`; 	
	chomp($cmdversion);
	if ($cmdversion=~/version/) {
	    if ($cmdversion=~/PBSPro/) {
		print STDERR "$0: check_rms: PBSpro found\n" if($verbose);
	    }
	} else {
	    &report_if_verbose("%s","$0: check_rms_PBS: could not obtain version info from command $cmd\n");
	    $rc=0;
	}
    }
    
    if ($rc==1)  {
    	$$rmsref = "PBS";
	&report_if_verbose("%s%s%s", "$0: check_rms_PBS: found PBS commands (",
			   join(",",(values(%{$cmdsref}))),
			   ")\n");
    } else {
	&report_if_verbose("%s","$0: check_rms_PBS: seems not to be a PBS system\n");
    }
    
    return($rc);
}

sub generate_step_rms_PBS {
    my($workflowxml, $laststep, $cmdsref)=@_;
    my($step,$envs,$key,$ukey);

    $envs="";
    foreach $key (keys(%{$cmdsref})) {
		$ukey=uc($key);
		$envs.="$ukey=$cmdsref->{$key} ";
    }
    $step="getdata";
    &add_exec_step_to_workflow($workflowxml,$step, $laststep, 
			       "$envs $^X rms/PBS/da_system_info_LML.pl               \$tmpdir/sysinfo_LML.xml",
			       "$envs $^X rms/PBS/da_nodes_info_LML.pl                \$tmpdir/nodes_LML.xml",
			       "$envs $^X rms/PBS/da_jobs_info_LML.pl                 \$tmpdir/jobs_LML.xml");
    $laststep=$step;

    $step="combineLML";
    &add_exec_step_to_workflow($workflowxml,$step, $laststep, 
			       "$^X \$instdir/LML_combiner/LML_combine_obj.pl  -v -o \$stepoutfile ".
			       "\$tmpdir/sysinfo_LML.xml \$tmpdir/jobs_LML.xml \$tmpdir/nodes_LML.xml");
    $laststep=$step;

    return($laststep);
}

$main::check_functions->{PBS}   =\&check_rms_PBS;
$main::generate_functions->{PBS}=\&generate_step_rms_PBS;

1;
