package org.eclipse.ptp.core.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.tests.harness.CoreTest;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;

public class FileStoreTests extends CoreTest {
	private static final String CONNECTION_NAME = "test_connection";
	private static final String USERNAME = "user";
	private static final String PASSWORD = "password";
	private static final String HOST = "localhost";
	private static final String PATH1 = "/home/user/sftp_test";
	private static final String PATH2 = PATH1 + "/.file1";
	private static final String TEST_STRING = "a string containing fairly *()(*&^$%## random text";

	private IRemoteServices fRemoteServices;
	private IRemoteConnection fRemoteConnection;
	private IRemoteFileManager fRemoteFileManager;
	
	public void testFileStore() {
		URI	path1Uri = fRemoteFileManager.toURI(PATH1);
		URI	path2Uri = fRemoteFileManager.toURI(PATH2);
		assertNotNull(path1Uri);
		assertNotNull(path2Uri);
		
		IFileStore store1 = null;
		IFileStore store2 = null;
		
		try {
			store1 = EFS.getStore(path1Uri);
			store2 = EFS.getStore(path2Uri);
		} catch (CoreException e) {
			fail("2.0", e);
		}
		
		for (int i = 0; i < 5; i++) {
			assertFalse(store1.fetchInfo().exists());
			try {
				store1.mkdir(EFS.NONE, null);
			} catch (CoreException e) {
				fail("3.0", e);
			}
			assertTrue(store1.fetchInfo().exists());
			
			assertFalse(store2.fetchInfo().exists());
			try {
				OutputStream stream = store2.openOutputStream(EFS.NONE, null);
				assertNotNull(stream);
				BufferedWriter buf = new BufferedWriter(new OutputStreamWriter(stream));
				buf.write(TEST_STRING);
				buf.close();
			} catch (Exception e) {
				fail("4.0", e);
			}
			assertTrue(store2.fetchInfo().exists());
			
			try {
				InputStream stream = store2.openInputStream(EFS.NONE, null);
				assertNotNull(stream);
				BufferedReader buf = new BufferedReader(new InputStreamReader(stream));
				String line = buf.readLine().trim();
				assertTrue(line.equals(TEST_STRING));
				buf.close();
			} catch (Exception e) {
				fail("5.0", e);
			}
	
			try {
				store2.delete(EFS.NONE, null);
			} catch (CoreException e) {
				fail("6.0", e);
			}
			assertFalse(store2.fetchInfo().exists());
	
			try {
				store1.delete(EFS.NONE, null);
			} catch (CoreException e) {
				fail("7.0", e);
			}
			assertFalse(store1.fetchInfo().exists());
		}

	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		fRemoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices("org.eclipse.ptp.remote.RemoteTools");
		assertNotNull(fRemoteServices);
		
		IRemoteConnectionManager connMgr = fRemoteServices.getConnectionManager();
		assertNotNull(connMgr);
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("ptp.localhost-selection", "false");
		map.put("ptp.login-username", USERNAME);
		map.put("ptp.login-password", PASSWORD);
		map.put("ptp.connection-address", HOST);
		map.put("ptp.connection-port", "22");
		map.put("ptp.key-path", "");
		map.put("ptp.key-passphrase", "");
		map.put("ptp.is-passwd-auth", "true");
		map.put("ptp.connection-timeout", "5");
		map.put("ptp.cipher-type", "default");
		
		try {
			fRemoteConnection = connMgr.newConnection(CONNECTION_NAME, map);
		} catch (RemoteConnectionException e) {
			fail(e.getLocalizedMessage());
		}
		assertNotNull(fRemoteConnection);
		
		fRemoteFileManager = fRemoteServices.getFileManager(fRemoteConnection);
		assertNotNull(fRemoteFileManager);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		fRemoteConnection.close();
		IRemoteConnectionManager connMgr = fRemoteServices.getConnectionManager();
		assertNotNull(connMgr);
		connMgr.removeConnection(fRemoteConnection);
	}
	
}
