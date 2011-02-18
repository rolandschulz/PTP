/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.remote.launch.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.RemoteVariableManager;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.core.messages.Messages;
import org.eclipse.ptp.remote.internal.core.DebugUtil;
import org.osgi.framework.Bundle;

/**
 * @since 4.0
 */
public abstract class AbstractRemoteServerRunner extends Job {
	private static String LAUNCH_COMMAND_VAR = "launch_command"; //$NON-NLS-1$
	private static String PAYLOAD_VAR = "payload"; //$NON-NLS-1$
	private static String WORKING_DIR_VAR = "working_dir"; //$NON-NLS-1$
	private static String VERIFY_COMMAND_VAR = "verify_command"; //$NON-NLS-1$
	private static String VERIFY_FAIL_MESSAGE_VAR = "verify_fail_message"; //$NON-NLS-1$
	private static String VERIFY_PATTERN_VAR = "verify_pattern"; //$NON-NLS-1$

	public enum ServerState {
		/**
		 * @since 5.0
		 */
		STOPPED, STARTING, RUNNING
	}

	private final boolean DEBUG = true;

	private final Map<String, String> fEnv = new HashMap<String, String>();
	private final Map<String, String> fVars = new HashMap<String, String>();
	private final String fServerName;

	private ServerState fServerState = ServerState.STOPPED;
	private IRemoteProcess fRemoteProcess;
	private IRemoteConnection fRemoteConnection;
	private Bundle fBundle;

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
	 * Get the launch command for this server
	 * 
	 * @return launch command
	 */
	public String getLaunchCommand() {
		return fVars.get(LAUNCH_COMMAND_VAR);
	}

	/**
	 * Get the payload. The payload is copied to the remote system using the
	 * supplied connection if it doesn't exist.
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
	public synchronized ServerState getServerState() {
		return fServerState;
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
	 * @since 5.0 Gets the verify command.
	 * 
	 * @return the verify command
	 */
	public String getVerifyCommand() {
		return fVars.get(VERIFY_COMMAND_VAR);
	}

	/**
	 * @since 5.0 Gets the verify fail message.
	 * 
	 * @return the verify fail message
	 */
	public String getVerifyFailMessage() {
		return fVars.get(VERIFY_FAIL_MESSAGE_VAR);
	}

