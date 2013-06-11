/*******************************************************************************
 * Copyright (c) 2009,2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.remote.server.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.remote.server.core.Activator;
import org.eclipse.ptp.internal.remote.server.core.DebugUtil;
import org.eclipse.ptp.internal.remote.server.core.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.osgi.framework.Bundle;

/**
 * @since 5.0
 */
public abstract class AbstractRemoteServerRunner extends Job {
	public enum ServerState {
		/**
		 * @since 5.0
		 */
		STOPPED, STARTING, RUNNING
	}

	private static String LAUNCH_COMMAND_VAR = "launch_command"; //$NON-NLS-1$
	private static String UNPACK_COMMAND_VAR = "unpack_command"; //$NON-NLS-1$
	private static String PAYLOAD_VAR = "payload"; //$NON-NLS-1$
	private static String WORKING_DIR_VAR = "working_dir"; //$NON-NLS-1$
	private static String VERIFY_LAUNCH_COMMAND_VAR = "verify_launch_command"; //$NON-NLS-1$
	private static String VERIFY_LAUNCH_FAIL_MESSAGE_VAR = "verify_launch_fail_message"; //$NON-NLS-1$
	private static String VERIFY_LAUNCH_PATTERN_VAR = "verify_launch_pattern"; //$NON-NLS-1$
	private static String VERIFY_UNPACK_COMMAND_VAR = "verify_unpack_command"; //$NON-NLS-1$
	private static String VERIFY_UNPACK_FAIL_MESSAGE_VAR = "verify_unpack_fail_message"; //$NON-NLS-1$
	private static String VERIFY_UNPACK_PATTERN_VAR = "verify_unpack_pattern"; //$NON-NLS-1$

	private final boolean DEBUG = true;

	private final Map<String, String> fEnv = new HashMap<String, String>();
	private final Map<String, String> fVars = new HashMap<String, String>();
	private final String fServerName;

	private volatile ServerState fServerState = ServerState.STOPPED;
	private IRemoteProcess fRemoteProcess;
	private IRemoteConnection fRemoteConnection;
	private Bundle fBundle;
	private boolean fContinuous = true;
	private IStatus fStatus;

	/**
	 * Save stderr output so if there is a nonzero exit code we can expose message to the user - Bug 395517
	 */
	private String stdErrOutput;

	public AbstractRemoteServerRunner(String name) {
		super(name);
		fServerName = name;
		setPriority(Job.LONG);
		setSystem(!DEBUG);
	}

	public Map<String, String> getEnv() {
		return fEnv;
	}

	/**
	 * Get the error stream of the remote process. This should only be used for non-continuous processes and is only valid for
	 * servers in the RUNNING state.
	 * 
	 * @return InputStream error stream or null if the server has not been started
	 */
	public InputStream getErrorStream() {
		if (fRemoteProcess != null) {
			return fRemoteProcess.getErrorStream();
		}
		return null;
	}

	/**
	 * Get the input stream of the remote process. This should only be used for non-continuous processes and is only valid for
	 * servers in the RUNNING state.
	 * 
	 * @return InputStream input stream or null if the server has not been started
	 */
	public InputStream getInputStream() {
		if (fRemoteProcess != null) {
			return fRemoteProcess.getInputStream();
		}
		return null;
	}

	/**
	 * Get the launch command for this server
	 * 
	 * @return launch command
	 */
	public String getLaunchCommand() {
		return fVars.get(LAUNCH_COMMAND_VAR);
	}

	/**
	 * Get the output stream of the remote process. This should only be used for non-continuous processes.
	 * 
	 * @return OutputStream output stream or null if the server has not been started
	 */
	public OutputStream getOutputStream() {
		if (fRemoteProcess != null) {
			return fRemoteProcess.getOutputStream();
		}
		return null;
	}

	/**
	 * Get the payload. The payload is copied to the remote system using the supplied connection if it doesn't exist.
	 * 
	 * @return
	 */
	public String getPayload() {
		return fVars.get(PAYLOAD_VAR);
	}

