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

#
# This file is executed within systemsim-cell. Do not execute this file directly from shell.
#

##
# Prints a token and a message that is recognized by Eclipse Cell IDE simulator plugin.
# @param tagname the token that identifies the message
# @param messagetext the message itself
#
proc Cell_Status { tagname messagetext } {
	puts "||| $tagname: $messagetext |||"
}
Cell_Status "INIT" "Parse"


##
# Prints an error message that is recognized by Eclipse Cell IDE simulator plugin.
# @param genericmessage the error message
# @param errormessage a message reported by TCL ou a called script
#
proc Cell_Error { genericmessage {errormessage "" } } {
	if { [ string length $errormessage ] == 0 } {
		Cell_Status "ERROR"  "$genericmessage"
		exit 1
	} else {
		Cell_Status "ERROR" "$genericmessage Reported error: $errormessage"
		exit 1
	}	
}

if { ! [ info exists env(SYSTEMSIM_TOP) ] } {
	Cell_Error "Environment variable is missing: SYSTEMSIM_TOP. This TCL file should be interpreted by systemsim."
}

if { ! [ info exists env(IMAGES_DIR) ] } {
	Cell_Error "Environment variable is missing: IMAGES_DIR. This TCL file should be interpreted by systemsim."
}

if { ! [ info exists env(LIB_DIR) ] } {
	Cell_Error "Environment variable is missing: LIB_DIR. This TCL file should be interpreted by systemsim."
}

##
# Ensures that a required file exists and is readable. If not, terminates simulator
# with error message that will be recognized by Eclipse Cell IDE simulator plugin.
# @param filename path to the file to be checked
# @param description describes the file in the error message.
#
proc Cell_CheckFile { filename {description "" } } {
	if { ! [ file exists $filename ] } {
		if { [ string length $description ] == 0 } {
			Cell_Error "Cannot find file $filename. Check if path is correct."
		} else {
			Cell_Error "Cannot find file for $description ($filename). Check if path is correct."
		}
	}
	if { ! [ file readable $filename ] } {
		if { [ string length $description ] == 0 } {
			Cell_Error "Cannot read file $filename. Check read permissions."
		} else {
			Cell_Error "Cannot read file for $description ($filename). Check read permissions."
		}
	}
}

proc Cell_RetrieveLaunchConfiguration { } {
	global env
	global SYSTEMSIM_TOP
	global CELL_VAR_DIR
	global CELL_WORKDIR
	global CELL_USERNAME
	global CELL_USERID
	
	puts "LAUNCH INFORMATION:"
	set CELL_WORKDIR [ file normalize . ]
	set CELL_VAR_DIR [ file normalize $CELL_WORKDIR/runinfo ]
	puts "   * systemsim base directory: $SYSTEMSIM_TOP"
	puts "   * Working directory: $CELL_WORKDIR"
	puts "   * Launch information directory: $CELL_VAR_DIR"
	if {![file writable $CELL_WORKDIR ]} {
		Cell_Error "Simulator working directory is not writeable ($CELL_WORKDIR). Check write permissions."
	}
	if {[file exists $CELL_VAR_DIR ]} {
		Cell_Error "There is already a simulator running in the working directory ($CELL_WORKDIR). Otherwise, remove the directory $CELL_VAR_DIR."
	}
	set result [ 
		catch {
			exec id -un
		} CELL_USERNAME
	]
	if { $result != 0 } {
		set errormessage $CELL_USERNAME
		Cell_Error "Could not retrieve user information." $errormessage
	}
	set result [ 
		catch {
			exec id -u
		} CELL_USERID
	]
	if { $result != 0 } {
		set errormessage $CELL_USERID
		Cell_Error "Could not retrieve user information." $errormessage
	}
	puts "   * User name: $CELL_USERNAME, $CELL_USERID"
	puts ""

}

