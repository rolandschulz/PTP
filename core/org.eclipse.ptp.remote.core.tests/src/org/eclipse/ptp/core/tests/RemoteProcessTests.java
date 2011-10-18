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
	private static final String USERNAME = "username"; //$NON-NLS-1$
	private static final String PASSWORD = ""; //$NON-NLS-1$
	private static final String HOST = "localhost"; //$NON-NLS-1$
	private static int NUM_THREADS = 15;

	private IRemoteServices fRemoteServices;
	private IRemoteConnection fRemoteConnection;

	public void testProcess() {
		Thread[] threads = new Thread[NUM_THREADS];

		for (int t = 0; t < NUM_THREADS; t++) {
			System.out.println("creating thread...");
			Thread thread = new Thread("test thread " + t) {
				@Override
				public void run() {
					System.out.println("Thread " + getId() + " starting...");

					IRemoteProcessBuilder builder = fRemoteServices.getProcessBuilder(fRemoteConnection, "perl", "-V:version"); //$NON-NLS-1$
					builder.redirectErrorStream(true);
					for (int i = 0; i < 100; i++) {
						System.out.println("Testing process " + i + " (of 10)..." + getId());
						try {
							IRemoteProcess proc = builder.start();
							System.out.println("start proc (" + getId() + ")");
							BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
							String line;
							while ((line = stdout.readLine()) != null) {
								System.out.println("read (" + getId() + ") " + line);
							}
							try {
								System.out.println("about to wait (" + getId() + ")");
								proc.waitFor();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} catch (IOException e) {
							e.printStackTrace();
							fail(e.getLocalizedMessage());
						}
						// try {
						// Thread.sleep(500);
						// } catch (InterruptedException e) {
						// e.printStackTrace();
						// }
					}
				}

			};
			thread.start();
			threads[t] = thread;
		}
		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
			}
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