	/**
	 * Get the remote connection used to launch the server
	 * 
	 * @return remote connection
	 */
	public IRemoteConnection getRemoteConnection() {
		return fRemoteConnection;
	}

	/**
	 * Get the current state of the server.
	 * 
	 * @return server state
	 */
	public ServerState getServerState() {
		return fServerState;
	}

	/**
	 * Get the unpack command for this server
	 * 
	 * @return unpack command
	 */
	public String getUnpackCommand() {
		return fVars.get(UNPACK_COMMAND_VAR);
	}

	/**
	 * Get the value of a variable that will be expanded in the launch command
	 * 
	 * @param name
	 *            variable name
	 * @returns variable value
	 */
	public String getVariable(String name) {
		return fVars.get(name);
	}

	/**
	 * @since 5.0 Gets the verify launch command.
	 * 
	 * @return the verify launch command
	 */
	public String getVerifyLaunchCommand() {
		return fVars.get(VERIFY_LAUNCH_COMMAND_VAR);
	}

	/**
	 * @since 5.0 Gets the verify launch fail message.
	 * 
	 * @return the verify launch fail message
	 */
	public String getVerifyLaunchFailMessage() {
		return fVars.get(VERIFY_LAUNCH_FAIL_MESSAGE_VAR);
	}

	/**
	 * @since 5.0 Gets the verify launch pattern.
	 * 
	 * @return the verify launch pattern
	 */
	public String getVerifyLaunchPattern() {
		return fVars.get(VERIFY_LAUNCH_PATTERN_VAR);
	}

	/**
	 * @since 5.0 Gets the verify unpack command.
	 * 
	 * @return the verify unpack command
	 */
	public String getVerifyUnpackCommand() {
		return fVars.get(VERIFY_UNPACK_COMMAND_VAR);
	}

	/**
	 * @since 5.0 Gets the verify unpack fail message.
	 * 
	 * @return the verify unpack fail message
	 */
	public String getVerifyUnpackFailMessage() {
		return fVars.get(VERIFY_UNPACK_FAIL_MESSAGE_VAR);
	}

	/**
	 * @since 5.0 Gets the verify unpack pattern.
	 * 
	 * @return the verify unpack pattern
	 */
	public String getVerifyUnpackPattern() {
		return fVars.get(VERIFY_UNPACK_PATTERN_VAR);
	}

	/**
	 * Get the working directory. This is the location of the payload.
	 * 
	 * @return working directory
	 */
	public String getWorkingDir() {
		return fVars.get(WORKING_DIR_VAR);
	}

	/**
	 * Set the id of the bundle containing the remote server file.
	 * 
	 * @param id
	 *            bundle id
	 */
	public void setBundleId(String id) {
		fBundle = Platform.getBundle(id);
	}

	/**
	 * Set flag to indicate job is continuous or only runs once.
	 * 
	 * @param continuous
	 *            true if the job runs continuously
	 */
	public void setContinuous(boolean continuous) {
		fContinuous = continuous;
	}

	/**
	 * Set the environment prior to launching the server.
	 * 
	 * @param env
	 *            string containing environment (as returned by "env" command)
	 */
	public void setEnv(String env) {
		if (env != null) {
			for (String vars : env.split("\n")) { //$NON-NLS-1$
				String[] envVar = vars.split("="); //$NON-NLS-1$
				if (envVar.length == 2) {
					fEnv.put(envVar[0], envVar[1]);
				}
			}
		}
	}

	/**
	 * Set the command used to launch the server
	 * 
	 * @param command
	 *            launch command
	 */
	public void setLaunchCommand(String command) {
		fVars.put(LAUNCH_COMMAND_VAR, command);
	}

	/**
	 * Set the name of the payload
	 * 
	 * @param file
	 *            payload name
	 */
	public void setPayload(String file) {
		fVars.put(PAYLOAD_VAR, file);
	}

