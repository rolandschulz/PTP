#!/usr/bin/perl -w
#*******************************************************************************
#* Copyright (c) 2013 Forschungszentrum Juelich GmbH.
#* All rights reserved. This program and the accompanying materials
#* are made available under the terms of the Eclipse Public License v1.0
#* which accompanies this distribution, and is available at
#* http://www.eclipse.org/legal/epl-v10.html
#*
#* Contributors:
#*    Carsten Karbach (Forschungszentrum Juelich GmbH) 
#*******************************************************************************/ 
use strict;

my $tmpdir;#tmp directory produced within .eclipsesettings folder after lml_da call
my $targetdir;#target directory, where test package should be generated
my $instdir;#path to root directory of lml_da installation

if(scalar(@ARGV) >= 3 ){#Use user parameters
	$tmpdir = $ARGV[0];#tmp directory produced within .eclipsesettings folder after lml_da call
	$targetdir = $ARGV[1];#target directory, where test package should be generated
	$instdir = $ARGV[2];#path to root directory of lml_da installation
}
else{#Generate everything on your own
	
	my $rms = "";
	if(scalar(@ARGV) == 1){
		$rms = $ARGV[0];
	}
	
	my $rmsopt = "";
	if($rms ne ""){
		$rmsopt = "-rms $rms";
	}
	
	print "Generate 'tmpdir' in .eclipsesettings\n";
	
	#Create a tmpdir by calling LML_da_driver.pl
	
	my $srcDir = $0;#Extract directory path to this script
	if($srcDir =~ /\// ){
		$srcDir =~ s/[^\/]+\.pl$//;
	}
	else{
		$srcDir = "./";
	}
	
	my $cmd = "cd ".$srcDir."../; perl LML_da_driver.pl $rmsopt -k -v -tmpdir ./tmpdir samples/layout_default.xml result.xml";
	printAndExecCommand($cmd);
	
	$tmpdir = $srcDir."../tmpdir";
	$targetdir = $srcDir."../testdir";
	$instdir = $srcDir."..";
}

print "Start test directory generation with tmp directory = '$tmpdir', target directory = '$targetdir' and LML_da root directory = '$instdir'\n";

if(!defined($instdir)){
	$instdir = ".";
}

#Delete target dir and recreate it
my $status = printAndExecCommand("rm -rf $targetdir; mkdir $targetdir");
#Copy all files into test subdirectory of target
$status = printAndExecCommand("cp -r $tmpdir $targetdir/test");
$status = printAndExecCommand("rm -f $targetdir/test/datastep* $targetdir/test/LML_da* $targetdir/test/report.log $targetdir/test/*LML.xml");

#Adjust workflow file to test
open IN, "<", "$targetdir/test/workflow.xml" or die $!;
open OUT, ">", "$targetdir/test/newworkflow.xml" or die $!;
my $line;
my %localscripts;#E.g. CMD_NODEINFO -> CMD_NODEINFO.sh
my %remotecommands;#E.g. CMD_NODEINFO -> /usr/bin/pbsnodes
my $rms = "TORQUE";# Stores the remote system used as lml da adapter
while($line = <IN> ){
	chomp($line);
	my $permdirLine = 0;
	if($line =~ /<var key="tmpdir" value/){
		$line = '<var key="tmpdir" value="./test"/>';
	}
	if($line =~ /<var key="permdir"/){
		$line = '<var key="permdir" value="./testperm"/>';
		$permdirLine = 1;
	}
	
	#Make sure that there is a blank before LML2LML, but do not remove it
	if($line=~/\s+LML2LML\/LML2LML.pl/){
		$line=~ s/LML2LML\/LML2LML.pl/\$instdir\/LML2LML\/LML2LML.pl/g;
	}

	#Replace the batch system commands with local shell scripts
	if($line =~ /exec\s*=\s*"(CMD_.*)"/){
		my $command = $1;
		my @parts = split(/\s+/, $command);
		
		if($command =~ /rms\/(\w+)\//){
			$rms = $1;
		}
		
		foreach(@parts){
			if($_ =~ /CMD_.+=.+/ ){
				my @keyvalue = split(/=/, $_);
				
				$localscripts{$keyvalue[0]} = $keyvalue[0].".sh"; 
				$remotecommands{$keyvalue[0]} = $keyvalue[1]; 
				
				$keyvalue[1] = '$tmpdir/'.$localscripts{$keyvalue[0]};
				$_ = join( "=", @keyvalue);
			}
			
			if($_ =~ /^rms\/.+\/.+/ ){
				$_ = '$instdir/'.$_;
			}
		}
		
		$command = join(" ", @parts);
		$line = "<cmd exec=\"$command\"/>";
		
	}
	
	print OUT $line."\n";
	if($permdirLine){
		print OUT '<var key="instdir" value="../../../../org.eclipse.ptp.rm.lml.da"/>'."\n";
	}
}

