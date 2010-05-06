package org.eclipse.ptp.core.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;

public class RemoteConnectionTests extends TestCase {
	private static final String USERNAME = "user"; //$NON-NLS-1$
	private static final String PASSWORD = "password"; //$NON-NLS-1$
	private static final String HOST = "localhost"; //$NON-NLS-1$
	
	private IRemoteServices fRemoteServices;
	private IRemoteConnection fRemoteConnection;
	
	public void testEnv() {
		String var = fRemoteConnection.getEnv("SHELL"); //$NON-NLS-1$
		assertNotNull(var);
		
		var = fRemoteConnection.getEnv("FOO_VAR_SHOULD_NOT_BE_DEFINED"); //$NON-NLS-1$
		assertNull(var);
		
		assertNotNull(fRemoteConnection.getProperty("os.name")); //$NON-NLS-1$
		assertNotNull(fRemoteConnection.getProperty("os.arch")); //$NON-NLS-1$
		assertNotNull(fRemoteConnection.getProperty("os.version")); //$NON-NLS-1$
		assertNotNull(fRemoteConnection.getProperty("file.separator")); //$NON-NLS-1$
		assertNotNull(fRemoteConnection.getProperty("path.separator")); //$NON-NLS-1$
		assertNotNull(fRemoteConnection.getProperty("line.separator")); //$NON-NLS-1$
		
		IRemoteProcessBuilder builder = fRemoteServices.getProcessBuilder(fRemoteConnection, "env"); //$NON-NLS-1$
		builder.environment().put("FOO", "BAR"); //$NON-NLS-1$ //$NON-NLS-2$
		builder.environment().put("USER", "FOO"); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			IRemoteProcess proc = builder.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line;
			while ((line = stdout.readLine()) != null) {
				String[] kv = line.trim().split("="); //$NON-NLS-1$
				if (kv.length == 2) {
					if (kv[0].equals("FOO")) assertTrue(kv[1].equals("BAR")); //$NON-NLS-1$ //$NON-NLS-2$
					if (kv[0].equals("USER")) assertTrue(kv[1].equals("FOO")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	public void testCopy() {
		final IRemoteFileManager fileManager = fRemoteServices.getFileManager(fRemoteConnection);

		final IFileSystem fileSystem = EFS.getLocalFileSystem();
		final IFileStore srcFileStore = fileSystem.getStore(new Path("/tmp/log_src.txt"));
		final IFileStore dstFileStore = fileManager.getResource("/tmp").getChild("log_dst.txt");
		try {
			srcFileStore.delete(EFS.NONE, new NullProgressMonitor());
			dstFileStore.delete(EFS.NONE, new NullProgressMonitor());
			OutputStream stream = srcFileStore.openOutputStream(EFS.NONE, new NullProgressMonitor());
			stream.write(new byte[]{'f', 'o', 'o', '\n'});
			stream.close();
			srcFileStore.copy(dstFileStore, EFS.NONE, new NullProgressMonitor());
		} catch (CoreException e) {
			fail(e.getLocalizedMessage());
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}
		IFileInfo srcInfo = srcFileStore.fetchInfo();
		IFileInfo dstInfo = dstFileStore.fetchInfo();
		assertTrue(dstInfo.exists());
		assertTrue(srcInfo.getLength() == dstInfo.getLength());
		try {
			InputStream stream = dstFileStore.openInputStream(EFS.NONE, new NullProgressMonitor());
			byte[] b = new byte[4];
			stream.read(b);
			stream.close();
			assertTrue(b[0] == 'f');
			assertTrue(b[1] == 'o');
			assertTrue(b[2] == 'o');
			assertTrue(b[3] == '\n');
		} catch (CoreException e) {
			fail(e.getLocalizedMessage());
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
		}
		
//		try {
//			srcFileStore.delete(EFS.NONE, new NullProgressMonitor());
//			dstFileStore.delete(EFS.NONE, new NullProgressMonitor());
//		} catch (CoreException e) {
//			fail();
//		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		fRemoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices("org.eclipse.ptp.remote.RemoteTools"); //$NON-NLS-1$
		assertNotNull(fRemoteServices);
		
		IRemoteConnectionManager connMgr = fRemoteServices.getConnectionManager();
		assertNotNull(connMgr);
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("ptp.localhost-selection", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("ptp.login-username", USERNAME); //$NON-NLS-1$
		map.put("ptp.login-password", PASSWORD); //$NON-NLS-1$
		map.put("ptp.connection-address", HOST); //$NON-NLS-1$
		map.put("ptp.connection-port", "22"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("ptp.key-path", ""); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("ptp.key-passphrase", ""); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("ptp.is-passwd-auth", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("ptp.connection-timeout", "5"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("ptp.cipher-type", "default"); //$NON-NLS-1$ //$NON-NLS-2$
		
		try {
			fRemoteConnection = connMgr.newConnection("test_connection", map); //$NON-NLS-1$
		} catch (RemoteConnectionException e) {
			fail(e.getLocalizedMessage());
		}
		assertNotNull(fRemoteConnection);
		
		try {
			fRemoteConnection.open(new NullProgressMonitor());
		} catch (RemoteConnectionException e) {
			fail(e.getLocalizedMessage());
		}
		assertTrue(fRemoteConnection.isOpen());
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