	/**
	 * Set the connection used to launch the server
	 * 
	 * @param conn
	 *            remote connection
	 */
	public void setRemoteConnection(IRemoteConnection conn) {
		fRemoteConnection = conn;
		setName(fServerName + " (" + conn.getName() + ")");//$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * Set the command used to unpack the payload
	 * 
	 * @param command
	 *            unpack command
	 */
	public void setUnpackCommand(String command) {
		fVars.put(UNPACK_COMMAND_VAR, command);
	}

	/**
	 * Set the value of a variable that will be expended in the launch command
	 * 
	 * @param name
	 *            variable name
	 * @param value
	 *            variable value
	 */
	public void setVariable(String name, String value) {
		fVars.put(name, value);
	}

	/**
	 * @since 5.0 Sets the verify command.
	 * 
	 * @param verifyCommand
	 *            the new verify command
	 */
	public void setVerifyLaunchCommand(String verifyCommand) {
		fVars.put(VERIFY_LAUNCH_COMMAND_VAR, verifyCommand);
	}

	/**
	 * @since 5.0 Sets the verify fail message.
	 * 
	 * @param verifyFailMessage
	 *            the new verify fail message
	 */
	public void setVerifyLaunchFailMessage(String verifyFailMessage) {
		fVars.put(VERIFY_LAUNCH_FAIL_MESSAGE_VAR, verifyFailMessage);
	}

	/**
	 * @since 5.0 Sets the verify pattern.
	 * 
	 * @param verifyPattern
	 *            the new verify pattern
	 */
	public void setVerifyLaunchPattern(String verifyPattern) {
		fVars.put(VERIFY_LAUNCH_PATTERN_VAR, verifyPattern);
	}

	/**
	 * @since 5.0 Sets the verify unpack command.
	 * 
	 * @param verifyCommand
	 *            the new verify command
	 */
	public void setVerifyUnpackCommand(String verifyCommand) {
		fVars.put(VERIFY_UNPACK_COMMAND_VAR, verifyCommand);
	}

	/**
	 * @since 5.0 Sets the verify unpack fail message.
	 * 
	 * @param verifyFailMessage
	 *            the new verify fail message
	 */
	public void setVerifyUnpackFailMessage(String verifyFailMessage) {
		fVars.put(VERIFY_UNPACK_FAIL_MESSAGE_VAR, verifyFailMessage);
	}

	/**
	 * @since 5.0 Sets the verify unpack pattern.
	 * 
	 * @param verifyPattern
	 *            the new verify pattern
	 */
	public void setVerifyUnpackPattern(String verifyPattern) {
		fVars.put(VERIFY_UNPACK_PATTERN_VAR, verifyPattern);
	}

	/**
	 * Set the working directory. This is the location of the payload on the remote system.
	 * 
	 * @param workDir
	 *            working directory
	 */
	public void setWorkDir(String workDir) {
		fVars.put(WORKING_DIR_VAR, workDir);
	}

	/**
	 * Start the server launch. Clients should check the server status with {@link #getServerState()} or use
	 * {@link #waitForServerStart} to determine when the server has actually started.
	 * 
	 * @param monitor
	 *            progress monitor that can be used to cancel the launch
	 * @throws IOException
	 *             if the launch fails
	 */
	public void startServer(IProgressMonitor monitor) throws IOException {
		SubMonitor subMon = SubMonitor.convert(monitor, 100);
		try {
			if (fRemoteConnection != null && fServerState != ServerState.RUNNING) {
				if (!doServerStarting(subMon.newChild(10))) {
					throw new IOException(Messages.AbstractRemoteServerRunner_serverRestartAborted);
				}
				setServerState(ServerState.STARTING);
				if (!fRemoteConnection.isOpen()) {
					try {
						fRemoteConnection.open(subMon.newChild(10));
					} catch (RemoteConnectionException e) {
						throw new IOException(e.getMessage());
					}
					if (subMon.isCanceled()) {
						return;
					}
					if (!fRemoteConnection.isOpen()) {
						throw new IOException(Messages.AbstractRemoteServerRunner_unableToOpenConnection);
					}
				}

				/*
				 * Check if the launch command is valid
				 */
				if ((getVerifyLaunchCommand() != null && getVerifyLaunchCommand().length() != 0)
						&& !isValidCommand(getVerifyLaunchCommand(), getVerifyLaunchPattern(), subMon.newChild(10))) {
					if (getVerifyLaunchFailMessage() != null && getVerifyLaunchFailMessage().length() != 0) {
						setServerState(ServerState.STOPPED);
						throw new IOException(NLS.bind(getVerifyLaunchFailMessage(),
								new Object[] { fServerName, fRemoteConnection.getName() }));
					}
					setServerState(ServerState.STOPPED);
					throw new IOException(Messages.AbstractRemoteServerRunner_cannotRunServerMissingRequirements);
				}

				/*
				 * Check if the unpack command is valid
				 */
				if ((getVerifyUnpackCommand() != null && getVerifyUnpackCommand().length() != 0)
						&& !isValidCommand(getVerifyUnpackCommand(), getVerifyUnpackPattern(), subMon.newChild(10))) {
					if (getVerifyUnpackFailMessage() != null && getVerifyUnpackFailMessage().length() != 0) {
						setServerState(ServerState.STOPPED);
						throw new IOException(NLS.bind(getVerifyUnpackFailMessage(),
								new Object[] { fServerName, fRemoteConnection.getName() }));
					}
					setServerState(ServerState.STOPPED);
					throw new IOException(Messages.AbstractRemoteServerRunner_cannotRunUnpack);
				}

				fStatus = Status.OK_STATUS;

				if (!subMon.isCanceled()) {
					schedule();
				}
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Update the server with a new version if necessary.
	 * 
	 * @param monitor
	 *            progress monitor
	 * @since 6.0
	 */
	public void updateServer(IProgressMonitor monitor) throws IOException {
		SubMonitor subMon = SubMonitor.convert(monitor, 100);
		try {
			if (fRemoteConnection != null) {
				if (!fRemoteConnection.isOpen()) {
					try {
						fRemoteConnection.open(subMon.newChild(20));
					} catch (RemoteConnectionException e) {
						throw new IOException(e.getMessage());
					}
					if (subMon.isCanceled()) {
						return;
					}
					if (!fRemoteConnection.isOpen()) {
						throw new IOException(Messages.AbstractRemoteServerRunner_unableToOpenConnection);
					}
				}

				doUpdate(subMon.newChild(80));
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}

	}

	/**
	 * Wait for the server to finished. Will do nothing if the server is stopped.
	 * 
	 * @param monitor
	 *            progress monitor to cancel waiting
	 * @since 6.0
	 */
	public IStatus waitForServerFinish(IProgressMonitor monitor) {
		SubMonitor subMon = SubMonitor.convert(monitor, 100);
		try {
			while (getServerState() != ServerState.STOPPED && !subMon.isCanceled()) {
				synchronized (this) {
					try {
						wait(100);
					} catch (InterruptedException e) {
						// Ignore
					}
				}
			}
			return fStatus;
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Wait for the server to start up for at most "timeout" ms. Will do nothing if the server is stopped.
	 * 
	 * @param timeout
	 *            time (in ms) to wait for server startup. A timeout of 0 means wait forever.
	 * @since 5.0
	 */
	public void waitForServerStart(int timeout) {
		int waitVal = timeout < 1000 ? timeout : 1000;
		int dec = timeout > 0 ? 1000 : 0;
		while (getServerState() == ServerState.STARTING && timeout >= 0) {
			try {
				synchronized (this) {
					wait(waitVal);
				}
				timeout -= dec;
			} catch (InterruptedException e) {
				// Ignore
			}
		}
	}

	/**
	 * Wait for the server to start up. Will do nothing if the server is stopped.
	 * 
	 * @param monitor
	 *            progress monitor to cancel waiting
	 * @since 5.0
	 */
	public void waitForServerStart(IProgressMonitor monitor) {
		SubMonitor subMon = SubMonitor.convert(monitor, 100);
		try {
			while (getServerState() == ServerState.STARTING && !subMon.isCanceled()) {
				synchronized (this) {
					try {
						wait(100);
					} catch (InterruptedException e) {
						// Ignore
					}
				}
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Check if the payload exists on the remote machine, and if not, or if it has changed then upload a copy.
	 * 
	 * @param conn
	 *            remote connection
	 * @param directory
	 *            directory containing payload
	 * @param monitor
	 *            progress monitor
	 * @return true if a new copy of the payload was uploaded, false otherwise
	 * @throws IOException
	 *             thrown if any errors occur
	 */
	private boolean checkAndUploadPayload(IFileStore directory, IProgressMonitor monitor) throws IOException {
		SubMonitor subMon = SubMonitor.convert(monitor, 100);
		try {
			IFileStore server = directory.getChild(getPayload());
			IFileInfo serverInfo = server.fetchInfo(EFS.NONE, subMon.newChild(10));
			IFileStore local = null;
			URL jarURL = FileLocator.find(fBundle, new Path(getPayload()), null);
			if (jarURL != null) {
				jarURL = FileLocator.toFileURL(jarURL);
				local = EFS.getStore(URIUtil.toURI(jarURL));
			}
			if (local == null) {
				throw new IOException(NLS.bind(Messages.AbstractRemoteServerRunner_unableToLocatePayload, new Object[] {
						getPayload(), fBundle.getSymbolicName() }));
			}
			IFileInfo localInfo = local.fetchInfo(EFS.NONE, subMon.newChild(10));
			if (!serverInfo.exists() || serverInfo.getLength() != localInfo.getLength()) {
				local.copy(server, EFS.OVERWRITE, subMon.newChild(70));
				return true;
			}
			return false;
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	private IFileStore doUpdate(IProgressMonitor monitor) throws IOException {
		SubMonitor subMon = SubMonitor.convert(monitor, 100);
		try {
			/*
			 * Check if the remote file exists or is a different size to the local version and copy over if required.
			 */
			IRemoteFileManager fileManager = fRemoteConnection.getRemoteServices().getFileManager(fRemoteConnection);
			IFileStore directory = fileManager.getResource(getWorkingDir());
			/*
			 * Create the directory if it doesn't exist (has no effect if the directory already exists). Also, check if a file of
			 * this name exists and generate exception if it does.
			 */
			directory.mkdir(EFS.NONE, subMon.newChild(10));

			if (checkAndUploadPayload(directory, subMon.newChild(30))) {
				if (!subMon.isCanceled()) {
					unpackPayload(directory, subMon.newChild(30));
				}
			}

			return directory;
		} catch (CoreException e) {
			throw new IOException(e.getMessage());
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}

	}

	/**
	 * Checks if the command is valid. It uses a pattern which is define in the "plugin.xml" file to match with the output
	 * 
	 * @param monitor
	 *            monitor object
	 * @return true, if the valid version is installed on the remote server
	 */
	private boolean isValidCommand(String command, String verifyPattern, IProgressMonitor monitor) throws IOException {
		SubMonitor subMon = SubMonitor.convert(monitor, 100);
		try {
			// compile the pattern for search
			Pattern pattern = Pattern.compile(verifyPattern);

			// get the remote process that runs the verify command
			IRemoteProcess p = runCommand(command, Messages.AbstractRemoteServerRunner_runningValidate, null, true,
					subMon.newChild(100));
			// get the buffer reader
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

			// read the output from the command
			try {
				String s;
				while ((s = stdInput.readLine()) != null) {
					// get a matcher object
					Matcher m = pattern.matcher(s);
					if (m.matches()) {
						return true;
					}
				}
			} catch (IOException e) {
				/*
				 * For some reason we're sometimes seeing a "write end dead" message here even though the correct result is
				 * returned. Ignore this exception for now, though the root cause needs to be ascertained.
				 */
				Activator.log(e);
			}

			return false;
		} finally {
			monitor.done();
		}
	}

	/**
	 * Launch the server use the supplied remote connection. The server file is cached on the remote machine prior to the first
	 * launch.
	 * 
	 * @param conn
	 *            connection to remote machine for launch
	 * @param monitor
	 *            progress monitor
	 * @return remote process representing the server invocation
	 * @throws IOException
	 */
	private IRemoteProcess launchServer(IProgressMonitor monitor) throws IOException {
		SubMonitor subMon = SubMonitor.convert(monitor, 100);
		try {
			IFileStore directory = doUpdate(subMon.newChild(50));

			/*
			 * Now launch the server.
			 */
			if (!subMon.isCanceled()) {
				return runCommand(getLaunchCommand(), Messages.AbstractRemoteServerRunner_launching, directory, false,
						subMon.newChild(50));
			}

			return null;
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Run version verify command on the remote server.
	 * 
	 * @param monitor
	 *            the monitor object
	 * @return the IRemoteProcess object
	 * @throws Exception
	 *             the exception
	 */
	private IRemoteProcess runCommand(String command, String message, IFileStore directory, boolean redirect,
			IProgressMonitor monitor) throws IOException {
		SubMonitor subMon = SubMonitor.convert(monitor);
		subMon.subTask(message);
		try {
			RemoteVariableManager varMgr = RemoteVariableManager.getInstance();
			varMgr.setVars(fVars);
			String cmdToRun = varMgr.performStringSubstitution(command);
			List<String> cmdArgs = Arrays.asList(cmdToRun.split(" ")); //$NON-NLS-1$
			IRemoteProcessBuilder builder = fRemoteConnection.getRemoteServices().getProcessBuilder(fRemoteConnection, cmdArgs);
			if (directory != null) {
				builder.directory(directory);
			}
			builder.redirectErrorStream(redirect);
			builder.environment().putAll(getEnv());
			return builder.start();
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Run the unpack command on the remote machine. Waits for command to finish
	 * 
	 * @param conn
	 *            remote connection
	 * @param directory
	 * @param monitor
	 * @throws IOException
	 */
	private void unpackPayload(IFileStore directory, IProgressMonitor monitor) throws IOException {
		String unpackCommand = getUnpackCommand();
		if (unpackCommand != null && unpackCommand.length() != 0) {
			IRemoteProcess proc = runCommand(unpackCommand, Messages.AbstractRemoteServerRunner_unpackingPayload, directory, false,
					monitor);
			while (!proc.isCompleted() && !monitor.isCanceled()) {
				synchronized (this) {
					try {
						wait(100);
					} catch (InterruptedException e) {
						// Ignore
					}
				}
			}
		}
	}

	/**
	 * Called on termination of the server process
	 * 
	 * @since 5.0
	 */
	protected abstract void doServerFinished(IProgressMonitor monitor);

	/**
	 * Called once the server starts
	 * 
	 * @return false if server should be aborted
	 * @since 5.0
	 */
	protected abstract boolean doServerStarted(IProgressMonitor monitor);

	/**
	 * Called just prior to the server starting
	 * 
	 * @return false if start should be aborted
	 * @since 5.0
	 */
	protected abstract boolean doServerStarting(IProgressMonitor monitor);

	/**
	 * Called with each line of stderr from the server. Implementers can use this to determine when the server has successfully
	 * started.
	 * 
	 * @param output
	 *            line of stderr output from server
	 * @return true if the server has started
	 */
	protected boolean doVerifyServerRunningFromStderr(String output) {
		return false;
	}

	/**
	 * Called with each line of stdout from the server. Implementers can use this to determine when the server has successfully
	 * started.
	 * 
	 * @param output
	 *            line of stdout output from server
	 * @return true if the server has started
	 */
	protected boolean doVerifyServerRunningFromStdout(String output) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime. IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		stdErrOutput = "";//reset output string //$NON-NLS-1$
		assert getLaunchCommand() != null;

		final SubMonitor subMon = SubMonitor.convert(monitor, 100);

		try {
			if (subMon.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			fRemoteProcess = launchServer(subMon.newChild(50));

			if (fRemoteProcess == null || subMon.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			if (fContinuous) {
				final BufferedReader stdout = new BufferedReader(new InputStreamReader(fRemoteProcess.getInputStream()));
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							while (getServerState() != ServerState.STOPPED) {
								String output = stdout.readLine();
								if (output != null) {
									if (getServerState() == ServerState.STARTING && doVerifyServerRunningFromStdout(output)) {
										if (!doServerStarted(subMon.newChild(10))) {
											fRemoteProcess.destroy();
										}
										setServerState(ServerState.RUNNING);
									}
									if (DebugUtil.SERVER_TRACING) {
										System.out.println("SERVER: " + output); //$NON-NLS-1$
									}
								}
							}
							stdout.close();
						} catch (IOException e) {
							// Ignore
						}
					}
				}, "server stdout").start(); //$NON-NLS-1$
			}

			final BufferedReader stderr = new BufferedReader(new InputStreamReader(fRemoteProcess.getErrorStream()));
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						while (getServerState() != ServerState.STOPPED) {
							String output = stderr.readLine();
							if (output != null) {
								if (fContinuous && getServerState() == ServerState.STARTING
										&& doVerifyServerRunningFromStderr(output)) {
									if (!doServerStarted(subMon.newChild(10))) {
										fRemoteProcess.destroy();
									}
									setServerState(ServerState.RUNNING);
								}
								if (DebugUtil.SERVER_TRACING) {
									System.out.println("SERVER: " + output); //$NON-NLS-1$
								}
								stdErrOutput += output;
							}
						}
						stderr.close();
					} catch (IOException e) {
						// Ignore
					}
				}
			}, "server stderr").start(); //$NON-NLS-1$

			if (!fContinuous) {
				setServerState(ServerState.RUNNING);
			}

			subMon.worked(40);
			subMon.subTask(Messages.AbstractRemoteServerRunner_serverRunningCancelToTerminate);

			/*
			 * Wait while running but not canceled.
			 */
			while (!fRemoteProcess.isCompleted() && !subMon.isCanceled()) {
				synchronized (this) {
					try {
						wait(500);
					} catch (InterruptedException e) {
						// Ignore interrupt;
					}
				}
			}

			/*
			 * Kill process if user cancels
			 */
			if (!fRemoteProcess.isCompleted()) {
				fRemoteProcess.destroy();
			}

			try {
				fRemoteProcess.waitFor();
			} catch (InterruptedException e) {
				// Do nothing
			}

			fStatus = subMon.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;

			/*
			 * Check if process terminated successfully (if not canceled).
			 */
			if (fRemoteProcess.exitValue() != 0) {
				if (!subMon.isCanceled()) {
					// Create exception object so the details of the problem can be shown in the ErrorDialog's Details section
					RemoteServerException exc = new RemoteServerException(stdErrOutput);
					String msg = NLS.bind(Messages.AbstractRemoteServerRunner_serverFinishedWithExitCode,
							fRemoteProcess.exitValue());
					fStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, msg, exc);
				}
			}
		} catch (IOException e) {
			fStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), null);
		} finally {
			doServerFinished(subMon.newChild(1));
			setServerState(ServerState.STOPPED);
			if (monitor != null) {
				monitor.done();
			}
		}
		return fStatus;
	}

	/**
	 * Exception to be attached to errors running commands on remote server - since the Exception class type
	 * is shown in the Error Log View, we make our own name here so as to not be a "java.lang.Exception"
	 */
	@SuppressWarnings("serial")
	private class RemoteServerException extends Exception {
		public RemoteServerException(String message) {
			super(message);
		}
	}

	/**
	 * Change the state of the server
	 * 
	 * @param state
	 *            new server state
	 */
	protected void setServerState(ServerState state) {
		if (fServerState != state) {
			if (DebugUtil.SERVER_TRACING) {
				System.out.println("SERVER RUNNER: " + state.toString()); //$NON-NLS-1$
			}
			fServerState = state;
			synchronized (this) {
				notifyAll();
			}
		}
	}

	/**
	 * Terminate the server
	 */
	protected void terminateServer() {
		if (fServerState == ServerState.RUNNING && fRemoteProcess != null) {
			fRemoteProcess.destroy();
		}
	}
}
