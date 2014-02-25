#!/usr/bin/perl -w
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
use strict;

#*********************************************************************************
# Summary of the installation procedure:
#
# 1) Check the options configured in the ./config file
#  -> Check the RMS hint and whether required commands are available
#  -> Test the cache directory accessibility
# 2) Generate the script called repeatedly, which generates the raw LML file
#  -> Make the script executable 
# 3) Create a file containing the cron job
# 4) Create a daemon script as alternative for crontab
# 5) Explain, how to add the cronjob and how to use the daemon script
#
#*********************************************************************************

use FindBin;
use lib "$FindBin::RealBin/../../lib";#Include LML_DA library
use RMS_check;
use Data_cache;

my $crontabScriptName = "LML_da_crontab.sh";#Name of the script triggered by the crontab
my $cronjobFile = "crontab.add";#Name of the cronjob file added to the existing cronjobs
my $daemon = "daemon_script.sh"; # Script, which can run the crontab script repeatedly 
my $hostname = `hostname`;#Can be available in the option values from the config file

my $step = 1;

chomp($hostname);

readOptionsFromConfig();

my %options = %{readOptionsFromConfig()};

my $rms_check = RMS_check->new();
my $rms_directory = "../../rms"; #Relative path to all RMS directories
my $lmlDaInstallDirectory = $FindBin::RealBin."/../../";

print "Installation options: ";
foreach my $key (keys(%options) ){
	my $value = $options{$key};
	if(defined($value)){
		print $key." => \"".$value."\" ";
	}
}
print "\n\n";

print $step.") Check the options configured in the ./config file\n";
$step++;
#Check, if needed options are set
if(! defined($options{rms}) ){
	print STDERR "There is nothing defined as RMS hint. Please set the rms option in the config file. Installation aborted.\n";
	exit 1;
}
if(! defined($options{cachedir}) ){
	print STDERR "There is no cache directory defined. Please set the cachedir option in the config file. Installation aborted.\n";
	exit 1;
}
if(! defined($options{croninterval}) ){
	print STDERR "There is no interval for the crontab configuration defined. Please set the croninterval option in the config file. Installation aborted.\n";
	exit 1;
}
if(! defined($options{permdir}) ){
	print STDERR "There is no permanent directory for the LML DA run defined. Please set the permdir option in the config file. Installation aborted.\n";
	exit 1;
}
if(defined($options{installdir}) && $options{installdir} ne ""){
	$lmlDaInstallDirectory = $options{installdir};
}
if(! -d $lmlDaInstallDirectory){
	print STDERR "The installation directory \"".$lmlDaInstallDirectory."\" does not exist, please create it. Installation aborted.\n";
	exit 1;
}
if(defined($options{installdir}) && $options{installdir} ne ""){
	#Copy all the files to the target directory, adapt the config installdir, run the installation script there
	
	print "Copying LML_DA files to the install directory \"".$lmlDaInstallDirectory."\"\n";
	my $copyResult = system("cp -r ../../ $lmlDaInstallDirectory");
	if($copyResult != 0){
		print STDERR "Installation aborted\n";
		exit 1;
	}
	
	$options{installdir} = "";
	print "Adapting config file for the new installation path in \"$lmlDaInstallDirectory/util/install/config\"\n";
	writeOptionsToConfig("./config", "$lmlDaInstallDirectory/util/install/config", \%options);
	
	print "Start installation in \"$lmlDaInstallDirectory\"\n";
	print "***********************************************************\n";
	system("cd $lmlDaInstallDirectory/util/install/; perl installCrontab.pl");
	print "***********************************************************\n";
	print "Proceed with your installation steps from the directory \"$lmlDaInstallDirectory/util/install/\"\n";
	
	exit 0;
}

#Detect the RMS if necessary
if($options{rms} eq "undef"){
	$options{rms} = $rms_check->detectRMS($rms_directory);
	print "Detected RMS \"".$options{rms}."\"\n";
}
else{#Validate the RMS hint or make suggestion on a valid one
	if(! $rms_check->isRMSValid($rms_directory, $options{rms} ) ){
		print STDERR "The RMS hint \"$options{rms}\" is invalid.\n";
		my $suggestion = $rms_check->detectRMS($rms_directory);
		if($suggestion){
			print STDERR "Try to use $suggestion as RMS hint, set rms = \"$suggestion\" in the config file. Installation aborted.\n";
		}
		else{
			print STDERR "Could not detect any available RMS. Installation aborted.\n";
		}
		
		exit 1;
	}
	else{
		print "RMS \"$options{rms}\" appears to be valid.\n";
	}
}

#Check, if cache directory can be used
my $cache = Data_cache->new( $options{cachedir}, "0" );
if( ! $cache->isCacheUsable() ){
	print STDERR "Cannot make use of the defined cache directory \"$options{cachedir}\". Please make sure, that the path is valid and a directory with permission mask 0755 can be created. Installation aborted.\n";
	exit 1;
}

print "\n";
print $step.") Generate the script called repeatedly, which generates the raw LML file\n";
$step++;

#Generate the script called by the crontab entry
print "LML_DA directory is \"$lmlDaInstallDirectory\" \n";
print "Generating script for crontab in the file \"".$FindBin::RealBin."/$crontabScriptName\"\n";

