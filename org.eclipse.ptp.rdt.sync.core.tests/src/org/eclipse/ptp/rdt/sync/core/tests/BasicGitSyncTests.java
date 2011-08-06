package org.eclipse.ptp.rdt.sync.core.tests;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import junit.framework.Assert;

import org.eclipse.ptp.rdt.sync.git.core.CommandRunner;
import org.eclipse.ptp.rdt.sync.git.core.RemoteSyncException;
import org.eclipse.ptp.rdt.sync.git.core.CommandRunner.CommandResults;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.junit.BeforeClass;
import org.junit.Test;

public class BasicGitSyncTests {
	static IRemoteConnection fRemoteConnection;
	
	@BeforeClass
	public static void createRemoteConnection() throws RemoteConnectionException, IOException {

		Properties prop = new Properties();
		InputStream in = BasicGitSyncTests.class.getResourceAsStream("remotehost.properties");
		prop.load(in);
		in.close();
		
		String host = prop.getProperty("host");
		String username = prop.getProperty("username");
		String password = prop.getProperty("password");
		
		Assert.assertTrue(host!=null && username!=null && password != null); /*missing fields in property file*/ 

		if (username.equals("")) username=System.getProperty("user.name");
		
		IRemoteServices fRemoteServices;
		
		fRemoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(
				"org.eclipse.ptp.remote.RemoteTools"); //$NON-NLS-1$
		Assert.assertNotNull(fRemoteServices);

		IRemoteConnectionManager connMgr = fRemoteServices
				.getConnectionManager();
		org.junit.Assert.assertNotNull(connMgr);

		fRemoteConnection = connMgr.newConnection("test_connection"); //$NON-NLS-1$

		Assert.assertNotNull(fRemoteConnection);
		fRemoteConnection.setAddress(host);
		fRemoteConnection.setUsername(username);
		fRemoteConnection.setPassword(password);
	}
	
	@Test
	public void testCommandRunner() throws RemoteSyncException, IOException, InterruptedException, RemoteConnectionException {
		CommandResults results = CommandRunner.executeRemoteCommand(fRemoteConnection, "echo -n hi", "", null);
		Assert.assertEquals(results.getStdout(),"hi");
	}
}
