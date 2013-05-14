#!/usr/bin/perl -w
#*******************************************************************************
#* Copyright (c) 2011 University of Illinois and others.  All rights reserved.
#* This program and the accompanying materials are made available under the
#* terms of the Eclipse Public License v1.0 which accompanies this distribution,
#* and is available at http://www.eclipse.org/legal/epl-v10.html 
#* 
#* Contributors: 
#*     Jeff Overbey (Illinois/NCSA) - Design and implementation
#*     Carsten Karbach (Forschungszentrum Juelich GmbH)
#*******************************************************************************

package GEHelper;

use strict;
use warnings;

use POSIX qw(ceil floor);

use Exporter;
our @ISA= qw( Exporter );
our @EXPORT_OK = qw( parsexml trimtext get_jobs get_job_nodes get_nodes should_group_nodes group_nodes MAX_CORES );
our @EXPORT = qw( get_jobs get_job_nodes get_nodes should_group_nodes group_nodes MAX_CORES );

use constant {
    MAX_NODES => 10000, # If the system has more nodes than this, nodes will be
                        # grouped so that LML displays one node per box rather
                        # than one core per box, in general nodes should not be grouped
    MAX_CORES => 64,    # If nodes are not grouped, this is the maximum number
                        # of cores that will be displayed for a single node
};

# The return value of get_nodes() is cached here to avoid re-executing qhost
my %cached_nodes = ();

my $cmd_jobinfo="/usr/bin/qstat";
$cmd_jobinfo=$ENV{"CMD_JOBINFO"} if($ENV{"CMD_JOBINFO"});

my $cmd_nodeinfo="/usr/bin/qhost";
$cmd_nodeinfo=$ENV{"CMD_NODEINFO"} if($ENV{"CMD_NODEINFO"}); 



###############################################################################
# get_jobs - Returns a hash containing information about jobs (from qstat)
###############################################################################
#
# The returned hash has job numbers as its keys and dictionaries of LML
# attributes as its values.
#
sub get_jobs {
    my %qstat_xml = parsexml("$cmd_jobinfo -u '*' -s prsz -r -xml |");
    trimtext(\%qstat_xml);

    my %jobs = ();

    my @jobs_in_queue;
    if(defined($qstat_xml{queue_info}[0]{job_list})){
    	@jobs_in_queue = @{$qstat_xml{queue_info}[0]{job_list}};
    } 
    else{
    	@jobs_in_queue = ();
    }
    
    my @jobs_not_in_queue;
    if(defined($qstat_xml{job_info}[0]{job_list})){
    	@jobs_not_in_queue = @{$qstat_xml{job_info}[0]{job_list}};
    } 
    else{
    	@jobs_not_in_queue = ();
    }
    
    for my $job (@jobs_in_queue, @jobs_not_in_queue) {
        # The hash referenced by $job corresponds to an XML element like this:
        # <job_list state="running">
        #     <JB_job_number>344911</JB_job_number>
        #     <JAT_prio>0.12923</JAT_prio>
        #     <JB_name>RF-38</JB_name>
        #     <JB_owner>tg458455</JB_owner>
        #     <state>r</state>
        #     <JAT_start_time>2011-11-29T03:21:14</JAT_start_time>
        #     <queue_name>normal@c327-104.ls4.tacc.utexas.edu</queue_name>
        #     <slots>24</slots>
        #     <full_job_name>RF-38</full_job_name>
        #     <requested_pe name="12way">24</requested_pe>
        #     <granted_pe name="12way">24</granted_pe>
        #     <hard_request name="h_rt" resource_contribution="0.000000">86400</hard_request>
        #     <hard_request name="mem_total" resource_contribution="0.000000">23.4G</hard_request>
        #     <hard_req_queue>normal</hard_req_queue>
        # </job_list>
        my $jobid                 = $job->{JB_job_number}[0]{_text};
        $jobs{$jobid}{step}       = $jobid;
        $jobs{$jobid}{name}       = $jobid; #$job->{JB_name}[0]{_text};
        $jobs{$jobid}{owner}      = $job->{JB_owner}[0]{_text};
        $jobs{$jobid}{state}      = $job->{_attrs}{state};
        $jobs{$jobid}{totalcores} = $job->{slots}[0]{_text};
        $jobs{$jobid}{queue}      = $job->{queue_name}[0]{_text};
        if ($job->{_attrs}{state} ne "running") {
            $jobs{$jobid}{queuedate} = $job->{JB_submission_time}[0]{_text};
        } else {
            $jobs{$jobid}{dispatchdate} = $job->{JAT_start_time}[0]{_text};
        }
        $jobs{$jobid}{wall}       = '-';
        if (defined ($job->{hard_request})) {
            for my $request (@{$job->{hard_request}}) {
                if ($request->{_attrs}{name} eq 'h_rt') {
                    $jobs{$jobid}{wall} = $request->{_text};
                }
            }
        }
    }

    return %jobs;
}