	/**
	 * @since 5.0 Gets the verify pattern.
	 * 
	 * @return the verify pattern
	 */
	public String getVerifyPattern() {
		return fVars.get(VERIFY_PATTERN_VAR);
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
	 * @param fVerifyCommand
	 *            the new verify command
	 */
	public void setVerifyCommand(String fVerifyCommand) {
		fVars.put(VERIFY_COMMAND_VAR, fVerifyCommand);
	}

	/**
	 * @since 5.0 Sets the verify fail message.
	 * 
	 * @param fVerifyFailMessage
	 *            the new verify fail message
	 */
	public void setVerifyFailMessage(String fVerifyFailMessage) {
		fVars.put(VERIFY_FAIL_MESSAGE_VAR, fVerifyFailMessage);
	}

	/**
	 * @since 5.0 Sets the verify pattern.
	 * 
	 * @param fVerifyPattern
	 *            the new verify pattern
	 */
	public void setVerifyPattern(String fVerifyPattern) {
		fVars.put(VERIFY_PATTERN_VAR, fVerifyPattern);
	}

	/**
	 * Set the working directory. This is the location of the payload on the
	 * remote system.
	 * 
	 * @param workDir
	 *            working directory
	 */
	public void setWorkDir(String workDir) {
		fVars.put(WORKING_DIR_VAR, workDir);
	}

	/**
	 * Start the server launch. Clients should check the server status with
	 * {@link #getServerState()} or use {@link #waitForServerStart} to determine
	 * when the server has actually started.
	 * 
	 * @param monitor
	 *            progress monitor that can be used to cancel the launch
	 * @throws IOException
	 *             if the launch fails
	 */
	public synchronized void startServer(IProgressMonitor monitor) throws IOException {
		SubMonitor subMon = SubMonitor.convert(monitor, 100);
		try {
			if (fRemoteConnection != null && fServerState != ServerState.RUNNING) {
				if (!doServerStarting(subMon.newChild(10))) {
					throw new IOException(Messages.AbstractRemoteServerRunner_6);
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
						throw new IOException(Messages.AbstractRemoteServerRunner_7);
					}
				}

				// Check if the valid java version is installed on the server
				if ((getVerifyCommand() != null && getVerifyCommand().length() != 0) && !isValidVersionInstalled(subMon)) {
					if (getVerifyFailMessage() != null && getVerifyFailMessage().length() != 0) {
						throw new IOException(getVerifyFailMessage());
					}
					throw new IOException(Messages.AbstractRemoteServerRunner_12);
				}

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
	 * Wait for the server to start up for at most "timeout" ms. Will do nothing
	 * if the server is stopped.
	 * 
	 * @param timeout
	 *            time (in ms) to wait for server startup. A timeout of 0 means
	 *            wait forever.
	 * @since 5.0
	 */
	public synchronized void waitForServerStart(int timeout) {
		if (getServerState() == ServerState.STARTING) {
			int dec = timeout > 0 ? 1000 : 0;
			while (timeout >= 0 && getServerState() != ServerState.RUNNING) {
				try {
					wait(1000);
					timeout -= dec;
				} catch (InterruptedException e) {
				}
			}
		}
	}

	/**
	 * Wait for the server to start up. Will do nothing if the server is
	 * stopped.
	 * 
	 * @param monitor
	 *            progress monitor to cancel waiting
	 * @since 5.0
	 */
	public synchronized void waitForServerStart(IProgressMonitor monitor) {
		SubMonitor subMon = SubMonitor.convert(monitor, 100);
		try {
			if (getServerState() == ServerState.STARTING) {
				while (!subMon.isCanceled() && getServerState() != ServerState.RUNNING) {
					try {
						wait(100);
					} catch (InterruptedException e) {
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
	 * Checks if the valid version installed on the remote server. It uses a
	 * pattern which is define in the "plugin.xml" file to match with the output
	 * 
	 * @param monitor
	 *            monitor object
	 * @return true, if the valid version is installed on the remote server
	 */
	private boolean isValidVersionInstalled(IProgressMonitor monitor) throws IOException {
		SubMonitor subMon = SubMonitor.convert(monitor);
		try {
			StringBuilder sb = new StringBuilder();
			String s;

			// get the remote process that runs the verify command
			IRemoteProcess p = runVerifyCommand(subMon);
			// get the buffer reader
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

			// read the output from the command
			while ((s = stdInput.readLine()) != null) {
				sb.append(s);
			}
			// compile the pattern for search
			Pattern pattern = Pattern.compile(getVerifyPattern());
			// get a matcher object
			Matcher m = pattern.matcher(sb.toString());

			while (m.find()) {
				// return true if we find the specified pattern matched
				// with the output stream
				return true;
			}

			return false;
		} finally {
			monitor.done();
		}
	}

	/**
	 * Launch the server use the supplied remote connection. The server file is
	 * cached on the remote machine prior to the first launch.
	 * 
	 * @param conn
	 *            connection to remote machine for launch
	 * @param monitor
	 *            progress monitor
	 * @return remote process representing the server invocation
	 * @throws IOException
	 */
	private IRemoteProcess launchServer(IRemoteConnection conn, IProgressMonitor monitor) throws IOException {
		try {
			SubMonitor subMon = SubMonitor.convert(monitor, 100);
			/*
			 * First check if the remote file exists or is a different size to
			 * the local version and copy over if required.
			 */
			IRemoteFileManager fileManager = conn.getRemoteServices().getFileManager(conn);
			IFileStore directory = fileManager.getResource(getWorkingDir());
			/*
			 * Create the directory if it doesn't exist (has no effect if the
			 * directory already exists). Also, check if a file of this name
			 * exists and generate exception if it does.
			 */
			directory.mkdir(EFS.NONE, subMon.newChild(10));
			IFileStore server = directory.getChild(getPayload());
			IFileInfo serverInfo = server.fetchInfo(EFS.NONE, subMon.newChild(10));
			IFileStore local = null;
			URL jarURL = FileLocator.find(fBundle, new Path(getPayload()), null);
			if (jarURL != null) {
				jarURL = FileLocator.toFileURL(jarURL);
				local = EFS.getStore(URIUtil.toURI(jarURL));
			}
			if (local == null) {
				throw new IOException(NLS.bind(Messages.AbstractRemoteServerRunner_11,
						new Object[] { getPayload(), fBundle.getSymbolicName() }));
			}
			IFileInfo localInfo = local.fetchInfo(EFS.NONE, subMon.newChild(10));
			if (!serverInfo.exists() || serverInfo.getLength() != localInfo.getLength()) {
				local.copy(server, EFS.OVERWRITE, subMon.newChild(70));
			}

			/*
			 * Now launch the server.
			 */
			subMon.subTask(Messages.AbstractRemoteServerRunner_5);
			RemoteVariableManager varMgr = RemoteVariableManager.getInstance();
			varMgr.setVars(fVars);
			String launchCmd = varMgr.performStringSubstitution(getLaunchCommand());
			List<String> launchArgs = Arrays.asList(launchCmd.split(" ")); //$NON-NLS-1$
			IRemoteProcessBuilder builder = conn.getRemoteServices().getProcessBuilder(conn, launchArgs);
			builder.directory(directory);
			builder.environment().putAll(getEnv());
			return builder.start();
		} catch (Exception e) {
			throw new IOException(e.getMessage());
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
	private IRemoteProcess runVerifyCommand(IProgressMonitor monitor) throws IOException {
		SubMonitor subMon = SubMonitor.convert(monitor);
		subMon.subTask(Messages.AbstractRemoteServerRunner_13);
		try {
			// specify the verify command to check the software version
			List<String> verifyArgs = Arrays.asList(getVerifyCommand().split(" ")); //$NON-NLS-1$
			IRemoteProcessBuilder builder = getRemoteConnection().getRemoteServices().getProcessBuilder(getRemoteConnection(),
					verifyArgs);
			builder.redirectErrorStream(true);
			builder.environment().putAll(getEnv());
			return builder.start();
		} finally {
			monitor.done();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.Job#canceling()
	 */
	@Override
	protected void canceling() {
		terminateServer();
	}

	/**
	 * Called on termination of the server process
	 * 
	 * @since 5.0
	 */
	protected abstract void doServerFinished(IProgressMonitor monitor);

	/**
	 * Called just prior to the server starting
	 * 
	 * @return false if start should be aborted
	 * @since 5.0
	 */
	protected abstract boolean doServerStarting(IProgressMonitor monitor);

	/**
	 * Called once the server starts
	 * 
	 * @return false if server should be aborted
	 * @since 5.0
	 */
	protected abstract boolean doServerStarted(IProgressMonitor monitor);

	/**
	 * Called with each line of stderr from the server. Implementers can use
	 * this to determine when the server has successfully started.
	 * 
	 * @param output
	 *            line of stderr output from server
	 * @return true if the server has started
	 */
	protected abstract boolean doVerifyServerRunningFromStderr(String output);

	/**
	 * Called with each line of stdout from the server. Implementers can use
	 * this to determine when the server has successfully started.
	 * 
	 * @param output
	 *            line of stdout output from server
	 * @return true if the server has started
	 */
	protected abstract boolean doVerifyServerRunningFromStdout(String output);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		assert getLaunchCommand() != null;
		assert fRemoteProcess == null;

		final SubMonitor subMon = SubMonitor.convert(monitor, 100);

		try {
			if (subMon.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			fRemoteProcess = launchServer(fRemoteConnection, subMon.newChild(50));

			if (subMon.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			final BufferedReader stdout = new BufferedReader(new InputStreamReader(fRemoteProcess.getInputStream()));
			new Thread(new Runnable() {
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
					} catch (IOException e) {
						// Ignore
					}
				}
			}, "dstore server stdout").start(); //$NON-NLS-1$

			final BufferedReader stderr = new BufferedReader(new InputStreamReader(fRemoteProcess.getErrorStream()));
			new Thread(new Runnable() {
				public void run() {
					try {
						while (getServerState() != ServerState.STOPPED) {
							String output = stderr.readLine();
							if (output != null) {
								if (getServerState() == ServerState.STARTING && doVerifyServerRunningFromStderr(output)) {
									if (!doServerStarted(subMon.newChild(10))) {
										fRemoteProcess.destroy();
									}
									setServerState(ServerState.RUNNING);
								}
								PTPRemoteCorePlugin
										.getDefault()
										.getLog()
										.log(new Status(IStatus.ERROR, PTPRemoteCorePlugin.getUniqueIdentifier(), fServerName
												+ ": " + output)); //$NON-NLS-1$
							}
						}
					} catch (IOException e) {
						// Ignore
					}
				}
			}, "dstore server stderr").start(); //$NON-NLS-1$

			subMon.worked(40);
			subMon.subTask(Messages.AbstractRemoteServerRunner_1);

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

			try {
				fRemoteProcess.waitFor();
			} catch (InterruptedException e) {
				// Do nothing
			}

			/*
			 * Check if process terminated successfully (if not canceled).
			 */
			if (fRemoteProcess.exitValue() != 0) {
				if (!subMon.isCanceled()) {
					throw new CoreException(new Status(IStatus.ERROR, PTPRemoteCorePlugin.getUniqueIdentifier(), NLS.bind(
							Messages.AbstractRemoteServerRunner_3, fRemoteProcess.exitValue())));
				}
			}
			return subMon.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		} catch (IOException e) {
			return new Status(IStatus.ERROR, PTPRemoteCorePlugin.getUniqueIdentifier(), e.getMessage(), null);
		} finally {
			synchronized (this) {
				fRemoteProcess = null;
				doServerFinished(subMon.newChild(1));
			}
			setServerState(ServerState.STOPPED);
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Change the state of the server
	 * 
	 * @param state
	 *            new server state
	 */
	protected synchronized void setServerState(ServerState state) {
		if (fServerState != state) {
			if (DebugUtil.SERVER_TRACING) {
				System.out.println("SERVER RUNNER: " + state.toString()); //$NON-NLS-1$
			}
			fServerState = state;
			this.notifyAll();
		}
	}

	/**
	 * Terminate the server
	 */
	protected synchronized void terminateServer() {
		if (fServerState == ServerState.RUNNING && fRemoteProcess != null) {
			fRemoteProcess.destroy();
		}
	}
}