proc Cell_RetrieveMachineConfiguration { } {
	global env
	global CELL_CPU_CONFIG
	global CELL_MEMORY_SIZE
	
	puts "MACHINE CONFIGURATION:"
	if { [ info exists env(CELL_CPU_CONFIG) ] } {
		puts "   * Customized CPU"
		set CELL_CPU_CONFIG $env(CELL_CPU_CONFIG)
	} else {
		puts "   * Default CPU (single processor Cell)"
		set CELL_CPU_CONFIG "myconf config cider be_mode 6; myconf config cider bridge_type 0"
	}
	if { [ info exists env(CELL_MEMORY_SIZE) ] } {
		set CELL_MEMORY_SIZE $env(CELL_MEMORY_SIZE)
	} else {
		set CELL_MEMORY_SIZE 256
	}
	puts "   * Memory: $CELL_MEMORY_SIZE MB"
	puts ""
}

proc Cell_RetrieveFileSystemConfiguration { } {
	global env
	global CELL_WORKDIR
	global CELL_KERNEL_IMAGE
	global CELL_ROOT_IMAGE
	global CELL_ROOT_PERSISTENCE
	global CELL_ROOT_JOURNAL
	global CELL_EXTRA_IMAGE
	global CELL_EXTRA_PERSISTENCE
	global CELL_EXTRA_JOURNAL
	global CELL_EXTRA_INIT
	global CELL_EXTRA_MOUNTPOINT
	global CELL_EXTRA_TYPE
	
	# Check for kernel image
	puts "FILE SYSTEM:"
	puts "   * Kernel:"
	if { [ info exists env(CELL_KERNEL_IMAGE) ] } {
		set CELL_KERNEL_IMAGE [ file normalize $env(CELL_KERNEL_IMAGE) ]
	} else {
		set filename $CELL_WORKDIR/vmlinux
		puts "     checking $filename..."
		
		if { [ file exists $filename ] } {
			set CELL_KERNEL_IMAGE $filename
		} else {
			set filename $env(IMAGES_DIR)/cell/vmlinux 
			puts "     checking $filename..."
			if { [ file exists $filename ] } {
				set CELL_KERNEL_IMAGE $filename
			} else {
				Cell_Error "No operating system kernel image file found."
			}
		}
	}
	puts "     using  $CELL_KERNEL_IMAGE"
	Cell_CheckFile $CELL_KERNEL_IMAGE "operating system kernel image"
	
	# Check for root file system
	puts "   * Root file system:"
	if { [ info exists env(CELL_ROOT_IMAGE) ] } {
		set CELL_ROOT_IMAGE [ file normalize $env(CELL_ROOT_IMAGE) ]
	} else {
		set filename $CELL_WORKDIR/sysroot_disk
		puts "     checking $filename..."
		
		if { [ file exists $filename ] } {
			set CELL_ROOT_IMAGE $filename
		} else {
			set filename $env(IMAGES_DIR)/cell/sysroot_disk 
			puts "     checking $filename..."
			if { [ file exists $filename ] } {
				set CELL_ROOT_IMAGE $filename
			} else {
				Cell_Error "No root file system image file found."
			}
		}
	}
	puts "     using $CELL_ROOT_IMAGE"
	Cell_CheckFile $CELL_ROOT_IMAGE "root file system image"
	if { [ info exists env(CELL_ROOT_PERSISTENCE) ] } {
		set CELL_ROOT_PERSISTENCE $env(CELL_ROOT_PERSISTENCE)
	} else {
		set CELL_ROOT_PERSISTENCE "discard"
	}
	switch $CELL_ROOT_PERSISTENCE {
		"discard" {
			puts "     (discard changes when simulator exists)"
		}
		"write" {
			puts "     (write changes directly on image file)"
			if {![file writable $CELL_ROOT_IMAGE ]} {
				Cell_Error "Root file system image is not writeable ($CELL_ROOT_IMAGE). Check write permissions."
			}
		}
		"journal" {
			if { [ info exists env(CELL_ROOT_JOURNAL) ] } {
				set CELL_ROOT_JOURNAL $env(CELL_ROOT_JOURNAL)
			} else {
				set CELL_ROOT_JOURNAL "$CELL_WORKDIR/[ file tail $CELL_ROOT_IMAGE ].changes"
			}
			puts "     (journal changes to $CELL_ROOT_JOURNAL)"
		}
		default {
			Cell_Error "Invalid value for CELL_ROOT_PERSISTENCE: $CELL_ROOT_PERSISTENCE."
		}
	}
	
	# Check for alternative file system
	if { [ info exists env(CELL_EXTRA_IMAGE) ] } {
		set CELL_EXTRA_IMAGE [ file normalize $env(CELL_EXTRA_IMAGE) ]
		set CELL_EXTRA_INIT true

		if { [ info exists env(CELL_EXTRA_TYPE) ] } {
			set CELL_EXTRA_TYPE $env(CELL_EXTRA_TYPE)
		} else {
			set CELL_EXTRA_TYPE "ext2"
		}
		switch $CELL_EXTRA_TYPE {
			"ext2" -
			"iso9660" {
				# Ok, accept.	
			}
			default {
				Cell_Error "Invalid value for CELL_EXTRA_TYPE: $CELL_EXTRA_TYPE."
			}
		}
		if { [ info exists env(CELL_EXTRA_MOUNTPOINT) ] } {
			set CELL_EXTRA_MOUNTPOINT $env(CELL_EXTRA_MOUNTPOINT)
		} else {
			set CELL_EXTRA_MOUNTPOINT "/mnt"
		}
		puts "   * Extra file system: $CELL_EXTRA_IMAGE ($CELL_EXTRA_TYPE) on $CELL_EXTRA_MOUNTPOINT"

		if { [ info exists env(CELL_EXTRA_PERSISTENCE) ] } {
			set CELL_EXTRA_PERSISTENCE $env(CELL_EXTRA_PERSISTENCE)
		} else {
			set CELL_EXTRA_PERSISTENCE "discard"
		}
		switch $CELL_EXTRA_PERSISTENCE {
			"readonly" {
				puts "     (readonly)"
			}
			"discard" {
				puts "     (discard changes when simulator exists)"
			}
			"write" {
				puts "     (write changes directly on image file)"
				if {![file writable $CELL_EXTRA_IMAGE ]} {
					Cell_Error "Extra file system image is not writeable ($CELL_EXTRA_IMAGE). Check write permissions."
				}
			}
			"journal" {
				if { [ info exists env(CELL_EXTRA_JOURNAL) ] } {
					set CELL_EXTRA_JOURNAL $env(CELL_EXTRA_JOURNAL)
				} else {
					set CELL_EXTRA_JOURNAL "$CELL_WORKDIR/[ file tail $CELL_EXTRA_IMAGE ].changes"
				}
				puts "     (journal changes to $CELL_EXTRA_JOURNAL)"
			}
			default {
				Cell_Error "Invalid value for CELL_EXTRA_PERSISTENCE: $CELL_EXTRA_PERSISTENCE."
			}
		}
	} else {
		set CELL_EXTRA_INIT false
	}
}