###############################################################################
# get_job_nodes - Returns a hash containing information about what nodes each
#                 job is running on (from qhost)
###############################################################################
#
# The returned hash has job numbers as its keys and a list of node names attached with the
# number of cores used on each node. E.g. for one job this string could look like (node1,10)(node2,7).
# I.e. 10 cores are used on node1 and 7 cores on node2.
# Unfortunately, there is no information about the exact
# cores a job is assigned to on nodes with multiple cores.
#
sub get_job_nodes {
    my %qhost_xml = parsexml("$cmd_nodeinfo -j -xml |");
    trimtext(\%qhost_xml);

    my %job_hosts = ();
    for my $host (@{$qhost_xml{host}}) {
        # The hash referenced by $host corresponds to an XML element like
        # <host name='c300-001.ls4.tacc.utexas.edu'>
        #   <hostvalue name='arch_string'>lx24-amd64</hostvalue>
        #   <hostvalue name='num_proc'>12</hostvalue>
        #   <hostvalue name='load_avg'>0.02</hostvalue>
        #   <hostvalue name='mem_total'>47.2G</hostvalue>
        #   <hostvalue name='mem_used'>892.5M</hostvalue>
        #   <hostvalue name='swap_total'>0.0</hostvalue>
        #   <hostvalue name='swap_used'>0.0</hostvalue>
        # </host>
        my $hostname = $host->{_attrs}{name};
        $hostname =~ s/\..*//;
        if (defined($host->{job})) {
            for my $jobelt (@{$host->{job}}) {
                # The hash referenced by $jobelt corresponds to an XML element like
                #  <job name='353538'>
                #   <jobvalue jobid='353538' name='priority'>'0.005689'</jobvalue>
                #   <jobvalue jobid='353538' name='qinstance_name'>grace@c300-003.ls4.tacc.utexas.edu</jobvalue>
                #   <jobvalue jobid='353538' name='job_name'>acc+att.2008-01</jobvalue>
                #   <jobvalue jobid='353538' name='job_owner'>byaa705</jobvalue>
                #   <jobvalue jobid='353538' name='job_state'>r</jobvalue>
                #   <jobvalue jobid='353538' name='start_time'>1322585173</jobvalue>
                #   <jobvalue jobid='353538' name='queue_name'>grace@c300-003.ls4.tacc.utexas.edu</jobvalue>
                #   <jobvalue jobid='353538' name='pe_master'>SLAVE</jobvalue>
                # </job>
                my $jobid = $jobelt->{_attrs}{name};
                if (!defined($job_hosts{$jobid})) {
                    $job_hosts{$jobid} = {};
                }
                if(! defined($job_hosts{$jobid}->{$hostname})){
                	$job_hosts{$jobid}->{$hostname} = 0;
                }
                
                #Check if pe_master has the value MASTER, 
                #in this case do not increase the number of used slots on this host
                
                my $ismaster = 0;
                
                if( defined( $jobelt->{'jobvalue'} ) ){
                	 my @jobvalues = @{ $jobelt->{'jobvalue'} };
                	 for my $jobval (@jobvalues){
                	 	if(defined ($jobval->{_attrs}{name} ) && $jobval->{_attrs}{name} eq "pe_master" ){
                	 		if(defined($jobval->{_text}) && $jobval->{_text} eq "MASTER"){
                	 			$ismaster = 1;
                	 			last;
                	 		}
                	 	}
                	 }
                }
                
                if($ismaster == 0){
                	$job_hosts{$jobid}->{$hostname} += 1;
                }
            }
        }
    }

    for my $jobid (keys %job_hosts) {
        my $nodelist = '';
        for my $node (keys %{$job_hosts{$jobid}}) {
            $nodelist .= "($node,$job_hosts{$jobid}->{$node})";
        }
        $job_hosts{$jobid} = $nodelist;
    }

    return %job_hosts;
}



