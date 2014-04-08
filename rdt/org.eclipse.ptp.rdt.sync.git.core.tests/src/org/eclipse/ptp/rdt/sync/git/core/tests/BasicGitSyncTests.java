/*******************************************************************************
 * Copyright (c) 2011, 2014 University of Tennessee and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Roland Schulz - initial API and implementation
 *     John Eblen - update and augment for Luna
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.git.core.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.internal.rdt.sync.core.RDTSyncCorePlugin;
import org.eclipse.ptp.rdt.sync.core.RemoteLocation;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.junit.Test;

public class BasicGitSyncTests {
	private static String remoteServicesProvider = "org.eclipse.remote.JSch";
	private static String testProjectName = "testSyncProject";
	private static String testConnectionName = "testSyncConnection";
	private static String testRemoteDirName = "junit";

	String host = null;
	String username = null;
	String password = null;
	String remoteBaseDir = null;

	public BasicGitSyncTests() {
		Properties prop = new Properties();
		InputStream in = BasicGitSyncTests.class.getResourceAsStream("remotehost.properties");
		try {
			prop.load(in);
		} catch (IOException e) {
			RDTSyncCorePlugin.log("Unable to load testing properties", e);
		}
		try {
			in.close();
		} catch (IOException e) {
			RDTSyncCorePlugin.log("Unable to close test properties file", e);
		}

		host = prop.getProperty("host");
		username = prop.getProperty("username");
		password = prop.getProperty("password");
		remoteBaseDir = prop.getProperty("remoteBaseDir");
		// TODO: Change so that password is not required in config file.
		assertTrue(host != null && password != null);

		if (username == null || username.equals("")) {
			username = System.getProperty("user.name");
		}
		if (remoteBaseDir == null || remoteBaseDir.equals("")) {
			remoteBaseDir = "/tmp";
		}
	}

	@Test
	public void basicGitSyncTest() {
		IProject project;
		IRemoteConnection conn;

		try {
			// TODO: Pop up a dialog for password if it is not specified.
			conn = createTestConnection(host, username, password, remoteBaseDir);
		} catch (RemoteConnectionException e) {
			RDTSyncCorePlugin.log("Unable to create test connection to host: " + host + " directory: " + remoteBaseDir, e);
			return;
		}
		try {
			project = createTestProject();
		} catch (CoreException e) {
			RDTSyncCorePlugin.log("Unable to create test project", e);
			deleteConnection(conn);
			return;
		}

		RemoteLocation remoteLocation = new RemoteLocation();
		remoteLocation.setConnection(conn);
		remoteLocation.setLocation(remoteBaseDir + testRemoteDirName);
		remoteLocation.setRemoteServicesId(conn.getRemoteServices().getId());

		RDTSyncCorePlugin.log("I am now thoroughly testing Sync!");

		deleteRemoteFiles(remoteLocation, remoteBaseDir);
		deleteConnection(conn);
		deleteProject(project);
	}

	private static IProject createTestProject() throws CoreException {
		IProgressMonitor progressMonitor = new NullProgressMonitor();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(testProjectName);
		project.create(progressMonitor);
		project.open(progressMonitor);
		return project;
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

	private static void deleteRemoteFiles(RemoteLocation rl, String dir) {
		// TODO: Figure out a safe way to remove remote testing directory
	}

	private static void deleteConnection(IRemoteConnection conn) {
		IRemoteConnectionManager connMgr = getRemoteConnectionManager();
		try {
			conn.close();
			connMgr.removeConnection(conn);
		} catch (RemoteConnectionException e) {
			RDTSyncCorePlugin.log("Unable to delete connection", e);
		}
	}

	private static void deleteProject(IProject project) {
		try {
			project.delete(true, true, null);
		} catch (CoreException e) {
			RDTSyncCorePlugin.log("Unable to delete project " + testProjectName, e);
		}
	}

	private static IRemoteConnectionManager getRemoteConnectionManager() {
		IRemoteServices remoteServices = RemoteServices.getRemoteServices(remoteServicesProvider);
		assertNotNull(remoteServices);

		return remoteServices.getConnectionManager();
	}
}
