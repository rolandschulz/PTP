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
use Getopt::Long;
use Data::Dumper;
use Storable qw(dclone); 

use FindBin;
use lib "$FindBin::RealBin/";
use lib "$FindBin::RealBin/../lib";

use LML_file_obj;
use LML_gen_table;
use LML_gen_nodedisplay;

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
my ($filename);


#####################################################################
# get command line parameter
#####################################################################

# option handling
my $opt_outfile="./test.xml";
my $opt_verbose=0;
my $opt_timings=0;
my $opt_dump=0;
my $opt_demo=0;
my $opt_layout="./layout.xml";
usage($0) if( ! GetOptions( 
			    'verbose'          => \$opt_verbose,
			    'timings'          => \$opt_timings,
			    'dump'             => \$opt_dump,
			    'demo'             => \$opt_demo,
			    'layout=s'         => \$opt_layout,
			    'output=s'         => \$opt_outfile
			    ) );

#print "@ARGV ($opt_outfile)\n";
if ($#ARGV < 0) {
    &usage($0);
}
my $LML_filename    = $ARGV[0];

my $system_sysprio=-1;
my $maxtopdogs=-1;
my $filehandler_layout;
my $filehandler_LML;
my $filehandler_out;

$filehandler_layout = LML_file_obj->new($opt_verbose,$opt_timings);
$filehandler_LML    = LML_file_obj->new($opt_verbose,$opt_timings);

$filehandler_out    = LML_file_obj->new($opt_verbose,$opt_timings);
$filehandler_out->init_file_obj();

print "reading file: $LML_filename  ...\n" if($opt_verbose); 
$filehandler_LML->read_lml_fast($LML_filename);
if($opt_verbose) {
    print $filehandler_LML->get_stat();
}

if($opt_demo) {
    &modify_jobs_anonymous($filehandler_LML);
}

print "reading file: $opt_layout  ...\n" if($opt_verbose); 
$filehandler_layout->read_lml_fast($opt_layout);
if($opt_verbose) {
    print $filehandler_layout->get_stat();
}

# determine system type
my $system_type      = "unknown";
my $system_idlistref = [];
{
    my($key,$ref);
    keys(%{$filehandler_LML->{DATA}->{OBJECT}}); # reset iterator
    while(($key,$ref)=each(%{$filehandler_LML->{DATA}->{OBJECT}})) {
	if($ref->{type} eq 'system') {
	    $ref=$filehandler_LML->{DATA}->{INFODATA}->{$key};
	    push(@{$system_idlistref},$key);
	    if($ref->{type}) {
		$system_type=$ref->{type};
		printf("scan system: type is %s\n",$system_type);
	    }
	    last; 
	}
    }
}
    
    

#########################
# process table layout
#########################
my ($tid,$tlayoutref);
#print Dumper($filehandler_layout->{DATA}->{TABLELAYOUT});
foreach $tid (keys(%{$filehandler_layout->{DATA}->{TABLELAYOUT}})) {
    my($table_handler,$tlayoutref_gid,$numids,$idlistref,$cnt,$active);

    $tlayoutref     = $filehandler_layout->{DATA}->{TABLELAYOUT}->{$tid};
    $tlayoutref_gid = $filehandler_layout->{DATA}->{TABLELAYOUT}->{$tid}->{gid};

    # check if layout if is active
    $active=1;    
    if(exists($filehandler_layout->{DATA}->{TABLELAYOUT}->{$tid}->{'active'})) {
	$active=0 if($filehandler_layout->{DATA}->{TABLELAYOUT}->{$tid}->{'active'} eq "false");
    }
    next if(!$active);

    # create table handler and process request
    $table_handler = LML_gen_table->new($opt_verbose,$opt_timings);
    $numids=$table_handler->process($tlayoutref,$filehandler_LML);
    if($numids>=0) {
	# add elements, objects, and info to output LML files
	$idlistref=$table_handler->get_ids();
	print "Table Layout: $tid processed ($numids objects found)\n"  if($opt_verbose);
	$cnt=&copy_objects_of_elements($filehandler_LML,$filehandler_out,$idlistref,"OBJECT");
	print "Table Layout: objects           of $tid copied ($cnt new objects)\n"  if($opt_verbose);
	$cnt=&copy_objects_of_elements($filehandler_LML,$filehandler_out,$idlistref,"INFO");
	print "Table Layout: info objects      of $tid copied ($cnt new objects)\n"  if($opt_verbose);
	$cnt=&copy_objects_of_elements($filehandler_LML,$filehandler_out,$idlistref,"INFODATA");
	print "Table Layout: info data objects of $tid copied ($cnt new objects)\n"  if($opt_verbose);
	
	$filehandler_out->{DATA}->{TABLE}->{$tid}=$table_handler->get_lml_table();
	$filehandler_out->{DATA}->{TABLELAYOUT}->{$tid}=$table_handler->get_lml_tablelayout();
    }
}

