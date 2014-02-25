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

#**********************************
# This package allows to create 
# a cache directory. It provides
# mutexes for synchronization of
# multiple processes and refreshes
# cached raw data.
#
#**********************************
package Data_cache;
use strict;
use Fcntl;

#************************************************
# Creates a new cache handler. The cache directory
# and all needed paths are set. The optional 
# parameter allows to set the cache's directory.
#
# $_[0] cacheDir the path to the used cache directory
#		can be omitted, than a default directory is used
#
# $_[1] updateInterval the update interval in seconds, 
#			negative value to make this cache a read-only cache,
#			can be undefined to let the cache use its default value
#
# @return instance of this class
#
#************************************************
sub new {
    my $self  = {};
    my $proto = shift;
    my $class = ref($proto) || $proto;

	$self->{HOSTNAME} = `hostname`;  
	chomp($self->{HOSTNAME});  
    my $cacheDir = shift;
    if(!defined($cacheDir)){
    	$cacheDir = "/tmp/LMLCache_".$self->{HOSTNAME}."/";#The directory were to store cache files;
    }
	
	$self->{UPDATEINTERVAL} = shift;
	if(! defined($self->{UPDATEINTERVAL}) ){
		$self->{UPDATEINTERVAL} = 60; # Time in seconds until a new raw file has to be generated
	}
	
	$self->{MAXMUTEXAGE} = 180; #Maximum time in seconds till a mutex file is allowed to be deleted. In that case it is expected that the corresponding lml_da instance is hanging or failed unexpectedly
	
	$self->{MUTEXCHECKSTRING} = "$$".$self->{HOSTNAME};#This string is printed into the mutex file and checked again afterwards for dirty updates
	
    bless $self, $class;
    
    $self->setCacheDirectory($cacheDir);
    
    return $self;
}

#**************************************
#
# @return path to the LML raw file cached with this object
#
#**************************************
sub getRawFilePath{
	my $self = shift;
	return $self->{RAWFILE};
}

#**************************************
#
# @return directory path used for all data caching
#
#**************************************
sub getCacheDirectory{
	my $self = shift;
	return $self->{CACHEDIR};
}

#***************************************************
# Change the directory, where to store cache files.
# Raw file and mutex file locations are adjusted.
# Creates the directory if necessarry.
#
# @param $_[1] path to the cache directory.
#
#
#***************************************************
sub setCacheDirectory{
	my $self = shift;
	my $newDir = shift;
	
	if($newDir !~ /.*\/$/  ){
		$newDir = $newDir."/";
	}
	
	$self->{CACHEDIR} = $newDir;
	$self->{RAWFILE} = $self->{CACHEDIR}."LML_raw.xml";#the raw LML file containing global data of the monitored system
	$self->{MUTEXFILE} = $self->{CACHEDIR}."MUTEX";#Filename of the file used for multiple lml_da instance synchronization
	
	$self->{CACHECREATED} = $self->createCacheDir();
}

#*********************************************************
#
# Set the update interval in seconds. The default value is
# 60 seconds. Here you can configure a custom update interval,
# at which an LML raw file update is triggered.
# A negative value makes this cache to a read-only cache.
#
# @param $_[1] the new update interval in seconds
#
#*********************************************************
sub setUpdateInterval{
	my $self = shift;
	
	$self->{UPDATEINTERVAL} = shift;
	
	if(!defined($self->{UPDATEINTERVAL}) ){
		$self->{UPDATEINTERVAL} = 60;
	}
}

#***************************************************************************
# Check, if the cache directory exists. If not, create the 
# directory. The directory is not created with a negative updateInterval.
#
# @return 1, if cache directory was created successfully, false otherwise
#***************************************************************************
sub createCacheDir{
	my $self = shift;
	
	if(! -d $self->{CACHEDIR}){
		if($self->{UPDATEINTERVAL} >= 0){
			return mkdir($self->{CACHEDIR},0755);
		}
		else{
			return 0;
		}
	}
	
	return 1;
}

#***********************************************************
#
# @return 1, if cache can be used, 0 if anything failed or permissions are missing
#
#************************************************************
sub isCacheUsable{
	my $self = shift;
	
	if($self->{UPDATEINTERVAL} >= 0){
		return $self->{CACHECREATED} && -w $self->{CACHEDIR} && -r $self->{CACHEDIR};
	}
	else{#Only read rights necessary
		my $cacheDirOk = $self->{CACHECREATED} && -r $self->{CACHEDIR};
		my $rawFileOk = -r $self->{RAWFILE};
		#Make sure, that the cache is actively updated
		my $rawFileUptodate = $self->getFileAge($self->{RAWFILE}) <= $self->{MAXMUTEXAGE};
		return $cacheDirOk && $rawFileOk && $rawFileUptodate;
	}
}

#************************************************************
# Retrieve age of a file in seconds.
#
# @param $_[1] name of the file, whose age is requested
#
# @return the age of the file in seconds or -1 if something went wrong
#
#************************************************************
sub getFileAge{
	my $self = shift;
	my $filename = shift;
	
	if(-e $filename){
		$^T = time(); # Compare with the real current time, not the script starting time
		my $dayAge = -M $filename;
		if(! defined($dayAge)){
			return -1;
		} 
		my $fileAgeInSeconds = 86400 * $dayAge;
		
		return $fileAgeInSeconds;
	}
	else{
		return -1;
	}	
}

