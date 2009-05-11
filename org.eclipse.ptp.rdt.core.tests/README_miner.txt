###############################################################################
# Copyright (c) 2009 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#     IBM Corporation
###############################################################################


**** How to run the miner tests ****


1) Download and extract the RDT server jar (http://wiki.eclipse.org/PTP/builds).

2) Right click the minertest.jardesc file and select Open With > Jar Export Wizard.
   Enter an export destination and click Finish.

3) Copy the minertests.jar file into the RDT server directory.

4) Copy the JUnit 3 jar into the RDT server directory (you can find this in your eclipse installation's plugins folder).

5) Create a file of include paths so that the standalone indexer can find system headers.
   This is just a text file with each include path on a separate line.
   
6) Invoke JUnit from the command line. 

Here is an example of how to run the miner tests from the command line:

java -Djava.ext.dirs=. -Dminertest.includePathsFile=includes.txt 
     junit.textui.TestRunner org.eclipse.ptp.rdt.core.tests.miner.StandaloneIndexerTest


The system property -Djava.ext.dirs=. tells the virtual machine to include all the jars in the
current directory on the classpath.

The system property -Dminertest.includePathsFile=includes.txt is used to provide the name
of the file that contains the list of include paths.


