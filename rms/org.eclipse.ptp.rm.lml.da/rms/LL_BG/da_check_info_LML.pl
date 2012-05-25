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


sub check_rms_LL_BG_type {
    my $type="unknown";
    my $cpu=`uname -m`;
    if($cpu eq "ppc64") {
	if (-d "/bgl/BlueLight") {
	    $type="BG/L";
	} elsif(-d "/bgsys/drivers/ppcfloor/hwi" )  {
	    $type="BG/Q";
	} elsif(-d "/bgsys" )  {
	    $type="BG/P";
	}
    }
    return($type);
}


sub check_rms_LL_BG {
    my($rmsref,$cmdsref,$verbose)=@_;
    my($key,$cmd);
    my $rc=1;
    
    my %cmdname=(
	"job"  => "llq",
	"node" => "llbgstatus",
	"block" => "llbgstatus",
	);
    my %cmdpath=(
	"job" => "/usr/bin/llq",
	"node" => "/usr/bin/llbgstatus",
	"block" => "/usr/bin/llbgstatus",
	);
    
    foreach $key (keys(%cmdname)) {
	# check for job query cmd
	if (exists($cmdsref->{"cmd_${key}info"})) {
	    $cmd=$cmdsref->{"cmd_${key}info"};
	} else {
	    $cmd=$cmdpath{$key};
	}
	if (!-f $cmd) {
	    my $cmdpath=`which $cmdname{$key} 2>/dev/null`; 	# last try: which 
	    if (!$?) {
		chomp($cmdpath);
		$cmd=$cmdpath;
		&report_if_verbose("%s","$0: check_rms_LL_BG: found $cmdname{$key} by which ($cmd)\n");
	    } 
	}
	if (-f $cmd) {
	    $cmdsref->{"cmd_${key}info"}=$cmd;
	} else {
	    &report_if_verbose("%s","$0: check_rms_LL_BG: no cmd found for $cmdname{$key}\n");
	    $rc=0;
	}

    }
    
    if ($rc==1) {
	$$rmsref="LL_BG";
	&report_if_verbose("%s%s%s", "$0: check_rms_LL_BG: found LL commands (",
			   join(",",(values(%{$cmdsref}))),
			   ")\n");
    } else {
	&report_if_verbose("%s","$0: check_rms_LL_BG: seems not to be a LL system\n");
    }
    
    return $rc;
}

sub generate_step_rms_LL_BG {
    my($workflowxml, $laststep, $cmdsref)=@_;
    my($step,$envs,$key,$ukey);
    
    $envs="";
    foreach $key (keys(%{$cmdsref})) {
	$ukey=uc($key);
	$envs.="$ukey=$cmdsref->{$key} ";
    }
    
    $step="getdata";
    &add_exec_step_to_workflow($workflowxml,$step, $laststep, 
			       "$envs $^X rms/LL_BG/da_system_info_LML.pl               \$tmpdir/sysinfo_LML.xml",
			       "$envs $^X rms/LL_BG/da_nodes_info_LML.pl                \$tmpdir/nodes_LML.xml",
			       "$envs $^X rms/LL_BG/da_blocks_info_LML.pl                \$tmpdir/blocks_LML.xml",
			       "$envs $^X rms/LL_BG/da_jobs_info_LML.pl                 \$tmpdir/jobs_LML.xml");
    $laststep=$step;
    
    $step="combineLML";
    &add_exec_step_to_workflow($workflowxml,$step, $laststep, 
			       "$^X \$instdir/LML_combiner/LML_combine_obj.pl  -v -o \$stepoutfile ".
			       "\$tmpdir/sysinfo_LML.xml \$tmpdir/jobs_LML.xml \$tmpdir/nodes_LML.xml \$tmpdir/blocks_LML.xml");
    $laststep=$step;
    
    return($laststep);
}

$main::check_functions->{LL_BG}   =\&check_rms_LL_BG;
$main::generate_functions->{LL_BG}=\&generate_step_rms_LL_BG;

1;
