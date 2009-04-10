#*******************************************************************************
# Copyright (c) 2008 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     IBM Corporation - initial API and implementation
#*******************************************************************************

# export display for running the tests
export DISPLAY=:1

# set up to use the Java 5 JRE
export PATH=/opt/public/common/ibm-java2-ppc-50/bin:$PATH

# make sure we're in the releng project dir 
cd `dirname $0`

umask 0022

# Checkout basebuilder to run the build
mkdir -p tools
cd tools
cvs -d:pserver:anonymous@dev.eclipse.org:/cvsroot/eclipse \
	checkout -r R35_M6 org.eclipse.releng.basebuilder
cd ..

# Let's go!
java -jar tools/org.eclipse.releng.basebuilder/plugins/org.eclipse.equinox.launcher.jar \
	-ws gtk -arch ppc -os linux -application org.eclipse.ant.core.antRunner $*

exit 0