############################
# process nodedisplay layout
############################

#print Dumper($filehandler_layout->{DATA});
#print Dumper($filehandler_layout->{DATA}->{NODEDISPLAYLAYOUT});
my ($nid,$nlayoutref,$ndcnt,$el1);
$ndcnt=1;
foreach $nid (keys(%{$filehandler_layout->{DATA}->{NODEDISPLAYLAYOUT}})) {
    my($nd_handler,$numids,$idlistref,$cnt,$nlayoutref_gid,$nschemeref,,$active);

    $nlayoutref=$filehandler_layout->{DATA}->{NODEDISPLAYLAYOUT}->{$nid};
    $nlayoutref_gid=$filehandler_layout->{DATA}->{NODEDISPLAYLAYOUT}->{$nid}->{gid};

    # check if layout if is active
    $active=1;    
    if(exists($filehandler_layout->{DATA}->{NODEDISPLAYLAYOUT}->{$nid}->{'active'})) {
	$active=0 if($filehandler_layout->{DATA}->{NODEDISPLAYLAYOUT}->{$nid}->{'active'} eq "false");
    }
    next if(!$active);

    # check if scheme is given
    $nschemeref=undef;
    if(exists($filehandler_layout->{DATA}->{NODEDISPLAY})) {
		if(exists($filehandler_layout->{DATA}->{NODEDISPLAY}->{$nlayoutref_gid})) {
		    if($nlayoutref_gid ne "org.eclipse.ptp.rm.lml.ui.SystemMonitorView") {
			# check if el1 with attribute min is available
			if($el1=$filehandler_layout->{DATA}->{NODEDISPLAY}->{$nlayoutref_gid}->{schemeroot}->get_child({ _name => "el1" })) {
			    if(exists($el1->{ATTR}->{min})) {
				$nschemeref=$filehandler_layout->{DATA}->{NODEDISPLAY}->{$nlayoutref_gid}->{schemeroot};
			    }
			}
		    } else {
			# generate a new not dummy gid
			$filehandler_layout->{DATA}->{NODEDISPLAYLAYOUT}->{$nid}->{gid}="nd_$ndcnt";
			$cnt++;
		    }
		}		
    }
    #Use schemehint in nodedisplaylayout, if hint is provided and if nodedisplay tag is missing
	if(!defined($nschemeref) ){
		if(defined($nlayoutref->{schemehint})){
			$nschemeref = $nlayoutref->{schemehint};
		}
	}

    # create nd handler and process request
    $nd_handler = LML_gen_nodedisplay->new($opt_verbose,$opt_timings);
    $numids=$nd_handler->process($nlayoutref,$nschemeref,$filehandler_LML);

    # add elements, objects, and info to output LML files
    $idlistref=$nd_handler->get_ids();
    print "Nodedisplay Layout: $nid processed ($numids objects found)\n"  if($opt_verbose);
    $cnt=&copy_objects_of_elements($filehandler_LML,$filehandler_out,$idlistref,"OBJECT");
    print "Nodedisplay Layout: objects           of $nid copied ($cnt new objects)\n"  if($opt_verbose);
    $cnt=&copy_objects_of_elements($filehandler_LML,$filehandler_out,$idlistref,"INFO");
    print "Nodedisplay Layout: info objects      of $nid copied ($cnt new objects)\n"  if($opt_verbose);
    $cnt=&copy_objects_of_elements($filehandler_LML,$filehandler_out,$idlistref,"INFODATA");
    print "Nodedisplay Layout: info data objects of $nid copied ($cnt new objects)\n"  if($opt_verbose);

    $filehandler_out->{DATA}->{NODEDISPLAY}->{$nid}=$nd_handler->get_lml_nodedisplay();
    $filehandler_out->{DATA}->{NODEDISPLAYLAYOUT}->{$nid}=$nd_handler->get_lml_nodedisplaylayout();
}