proc Cell_RetrieveSwitches { } {
	global env
	global CELL_CONSOLE_PORT
	global CELL_CONSOLE_INIT
	global CELL_CONSOLE_ECHO
	global CELL_CONSOLE_COMMANDS
	global CELL_NET_INIT

	if { [ info exists env(CELL_CONSOLE_PORT) ] } {
		set CELL_CONSOLE_PORT $env(CELL_CONSOLE_PORT) 
		set CELL_CONSOLE_INIT true
	} else { 
		set CELL_CONSOLE_INIT false
	}

	if { [ info exists env(CELL_CONSOLE_ECHO) ] } { 
		set CELL_CONSOLE_ECHO $env(CELL_CONSOLE_ECHO)
	} else { 
		set CELL_CONSOLE_ECHO true 
	}

	if { [ info exists env(CELL_NET_INIT) ] } { 
		set CELL_NET_INIT $env(CELL_NET_INIT)
	} else { 
		set CELL_NET_INIT false 
	}
	
	if { [ info exists env(CELL_CONSOLE_COMMANDS) ] } { 
		set CELL_CONSOLE_COMMANDS $env(CELL_CONSOLE_COMMANDS)
	} else { 
		set CELL_CONSOLE_COMMANDS "# No customization" 
	}

	puts "SWITCHES:"
	if { $CELL_CONSOLE_INIT } {
		puts "   * Wait for console on port: $CELL_CONSOLE_PORT"
	} else {
		puts "   * Don't redirect console to socket."
	}
	if { $CELL_CONSOLE_ECHO } {
		puts "   * Echo console output."
	} else {
		puts "   * Don't echo console output."
	} 
	if { $CELL_NET_INIT } {
		puts "   * Use bogusnet."
	} else {
		puts "   * Don't use bogusnet."
	}
	puts ""
}

	
proc Cell_RetrieveBogusnet { } {
	global env
	global CELL_NET_INIT
	global CELL_NET_IP_HOST
	global CELL_NET_IP_SIMULATOR
	global CELL_NET_MAC_SIMULATOR
	global CELL_NET_MASK

	puts "BOGUS NET:"
	if { $CELL_NET_INIT } {
		if { [ info exists env(CELL_NET_IP_HOST) ] } {
			set CELL_NET_IP_HOST $env(CELL_NET_IP_HOST) 
		} else { 
			set CELL_NET_IP_HOST "172.20.0.1"
		}
		if { [ info exists env(CELL_NET_IP_SIMULATOR) ] } {
			set CELL_NET_IP_SIMULATOR $env(CELL_NET_IP_SIMULATOR) 
		} else { 
			set CELL_NET_IP_SIMULATOR "172.20.0.2"
		}
		if { [ info exists env(CELL_NET_MASK) ] } {
			set CELL_NET_MASK $env(CELL_NET_MASK) 
		} else { 
			set CELL_NET_MASK "255.255.0.0"
		}
		if { [ info exists env(CELL_NET_MAC_SIMULATOR) ] } { 
			set CELL_NET_MAC_SIMULATOR $env(CELL_NET_MAC_SIMULATOR) 
		} else {
			# TODO: generate automatically
			set CELL_NET_MAC_SIMULATOR "00:01:6C:EA:A0:23" 
		}
		puts "   * Host IP address: $CELL_NET_IP_HOST"
		puts "   * Simulator IP address: $CELL_NET_IP_SIMULATOR"
		puts "   * Simulator MAC address: $CELL_NET_MAC_SIMULATOR"
		
		# Check if there is already on interface using the ip address.
		# Grep returns 0 for success (found matching ip address) and 1 for failure
		set result [ 	
			catch {
				set has_ip [ exec /sbin/ifconfig | grep -c "addr:$CELL_NET_IP_HOST" ]
			} addresscount
		]
		if { $result == 0 } {
			Cell_Error "There is already an interface using address range $CELL_NET_IP_HOST."
		}
		set result [ 	
			catch {
				set has_ip [ exec /sbin/ifconfig | grep -c "addr:$CELL_NET_IP_SIMULATOR" ]
			} addresscount
		]
		if { $result == 0 } {
			Cell_Error "There is already an interface using address range $CELL_NET_IP_SIMULATOR."
		}
		if { ! [ file exists /dev/net/tun ] } {
			Cell_Error "The device /dev/net/tun does not exist."
		}
	} else {
		puts "   * Don't use bogusnet."
	}
	puts ""
}


