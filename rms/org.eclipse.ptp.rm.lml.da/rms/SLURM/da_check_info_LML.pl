#!/usr/bin/perl -w
#*******************************************************************************
#* Copyright (c) 2011 IBM.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*
#* Contributors:
#*    Simon Wail (IBM)
#*******************************************************************************/
use strict;

sub check_rms_SLURM {
    my ( $rmsref, $cmdsref, $verbose ) = @_;
    my ( $key, $cmd );
    my $rc = 1;

    my %cmdname = (
        "job"  => "scontrol",
        "node" => "scontrol",
    );

    my %cmdpath = (
        "job"  => "/usr/bin/scontrol",
        "node" => "/usr/bin/scontrol",
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
                &report_if_verbose( "%s", "$0: check_rms_SLURM: found $cmdname{$key} by which ($cmd)\n" );
            }
        }
        if ( -f $cmd ) {
            $cmdsref->{"cmd_${key}info"} = $cmd;
        }
        else {
            &report_if_verbose( "%s", "$0: check_rms_SLURM: no cmd found for $cmdname{$key}\n" );
            $rc = 0;
        }
    }

    if ( $rc == 1 ) {
        $$rmsref = "SLURM";
        &report_if_verbose( "%s%s%s", "$0: check_rms_SLURM: found SLURM commands (",
            join( ",", ( values( %{$cmdsref} ) ) ), ")\n" );
    }
    else {
        &report_if_verbose( "%s", "$0: check_rms_SLURM: seems not to be a SLURM system\n" );
    }

    return ($rc);
}

sub generate_step_rms_SLURM {
    my ( $workflowxml, $laststep, $cmdsref ) = @_;
    my ( $step, $envs, $key, $ukey );

    $envs = "";
    foreach $key ( keys( %{$cmdsref} ) ) {
        $ukey = uc( $key );
        $envs .= "$ukey=$cmdsref->{$key} ";
    }
    $step = "getdata";
    &add_exec_step_to_workflow(    $workflowxml, $step, $laststep,
        "$envs $^X rms/SLURM/da_system_info_LML.pl  \$tmpdir/sysinfo_LML.xml",
        "$envs $^X rms/SLURM/da_nodes_info_LML.pl   \$tmpdir/nodes_LML.xml",
        "$envs $^X rms/SLURM/da_jobs_info_LML.pl    \$tmpdir/jobs_LML.xml"
    );
    $laststep = $step;

    $step = "combineLML";
    &add_exec_step_to_workflow( $workflowxml, $step, $laststep,
        "$^X \$instdir/LML_combiner/LML_combine_obj.pl  -v -o \$stepoutfile "
        . "\$tmpdir/sysinfo_LML.xml \$tmpdir/jobs_LML.xml \$tmpdir/nodes_LML.xml" );
    $laststep = $step;

    return ($laststep);
}

$main::check_functions->{SLURM} = \&check_rms_SLURM;
$main::generate_functions->{SLURM} = \&generate_step_rms_SLURM;

1;
