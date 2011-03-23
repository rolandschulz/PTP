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

my $patint="([\\+\\-\\d]+)";   # Pattern for Integer number
my $patfp ="([\\+\\-\\d.E]+)"; # Pattern for Floating Point number
my $patwrd="([\^\\s]+)";       # Pattern for Work (all noblank characters)
my $patbl ="\\s+";             # Pattern for blank space (variable length)

#####################################################################
# get user info / check system 
#####################################################################
my $UserID = getpwuid($<);
my $Hostname = `hostname`;
my $verbose=1;
my ($line,%jobs,%jobnr,$key,$value,$count,%notmappedkeys,%notfoundkeys);

#unless( ($Hostname =~ /jugenes\d/) && ($UserID =~ /llstat/) ) {
#  die "da_jobs_info_LML.pl can only be used as llstat on jugenesX!";
#}

#####################################################################
# get command line parameter
#####################################################################
if ($#ARGV != 0) {
  die " Usage: $0 <filename> $#ARGV\n";
}
my $filename = $ARGV[0];

my $system_sysprio=-1;
my $maxtopdogs=-1;

my %mapping = (
    "Account"                                => "",
    "Adapter_Requirement"                    => "",
    "Allocated_Host"                         => "nodelist",
    "Args"                                   => "",
    "As_Hard_Limit"                          => "",
    "As_Soft_Limit"                          => "",
    "BG_Partition_State"                     => "bgp_state",
    "BG_Partition_Type"                      => "bgp_type",
    "BG_Requirements"                        => "",
    "Base_Partition_List"                    => "nodelist",
    "Blue_Gene_Job_Id"                       => "",
    "Blue_Gene_Status"                       => "",
    "Bulk_Transfer"                          => "",
    "Checkpoint_File"                        => "",
    "Checkpointable"                         => "",
    "Ckpt_Accum_Time"                        => "",
    "Ckpt_Elapse_Time"                       => "",
    "Ckpt_Execute_Dir"                       => "",
    "Ckpt_Hard_Limit"                        => "",
    "Ckpt_Soft_Limit"                        => "",
    "Ckpt_Start_Time"                        => "",
    "Class"                                  => "queue",
    "Cluster_Option"                         => "",
    "Cmd"                                    => "",
    "Comment"                                => "comment",
    "Completion_Code"                        => "",
    "Completion_Date"                        => "",
    "Core_Hard_Limit"                        => "",
    "Core_Soft_Limit"                        => "",
    "Coschedule"                             => "",
    "Cpu_Hard_Limit"                         => "",
    "Cpu_Soft_Limit"                         => "",
    "Cpus_Per_Core"                          => "",
    "Data_Hard_Limit"                        => "",
    "Data_Soft_Limit"                        => "",
    "Data_Stg_Dependency"                    => "",
    "Dependency"                             => "dependency",
    "Dispatch_Time"                          => "dispatchdate",
    "Env"                                    => "",
    "Err"                                    => "",
    "Error_Text"                             => "",
    "Fail_Ckpt_Time/Date"                    => "",
    "Favored_Job"                            => "favored",
    "File_Hard_Limit"                        => "",
    "File_Soft_Limit"                        => "",
    "Good_Ckpt_Time/Date"                    => "",
    "Hold_Job_Until"                         => "",
    "IONodes_Per_BP"                         => "",
    "In"                                     => "",
    "Initial_Working_Dir"                    => "",
    "Job_Name"                               => "name",
    "Job_Queue_Key"                          => "",
    "Job_Step_Id"                            => "step",
    "Large_Page"                             => "",
    "LoadLeveler_Group"                      => "group",
    "Locks_Hard_Limit"                       => "",
    "Locks_Soft_Limit"                       => "",
    "Max_Processors"                         => "",
    "Mcm_Affinity_Option"                    => "",
    "Memlock_Hard_Limit"                     => "",
    "Memlock_Soft_Limit"                     => "",
    "MetaCluster_Job"                        => "",
    "Min_Processors"                         => "",
    "Negotiator_Messages"                    => "",
    "Node_Resources"                         => "",
    "Node_Usage"                             => "",
    "Nofile_Hard_Limit"                      => "",
    "Nofile_Soft_Limit"                      => "",
    "Notifications"                          => "",
    "Notify_User"                            => "",
    "Nproc_Hard_Limit"                       => "",
    "Nproc_Soft_Limit"                       => "",
    "Out"                                    => "",
    "Outbound_Schedds"                       => "",
    "Owner"                                  => "owner",
    "Parallel_Threads"                       => "",
    "Partition_Allocated"                    => "bgp_partalloc",
    "Partition_Requested"                    => "bgp_partreq",
    "Preempt_Wait_Count"                     => "",
    "Preemptable"                            => "",
    "Preferences"                            => "",
    "Previous_q_sysprio"                     => "",
    "Queue_Date"                             => "queuedate",
    "RSet"                                   => "",
    "Recurring"                              => "",
    "Requested_Cluster"                      => "",
    "Requested_Res._ID"                      => "",
    "Requirements"                           => "",
    "Reservation_ID"                         => "",
    "Resources"                              => "",
    "Restart"                                => "restart",
    "Restart_From_Ckpt"                      => "",
    "Restart_Same_Nodes"                     => "",
    "Rotate"                                 => "",
    "Rss_Hard_Limit"                         => "",
    "Rss_Soft_Limit"                         => "",
    "SMT_required"                           => "",
    "Schedd_History"                         => "",
    "Schedd_Host"                            => "",
    "Scheduling_Cluster"                     => "",
    "Sending_Cluster"                        => "",
    "Shell"                                  => "",
    "Stack_Hard_Limit"                       => "",
    "Stack_Soft_Limit"                       => "",
    "Status"                                 => "state",
    "Step_Adapter_Memory"                    => "",
    "Step_Cpu_Hard_Limit"                    => "",
    "Step_Cpu_Soft_Limit"                    => "",
    "Step_Cpus"                              => "",
    "Step_Large_Page_Mem"                    => "",
    "Step_Name"                              => "",
    "Step_Real_Memory"                       => "",
    "Step_Type"                              => "",
    "Step_Virtual_Memory"                    => "",
    "Structure_Version"                      => "",
    "Submitting_Cluster"                     => "",
    "Submitting_Host"                        => "",
    "Submitting_User"                        => "",
    "System_Priority"                        => "",
    "Task_Affinity"                          => "",
    "Unix_Group"                             => "",
    "User_Hold_Time"                         => "",
    "User_Priority"                          => "",
    "Virtual_Image_Size"                     => "",
    "Wall_Clk_Hard_Limit"                    => "wall",
    "Wall_Clk_Soft_Limit"                    => "wallsoft",
    "Wiring_Allocated"                       => "",
    "Wiring_Requested"                       => "",
    "class_sysprio"                          => "classprio",
    "group_sysprio"                          => "groupprio",
    "q_sysprio"                              => "",
    "user_sysprio"                           => "userprio",
    "Allocated_Hosts"                        => "",
    "Exec_Args"                              => "",
    "Executable"                             => "",
    "Mcm_Affinity_Options"                   => "",
    "Name"                                   => "",
    "Node_actual"                            => "",
    "Node_maximum"                           => "",
    "Node_minimum"                           => "",
    "Num_Task_Inst"                          => "",
    "Step_rCxt_Blocks"                       => "",
    "TaskInstances"                          => "nodelist",
    "TaskInstances"                          => "nodelist",
    );

