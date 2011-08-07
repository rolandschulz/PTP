package org.eclipse.ptp.rdt.sync.core.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.rdt.sync.git.core.CommandRunner;
import org.eclipse.ptp.rdt.sync.git.core.RemoteSyncException;
import org.eclipse.ptp.rdt.sync.git.core.CommandRunner.CommandResults;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

public class BasicGitSyncTests {
	private static IRemoteConnection fRemoteConnection;
	private static String remoteBaseDir;
	
	
/* Ideas for further tests:
 * 
 * - older/different git version(s)
 * - committing modified files (currently only adding empty files) 
 * - files with odd filenames
 */
	
	/* The connection is created before all tests. This speeds up the tests and requires the password  
	 * to be entered only once if the user doesn't want to store the password in the property file. 
	 */
	@BeforeClass
	public static void createRemoteConnection() throws RemoteConnectionException, IOException {

		/* read in property-file */
		Properties prop = new Properties();
		InputStream in = BasicGitSyncTests.class.getResourceAsStream("remotehost.properties");
		prop.load(in);
		in.close();
		
		/* read property values */
		String host = prop.getProperty("host");
		String username = prop.getProperty("username");
		String password = prop.getProperty("password");
		remoteBaseDir = prop.getProperty("remoteBaseDir");
		
		/* check all properties are available */
		assertTrue(host!=null && username!=null && password != null && remoteBaseDir != null); /*missing fields in property file*/ 

		/* set default values if value is empty - we still require the property to be available - should we?*/
		if (username.equals("")) username=System.getProperty("user.name");
		if (remoteBaseDir.equals("")) remoteBaseDir="/tmp";
		
		/* setup remote connection */
		IRemoteServices fRemoteServices;
		
		fRemoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(
				"org.eclipse.ptp.remote.RemoteTools"); //$NON-NLS-1$
		assertNotNull(fRemoteServices);

		IRemoteConnectionManager connMgr = fRemoteServices
				.getConnectionManager();
		assertNotNull(connMgr);

		fRemoteConnection = connMgr.newConnection("test_connection"); //$NON-NLS-1$

		assertNotNull(fRemoteConnection);
		fRemoteConnection.setAddress(host);
		fRemoteConnection.setUsername(username);
		fRemoteConnection.setPassword(password);
	}
	
	@Test
	public void testCommandRunner() throws RemoteSyncException, IOException, InterruptedException, RemoteConnectionException {
		CommandResults results = CommandRunner.executeRemoteCommand(fRemoteConnection, "echo -n hi", "", null);
		assertEquals(results.getStdout(),"hi");
	}
	
	/* The rule objects are created before each test and than deleted again */
	@Rule
	public TemporaryGitRemoteSyncConnection tempGitConn = 
								new TemporaryGitRemoteSyncConnection(fRemoteConnection, remoteBaseDir);
	
	@Test 
	public void testSyncLocalToRemoteEmpty() throws RemoteSyncException {
		tempGitConn.getGITConn().syncLocalToRemote(null);
	}
	
	@Test 
	public void testSyncRemoteToLocalEmpty() throws RemoteSyncException {
		tempGitConn.getGITConn().syncRemoteToLocal(null, false);
	}
	
	@Test 
	public void testSyncLocalToRemoteRegularFile() throws IOException, CoreException {
		tempGitConn.getLocalFolder().newFile("testFile");
		tempGitConn.getGITConn().syncLocalToRemote(null);
		//assertTrue(tempGitConn.getFileManager().getResource(tempGitConn.getRemoteFolder()+"/testFile").fetchInfo().exists());
		assertArrayEquals(tempGitConn.getFileManager().getResource(tempGitConn.getRemoteFolder()+"/testFile").childNames(EFS.NONE, null),
				 new String[]{"testFile"});
	}
	
	@Test 
	public void testSyncRemoteToLocalRegularFile() throws CoreException, IOException {
		tempGitConn.getFileManager().getResource(tempGitConn.getRemoteFolder()+"/testFile").openOutputStream(EFS.NONE, null).close();
		tempGitConn.getGITConn().syncRemoteToLocal(null, true);
		assertArrayEquals(tempGitConn.getLocalFolder().getRoot().list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !name.equals(".ptp-sync");
			}
		}), new String[]{"testFile"});
	}
}