proc Cell_RetrieveSSHLaunch { } {
	global env
	global CELL_WORKDIR
	global CELL_NET_INIT
	global CELL_SSH_INIT
	puts "SSH SERVER:"

	if { $CELL_NET_INIT } {
		if { [ info exists env(CELL_SSH_INIT) ] } {
			set CELL_SSH_INIT $env(CELL_SSH_INIT)
		} else {
			set CELL_SSH_INIT "false"
		}
		
		switch $CELL_SSH_INIT {
			"true" {
				puts "   * $CELL_WORKDIR/configure.sh"
				puts "   * $CELL_WORKDIR/ssh_host_dsa_key"
				puts "   * $CELL_WORKDIR/ssh_host_dsa_key.pub"
				puts "   * $CELL_WORKDIR/ssh_host_rsa_key"
				puts "   * $CELL_WORKDIR/ssh_host_rsa_key.pub"
				puts "   * $CELL_WORKDIR/sshd_config"
			
				Cell_CheckFile $CELL_WORKDIR/ssh_host_dsa_key
				Cell_CheckFile $CELL_WORKDIR/ssh_host_dsa_key.pub
				Cell_CheckFile $CELL_WORKDIR/ssh_host_rsa_key
				Cell_CheckFile $CELL_WORKDIR/ssh_host_rsa_key.pub
				Cell_CheckFile $CELL_WORKDIR/sshd_config
			}
			"false" {
				puts "   * Don't launch sshserver in systemsim."
			}
			default {
				Cell_Error "Invalid value for CELL_SSH_INIT: $CELL_SSH_INIT."
			}
		}

	} else {
		set CELL_LAUNCH_SSH false
		puts "   * Don't launch sshserver in systemsim (not possible without bogusnet)."
	}
}

