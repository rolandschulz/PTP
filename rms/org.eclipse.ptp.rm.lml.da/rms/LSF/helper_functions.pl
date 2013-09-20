#!/usr/bin/perl -w
#*******************************************************************************
#* Copyright (c) 2012 Forschungszentrum Juelich GmbH.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*
#* Contributors:
#*    Carsten Karbach (Forschungszentrum Juelich GmbH) 
#*******************************************************************************/ 

#This file collects shared functions used within this rms adapter
#The functions are included if needed with the "require" command

use strict;

#***************************************************************************
# Removes trailing and leading white spaces from the given parameter.
#
# @param $_[0] the scalar with possibly trailing and leading white spaces
#
# @return $_[0] without white spaces at beginning and end
#***************************************************************************
sub removeWhiteSpaces{
	my $result = shift(@_);
	
	$result =~ s/^\s*//;
    $result =~ s/\s*$//;
    
    return $result;
}

#***************************************************************************
# Get all lines from a filehandle, which start with leading white space
# and concatinate them without the white space into a single line.
#
#Example: <$in> contains the following
#Job <10425661>, User <mflehmig>, Project <hpc-flis>, Job Group </hpc-flis>, Sta
#                     tus <RUN>, Queue <interactive>, Interactive pseudo-termina
#                     l shell mode, ssh X11 forwarding mode, Job Priority <10>, 
#                     Command <bash>
#Thu Nov 15 07:18:41: Submitted from host <login1>, CWD <$HOME>, Requested Resou
#
#$_[1] will be set to the first line "Job <10425661>, User <mflehmig>, Project <hpc-flis>, Job Group </hpc-flis>, Sta"
#
#This function will return "Job <10425661>, User <mflehmig>, Project <hpc-flis>, Job Group </hpc-flis>, Status <RUN>, Queue <interactive>, Interactive pseudo-terminal shell mode, ssh X11 forwarding mode, Job Priority <10>,Command <bash>"
#Moreover, $_[1] will be set to the chomped line "Thu Nov 15 07:18:41: Submitted from host <login1>, CWD <$HOME>, Requested Resou"
#
#
# @param $_[0] reference to the file handle
# @param $_[1] contains the first line, which is already read from the filehandle
#				This variable is replaced by the first line, which is not handled by this function
#
# @return $_[0] concatinated line and replace $_[1]
#***************************************************************************
sub concatLines{
	my $in = $_[0];
	my $firstline = $_[1];
	
	chomp($firstline);
	
	my $line = $firstline;
	my $data = $firstline;
	
	while($line=<$in>){
    	chomp($line);
    	if($line =~ /^\s+\S+.*$/ ){
    		$line =~ s/^\s*//;
    		$data = $data.$line;
    	}
    	else{
    		#Continue with new line
    		$_[1] = $line;
    		last;
    	}
    }
    
    return $data;
}

#***************************************************************************
# Extract key value pairs from a single line with the format (.+<\S+>,)*.+<\S+>
# E.g. Job <10425661>, User <mflehmig>, Project <hpc-flis>, Job Group </hpc-flis>, Status <RUN>
#
# @param $_[0] data line in the given format
#
# @return $_[0] key value hash reference
#***************************************************************************
sub extractKeyValuePairs{
	my $data = $_[0];
	
	my (%keyvalues, $key, $value);
	
	#Handle key value pairs given in data
    my @keyvalues = split(/,/, $data);
	#Remove leading and trailing white spaces
    foreach my $keyvalue (@keyvalues){
    	$keyvalue = removeWhiteSpaces($keyvalue); 
    }
    #Split into key and value
    foreach my $keyvalue (@keyvalues){
    	if($keyvalue =~ /([^<>]+)<(.+)>/ ){
    		$key = removeWhiteSpaces($1);
    		$value = $2;
    		# Save into job's attributes, they are mapped later
    		$keyvalues{$key} = $value;
    	}
    }
    
    $data="blubb";
    
    return \%keyvalues;
}

#***************************************************************************
#
# @return current year in 4 digits
#
#***************************************************************************
sub getCurrentYear{
	my($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$idst)=localtime(time());
	return $year+1900;
}




1