###############################################################################
# get_nodes - Returns a hash containing information about nodes (from qhost)
###############################################################################
#
# The returned hash has node names as its keys and dictionaries of LML
# attributes as its values.
#
sub get_nodes {
    if (scalar keys %cached_nodes > 0) {
        return %cached_nodes;
    }

    my %qhost_xml = parsexml("$cmd_nodeinfo -xml |");
    trimtext(\%qhost_xml);

    my %nodes = ();
    for my $host (@{$qhost_xml{host}}) {
        # The hash referenced by $host corresponds to an XML element like
        # <host name='c300-001.ls4.tacc.utexas.edu'>
        #   <hostvalue name='arch_string'>lx24-amd64</hostvalue>
        #   <hostvalue name='num_proc'>12</hostvalue>
        #   <hostvalue name='load_avg'>0.02</hostvalue>
        #   <hostvalue name='mem_total'>47.2G</hostvalue>
        #   <hostvalue name='mem_used'>892.5M</hostvalue>
        #   <hostvalue name='swap_total'>0.0</hostvalue>
        #   <hostvalue name='swap_used'>0.0</hostvalue>
        # </host>

        my $nodeid = $host->{_attrs}{name};
        $nodeid =~ s/\..*//;

        my %hostvalues = ();
        for my $hostvalue (@{$host->{hostvalue}}) {
            my $key = $hostvalue->{_attrs}{name};
            my $value = $hostvalue->{_text};
            $hostvalues{$key} = $value;
        }

        $nodes{$nodeid}{id} = $nodeid;

        $nodes{$nodeid}{ntype} = $hostvalues{arch_string};

        if (defined($hostvalues{num_proc})) {
            my $ncores = $hostvalues{num_proc};
            if ($ncores =~ /^[0-9]+$/) {
                if ($ncores > MAX_CORES) {
                    $ncores = MAX_CORES;
                } else {
                    $nodes{$nodeid}{ncores} = $ncores;
                }
            } else {
                $nodes{$nodeid}{ncores} = 1;
            }
        }

        $nodes{$nodeid}{physmem} = $hostvalues{mem_total};
    }

    %cached_nodes = %nodes;
    return %nodes;
}



###############################################################################
# should_group_nodes - Returns true if nodes should be grouped in the LML
#                      display (for performance reasons)
###############################################################################
sub should_group_nodes {
    my %nodes = get_nodes();
    my $num_nodes = scalar keys %nodes;
    return $num_nodes > MAX_NODES;
}



###############################################################################
# group_nodes - Places the system's nodes into groups such that there are no
#               more than MAX_NODES groups.
###############################################################################
#
# Three values are returned:
# - A reference to a hash mapping node names to group names
# - A reference to a hash mapping node names to their 0-based indices in the group
# - The (maximum) number of nodes in each group
#
sub group_nodes {
    my %nodes = get_nodes();
    my @nodeids = sort(keys(%nodes));
    my $num_nodes = $#nodeids + 1;

    my %node_group = ();
    my %node_index = ();
    my $nodes_per_group = ceil($num_nodes / MAX_NODES);
    if ($num_nodes <= MAX_NODES) {
        %node_group = map { $_ => $_ } @nodeids;
        %node_index = map { $_ => 0 } @nodeids;
    } else {
        my $nodenum = 0;
        for my $nodeid (@nodeids) {
            $node_group{$nodeid} = "Group" . floor($nodenum / $nodes_per_group);
            $node_index{$nodeid} = $nodenum % $nodes_per_group;
            $nodenum++;
        }
    }
    return (\%node_group, \%node_index, $nodes_per_group);
}