#Generate local .sh scripts, which only zcat the actual results
foreach my $varname(keys(%localscripts)){
	print "Creating $localscripts{$varname}\n";
	
	my $params = getParametersForBatchCommand($rms, $varname, $instdir);
	if(defined($params) && $params ne ""){
		print "\tfound parameters $params for command $varname\n";
	}
	my $outfile = $localscripts{$varname};
	$outfile =~ s/\.sh/\.out/;
	#Call remote command with given params
	$status = printAndExecCommand("$remotecommands{$varname} > $targetdir/test/$outfile $params");
	
	#Zip the output file
	$status = printAndExecCommand("gzip $targetdir/test/$outfile");
	
	#Generate local script
	$status = printAndExecCommand("echo \"zcat test/$outfile.gz\" > $targetdir/test/$localscripts{$varname} ");
	
	$status = printAndExecCommand("chmod 700 $targetdir/test/$localscripts{$varname}");
	
	print "\n";
}

close(OUT);

#mv the created new workflow to the workflow.xml file
$status = printAndExecCommand("mv $targetdir/test/newworkflow.xml $targetdir/test/workflow.xml");

print "Test package successfully created in folder $targetdir\n";


#***********************************************************
# Searches for the command arguments for a batch system
# command within the server scripts of a given rms.
#
# $_[0] name of rms folder containing the scripts, e.g. TORQUE/LL/LL_BG
# $_[1] command variable name, e.g. CMD_NODEINFO
# $_[2] path to installation directory of lml_da
#
#***********************************************************
sub getParametersForBatchCommand{
	my $rms = shift;
	my $varname = shift;
	my $instdir = shift;
	my $result = "";
	
	my $varpart = $varname;
	$varpart =~ s/CMD_//;
	$varpart =~ s/INFO//;
	
	#Special case for GRIDENGINE, source files do not match the required format -> thus explicit reference to their parameters necessary
	if($rms eq "GRIDENGINE"){
		if($varpart eq "JOB"){
			return '-u \'*\' -s prsz -r -xml';
		}
		if($varpart eq "NODE"){
			return '-j -xml';
		}
	}
	
	opendir(RMSDIR,"$instdir/rms/$rms") or die "Cannot open $instdir/rms/$rms -> this is not the root of lml_da installation\n";
    my @files = readdir(RMSDIR);
    closedir(RMSDIR);
    #Search for the open cmd statesments in the perl server scripts
    foreach my $file (@files) {
        if($file =~ /\.pl/ && $file =~ /$varpart/i ){
        	open SCRIPTIN, "<", "$instdir/rms/$rms/$file" or die $!;
        	my $varnameFound = 0;
        	my $myline;
        	while($myline = <SCRIPTIN>){
        		# Search for the variable name in lines like: $cmd=$ENV{"CMD_JOBINFO"} if($ENV{"CMD_JOBINFO"}); 
				if( $myline =~ /\$cmd\s*=\s*\$ENV\{\s*"$varname"\s*\}/ ){
					$varnameFound = 1;
				}
        		
        		if(! $varnameFound){
        			next;
        		}
        		#varname was found in a previous line
        		# Search for the following line: open(IN,"$cmd -r -a -v |");
        		
        		if($myline =~ /open\(\s*\w+\s*,\s*"\s*\$cmd\s+([^\|]+)\|\s*"\s*\)/ ){
        			$result = $1;
        			return $result;
        		}
        	}
        	close(SCRIPTIN);
        	
        }
    }
    
    return $result;
}

#*****************************************************************
# Executes a shell command with the system function. Before that, the
# executed command is printed to STDOUT.
#
# $_[0] contains the command, which has to be executed 
#
#
#*****************************************************************
sub printAndExecCommand{
	my $cmd = shift;
	my $status;
	print "*>".$cmd."\n";
	$status = system($cmd);
	return $status;
}

