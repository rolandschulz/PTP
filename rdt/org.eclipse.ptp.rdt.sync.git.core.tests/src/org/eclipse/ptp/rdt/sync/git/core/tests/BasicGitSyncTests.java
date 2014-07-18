/*******************************************************************************
 * Copyright (c) 2011, 2014 University of Tennessee and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Roland Schulz - initial API and implementation
 *     John Eblen - update and augment for Eclipse Luna
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.git.core.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.ptp.internal.rdt.sync.git.core.GitRepo;
import org.eclipse.ptp.internal.rdt.sync.git.core.JGitRepo;
import org.eclipse.ptp.rdt.sync.core.RemoteLocation;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.internal.jsch.core.JSchConnectionManager;
import org.eclipse.remote.internal.jsch.core.JSchConnectionWorkingCopy;
import org.junit.Test;

/**
 * JUnit tests for synchronized project's git sync service
 *
 * Tests are configured in "remotehost.properties." Basic sync operations are performed on and between the given local and remote
 * directories. Timing information is also logged, which is useful for identifying bottlenecks for a particular local/remote pair.
 */
@SuppressWarnings("restriction")
public class BasicGitSyncTests {
	private static String remoteServicesProvider = "org.eclipse.remote.JSch";
	private static String testConnectionName = "testSyncConnection";

	private String localBaseDir = null;
	private String remoteBaseDir = null;
	private int steps = 1;
	private IRemoteConnection remoteConn;
	private boolean init = false;

	/**
	 * Constructor - reads in test configuration
	 */
	public BasicGitSyncTests() {
		Properties prop = new Properties();
		InputStream in = BasicGitSyncTests.class.getResourceAsStream("remotehost.properties");
		try {
			prop.load(in);
		} catch (IOException e) {
			log("Unable to load testing properties", e);
		}
		try {
			in.close();
		} catch (IOException e) {
			log("Unable to close test properties file", e);
		}

		localBaseDir = prop.getProperty("localBaseDir");
		if (localBaseDir == null) {
			log("localBaseDir property required");
			return;
		}

		remoteBaseDir = prop.getProperty("remoteBaseDir");
		if (remoteBaseDir == null || remoteBaseDir.equals("")) {
			remoteBaseDir = "/tmp";
		}

		String numSteps = prop.getProperty("steps");
		if (numSteps != null) {
			steps = Integer.parseInt(numSteps);
		}

		try {
			// TODO: Pop up a dialog for password if it is not specified.
			remoteConn = createTestConnection(prop);
		} catch (RemoteConnectionException e) {
			log("Unable to create test connection", e);
			return;
		}
		init = true;
	}

	/**
	 * Testing code. Performs basic sync operations and reports timing information.
	 * @throws CoreException 
	 * @throws GitAPIException 
	 * @throws IOException 
	 * @throws MissingConnectionException 
	 */
	@Test
	public void basicGitSyncTest() throws CoreException, GitAPIException, IOException, MissingConnectionException {
		assertTrue("Test initialization failed", init);

		RemoteLocation remoteLocation = new RemoteLocation();
		remoteLocation.setConnection(remoteConn);
		remoteLocation.setLocation(remoteBaseDir);
		remoteLocation.setRemoteServicesId(remoteConn.getRemoteServices().getId());

		Timer stepTimer = new Timer();
		Timer totalTimer = new Timer();
		stepTimer.reset();
		totalTimer.reset();

		JGitRepo localRepo = new JGitRepo(new Path(localBaseDir), null);
		log("Created local JGit repository", stepTimer);

		GitRepo remoteRepo = new GitRepo(remoteLocation, null);
		log("Created remote Git repository", stepTimer);

		for (int i=0; i<steps; i++) {
			localRepo.commit(null);
			log("Commit local files", stepTimer);

			remoteRepo.uploadFilter(localRepo, null);
			log("Uploaded file filter", stepTimer);

			remoteRepo.commitRemoteFiles(null);
			log("Committed remote files", stepTimer);

			localRepo.fetch(remoteRepo, null);
			log("Fetched remote files", stepTimer);

			localRepo.merge(null);
			log("Local merged in remote changes", stepTimer);

			localRepo.push(remoteRepo, null);
			log("Pushed local files to remote", stepTimer);

			remoteRepo.merge(null);
			log("Remote merged in local changes", stepTimer);

			log("-------------------------------------------------");
			log("Total elapsed time for sync " + Integer.toString(i), totalTimer);
			log("\n");
			stepTimer.reset();
		}
		deleteConnection(remoteConn);
	}

	private static IRemoteConnection createTestConnection(Properties prop)
			throws RemoteConnectionException {
		JSchConnectionManager connMgr = getRemoteConnectionManager();
		assertNotNull(connMgr);

		String host = prop.getProperty("host");
		String username = prop.getProperty("username");
		String password = prop.getProperty("password");
		String keyFile = prop.getProperty("keyFile");
		String portString = prop.getProperty("port");
		if (host == null) {
			throw new RemoteConnectionException("host property required");
		}

		if (username == null || username.equals("")) {
			username = System.getProperty("user.name");
		}

		JSchConnectionWorkingCopy wc = (JSchConnectionWorkingCopy) connMgr.newConnection(testConnectionName); //$NON-NLS-1$  
		wc.setAddress(host);
		wc.setUsername(username);
		if (keyFile != null) {
			wc.setKeyFile(keyFile);
			wc.setIsPasswordAuth(false);
		} else {
			wc.setPassword(password);
			wc.setIsPasswordAuth(true);
		}
		if (portString != null) {
			wc.setPort(Integer.parseInt(portString));
		}
		IRemoteConnection conn = wc.save();
		assertNotNull(conn);

		if (!conn.isOpen()) {
			conn.open(null);
		}
		
		return conn;
	}

	private static void deleteConnection(IRemoteConnection conn) {
		JSchConnectionManager connMgr = getRemoteConnectionManager();
		try {
			conn.close();
			connMgr.removeConnection(conn);
		} catch (RemoteConnectionException e) {
			log("Unable to delete connection", e);
		}
	}

	private static JSchConnectionManager getRemoteConnectionManager() {
		IRemoteServices remoteServices = RemoteServices.getRemoteServices(remoteServicesProvider);
		assertNotNull(remoteServices);

		return (JSchConnectionManager) remoteServices.getConnectionManager();
	}

	// Report event and elapsed time, according to passed timer, to the log.
	// Note that time is recorded first and timer is reset last to minimize logging overhead.
	private static void log(String event, Timer timer) {
		double elapsed = timer.getElapsed() / Math.pow(10, 9);
		DecimalFormat df = new DecimalFormat("0.00");
		System.out.print(event + ":");
		for (int i=event.length(); i < 40; i++) {
			System.out.print(" ");
		}
		System.out.println(df.format(elapsed) + " sec");
		timer.reset();
	}

	private static void log(String event) {
		System.out.println(event);
	}

	private static void log(String event, Exception e) {
		System.out.println(event);
		e.printStackTrace();
	}
}
