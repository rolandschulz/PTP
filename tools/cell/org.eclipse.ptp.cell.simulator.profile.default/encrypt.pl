#!/usr/bin/perl 

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


# This script generates encrypted passwords.
#
# After booting Linux inside the Cell Simulator, the initialization script
# allows to execute a customized configuration script that may change the Linux
# environment.
#
# Typically, this customized script will add new users for loggin in via SSH.
# For example, to add a user called joe, whose password is 'secretword', one 
# would add following line to the customized script:
#
# useradd -m -p jkzsZb65Ksty2 joe
#
# This adds a user joe, create a homedir for him (-m option) and defines the 
# encrypted password jkzsZb65Ksty2 for his account.
# 
# Enter a password to encrypt:secretword
# Enter salt (two random alphanumerics):jk
# The encrypted password is: jkzsZb65Ksty2
#

print "Enter a password to encrypt: "; 
chomp(my $string = <STDIN>);

print "Enter salt (two random alphanumerics): "; 
chomp(my $salt = <STDIN>); 
 
my $encrypted_string = crypt($string, $salt);

print "The encrypted password is: $encrypted_string";
print "\n";