open(IN,"llq -l |");
my $jobid="-";
my $lastkey="-";
my (%nodelastcorenumber);
while($line=<IN>) {
    chomp($line);
    next if ($line=~/^\-+$/);
    last if($line=~/There is currently no job step to report/);

    if($line=~/^\s*Task Instance[:]\s*$patwrd[:]$patint/) {
	my($host,$num)=($1,$2);
	$nodelastcorenumber{$host}=0 if(!exists($nodelastcorenumber{$host}));
	if($num>=0) {
	    $jobs{$jobid}{TaskInstances}.="($host,".$nodelastcorenumber{$host}.")";
	    $nodelastcorenumber{$host}++;
	}
    } elsif($line=~/^=+ Job Step $patwrd =+/) {
	$jobid=$1;
    } elsif($line=~/^\s*([^\:]+): (.*)$/) {
	($key,$value)=($1,$2);
	$key=~s/\s+$//gs;
	$key=~s/\s/_/gs;
	$lastkey=$key;
	$jobs{$jobid}{$key}=$value;
    } else {
	$line=~s/^\s*//gs;
	$jobs{$jobid}{$lastkey}.=",$line";
    }

}
close(IN);

open(OUT,"> $filename") || die "cannot open file $filename";
printf(OUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
printf(OUT "<lml:lgui xmlns:lml=\"http://www.llview.de\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
printf(OUT "	xsi:schemaLocation=\"http://www.llview.de lgui.xsd\"\n");
printf(OUT "	version=\"0.7\"\>\n");
printf(OUT "<objects>\n");
$count=0;
foreach $jobid (sort(keys(%jobs))) {
    $count++;$jobnr{$jobid}=$count;
    printf(OUT "<object id=\"j%06d\" name=\"%s\" type=\"job\"/>\n",$count,$jobid);
}
printf(OUT "</objects>\n");
printf(OUT "<information>\n");
foreach $jobid (sort(keys(%jobs))) {
    printf(OUT "<info oid=\"j%06d\" type=\"short\">\n",$jobnr{$jobid});
    foreach $key (sort(keys(%{$jobs{$jobid}}))) {
	if(exists($mapping{$key})) {
	    if($mapping{$key} ne "") {
		$value=&modify($key,$mapping{$key},$jobs{$jobid}{$key});
		if($value) {
		    printf(OUT " <data %-20s value=\"%s\"/>\n","key=\"".$mapping{$key}."\"",$value);
		}
	    } else {
		$notmappedkeys{$key}++;
	    }
	} else {
	    $notfoundkeys{$key}++;
	}
    }
    printf(OUT "</info>\n");
}
printf(OUT "</information>\n");
 
printf(OUT "</lml:lgui>\n");

close(OUT);

foreach $key (sort(keys(%notfoundkeys))) {
#    printf("not found key %-40s: %6d times\n",$key,$notfoundkeys{$key});
    printf("%-40s => \"\",\n","\"".$key."\"",$notfoundkeys{$key});
}



sub modify {
    my($key,$mkey,$value)=@_;
    my $ret=$value;

    if(($mkey eq "wall") || ($mkey eq "wallsoft")) {
	if($value=~/\($patint seconds\)/) {
	    $ret=$1;
	}
	if($value=~/$patint minutes/) {
	    $ret=$1*60;
	}
    }

    if(($mkey eq "comment")) {
	$ret=~s/\"//gs;
    }
    if(($mkey eq "bgp_state")) {
	$ret=~s/\<unknown\>/unknown/gs;
    }

    return($ret);
}
