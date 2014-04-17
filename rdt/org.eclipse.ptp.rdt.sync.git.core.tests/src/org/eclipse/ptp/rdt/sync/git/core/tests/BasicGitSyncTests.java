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
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.junit.Test;

/**
 * JUnit tests for synchronized project's git sync service
 *
 * Tests are configured in "remotehost.properties." Basic sync operations are performed on and between the given local and remote
 * directories. Timing information is also logged, which is useful for identifying bottlenecks for a particular local/remote pair.
 */
public class BasicGitSyncTests {
	private static String remoteServicesProvider = "org.eclipse.remote.JSch";
	private static String testConnectionName = "testSyncConnection";

	private String host = null;
	private String username = null;
	private String password = null;
	private String localBaseDir = null;
	private String remoteBaseDir = null;
	private int steps = 1;

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

		host = prop.getProperty("host");
		username = prop.getProperty("username");
		password = prop.getProperty("password");
		localBaseDir = prop.getProperty("localBaseDir");
		remoteBaseDir = prop.getProperty("remoteBaseDir");
		String numSteps = prop.getProperty("steps");
		if (numSteps != null) {
			steps = Integer.parseInt(numSteps);
		}
		// TODO: Change so that password is not required in config file.
		assertTrue(host != null && password != null);
		if (username == null || username.equals("")) {
			username = System.getProperty("user.name");
		}
		if (remoteBaseDir == null || remoteBaseDir.equals("")) {
			remoteBaseDir = "/tmp";
		}
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
		IRemoteConnection conn;

		try {
			// TODO: Pop up a dialog for password if it is not specified.
			conn = createTestConnection(host, username, password, remoteBaseDir);
		} catch (RemoteConnectionException e) {
			log("Unable to create test connection to host: " + host + " directory: " + remoteBaseDir, e);
			return;
		}

		RemoteLocation remoteLocation = new RemoteLocation();
		remoteLocation.setConnection(conn);
		remoteLocation.setLocation(remoteBaseDir);
		remoteLocation.setRemoteServicesId(conn.getRemoteServices().getId());

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

			localRepo.fetch(remoteLocation, null);
			log("Fetched remote files", stepTimer);

			localRepo.merge(null);
			log("Local merged in remote changes", stepTimer);

			localRepo.push(remoteLocation, null);
			log("Pushed local files to remote", stepTimer);

			remoteRepo.merge(null);
			log("Remote merged in local changes", stepTimer);

			log("-------------------------------------------------");
			log("Total elapsed time for sync " + Integer.toString(i), totalTimer);
			log("\n");
			stepTimer.reset();
		}
		deleteConnection(conn);
	}

	private static IRemoteConnection createTestConnection(String host, String username, String password, String remoteBaseDir)
			throws RemoteConnectionException {
		IRemoteConnectionManager connMgr = getRemoteConnectionManager();
		assertNotNull(connMgr);

		IRemoteConnectionWorkingCopy wc = connMgr.newConnection(testConnectionName); //$NON-NLS-1$  
		wc.setAddress(host);
		wc.setUsername(username);
		wc.setPassword(password);
		IRemoteConnection conn = wc.save();
		assertNotNull(conn);

		if (!conn.isOpen()) {
			conn.open(null);
		}
		
		return conn;
	}

	private static void deleteConnection(IRemoteConnection conn) {
		IRemoteConnectionManager connMgr = getRemoteConnectionManager();
		try {
			conn.close();
			connMgr.removeConnection(conn);
		} catch (RemoteConnectionException e) {
			log("Unable to delete connection", e);
		}
	}

	private static IRemoteConnectionManager getRemoteConnectionManager() {
		IRemoteServices remoteServices = RemoteServices.getRemoteServices(remoteServicesProvider);
		assertNotNull(remoteServices);

		return remoteServices.getConnectionManager();
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
