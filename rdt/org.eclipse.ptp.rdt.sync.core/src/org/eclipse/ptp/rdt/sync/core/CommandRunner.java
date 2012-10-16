/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ptp.rdt.sync.core.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;

// Static class for running system-level commands. This includes local and remote directory operations and also running arbitrary
// commands on remote machines.
public class CommandRunner {
	// Nested convenience class for storing the results of a command.
	public static class CommandResults {
		private String stdout;
		private String stderr;
		private int exitCode;

		/**
		 * @return the exitCode
		 */
		public int getExitCode() {
			return exitCode;
		}

		/**
		 * @return the stderr
		 */
		public String getStderr() {
			return stderr;
		}

		/**
		 * @return the stdout
		 */
		public String getStdout() {
			return stdout;
		}

		/**
		 * @param exitCode
		 */
		public void setExitCode(int exitCode) {
			this.exitCode = exitCode;
		}

		/**
		 * @param stderr
		 */
		public void setStderr(String stderr) {
			this.stderr = stderr;
		}

		/**
		 * @param stdout
		 */
		public void setStdout(String stdout) {
			this.stdout = stdout;
		}
	};

	enum DirectoryStatus {
		NOT_A_DIRECTORY, NOT_PRESENT, PRESENT
	}

	/**
	 * Simply check if a local directory is present but do not create
	 * 
	 * @return the directory status
	 */
	public static DirectoryStatus checkLocalDirectory(String localDirectory) {
		final File localDir = new File(localDirectory);
		if (localDir.exists() == false) {
			return DirectoryStatus.NOT_PRESENT;
		}
		else if (localDir.isDirectory()) {
			return DirectoryStatus.PRESENT;
		}

		return DirectoryStatus.NOT_A_DIRECTORY;
	}

	/**
	 * Simply check if a remote directory is present but do not create
	 * 
	 * @param conn
	 * @param remoteDir
	 * @return the directory status
	 */
	public static DirectoryStatus checkRemoteDirectory(IRemoteConnection conn, String remoteDir) {
		final IRemoteFileManager fileManager = conn.getRemoteServices().getFileManager(conn);
		final IFileStore fileStore = fileManager.getResource(remoteDir);
		final IFileInfo fileInfo = fileStore.fetchInfo();

		if (fileInfo.exists() == false) {
			return DirectoryStatus.NOT_PRESENT;
		} else if (fileInfo.isDirectory() == false) {
			return DirectoryStatus.NOT_A_DIRECTORY;
		} else {
			return DirectoryStatus.PRESENT;
		}
	}

	/**
	 * This function creates the local directory if it does not exist.
	 * 
	 * @return whether the directory was already PRESENT
	 * TODO: Handle false return from mkdir
	 */
	public static DirectoryStatus createLocalDirectory(String localDirectory) {
		final DirectoryStatus directoryStatus = checkLocalDirectory(localDirectory);
		if (directoryStatus == DirectoryStatus.NOT_PRESENT) {
			final File localDir = new File(localDirectory);
			localDir.mkdir();
		}

		return directoryStatus;
	}

	/**
	 * This function creates the remote directory if it does not exist. Parent directories are also created if necessary. Note that
	 * this command does not overwrite if the requested remote directory exists but is not a directory.
	 * 
	 * @param conn
	 * @param remoteDir
	 * @param monitor 
	 * @throws CoreException
	 *             on problem creating the remote directory.
	 * @return whether the directory was already PRESENT
	 */
	public static DirectoryStatus createRemoteDirectory(IRemoteConnection conn, String remoteDir, IProgressMonitor monitor) throws CoreException {
		final IRemoteFileManager fileManager = conn.getRemoteServices().getFileManager(conn);
		final IFileStore fileStore = fileManager.getResource(remoteDir);
		final IFileInfo fileInfo = fileStore.fetchInfo();

		if (fileInfo.exists() == true) {
			if (fileInfo.isDirectory() == true) {
				return DirectoryStatus.PRESENT;
			} else {
				return DirectoryStatus.NOT_A_DIRECTORY;
			}
		}

		fileStore.mkdir(EFS.NONE, monitor);

		return DirectoryStatus.NOT_PRESENT;
	}

