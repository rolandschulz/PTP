#!/usr/bin/perl -w
#*******************************************************************************
#* Copyright (c) 2011-2012 IBM Corporation and others.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*
#* Contributors:
#*    Simon Wail (IBM)
#*	  Carsten Karbach (Forschungszentrum Juelich GmbH) 
#*******************************************************************************/
use strict;

sub check_rms_SLURM_ALPS {
    my ( $rmsref, $cmdsref, $verbose ) = @_;
    my ( $key, $cmd );
    my $rc = 1;

    my %cmdname = (
        "job"     => "scontrol",
        "node"    => "apstat",
		"app"     => "apstat",
		"node"    => "apstat",
		"nodemap" => "xtprocadmin",
		"sys"	  => "scontrol"
    );

    my %cmdpath = (
        "job"     => "/usr/bin/scontrol",
        "node"    => "/usr/bin/apstat",
        "app"     => "/usr/bin/apstat",
        "node"    => "/usr/bin/apstat",
		"nodemap" => "/usr/bin/xtprocadmin",
		"sys"	  => "/usr/bin/scontrol"
    );

    foreach $key ( keys(%cmdname) ) {
        # check for job query cmd
        if ( exists( $cmdsref->{"cmd_${key}info"} ) ) {
            $cmd = $cmdsref->{"cmd_${key}info"};
        }
        else {
            $cmd = $cmdpath{$key};
        }
        if ( ! -f $cmd ) {
            my $cmdpath = `which $cmdname{$key} 2>/dev/null`;  # last try: which
            if ( ! $? ) {
                chomp( $cmdpath );
                $cmd = $cmdpath;
                &report_if_verbose( "%s", "$0: check_rms_SLURM_ALPS: found $cmdname{$key} by which ($cmd)\n" );
            }
        }
        if ( -f $cmd ) {
            $cmdsref->{"cmd_${key}info"} = $cmd;
        }
        else {
            &report_if_verbose( "%s", "$0: check_rms_SLURM_ALPS: no cmd found for $cmdname{$key}\n" );
            $rc = 0;
        }
    }

    if ( $rc == 1 ) {
        $$rmsref = "SLURM_ALPS";
        &report_if_verbose( "%s%s%s", "$0: check_rms_SLURM_ALPS: found SLURM_ALPS commands (",
            join( ",", ( values( %{$cmdsref} ) ) ), ")\n" );
    }
    else {
        &report_if_verbose( "%s", "$0: check_rms_SLURM_ALPS: seems not to be a SLURM_ALPS system\n" );
    }

    return ($rc);
}

sub generate_step_rms_SLURM_ALPS {
    my ( $workflowxml, $laststep, $cmdsref ) = @_;
    my ( $step, $envs, $key, $ukey );

    $envs = "";
    foreach $key ( keys( %{$cmdsref} ) ) {
        $ukey = uc( $key );
        $envs .= "$ukey=$cmdsref->{$key} ";
    }
    $step = "getdata";
    &add_exec_step_to_workflow(    $workflowxml, $step, $laststep,
        "$envs $^X rms/SLURM_ALPS/da_system_info_LML.pl  \$tmpdir/sysinfo_LML.xml",
        "$envs $^X rms/SLURM_ALPS/da_jobs_info_LML.pl    \$tmpdir/jobs_LML.xml",
		"$envs $^X rms/SLURM_ALPS/da_apps_info_LML.pl                 \$tmpdir/apps_LML.xml",
        "$envs $^X rms/SLURM_ALPS/da_nodelist_info_LML.pl             \$tmpdir/nodelist_LML.xml",
        "$envs $^X rms/SLURM_ALPS/da_nodemap_info_LML.pl              \$tmpdir/nodemap_LML.xml",
    );
    $laststep = $step;

    $step = "combineLML";
    &add_exec_step_to_workflow( $workflowxml, $step, $laststep,
        "$^X \$instdir/LML_combiner/LML_combine_obj.pl  -v -o \$stepoutfile "
        . "\$tmpdir/sysinfo_LML.xml \$tmpdir/apps_LML.xml \$tmpdir/jobs_LML.xml \$tmpdir/nodelist_LML.xml \$tmpdir/nodemap_LML.xml" );
    $laststep = $step;

    return ($laststep);
}

$main::check_functions->{SLURM_ALPS} = \&check_rms_SLURM_ALPS;
$main::generate_functions->{SLURM_ALPS} = \&generate_step_rms_SLURM_ALPS;

1;
