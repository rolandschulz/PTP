#!/bin/bash

###############################################################################
# Copyright (c) 2006 IBM Corporation.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
# 
# Contributors:
#     IBM Corporation - Initial Implementation
#
###############################################################################

# Usage:
# cd working_directory
# /path/to/plugin/launch_standalone.tcl [ processor ]
#
# Where:
# processor is a plugin named org.eclipse.ptp.cell.simulator.architecture.processor
# and that contains a file name processor.tcl
#

# Required environment variable(s). Change value(s) according to the SDK 3.0 installation.
export SYSTEMSIM_TOP=/opt/ibm/systemsim-cell

# Required environment variable(s). Values must not be changed.
export CELL_NET_INIT=true
export CELL_SSH_INIT=true

# Forbidden environment variable(s). Their values must not be set.
#export CELL_CONSOLE_ECHO="true"
#export CELL_CONSOLE_PORT=4000
#export CELL_API_PORT=8800

# Optional environment variable(s). Change to customize the simulated machine.
# When not set, then default values will be used.
#export CELL_CPU_CONFIG="tcl script"
#export CELL_MEMORY_SIZE=256
#export CELL_KERNEL_IMAGE="path"
#export CELL_ROOT_IMAGE="path"
#export CELL_ROOT_PERSISTENCE="discard/write/journal"
#export CELL_ROOT_JOURNAL="path"
#export CELL_EXTRA_IMAGE="path"
#export CELL_EXTRA_PERSISTENCE="discard/write/journal"
#export CELL_EXTRA_JOURNAL="path"

# Optional environment variable(s). Change to allow multiple simulators
# running on the same machine. For each simulator, it is required to
# create a copy of this script with proper values for the following 
# environment variables. The MAC address should always look 
# like 02:00:00:*:*:*
#export CELL_NET_MASK="255.255.0.0"
#export CELL_NET_IP_HOST="172.20.0.1"
#export CELL_NET_IP_SIMULATOR="172.20.0.2"
#export CELL_NET_MAC_SIMULATOR="02:00:00:00:00:00"

# The launch script itself. Do not change code below.
pluginPath=$(
tclsh <<EOF
	set scriptPath [ file normalize $0 ]
	set pluginPath [ file dirname \$scriptPath ]
	puts \$pluginPath
EOF
)

curretPath=$(
tclsh <<EOF
	set curretPath [ file normalize . ]
	puts \$curretPath
EOF
)

runinfoPath=$curretPath/runinfo

tclsh <<EOF
	# Check if there is not running instance of the simulator.
	# The runinfo directory serves as a lock.
	if { [ file exist $runinfoPath] } {
		puts "There is already a simulator running in the working directory ($curretPath). Otherwise, remove the directory $runinfoPath."
		exit 1
	}

	# Test if deploy of files is required.
	# It is necessary to deploy files to the working directory if it is not the same as the plug-in directory.
	set deployRequired [ string compare $curretPath $pluginPath ]
	if { \$deployRequired } {
		puts "Deploy the files"
		foreach filename {
			sshd_config
			ssh_host_dsa_key
			ssh_host_dsa_key.pub
			ssh_host_rsa_key
			ssh_host_rsa_key.pub
			configure.sh
			simulator_init.tcl
		} {
			set hasError [
				catch {
					file copy -force $pluginPath/\$filename $curretPath
				} errorMessage
			]
			if { \$hasError} {
				puts "Failed to copy \$filename (\$errorMessage)"
				exit 1
			}
		}
	}
EOF

if [[ $? != 0 ]] ; 
then
	exit 1
fi

# Get information about the processor
# This information is expected to be in a file of another plug-in named
# org.eclipse.ptp.cell.simulator.arch.*/cpuname
# Where cpuname is the name passed as argument.

filename=$(
tclsh <<EOFB
	if { $# > 0 } {
		set cpuname $1
		#puts stderr "CPU: \$cpuname"
		
		set hasError [
			catch {
				set candidates [ glob "$pluginPath/../org.eclipse.ptp.cell.simulator.architecture.\$cpuname/\$cpuname.tcl" ]
			} errorMessage
		]
		if { \$hasError } {
			puts stderr "Could not find a plugin that provides information for '\$cpuname'."
			puts stderr "Avaiable cpu names are:"
			set hasError [
				catch {
					set candidates [ glob "$pluginPath/../org.eclipse.ptp.cell.simulator.architecture.*/*.tcl" ]
				} errorMessage
			]
			if { \$hasError } {
				puts stderr "  no candidates found"
				exit 1
			}
			foreach filename \$candidates {
				set filename [ file rootname \$filename ] 
				set filename [ file tail \$filename ]
				puts stderr "   \$filename"			
			}
			exit 1
		}
		set filename [ lindex \$candidates 0 ]
		set filename [ file normalize \$filename ]
		#puts stderr "Found CPU definition in \$filename"
		puts \$filename
	} {
		puts stderr "CPU: default"
	} 
EOFB
)

if [[ $? != 0 ]] ; 
then
	exit 1
fi

if [ $filename ]
then
	export CELL_CPU_CONFIG=$(cat $filename)
	#echo "TCL commands to configure CPU in $filename:"
	#echo $CELL_CPU_CONFIG
fi

$SYSTEMSIM_TOP/bin/systemsim -g -i -t -f $pluginPath/simulator_init.tcl
if [[ $? != 0 ]] ; 
then
	echo "Could not launch simulator"
fi

tclsh <<EOF
	# Clean up allocated ressources.
	if { [ file exist $runinfoPath ] } {
		# Remove the tun/tap device if it was created
		if { [ file exist $runinfoPath/TAP_DEVICE ] } {
			set hasError [
				catch {
					set tapDevice [ exec cat $runinfoPath/TAP_DEVICE ]
					exec $SYSTEMSIM_TOP/bin/snif -d \$tapDevice
				} errorMessage
			]
			if { \$hasError } {
				puts "Could not remove tun tap device."
				puts "Please remove manually"
				puts \$errorMessage
			}
		}
		
		# Remove runtime information.
		file delete -force $runinfoPath
	}
EOF
