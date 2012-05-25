#!/usr/bin/perl -w
#
#          LLview - supervising LoadLeveler batch queue utilization 
#
#   Copyright (C) 2010, Forschungszentrum Juelich GmbH, Federal Republic of
#   Germany. All rights reserved.
#
#   Redistribution and use in source and binary forms, with or without
#   modification, are permitted provided that the following conditions are met:
#
#   Redistributions of source code must retain the above copyright notice, this
#   list of conditions and the following disclaimer.
#
#     - Redistributions of source code must retain the above copyright notice,
#       this list of conditions and the following disclaimer.
#
#     - Redistributions in binary form must reproduce the above copyright
#       notice, this list of conditions and the following disclaimer in the
#       documentation and/or other materials provided with the distribution.
#
#     - Any publications that result from the use of this software shall
#       reasonably refer to the Research Centre's development.
#
#     - All advertising materials mentioning features or use of this software
#       must display the following acknowledgement:
#
#           This product includes software developed by Forschungszentrum
#           Juelich GmbH, Federal Republic of Germany.
#
#     - Forschungszentrum Juelich GmbH is not obligated to provide the user with
#       any support, consulting, training or assistance of any kind with regard
#       to the use, operation and performance of this software or to provide
#       the user with any updates, revisions or new versions.
#
#
#   THIS SOFTWARE IS PROVIDED BY FORSCHUNGSZENTRUM JUELICH GMBH "AS IS" AND ANY
#   EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
#   WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
#   DISCLAIMED. IN NO EVENT SHALL FORSCHUNGSZENTRUM JUELICH GMBH BE LIABLE FOR
#   ANY SPECIAL, DIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER
#   RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF
#   CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
#   CONNECTION WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
#
#
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
    "Allocated_Host"                         => "",
    "Args"                                   => "",
    "As_Hard_Limit"                          => "",
    "As_Soft_Limit"                          => "",
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
    "Shape_Allocated"                        => "",
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
    "BG_Block_Allocated"                     => "bg_partalloc",
    "BG_Block_Boot_Time"                     => "",
    "BG_Block_Requested"                     => "bg_partreq",
    "BG_Block_Status"                        => "",
    "BG_Connectivity_Allocated"              => "",
    "BG_Connectivity_Requested"              => "",
    "BG_Error_Text"                          => "",
    "BG_IOLinks_Per_MP"                      => "",
    "BG_Job_Id"                              => "",
    "BG_Midplane_List"                       => "nodelist",
    "BG_Node_Board_List"                     => "nodelist_boards",
    "BG_Node_Configuration"                  => "",
    "BG_Rotate"                              => "",
    "BG_Shape_Allocated"                     => "bg_shape_alloc",
    "BG_Shape_Requested"                     => "bg_shape_req",
    "BG_Size_Allocated"                      => "bg_size_alloc",
    "BG_Size_Requested"                      => "bg_size_req",
    "BG_Status"                              => "bg_state",
    "Eligibility_Time"                       => "",
    "Flexible_Res._ID"                       => "",
    "Monitor_Program"                        => "",
    "Network_Usages"                         => "",
    "Step_Resources"                         => "",
    "Stripe_Min_Networks"                    => "",
    "Topology_Requirement"                   => "",
    "Trace"                                  => "",
    
    "status"                                 => "status",
    "detailedstatus"                         => "detailedstatus",

    );

my $cmd="/usr/bin/llq";
$cmd=$ENV{"CMD_JOBINFO"} if($ENV{"CMD_JOBINFO"}); 

open(IN,"$cmd -l |");

my $jobid="-";
my $lastkey="-";

while($line=<IN>) {
    chomp($line);
    last if($line=~/There is currently no job step to report/);
    if($line=~/^=+ Job Step $patwrd =+/) {
	$jobid=$1;
    } elsif($line=~/^\s*([^\:]+): (.*)$/) {
	($key,$value)=($1,$2);
	$key=~s/\s/_/gs;
	$lastkey=$key;
	$jobs{$jobid}{$key}=$value;
    } else {
	$line=~s/^\s*//gs;
	$jobs{$jobid}{$lastkey}.=",$line";
    }

}
close(IN);

# add unknown but manatory attributes to jobs
foreach $jobid (sort(keys(%jobs))) {
    $jobs{$jobid}{group}      = "unknown" if(!exists($jobs{$jobid}{group}));
    $jobs{$jobid}{exec_host}  = "-" if(!exists($jobs{$jobid}{exec_host}));
    $jobs{$jobid}{totaltasks} = 0 if(!exists($jobs{$jobid}{totaltasks}));
    $jobs{$jobid}{spec}       = 0 if(!exists($jobs{$jobid}{spec}));
    ($jobs{$jobid}{status},$jobs{$jobid}{detailedstatus}) = &get_state($jobs{$jobid}{Status}); 
}

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
    printf("%-40s => \"\"\n","\"".$key."\"",$notfoundkeys{$key});
}


sub get_state {
    my($job_state)=@_;
    my($state,$detailed_state);

    $state="UNDETERMINED";$detailed_state="";

    if($job_state eq "Completed") {
	$state="COMPLETED";$detailed_state="JOB_OUTERR_READY";
    }
    if($job_state eq "User Hold") {
	$state="SUBMITTED";
	$detailed_state="USER_ON_HOLD";
    }
    if($job_state eq "System Hold") {
	$state="SUBMITTED";
	$detailed_state="SYSTEM_ON_HOLD";
    }
    if($job_state eq "Removed") {
	$state="COMPLETED";$detailed_state="JOB_OUTERR_READY";
    }    
    if($job_state eq "Idle") {
	$state="SUBMITTED";$detailed_state="";
    }    
    if($job_state eq "Not Queued") {
	$state="SUBMITTED";$detailed_state="JOB_NOT_QUEUED";
    }    
    if($job_state eq "Running") {
	$state="RUNNING";$detailed_state="";
    }    

    return($state,$detailed_state);
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
