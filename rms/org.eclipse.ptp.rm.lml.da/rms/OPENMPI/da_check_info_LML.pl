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

sub check_rms {
    my($rmsref,$cmdsref,$verbose)=@_;
    my($key,$infocmd,$cmd);
    my $rc=0;

    my %cmdname=(
		"job"  => "orte-ps",
		"node" => "orte-ps",
		"sys"  => "ompi_info",
	);
        
    my %cmdpath=(
		"job"  => "",
		"node" => "",
		"sys"  => "/usr/lib/mpi/gcc/openmpi/bin/ompi_info",
	);

    $infocmd=undef;

    # first:  check if ompi_info is given by request info
    if (exists($cmdsref->{"cmd_sysinfo"})) {
		if (-f $cmdsref->{"cmd_sysinfo"}) {
		    $infocmd=$cmdsref->{"cmd_sysinfo"}." --path bindir";
		}
    } 

    # second: check if ompi_info is given in PATH
    if (!$infocmd) {
		my $cmdpath=`which $cmdname{sys} 2>/dev/null`; 	
		if(!$?) {
		    chomp($cmdpath);
		    $infocmd=$cmdpath." --path bindir";
		}
    }

    # second: check if ompi_info is given in default path
    if (!$infocmd) {
		my $cmdpath=$cmdpath{sys}; 	
		if (!-f $cmdpath) {
		    $infocmd=$cmdpath." --path bindir";
		}
    }
 
    # return if no ompi_info found
    if (!$infocmd) {
		return($rc);
    }
   
    # get openmpi bindir
    my $bindir=`$infocmd`;
    chomp($bindir);
    $bindir=~s/^Bindir:\s*//gs;
	
    foreach $key (keys(%cmdname)) {
		$cmdpath{$key}=$bindir."/".$cmdname{$key};
	
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
				print STDERR "$0: check_rms: found $cmdname{$key} by which ($cmd)\n" if($verbose);
		    } else {
				last;
		    }
		}
		if (-f $cmd) {
		    $cmdsref->{"cmd_${key}info"}=$cmd;
		    $rc=1;
		}
    }
	if ($rc) {
		$$rmsref="OPENMPI";
    } else {
		print STDERR "$0: check_rms: seems not to be a OpenMPI system\n" if($verbose);
    }

    return($rc);
}


sub generate_step_rms {
    my($workflowxml, $laststep, $cmdsref)=@_;
    my($step,$envs,$key,$ukey);

    $envs="";
    foreach $key (keys(%{$cmdsref})) {
		$ukey=uc($key);
		$envs.="$ukey=$cmdsref->{$key} ";
    }

    $step="getdata";
    &add_exec_step_to_workflow($workflowxml,$step, $laststep, 
			       "$envs $^X rms/OPENMPI/da_jobs_info_LML.pl   \$tmpdir/jobs_nodes_LML.xml",
			       "$envs $^X rms/OPENMPI/da_system_info_LML.pl \$tmpdir/sysinfo_LML.xml");
    $laststep=$step;

    $step="combineLML";
    &add_exec_step_to_workflow($workflowxml,$step, $laststep, 
			       "$^X \$instdir/LML_combiner/LML_combine_obj.pl  -v -o \$stepoutfile ".
			       "\$tmpdir/sysinfo_LML.xml \$tmpdir/jobs_nodes_LML.xml");
    $laststep=$step;

    return($laststep);
}

1;