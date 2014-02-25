#*******************************************************************************
#* Copyright (c) 2014 Forschungszentrum Juelich GmbH.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*
#* Contributors:
#*    Carsten Karbach (Forschungszentrum Juelich GmbH) 
#*******************************************************************************/ 

#********************************************
# This package tries to automatically detect
# the system's resource management system (RMS) 
# such as TORQUE, SLURM or LSF.
# There is also a function for checking, if
# the current system provides the functions
# needed for a given RMS.
#********************************************
package RMS_check;

#************************************************
# Initialize the RMS checker.
#
# @return instance of this class
#
#************************************************
sub new {
    my $self  = {};
    my $proto = shift;
    my $class = ref($proto) || $proto;

    bless $self, $class;
    
    return $self;
}

#*******************************************************
# Traverse the RMS scripts and let them check, whether
# all commands needed for an RMS are available.
# Return the first RMS name, which might be the RMS
# used at this system.
#
# @param $_[0] reference to this package's instance
#
# @param $_[1] path to the rms directory within LML_DA
#
# @return the name of the RMS installed or an undefined
#			value, if no matching RMS is found
#
#*******************************************************
sub detectRMS{
	my $self = shift;
	my $rmsDirectory = shift;
	
	my $rms;#The detected rms
	
	my $r; # Current directory, whose check function is executed
	my %cmds_save=();
	foreach $r (<$rmsDirectory/*>) {
		my %cmds=(%cmds_save);
		my $test_rms=$r; # The currently tested RMS
		$test_rms=~s/(.*\/)//s; # Keep only the name of the rms

		if (do "$r/da_check_info_LML.pl") { #Execute the da_check_info_LML.pl script of the given RMS
			#Now a check function should be loaded into the check_functions hash
			if (exists($main::check_functions->{$test_rms})) {
				if ( &{$main::check_functions->{$test_rms}}(\$rms,\%cmds,0)) {
						$rms=$test_rms;
						last;
				} 
			}
		}
	}
	
	return $rms;
}

#*************************************
# Checks an RMS hint for validity.
# Tests, if the check function for that
# RMS exists and returns true.
#
# @param $_[0] reference to this package's instance
#
# @param $_[1] path to the rms directory within LML_DA
#
# @param $_[2] name of the validated RMS
#
# @return 1, if the RMS commands are
#			available, 0 otherwise
#
#*************************************
sub isRMSValid{
	my $self = shift;
	my $rmsDirectory = shift;
	my $rms = shift;
	
	$rms = uc $rms;
	
	if (do "$rmsDirectory/$rms/da_check_info_LML.pl") {
		if (exists($main::check_functions->{$rms})) {
			if ( &{$main::check_functions->{$rms}}(\$rms,\%cmds,$options{verbose})) {
				return 1;
			}
		}
	}
	
	return 0;
}

#*************************************************************
# Stub function for ignoring reports, when the check functions 
# are called only in the context of RMS_check.
#
#*************************************************************
sub report_if_verbose {
    if(defined($REPORT) ){
    	my $format = shift;
	    # print to protocol file
	    $REPORT.=sprintf( $format, @_ );
	    
	    if($options{verbose}) {
	        # print to stderr
	        printf(STDERR $format, @_);
	    }
    }
}


1;