#************************************************************************
# Check, if the raw LML file in the cache directory needs to be updated.
# This function returns true, if the file does not exist or if the raw 
# file is older than the configured update interval.
# For a negative update interval, this function always returns false. 
#
#
# @return 1, if the raw LML file needs to be updated, 0 otherwise
#
#************************************************************************
sub isUpdateRequired{
	
	my $self = shift;
	my $lmlAge = $self->getFileAge( $self->{RAWFILE} );
	
	if($self->{UPDATEINTERVAL} < 0){#Read only cache never updates
		return 0;
	}
	
	if($lmlAge == -1 || $lmlAge > $self->{UPDATEINTERVAL} ){
		return 1;
	}
	else{
		return 0;
	}
	
}

#**********************************
# Use shell's which to detect, if
# a shell command exists.
#
# @param $_[1] the command, which is requested
#
# @return 1, if the command exists, 0 otherwise
#**********************************
sub which{
	
	my $self = shift;
	my $cmd = shift;
	
	system("which $cmd > /dev/null 2> /dev/null");
	my $rc = $?;
	
	if($rc == 0){
		return 1;
	}
	else{
		return 0;
	}
	
}

#*************************************************************************
# Lock a mutex in order to synchronize multiple lml_da instances.
# The mutex can be used during cache file generation.
# This function blocks the calling process untill the mutex is unlocked.
#
#*************************************************************************
sub startMutex{
	my $self = shift;
	
	if($self->{UPDATEINTERVAL} < 0){#Not mutex start possible for a read-only cache
		return;
	}
	
	#Security check, mutex should not stop every instance from updating for infinity
	#Ignore the mutex, if it is too old
	if( $self->getFileAge($self->{MUTEXFILE}) > $self->{MAXMUTEXAGE} ){
		$self->stopMutex(1);#By passing 1 the function forces to delete the file
	}
	
	my $fileAccess = O_CREAT | O_RDWR;#Access mode used in case, that command lockfile is available, because file will already exist
	
	if( $self->which("lockfile") ){
		#This is blocking until the mutex is available, -1 defines a sleeptime of 1 second
		#-l $self->{MAXMUTEXAGE} tells lockfile to remove an existing lock, if it is older than the given limit in seconds
		system("lockfile -1 -l ".$self->{MAXMUTEXAGE}." -s 5 $self->{MUTEXFILE} >/dev/null 2>/dev/null");
		
		if($? != 0){
			#Unable to use lockfile
			return;
		}
		chmod 0644, $self->{MUTEXFILE};
	}
	else{
		#Mutex implementation with a non-synchronized file
		while(-e $self->{MUTEXFILE}){
			if( $self->getFileAge($self->{MUTEXFILE}) > $self->{MAXMUTEXAGE} ){
				$self->stopMutex(1);
			}
			
			sleep(1);
		}
		$fileAccess = O_CREAT | O_EXCL | O_RDWR;
	}
	
	if( ! sysopen(MUTEX, $self->{MUTEXFILE}, $fileAccess, 0444)){
		$self->startMutex();
		return;
	}
	
	chmod 0444, $self->{MUTEXFILE};
	print MUTEX $self->{MUTEXCHECKSTRING};
	
	close(MUTEX);
	#Check if mutex file is really only accessed by this process
	if( ! open MUTEXIN, "<$self->{MUTEXFILE}" ){
		#Cannot read mutex file anymore, something went wrong, try again
		$self->startMutex();
		return;
	}
	my $pidCheck = "";
	#Read entire file into pidCheck
	while(my $line=<MUTEXIN>){
		chomp($line);
		$pidCheck = $pidCheck.$line;
	}
	if($pidCheck ne $self->{MUTEXCHECKSTRING} ){#Try again, because another process interrupted
		$self->startMutex();
	}
}

#*************************************************************************
# Unlock the mutex. This function must be called as soon as the 
# synchronized work is completed.
# It should be called only, if before the startMutex command was called.
#
# @param $_[1] force, if set to 1, the stopping of the mutex is forced and
#			executed without further checking, otherwise it is checked, if
#			the mutex was created by this process
#
#*************************************************************************
sub stopMutex{
	my $self = shift;
	my $force = shift;
	
	if($self->{UPDATEINTERVAL} < 0){#Not mutex stop possible for a read-only cache
		return;
	}
	
	if($force){
		unlink $self->{MUTEXFILE}; 
		return;
	}
	
	if(-e $self->{MUTEXFILE}){
		#Check, if this is really my own mutex file
		open MUTEXIN, "<$self->{MUTEXFILE}";
		my $pidline = <MUTEXIN>;
		chomp($pidline);
		close(MUTEXIN);
		
		if($pidline eq $self->{MUTEXCHECKSTRING}){
			
			unlink $self->{MUTEXFILE}; 
			
		}
	}
	
}

#***********************************************************
# Check, if the data cache is already being updated by 
# checking the existance of a mutex file. 
#
# @return 1, if mutex file exists, 0 otherwise
#***********************************************************
sub isAlreadyUpdating{
	my $self = shift;
	
	if(-e $self->{MUTEXFILE}){
		return 1;
	}
	else{
		return 0;
	}
}

#*****************************************
# Check, if another lml_da instance is
# updating the data cache. If that is the
# case, wait for this instance to finish.
#
#
#*****************************************
sub waitUntilMutexIsUnlocked{
	my $self = shift;
	
	if(! $self->isAlreadyUpdating() ){
		return;
	}
	
	if($self->{UPDATEINTERVAL} < 0){ #Simple wait till mutex file is released
		while(-e $self->{MUTEXFILE}){
			sleep(1);
		}
	}
	else{# More reliable version for caches allowed to write files
		$self->startMutex();
		$self->stopMutex;
	}
}


1;

