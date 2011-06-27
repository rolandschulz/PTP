package org.eclipse.ptp.core.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;

public class RemoteProcessTests extends TestCase {
	private static final String USERNAME = "user"; //$NON-NLS-1$
	private static final String PASSWORD = "password"; //$NON-NLS-1$
	private static final String HOST = "host"; //$NON-NLS-1$

	private IRemoteServices fRemoteServices;
	private IRemoteConnection fRemoteConnection;

	public void testProcess() {
		for (int t = 0; t < 10; t++) {
			new Thread() {
				@Override
				public void run() {
					IRemoteProcessBuilder builder = fRemoteServices.getProcessBuilder(fRemoteConnection, "perl", "-V:version"); //$NON-NLS-1$
					builder.redirectErrorStream(true);
					for (int i = 0; i < 100; i++) {
						System.out.println("Testing process " + i + " (of 100)...");
						try {
							IRemoteProcess proc = builder.start();
							BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
							String line;
							while ((line = stdout.readLine()) != null) {
								System.out.println("read " + line);
							}
						} catch (IOException e) {
							fail(e.getLocalizedMessage());
						}
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							fail(e.getLocalizedMessage());
						}
					}
				}

			}.start();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		fRemoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices("org.eclipse.ptp.remote.RemoteTools"); //$NON-NLS-1$
		assertNotNull(fRemoteServices);

		IRemoteConnectionManager connMgr = fRemoteServices.getConnectionManager();
		assertNotNull(connMgr);

		try {
			fRemoteConnection = connMgr.newConnection("test_connection"); //$NON-NLS-1$
		} catch (RemoteConnectionException e) {
			fail(e.getLocalizedMessage());
		}
		assertNotNull(fRemoteConnection);
		fRemoteConnection.setAddress(HOST);
		fRemoteConnection.setUsername(USERNAME);
		fRemoteConnection.setPassword(PASSWORD);

		try {
			fRemoteConnection.open(new NullProgressMonitor());
		} catch (RemoteConnectionException e) {
			fail(e.getLocalizedMessage());
		}
		assertTrue(fRemoteConnection.isOpen());
	}

	/*
	 * (non-Javadoc)
	 * 
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
