#!/bin/sh

# Copyright (c) 2010 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
# 
# Contributors:
#   IBM - Initial API and implementation

#
# This build script is only used for development purposes. It can be used
# to generate a proxy server executable jar from a workspace.
#
# Usage: build.sh
#

JDT_UI=/path/to/eclipse/plugins/org.eclipse.jdt.ui_3.6.0.v20100513-0800.jar

rm -rf build_tmp
mkdir build_tmp
cd build_tmp
(cd ../bin; tar --exclude CVS -c -f - org) | tar xvf -
(cd ../../org.eclipse.ptp.rm.proxy.core/bin; tar --exclude CVS -c -f - org) | tar xvf -
(cd ../../org.eclipse.ptp.proxy.protocol/bin; tar --exclude CVS -c -f - org) | tar xvf -
(cd ../../org.eclipse.ptp.utils.core/bin; tar --exclude CVS -c -f - org) | tar xvf -

jar xf ${JDT_UI} jar-in-jar-loader.zip
unzip jar-in-jar-loader.zip
rm jar-in-jar-loader.zip

cat > ../manifest_tmp <<X
Rsrc-Class-Path: ./
Class-Path: ./
Rsrc-Main-Class: org.eclipse.ptp.rm.pbs.jproxy.PBSProxyRuntimeServer
Main-Class: org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader
X

jar cvmf ../manifest_tmp ../pbs_proxy.jar .
cd ..
rm -rf build_tmp
rm -f manifest_tmp

exit 0
