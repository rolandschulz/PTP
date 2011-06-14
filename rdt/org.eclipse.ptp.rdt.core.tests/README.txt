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


**** How to use the RDT testing plugin ****



The Remote Development Tools (RDT) depends on a connection to a remote
RDT server in order to provide tooling for remote C/C++ source code files.
In order to test these features the test suite must be run using a connection
to a server. The RDT testing plugin provides a custom JUnit test suite that
will automatically set up a DStore connection to the remote server and run
the tests under this connection.

In order to use this test suite two configuration files are required
and the test suite must be created programmatically:

In order run the RDT tests the following steps should be taken:



1) Create a new plug in project.



2) Somewhere in the project create a properties file to store connection properties.
   This is an example of how the contents of the file should look:
 
connection.class=org.eclipse.ptp.rdt.core.tests.DStoreTestConnection
connection.systemtypeid=org.eclipse.rse.systemtype.linux
connection.hostname=myhostname
connection.port=3000
connection.username=myusername
connection.password=mypassword
project.workspace=/home/myusername/test_workspace

The property keys have the following meanings:

connection.class
- Fully qualified name of a class that implements IRDTTestConnection. This class
  is responsible for setting up the test connection. Currently the RDT testing plugin
  provides one implementation for setting up a DStore connection. The remaining 
  properties in the configuration file depend on the IRDTTestConnection implementation.
  
connection.systemtypeid
- RSE system type id as defined in org.eclipse.rse.core.IRSESystemType.

project.workspace
- The directory where the tests will create temporary test projects.



3) Create a "service model" XML file. RDT maintains a service model configuration
   for each remote project. This configuration tells RDT which service providers
   to use, and this information is necessary to run the test suite. 
   The easiest way to create this file is to run RDT, create a project,
   and set up its service model to work with the test server. Once this project is
   set up go into the project's metadata directory, find the XML file that stores 
   the service model configuration and copy it.
   
The file will look something like this:

<service-model>
	<project name="${project_name}">
		<service-configuration name="Default">
			<service id="org.eclipse.ptp.rdt.core.BuildService" provider-id="org.eclipse.ptp.rdt.ui.RemoteBuildServiceProvider">
				<provider-configuration RemoteBuildServiceProvider.remoteToolsConnectionName="${host_name}" 
				                        RemoteBuildServiceProvider.remoteToolsProviderID="org.eclipse.ptp.remote.RSERemoteServices"/>
			</service>
			<service id="org.eclipse.ptp.rdt.core.CIndexingService" provider-id="org.eclipse.ptp.rdt.ui.RemoteCIndexServiceProvider">
				<provider-configuration host-name="${host_name}"/>
			</service>
		</service-configuration>
	</project>
</service-model>

The project name and host name should be replaced with the variables ${project_name} and ${host_name}.
These variables will be replaced with the values provided in the properties file.

   
   
4) Create a JUnit test suite. Here is an example:

public class ConnectionTestSuite extends TestCase {
	
	private static final String PROPERTIES_FILE    = "config/connection.properties";
	private static final String SERVICE_MODEL_FILE = "config/service_model.xml";
	
	public static Test suite() {
		
		File propertiesFile   = RDTTestPlugin.getDefault().getFileInPlugin(new Path(PROPERTIES_FILE));
		File serviceModelFile = RDTTestPlugin.getDefault().getFileInPlugin(new Path(SERVICE_MODEL_FILE));

		ConnectionSuite suite = new ConnectionSuite(propertiesFile, serviceModelFile);
		suite.setName(ConnectionTestSuite.class.getName());
		
		// add tests
		suite.addTest(CoreConnectionTestSuite.suite());
		suite.addTest(UIConnectionTestSuite.suite());
		
		return suite;
	}
}

The test suite must do the following:
- Create java.io.File objects for the connection properties file and the service model XML file.
- Create a ConnectionSuite object using the two files.
- Add tests to the suite.

When run the ConnectionSuite will first establish a connection to the server, then run all the
tests, then disconnect.



5) Write tests.

The testing plugin provides some APIs for managing remote projects:

ConnectionManager
- Provides methods to reset the service model and to get the URI of the
  location of the remote workspace.
  
RemoteTestProject
- Used to create an RDT project on the remote server.

