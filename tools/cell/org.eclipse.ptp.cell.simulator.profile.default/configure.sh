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

echo 'Configuring SSH server'

# Add user for running SSH server.
if grep -e sshd /etc/passwd >> /dev/null ;
then
	echo 'The "sshd" user was already configured'
else
	echo 'sshd:x:10000:10000:SSH Server:/var/run/sshd:/sbin/nologin' >> /etc/passwd
	echo 'Added the "sshd" user'
fi

# Add a user for connecting to the SSH server
#useradd -m -p er2R9MyNBuTkU -c 'default user for Cell IDE' user
useradd -m -c 'default user for Cell IDE' user
passwd -d user

# Upload pre-generated files with private/public key.
# This is not very secure since private keys are exposed, but since the simulated
# Linux will not be accessible by other hosts, nor contain sentive information,
# this approach will not be an issue.
callthru source $CELL_WORKDIR/ssh_host_dsa_key > /etc/ssh/ssh_host_dsa_key
callthru source $CELL_WORKDIR/ssh_host_dsa_key.pub > /etc/ssh/ssh_host_dsa_key.pub
callthru source $CELL_WORKDIR/ssh_host_rsa_key > /etc/ssh/ssh_host_rsa_key
callthru source $CELL_WORKDIR/ssh_host_rsa_key.pub > /etc/ssh/ssh_host_rsa_key.pub
callthru source $CELL_WORKDIR/sshd_config > /etc/ssh/sshd_config

chmod og-rwx /etc/ssh/ssh_host_dsa_key
chmod og-rwx /etc/ssh/ssh_host_dsa_key.pub
chmod og-rwx /etc/ssh/ssh_host_rsa_key
chmod og-rwx /etc/ssh/ssh_host_rsa_key.pub

if test -d /var/run/sshd ;
then
	false
else
	mkdir /var/run/sshd
	echo 'Created /var/run/sshd'
fi

# Finally, launch SSH server.
if ps -e | grep ssh ;
then
	echo 'The sshd server was already running'
else
	/usr/sbin/sshd
	echo 'Started sshd server'
fi