	/**
	 * Execute command on a remote host and wait for the command to complete.
	 * 
	 * @param conn
	 * @param command
	 * @param remoteDirectory
	 * 					Working directory for command
	 * @param monitor
	 * @return CommandResults (contains stdout, stderr, and exit code)
	 * @throws IOException
	 *             in several cases if there is a problem communicating with the remote host.
	 * @throws InterruptedException
	 *             if execution of remote command is interrupted.
	 * @throws RemoteConnectionException
	 * 			   if connection closed and cannot be opened. 
	 * @throws RemoteSyncException 
	 * 			   if other error
	 */
	public static CommandResults executeRemoteCommand(IRemoteConnection conn, String command, String remoteDirectory,
														IProgressMonitor monitor) throws 
																IOException, InterruptedException, RemoteConnectionException, RemoteSyncException {
		// Setup a new process
		final List<String> commandList = new LinkedList<String>();
		commandList.add("sh"); //$NON-NLS-1$
		commandList.add("-c"); //$NON-NLS-1$
		commandList.add(command);
		return executeRemoteCommand(conn, commandList, remoteDirectory, monitor);
	}
		
	/**
	 * Execute command on a remote host and wait for the command to complete.
	 * 
	 * @param conn
	 * @param commandList
	 * @param remoteDirectory
	 * 					Working directory for command
	 * @param monitor
	 * @return CommandResults (contains stdout, stderr, and exit code)
	 * @throws IOException
	 *             in several cases if there is a problem communicating with the remote host.
	 * @throws InterruptedException
	 *             if execution of remote command is interrupted.
	 * @throws RemoteConnectionException
	 * 			   if connection closed and cannot be opened. 
	 * @throws RemoteSyncException 
	 * 			   if other error
	 */
	public static CommandResults executeRemoteCommand(IRemoteConnection conn, List<String> commandList, String remoteDirectory,
															IProgressMonitor monitor) throws 
																	IOException, InterruptedException, RemoteConnectionException, RemoteSyncException {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		try {
			progress.subTask(Messages.CommandRunner_4);
			conn.open(progress.newChild(50));
			final IRemoteProcessBuilder rpb = conn.getRemoteServices().getProcessBuilder(conn, commandList);
			final IRemoteFileManager rfm = conn.getRemoteServices().getFileManager(conn);
			if (remoteDirectory != null && remoteDirectory.length() > 0) {
				rpb.directory(rfm.getResource(remoteDirectory));
			}

			// Run process and stream readers
			OutputStream output = new ByteArrayOutputStream();
			OutputStream error = new ByteArrayOutputStream();

			progress.subTask(Messages.CommandRunner_3);
			IRemoteProcess rp = rpb.start();
			StreamCopyThread getOutput = new StreamCopyThread(rp.getInputStream(), output);
			StreamCopyThread getError = new StreamCopyThread(rp.getErrorStream(), error);
			getOutput.start();
			getError.start();
			//wait for EOF with the change for the ProcessMonitor to cancel
			for (;;) {
				getOutput.join(250);
				if (!getOutput.isAlive()) break;
				if (progress.isCanceled()) {
					throw new RemoteSyncException(new Status(IStatus.CANCEL, RDTSyncCorePlugin.PLUGIN_ID, Messages.CommandRunner_0));
				}
			}
			//rp and getError should be finished as soon as getOutput is finished
			int exitCode = rp.waitFor();
			getError.halt();

			final CommandResults commandResults = new CommandResults();
			commandResults.setExitCode(exitCode);
			commandResults.setStdout(output.toString());
			commandResults.setStderr(error.toString());
			return commandResults;
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	// Enforce as static
	private CommandRunner() {
		throw new AssertionError(Messages.CommandRunner_1);
	}
}