# define defaut objects, like job 'empty'
&define_default_objects($filehandler_out);

# copy system element
{
    my($cnt);
    $cnt=&copy_objects_of_elements($filehandler_LML,$filehandler_out,$system_idlistref,"OBJECT");
    $cnt=&copy_objects_of_elements($filehandler_LML,$filehandler_out,$system_idlistref,"INFO");
    $cnt=&copy_objects_of_elements($filehandler_LML,$filehandler_out,$system_idlistref,"INFODATA");
}

# handle layout (split, abs) for positioning elements
if(exists($filehandler_layout->{DATA}->{SPLITLAYOUT})) {
    $filehandler_out->{DATA}->{SPLITLAYOUT}=dclone($filehandler_layout->{DATA}->{SPLITLAYOUT});
}


print "Writing output: $opt_outfile\n";
$filehandler_out->write_lml($opt_outfile);
if($opt_verbose) {
    print $filehandler_out->get_stat();
}

sub copy_objects_of_elements {
    my $fh_in=shift;
    my $fh_out=shift;
    my $idlistref=shift;
    my $element=shift;
    my($id,$cnt);
    
    $cnt=0;
    foreach $id (@{$idlistref}) {
	if(exists($fh_in->{DATA}->{$element}->{$id})) {
	    if(!exists($fh_out->{DATA}->{$element}->{$id})) {
		$fh_out->{DATA}->{$element}->{$id}=dclone($fh_in->{DATA}->{$element}->{$id});
		$cnt++;
	    }
	}
    }
    return($cnt);
}

sub define_default_objects  {
    my $fh_out=shift;
    my($id);

    $id='empty';
    $fh_out->{DATA}->{OBJECT}->{$id}->{type} = 'job';
    $fh_out->{DATA}->{OBJECT}->{$id}->{id}   = $id;
    $fh_out->{DATA}->{OBJECT}->{$id}->{color}= '#FFFFFF';
    $fh_out->{DATA}->{OBJECT}->{$id}->{name} = "Empty job";

    return(1);
}

sub modify_jobs_anonymous {
    my($filehandler_LML) = @_;
    my($tstart,$tdiff,$rc,$job);
    my($demoid,$partition,$reservation);
    my(%userlist,$user,$demoidcntuser);
    my(%grouplist,$group,$demoidcntgroup);

    $tstart=time;
    $demoidcntuser=0;
    keys(%{$filehandler_LML->{DATA}->{OBJECT}}); # reset iterator
    my($key,$ref);
    while(($key,$ref)=each(%{$filehandler_LML->{DATA}->{OBJECT}})) {
	if($ref->{type} eq 'job') {
	    $ref=$filehandler_LML->{DATA}->{INFODATA}->{$key};
	    if($ref->{owner}) {
		$user=$ref->{owner};
		if($userlist{$user}) { $demoid=$userlist{$user};} 
		else                 { 
		    $demoid=$userlist{$user}=sprintf("User#%03d",$demoidcntuser++);
#		    printf("demouser  ->  %-10s %10s (job)\n",$user,$demoid) if($opt_verbose); 
		}
		$ref->{owner}=$demoid;
	    }
	    if($ref->{group}) {
		$group=$ref->{group};
		if($grouplist{$group}) { $demoid=$grouplist{$group};} 
		else                 { 
		    $demoid=$grouplist{$group}=sprintf("Group#%03d",$demoidcntgroup++);
#		    printf("demogroup  ->  %-10s %10s (job)\n",$group,$demoid) if($opt_verbose); 
		}
		$ref->{group}=$demoid;
	    }
	    if($ref->{executable}) {
		$ref->{executable}="";
	    }
	    if($ref->{name}) {
		$ref->{name}="jobname";
	    }
	}
    }
    keys(%{$filehandler_LML->{DATA}->{OBJECT}}); # reset iterator
    $tdiff=time-$tstart;
    printf("$0: changed jobs to anonymous in %6.4f sec\n",$tdiff) if($opt_verbose);

    return($rc);
}


sub usage {
    die "Usage: $_[0] <options> <filenames> 
                -output <file>           : LML output filename
                -layout <file>           : LML layout filename
                -verbose                 : verbose
";
}

1;