# ---------------------------------------------------------------------------------------
# PARAMETER VALIDATION:
# Retrieve parameters given by the environment variables.
# Set default values for missing parameters that are optional.
# Validate values of parameters and verify the environment.
#
# Image files are first searched in the path provided in environment variables,
# then on the current directory, then on systemsim-cell default image directory.

Cell_Status "INIT" "Check"

set SYSTEMSIM_TOP $env(SYSTEMSIM_TOP)

puts "*******************************************************************************"
puts "* PARAMETER RETIVAL AND VALIDATION                                            *"
puts "* Following configuration will be used to launch the Cell Simulator           *"
puts "*******************************************************************************"

Cell_RetrieveLaunchConfiguration
Cell_RetrieveMachineConfiguration
Cell_RetrieveFileSystemConfiguration
Cell_RetrieveSwitches
Cell_RetrieveBogusnet
Cell_RetrieveSSHLaunch

puts "*******************************************************************************"

# ---------------------------------------------------------------------------------------
# CONFIG LOG:
# Save configuration log
set result [
	catch {
		exec mkdir -p $CELL_VAR_DIR
		exec echo $CELL_USERNAME > $CELL_VAR_DIR/USERNAME
		exec echo $CELL_USERID > $CELL_VAR_DIR/USERID
		exec echo [ pid ] > $CELL_VAR_DIR/PID
				
		if { $CELL_NET_INIT } {
			exec echo $CELL_NET_MAC_SIMULATOR > $CELL_VAR_DIR/NET_MAC_SIMULATOR
			exec echo $CELL_NET_IP_HOST > $CELL_VAR_DIR/NET_IP_HOST
			exec echo $CELL_NET_IP_SIMULATOR > $CELL_VAR_DIR/NET_IP_SIMULATOR
		}
		
		exec echo "cd $CELL_VAR_DIR\nkill -9 \$(cat PID)\nif \[ -f TAP_DEVICE \]\n   then $SYSTEMSIM_TOP/bin/snif -d \$(cat TAP_DEVICE)\nfi\nrm -rf ../runinfo\ncd .." > $CELL_VAR_DIR/cleanup.sh
		exec chmod a+x $CELL_VAR_DIR/cleanup.sh
		
	} error_message
]
if { $result != 0 } {
	Cell_Error "Could not save configuration log in $CELL_VAR_DIR." $error_message
}

# ---------------------------------------------------------------------------------------
# CONFIGURE THE MACHINE
# Following instructions are taken from .systemsim.tcl provided by the cell-sdk
Cell_Status "INIT" "Configure"
	
# Initialize the systemsim tcl environment
source $env(LIB_DIR)/cell/mambo_init.tcl
	
# Configure and create the simulated machine
define dup cell myconf
set result [ 
	catch {
		eval $CELL_CPU_CONFIG		
		myconf config memory_size "${CELL_MEMORY_SIZE}M" 
	} errormessage
]
if { $result != 0 } {
	Cell_Error "Could not create machine configuration." $errormessage
}
define machine myconf mysim
	
# Start the GUI if -g option was given
MamboInit::gui $env(LIB_DIR)/cell/gui/gui.tcl

# Construct the emulated device tree
build_firmware_tree

# Set boot parameters if desired
of::set_bootargs "lpj=8000000 console=hvc0 root=/dev/mambobd0 rw"

# Load the OS
set result [ catch { mysim mcm 0 load vmlinux $CELL_KERNEL_IMAGE 0x1000000  } errormessage ]
if { $result != 0 } {
	Cell_Error "Could not set up operating system image." $errormessage
}