open( SCRIPT, "> $crontabScriptName" ) or die "Could not create the crontab triggered script $crontabScriptName\n";

print SCRIPT "#!/bin/sh\n\n";

if( -e $ENV{"HOME"}."/.bashrc" ){
	print SCRIPT ". ".$ENV{"HOME"}."/.bashrc"."\n";
}
elsif(-e $ENV{"HOME"}."/.profile"){
	print SCRIPT ". ".$ENV{"HOME"}."/.profile"."\n";
}

print SCRIPT "cd $lmlDaInstallDirectory\n";
print SCRIPT "perl LML_da_driver.pl -cache=1 -cacheinterval=0 -rms=".$options{rms}." -permdir=".$options{permdir}." -cachedir=".$options{cachedir}." samples/request_sample_empty.xml result.xml\n";

close SCRIPT;

print "Making the crontab script executable\n";
#Make the script executable
chmod 0700, $crontabScriptName;

print "\n";
print $step.") Create a file containing the cron job\n";
$step++;

# Create a file containing the cron job
print "Generating a file named \"$cronjobFile\" containing the cronjob, which has to be added to crontab.\n";
open( CRONJOB, "> $cronjobFile") or die "Could not open cronjob file $cronjobFile\n";
print CRONJOB $options{croninterval}." sh ".$FindBin::RealBin."/".$crontabScriptName."  > /dev/null 2> /dev/null \n";
close CRONJOB;

print "\n";
print $step.") Create a daemon script as alternative for crontab\n";
$step++;
# Create a daemon script as alternative for a crontab:
print "Generating daemon script as alternative for crontab in \"".$FindBin::RealBin."/$daemon\"\n";

if( open( DAEMON, "> $daemon" ) ){
	my $interval = 60; #The time to sleep in seconds after LML_da was executed the last time
	if($options{croninterval} =~ /^\*\/(\d+)/ ){
		$interval = $1*60;
	}
	
	print DAEMON "#!/bin/sh\n\n";
	print DAEMON "while [ 1 ]\n";
	print DAEMON "do\n";
	print DAEMON " sh ".$FindBin::RealBin."/".$crontabScriptName."\n";
	print DAEMON " sleep $interval\n";
	print DAEMON "done\n";
	close DAEMON;
	
	print "Making the daemon script executable.\n";
	chmod 0700, $daemon;
}

print "\n";
print $step.") Explain, how to add the cronjob and how to use the daemon script\n";
$step++;
#Append cronjob to the existing crontab configuration
print "Please append the content of \"".$FindBin::RealBin."/$cronjobFile\" to your crontab configuration\n";
print "Alternatively you can run the script \"".($FindBin::RealBin."/".$crontabScriptName)."\" repeatedly with any other mechanism.\n";
print "E.g. the script \"".$FindBin::RealBin."/$daemon\" can be used for that purpose.\n";
print "Make sure, that the status data in \"".$options{cachedir}."\" is continuously updated after you have finished the installation.\n";
print "Note, that a crontab entry would be preferable as it is more reliable than the daemon script.\n";


#*********************************************
# Reads the config file in the local working
# directory. Parses all configuration options.
# Evaluates the values of the options.
# Replaces possible occurences of $hostname.
#
# @return reference to hash of key-value pairs 
#			of found options
#
#*********************************************
sub readOptionsFromConfig{
	
	my %options;
	
	if(! open( CONFIG, "< config" ) ){
		return \%options;
	}
	
	my $line;
	
	while($line = <CONFIG> ){
		#Ignore comments
		if($line =~ /^#/ ){
			next;
		}
		#Parse options
		if( $line =~ /\s*(\S+)\s* = \s*(".*")/ ){
			$options{$1} = eval($2);
		}
	}
	
	return \%options;
}

#***************************************************************
# Read the config file and replace parameter values in the
# output file. The config file is parsed. It is searched for
# lines defining the passed option parameters. If the key
# matches with one of the options the new value is placed
# into the config file. This is helpful for installing LML_DA
# in another directory. Then the installdir parameter is 
# replaced and the installation procedure is run in the new
# directory.
#
# @param $_[0] path to the input configuration file
#
# @param $_[1] path to the output configuration file
#
# @param $_[2] reference to the options hash, which should be 
#				replaced
#
#***************************************************************
sub writeOptionsToConfig{

	my $inputConfig = shift;
	my $outputConfig = shift;

	my $optionsRef = shift;	
	my %options = %{$optionsRef};
	
	if(! open(INPUT, "< $inputConfig")){
		return;
	}
	if(! open(OUTPUT, "> $outputConfig")){
		return;
	}
	
	while(my $line = <INPUT> ){
		#Parse options
		if($line !~ /^#/){
			if( $line =~ /\s*(\S+)\s* = \s*(".*")/ ){
				my $key = $1;
				my $value = $2;
				
				if(exists($options{$key})){
					$line = $key." = \"".$options{$key}."\"\n";
				}
			}
		}
		
		print OUTPUT $line;
	}
	
	close INPUT;
	close OUTPUT;
}
