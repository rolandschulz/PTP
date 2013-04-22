/*******************************************************************************
 * Copyright (c) 2011 University of Tennessee and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Roland Schulz - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rdt.sync.core.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.rdt.sync.git.core.CommandRunner;
import org.eclipse.ptp.internal.rdt.sync.git.core.CommandRunner.CommandResults;
import org.eclipse.ptp.rdt.sync.core.exceptions.RemoteSyncException;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@SuppressWarnings("restriction")
@RunWith(Parameterized.class)
public class BasicGitSyncTests {
	String host;
	String username;
	String password;
	String remoteBaseDir;
	String privatekey;

	public BasicGitSyncTests(String host, String username, String password, String remoteBaseDir, String privatekey) {
		this.host = host;
		this.username = username;
		this.password = password;
		this.remoteBaseDir = remoteBaseDir;
		this.privatekey = privatekey;
	}

	@Parameters
	public static Collection<Object[]> generateData() throws IOException {
		/* read in property-file */
		Properties prop = new Properties();
		InputStream in = BasicGitSyncTests.class.getResourceAsStream("remotehost.properties");
		prop.load(in);
		in.close();

		String configurations = prop.getProperty("configurations");

		List<Object[]> data = new ArrayList<Object[]>();
		for (String conf : configurations.split(" ")) {
			String host = prop.getProperty(conf + ".host");
			String username = prop.getProperty(conf + ".username");
			String password = prop.getProperty(conf + ".password");
			String remoteBaseDir = prop.getProperty(conf + ".remoteBaseDir");
			String privatekey = prop.getProperty(conf + ".privatekey");

			/* check all properties are available */
			assertTrue(host != null && username != null && password != null && remoteBaseDir != null); /*
																										 * missing fields in
																										 * property file
																										 */

			/* set default values if value is empty - we still require the property to be available - should we? */
			if (username.equals("")) {
				username = System.getProperty("user.name");
			}
			if (remoteBaseDir.equals("")) {
				remoteBaseDir = "/tmp";
			}
			data.add(new Object[] { host, username, password, remoteBaseDir, privatekey });
		}
		return data;
	}

	/*
	 * Ideas for further tests:
	 * 
	 * - committing modified files (currently only adding empty files)
	 * - files with odd filenames
	 */

	/* The rule objects are created before each test and than deleted again */
	@Rule
	public TemporaryGitRemoteSyncConnection tempGitConn = new TemporaryGitRemoteSyncConnection(this);

	@Test
	public void testCommandRunner() throws RemoteSyncException, IOException, InterruptedException, RemoteConnectionException {
		CommandResults results = CommandRunner.executeRemoteCommand(tempGitConn.getRemoteConn(), "echo -n hi", "", null);
		assertEquals(results.getStdout(), "hi");
	}

	@Test
	public void testSyncLocalToRemoteEmpty() throws RemoteSyncException {
		tempGitConn.getGITConn().syncLocalToRemote(null);
	}

	@Test
	public void testSyncRemoteToLocalEmpty() throws RemoteSyncException {
		tempGitConn.getGITConn().syncRemoteToLocal(null, false);
	}

	/*
	 * next two test sync empty file in one direction and than a change in the other
	 * (doesn't follow simple test recommendation but 2nd step requires first so no use in breaking apart
	 */
	@Test
	public void testSyncLocalToRemoteToLocal() throws IOException, CoreException {
		// sync empty file
		tempGitConn.getLocalFolder().newFile("testFile");
		tempGitConn.getGITConn().syncLocalToRemote(null);

		// check file was synced
		assertArrayEquals(
				tempGitConn.getFileManager().getResource(tempGitConn.getRemoteFolder() + "/testFile").childNames(EFS.NONE, null),
				new String[] { "testFile" });

		// TODO: change file and sync in other direction

	}

	@Test
	public void testSyncRemoteToLocalToRemote() throws CoreException, IOException {
		// sync empty file
		IFileStore remoteTestFile = tempGitConn.getFileManager().getResource(tempGitConn.getRemoteFolder() + "/testFile");
		remoteTestFile.openOutputStream(EFS.NONE, null).close();
		tempGitConn.getGITConn().syncRemoteToLocal(null, true);

		// check file was synced
		File localRoot = tempGitConn.getLocalFolder().getRoot();
		assertArrayEquals(localRoot.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !name.equals(".ptp-sync");
			}
		}), new String[] { "testFile" });

		// now sync change in other directions, first write data
		OutputStream out = new FileOutputStream(new File(localRoot.getAbsoluteFile(), "testFile"));
		byte data[] = "some data".getBytes();
		out.write(data);
		out.close();

		// sync
		tempGitConn.getGITConn().syncLocalToRemote(null);

		// read data and compare
		byte buf[] = new byte[data.length];
		InputStream in = remoteTestFile.openInputStream(EFS.NONE, null);
		in.read(buf);
		in.close();
		assertArrayEquals(data, buf);
	}
}