# Setup the root file system
set result [ 
	catch { 
		switch $CELL_ROOT_PERSISTENCE {
			# There is no option to mount the root file system as readonly.
			# This would be meaningless.
			"discard" {
				puts "mysim bogus disk init 0 $CELL_ROOT_IMAGE newcow $CELL_VAR_DIR/root_ignored 1024"
				mysim bogus disk init 0 $CELL_ROOT_IMAGE newcow $CELL_VAR_DIR/root_ignored 1024
			}
			"write" {
				puts "mysim bogus disk init 0 $CELL_ROOT_IMAGE rw"
				mysim bogus disk init 0 $CELL_ROOT_IMAGE rw
			}
			"journal" {
				puts "mysim bogus disk init 0 $CELL_ROOT_IMAGE cow $CELL_ROOT_JOURNAL 1024"
				mysim bogus disk init 0 $CELL_ROOT_IMAGE cow $CELL_ROOT_JOURNAL 1024
			}
		}
	} errormessage 
]
if { $result != 0 } {
	# (does not work)
	Cell_Error "Could not set up root file system." $errormessage 
}

# Setup the alternative file system
if { $CELL_EXTRA_INIT } {
	set result [ 
		catch { 
			switch $CELL_EXTRA_PERSISTENCE {
				"readonly" {
					puts "mysim bogus disk init 1 $CELL_EXTRA_IMAGE r"
					mysim bogus disk init 1 $CELL_EXTRA_IMAGE r
				}
				"discard" {
					puts "mysim bogus disk init 1 $CELL_EXTRA_IMAGE newcow $CELL_VAR_DIR/extra_ignored 1024 "
					mysim bogus disk init 1 $CELL_EXTRA_IMAGE newcow $CELL_VAR_DIR/extra_ignored 1024 
				}
				"write" {
					puts "mysim bogus disk init 1 $CELL_EXTRA_IMAGE rw"
					mysim bogus disk init 1 $CELL_EXTRA_IMAGE rw
				}
				"journal" {
					puts "mysim bogus disk init 1 $CELL_EXTRA_IMAGE cow $CELL_EXTRA_JOURNAL 1024"
					mysim bogus disk init 1 $CELL_EXTRA_IMAGE cow $CELL_EXTRA_JOURNAL 1024
				}
			}
		} errormessage 
	]
	if { $result != 0 } {
		# (does not work)
		Cell_Error "Could not set up alternative file system." $errormessage 
	}
}

# ---------------------------------------------------------------------------------------
# BOGUSNET:
# A new tap device must be created. The name of the device must
# be saved in a temporary file, so that it can be read by the 
# cleanup script and deallocated after systemsim exits.

if { $CELL_NET_INIT } {
	Cell_Status "INIT" "Bogusnet"
		# Create the tun device.
	# snif will create the device and print its name to stdout.
	# The output is captured and saved into CELL_TAP_DEVICE.
	# If snif fails, CELL_TAP_DEVICE will contain the error message instead.
	set result [ 
		catch {
			puts "Exec: $SYSTEMSIM_TOP/bin/snif -c -u $CELL_USERID $CELL_NET_IP_HOST $CELL_NET_MASK"
			exec $SYSTEMSIM_TOP/bin/snif -c -u $CELL_USERID $CELL_NET_IP_HOST $CELL_NET_MASK
		} CELL_TAP_DEVICE
	]
	if { $result != 0 } {
		set error_message $CELL_TAP_DEVICE
		Cell_Error "Failed to create tun/tap device." $errormessage
	}
	
	# Save the device name to the launch log so that the clean up script can
	# remove the tun/tap device again.
	set result [ 
		catch {
			exec echo $CELL_TAP_DEVICE > $CELL_VAR_DIR/TAP_DEVICE
		} error_message
	]
	if { $result != 0 } {
		exec $SYSTEMSIM_TOP/bin/snif -d $CELL_TAP_DEVICE
		Cell_Error "Could not save configuration log in $CELL_VAR_DIR." $error_message
	}
	
	# Check if the device was created with proper permissions.
	puts "Allocated tun/tap = $CELL_TAP_DEVICE."
	if { ! [ file readable /dev/net/tun ] } {
		exec $SYSTEMSIM_TOP/bin/snif -d $CELL_TAP_DEVICE
		Cell_Error "The device /dev/net/tun is not readable. Check udev rules."
	}
	if { ! [ file writable /dev/net/tun ] } {
		exec $SYSTEMSIM_TOP/bin/snif -d $CELL_TAP_DEVICE
		Cell_Error "The device /dev/net/tun is not writable. Check udev rules."
	}

	# Finally, start bogusnet
	mysim bogus net init 0 $CELL_NET_MAC_SIMULATOR $CELL_TAP_DEVICE 0 0
}