###### (PRIVATE) ##############################################################
# parsexml - Parses the given XML file into a hash.
###############################################################################
#
# The hash is keyed by the names of child elements.  It also contains
# the keys '_attrs' and '_text'.
#
# As an example, the XML file
#     <?xml version='1.0'?>
#     <aaa xxx="1">
#       <bbb xxx="" yyy="  2">This is child 1</bbb>
#       <bbb>This is child 2
#         <ccc/>
#       </bbb>
#     </aaa>
# is parsed into the hash
#     { '_text' => '\n\n\n',
#       '_attrs' => { 'xxx' => '1' },
#       'bbb' => [ { '_text' => 'This is child 1',
#                    '_attrs' => { 'xxx' => '',
#                                  'yyy' => '  2' }
#                  },
#                  { '_text' => 'This is child 2\n    \n  ',
#                    '_attrs' => {},
#                    'ccc' => [ { '_text' => '',
#                               '_attrs' => {} }
#                             ]
#                  }
#                ]
#     };
#
# This isn't fully general (e.g., it doesn't handle CDATA,
# and it assumes there won't be any child elements with the
# names '_attrs' or '_text') but it works for our purposes.
#
# Ideally, we could use XML::Simple and avoid this, except the
# TACC machines only have the XML::Parser module available.
sub parsexml {
    my ($file) = @_;

    use XML::Parser;

    # When we encounter a start tag, we will push the hash
    # for that XML element onto the stack.
    my @stack = ();

    my $parser = new XML::Parser(ErrorContext => 2);
    $parser->setHandlers(
        Start => sub {
                my ($parser, $elt, %attrs) = @_;
                # When we encounter an XML start tag, create a hash
                # for that XML element and push it onto the stack.
                my %element = ( _attrs => \%attrs, _text => '' );
                push(@stack, \%element);
            },
        Char => sub {
                my ($parser, $text) = @_;
                # When we encounter text, append it to the _text
                # for the current XML element.
                my $topref = $stack[-1];
                ${$topref}{_text} .= $text;
            },
        End => sub {
                my ($parser, $elt) = @_;
                # When we encounter an XML end tag, pop that
                # element's hash from the stack and mark it
                # as a child of the parent element.
                if ($#stack > 0) {
                    my $childref = pop(@stack);
                    my $parentref = $stack[-1];
                    if (not exists ${$parentref}{$elt}) {
                        ${$parentref}{$elt} = [];
                    }
                    my $arrayref = @{$parentref}{$elt};
                    push(@{$arrayref}, $childref);
                }
            });
    $parser->parsefile($file);
    if ($#stack == -1) {
        return undef;
    } else {
        return %{$stack[-1]};
    }
}



###### (PRIVATE) ##############################################################
# trimtext - Traverses the a tree returned by parsexml, removing leading and
#            trailing whitespace from all '_text' values.
###############################################################################
#
# As an example, when trimtext is applied to the example for parsexml above,
# the hash becomes the following:
#     { '_text' => '',
#       '_attrs' => { 'xxx' => '1' },
#       'bbb' => [ { '_text' => 'This is child 1',
#                    '_attrs' => { 'xxx' => '',
#                                  'yyy' => '  2' }
#                  },
#                  { '_text' => 'This is child 2',
#                    '_attrs' => {},
#                    'ccc' => [ { '_text' => '',
#                               '_attrs' => {} }
#                             ]
#                  }
#                ]
#     };
#
# Note that only the '_text' values are trimmed.  The attribute values (e.g.,
# for 'yyy') are not trimmed.
#
sub trimtext {
    my ($xml) = @_;
    for my $key (keys %{$xml}) {
        my $val = $xml->{$key};
        if ($key eq '_text') {
            $xml->{_text} =~ s/^\s+//;
            $xml->{_text} =~ s/\s+$//;
        } elsif (ref($val) eq "ARRAY") {
            for my $elt (@{$val}) {
                trimtext($elt);
            }
        } elsif (ref($val) eq "HASH") {
            trimtext($val);
        }
    }
    return $xml;
}

1;
