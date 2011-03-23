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
package LML_combine_obj_check;
use strict;
use Data::Dumper;
use lib "$FindBin::RealBin/../LML_specs";
use LML_specs;

my $VERSION='$Revision: 1.00 $';
my($debug)=0;

sub check_jobs {
    my($dataptr) = shift;
    my($inforef,$id,$key);
    my(%unknown_attr,%unset_attr);

    foreach $id (keys(%{$dataptr->{OBJECT}})) {
	if($dataptr->{OBJECT}->{$id}->{type} eq "job") {
	    $inforef=$dataptr->{INFODATA}->{$id};
	    foreach $key (keys %{$inforef}) {
		if(!exists($LML_specs::LMLattributes->{'job'}->{$key})) {
		    $unknown_attr{$key}++;
		}
	    }
	    foreach $key (keys %{$LML_specs::LMLattributes->{'job'}}) {
		next if ($LML_specs::LMLattributes->{'job'}->{$key}->[1] ne "M");
		if(!exists($inforef->{$key})) {
		    $unset_attr{$key}++;
		}
	    }

	}
    }
    foreach $key (sort keys(%unknown_attr)) {
	printf("check_jobs: WARNING: unknown attribute '%s' %d occurrences\n",$key,$unknown_attr{$key});
    }
    foreach $key (sort keys(%unset_attr)) {
	printf("check_jobs: WARNING: unset attribute '%s' %d occurrences\n",$key,$unset_attr{$key});
    }

    return(1);
} 


1;