# ---------------------------------------------------------------------------------------
# LINUX CONSOLE:
# Create a console redirected to a socket connected by Cell IDE.
	
if { $CELL_CONSOLE_INIT } {
	Cell_Status "INIT" "Console"

	# Last parameter with value 0 means try socket connection, without being 
	# interfered by GUI that is launching on the same time.
	# Without this parameter, GUI will cancel the socket.	
	set result [ catch { mysim console create eclipse inout listen $CELL_CONSOLE_PORT 100 20  } errormessage ]

	if { $result != 0 } {
		Cell_Error "Could not create console." $errormessage
		exit 1
	}
}
Cell_Status "INIT" "Configured"

# ---------------------------------------------------------------------------------------
# PAUSE/RESUME TRIGGERS:
# Notify Cell IDE when simulator is started or stopped.

proc Cell_Start { args } {
	Cell_Status "SIMULATOR" "Start"
}

proc Cell_Stop { args } {
	Cell_Status "SIMULATOR" "Stop"
}

mysim trigger set assoc "SIM_START" Cell_Start
mysim trigger set assoc "SIM_STOP" Cell_Stop

# ---------------------------------------------------------------------------------------
# SHUT DOWN:
# Notify Cell IDE when simulator is being shutdown.
# This triggers are unset once activated

proc Cell_ShutdownNotified { args } {
	array set triginfo $args
	mysim trigger clear console $triginfo(match)
	Cell_Status "SHUTDOWN" "Prepared"
}

proc Cell_ShutdownStarted { args } {
	array set triginfo $args
	mysim trigger clear console $triginfo(match)
	Cell_Status "SHUTDOWN" "Started"
}

proc Cell_ShutdownComplete { args } {
	array set triginfo $args
	mysim trigger clear console $triginfo(match)
	simstop
	Cell_Status "SHUTDOWN" "Complete"
	quit
}

mysim trigger set console "The system is going down for system halt NOW!" Cell_ShutdownNotified
mysim trigger set console "INIT: Switching to runlevel: 0" Cell_ShutdownStarted
mysim trigger set console "INIT: no more processes left in this runlevel" Cell_ShutdownComplete

proc writeConsole { t } {
	mysim console create console_id in string $t
}

# ---------------------------------------------------------------------------------------
# BOOT:
# Notify several steps during boot.
# After boot, pass run the configuration script, set configuration triggers.
# This triggers are unset once activated

proc Cell_BootedBios { args } {
	array set triginfo $args
	mysim trigger clear console $triginfo(match)
	Cell_Status "BOOT" "Linux"
}

proc Cell_BootedLinux { args } {
	array set triginfo $args
	mysim trigger clear console $triginfo(match)
	Cell_Status "BOOT" "System"
}

proc Cell_BootReady { args } {
	array set triginfo $args
	mysim trigger clear console $triginfo(match)
	Cell_DoCellConfiguration
}

proc Cell_BootNearlyReady { args } {
	array set triginfo $args
	mysim trigger clear console $triginfo(match)
	mysim trigger set console "#" Cell_BootReady
}

mysim trigger set console "Starting Linux" Cell_BootedBios
mysim trigger set console "Welcome to Fedora Core" Cell_BootedLinux
mysim trigger set console "INIT: Entering runlevel: 2" Cell_BootNearlyReady

# ---------------------------------------------------------------------------------------
# CONFIGURATION:
# Do modifications on the default running Linux environment to:
# - Create an user to launch applications
# - Launch ssh server for remote connections
# This triggers are unset once activated

proc writeConsole { t } {
	mysim console create console_id in string "$t\n"
}

proc Cell_NearlyConfigured { args } {
	array set triginfo $args
	mysim trigger clear console $triginfo(match)
	mysim trigger set console "#" Cell_Configured
}
	
proc Cell_Configured { args } {
	array set triginfo $args
	mysim trigger clear console $triginfo(match)
	Cell_Status "BOOT" "Complete"
	# Don't pause anymore, since simulator will receive SSH connections.
	# simstop
}

proc Cell_DoCellConfiguration { args } {
	Cell_Status "BOOT" "Configure"
	global CELL_SSH_INIT
	global CELL_HAS_USER_CONFIG
	global CELL_NET_IP_HOST
	global CELL_NET_IP_SIMULATOR
	global CELL_NET_MASK
	global CELL_NET_INIT
	global CELL_CONSOLE_ECHO
	global CELL_WORKDIR
	global CELL_EXTRA_INIT
	global CELL_EXTRA_MOUNTPOINT
	global CELL_EXTRA_TYPE
	global CELL_EXTRA_PERSISTENCE
	global CELL_CONSOLE_COMMANDS

	mysim trigger set console "Configuration complete" Cell_NearlyConfigured
	writeConsole "# Starting configuration"
	writeConsole "export CELL_WORKDIR=$CELL_WORKDIR"

	# Sets the simulator IP address and set environment variables with network configuration..
	if { $CELL_NET_INIT } {
		writeConsole "modprobe systemsim_net"
		writeConsole "ifconfig eth0 $CELL_NET_IP_SIMULATOR netmask $CELL_NET_MASK up"
		writeConsole "export CELL_NET_IP_SIMULATOR=$CELL_NET_IP_SIMULATOR"
		writeConsole "export CELL_NET_IP_HOST=$CELL_NET_IP_HOST"
		writeConsole "export CELL_NET_MASK=$CELL_NET_MASK"
	}

	# Upload required files. Actually, for performance reason, since writing
	# commands to the console is very slow, the script is called through
	# and the executed.
	if { $CELL_SSH_INIT } {
		writeConsole "cd /tmp/"
		writeConsole "callthru source \$CELL_WORKDIR/configure.sh > configure.sh"
		writeConsole "chmod u+x configure.sh"
		writeConsole "./configure.sh"
	}
	
	if { $CELL_EXTRA_INIT } {
		set mountswitch ""
		switch $CELL_EXTRA_PERSISTENCE {
			"discard" {
				set mountswitch "-w"
			}
			"write" {
				set mountswitch "-w"
			}
			"journal" {
				set mountswitch "-w"
			}
			"readonly" {
				set mountswitch "-r"
			}
		}
		writeConsole "mkdir -p $CELL_EXTRA_MOUNTPOINT"
		writeConsole "chmod a+wrx $CELL_EXTRA_MOUNTPOINT"
		switch $CELL_EXTRA_TYPE {
			"ext2" {
				puts "mount -t ext2 $mountswitch -o exec,suid /dev/mambobd1 $CELL_EXTRA_MOUNTPOINT"
				writeConsole "mount -t ext2 $mountswitch -o exec,suid /dev/mambobd1 $CELL_EXTRA_MOUNTPOINT"
			}
			"iso9660" {
				puts "mount -t iso9660 $mountswitch -o exec,uid=$(id -u user),gid=$(id -g user) /dev/mambobd1 $CELL_EXTRA_MOUNTPOINT"
				writeConsole "mount -t iso9660 $mountswitch -o exec,uid=$(id -u user),gid=$(id -g user) /dev/mambobd1 $CELL_EXTRA_MOUNTPOINT"
			}
		}
	}
	
	writeConsole $CELL_CONSOLE_COMMANDS

	if { ! $CELL_CONSOLE_ECHO } {
		writeConsole "stty -echo"
		writeConsole "echo 'Configuration complete'"
	} else {
		writeConsole "# Configuration complete"
	}
}

# ---------------------------------------------------------------------------------------
# RUN SIMULATOR
#for {set i 0} {$i < 8} {incr i} {
#	mysim spu $i set model fast
#}
mysim modify fast on
Cell_Status "BOOT" "Bios"
mysim go

