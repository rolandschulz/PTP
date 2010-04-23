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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.RemoteVariableManager;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.internal.core.DebugUtil;
import org.osgi.framework.Bundle;

public abstract class AbstractRemoteServerRunner extends Job {
	public enum ServerState {
		STARTING, RUNNING, FINISHED
	}

	private final boolean DEBUG = true;

	private final Map<String, String> fEnv = new HashMap<String, String>();
	private IRemoteConnection fRemoteConnection;
	private Bundle fBundle;
	private final String fServerName;

	private String fLaunchCommand;
	private String fWorkDir = null;
	private ServerState fServerState = ServerState.STARTING;
	private IRemoteProcess fRemoteProcess;

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
		return fLaunchCommand;
	}

	/**
	 * Get the payload. The payload is copied to the remote system using the
	 * supplied connection if it doesn't exist.
	 * 
	 * @return
	 */
	public String getPayload() {
		return RemoteVariableManager.getInstance().getVariable("payload"); //$NON-NLS-1$
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
	 * Get the working directory. This is the location of the payload.
	 * 
	 * @return working directory
	 */
	public String getWorkingDir() {
		return fWorkDir;
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
		fLaunchCommand = command;
	}

	/**
	 * Set the name of the payload
	 * 
	 * @param file
	 *            payload name
	 */
	public void setPayload(String file) {
		RemoteVariableManager.getInstance().setVariable("payload", file); //$NON-NLS-1$
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
	 * Set the working directory. This is the location of the payload on the
	 * remote system.
	 * 
	 * @param workDir
	 *            working directory
	 */
	public void setWorkDir(String workDir) {
		fWorkDir = workDir;
	}

	/**
	 * Launch the server. The payload is first copied to the working directory
	 * if it doesn't already exist. The server is then launched using the launch
	 * command.
	 * 
	 * @param monitor
	 *            progress monitor that can be used to cancel the launch
	 * @return true for successful launch
	 */
	public synchronized boolean startServer(IProgressMonitor monitor) {
		if (fRemoteConnection == null || fServerState == ServerState.RUNNING) {
			return false;
		}
		if (fServerState == ServerState.FINISHED) {
			if (!doRestartServer()) {
				return false;
			}
			setServerState(ServerState.STARTING);
		}
		if (!fRemoteConnection.isOpen()) {
			try {
				fRemoteConnection.open(null);
			} catch (RemoteConnectionException e) {
				e.printStackTrace();
				return false;
			}
		}
		schedule();
		while (!monitor.isCanceled() && getServerState() == ServerState.STARTING) {
			try {
				wait(100);
			} catch (InterruptedException e) {
				if (DebugUtil.SERVER_TRACING) {
					System.err.println("SERVER RUNNER: InterruptedException " + e.getLocalizedMessage()); //$NON-NLS-1$
				}
			}
		}
		if (monitor.isCanceled()) {
			terminateServer();
		}
		if (fServerState == ServerState.RUNNING) {
			if (!doStartServer()) {
				terminateServer();
				return false;
			}
			return true;
		}
		return false;
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
			/*
			 * First check if the remote file exists or is a different size to
			 * the local version and copy over if required.
			 */
			IRemoteFileManager fileManager = conn.getRemoteServices().getFileManager(conn);
			IFileStore directory = fileManager.getResource(getWorkingDir());
			if (!directory.fetchInfo(EFS.NONE, monitor).exists()) {
				return null;
			}
			IFileStore server = directory.getChild(getPayload());
			IFileInfo serverInfo = server.fetchInfo(EFS.NONE, monitor);
			IFileStore local = null;
			try {
				URL jarURL = FileLocator.find(fBundle, new Path(getPayload()), null);
				if (jarURL != null) {
					jarURL = FileLocator.toFileURL(jarURL);
					local = EFS.getStore(jarURL.toURI());
				}
			} catch (URISyntaxException e) {
				throw new IOException(e.getMessage());
			}
			if (local == null) {
				return null;
			}
			IFileInfo localInfo = local.fetchInfo(EFS.NONE, monitor);
			if (!serverInfo.exists() || serverInfo.getLength() != localInfo.getLength()) {
				local.copy(server, EFS.OVERWRITE, monitor);
			}

			/*
			 * Now launch the server.
			 */
			String launchCmd = RemoteVariableManager.getInstance().performStringSubstitution(getLaunchCommand());
			List<String> launchArgs = Arrays.asList(launchCmd.split(" ")); //$NON-NLS-1$
			IRemoteProcessBuilder builder = conn.getRemoteServices().getProcessBuilder(conn, launchArgs);
			builder.directory(directory);
			builder.environment().putAll(getEnv());
			return builder.start();
		} catch (CoreException e) {
			throw new IOException(e.getMessage());
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
	 */
	protected abstract void doFinishServer();

	/**
	 * Called if the server is restarted
	 * 
	 * @return false if restart should be aborted
	 */
	protected abstract boolean doRestartServer();

	/**
	 * Called once the server starts
	 * 
	 * @return false if server should be aborted
	 */
	protected abstract boolean doStartServer();

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
		assert fLaunchCommand != null;
		assert fRemoteProcess == null;

		try {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			fRemoteProcess = launchServer(fRemoteConnection, monitor);

			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			if (fRemoteProcess == null) {
				setServerState(ServerState.FINISHED);
				return new Status(IStatus.ERROR, PTPRemoteCorePlugin.PLUGIN_ID, "failed to start server", null); //$NON-NLS-1$
			}

			final BufferedReader stdout = new BufferedReader(new InputStreamReader(fRemoteProcess.getInputStream()));
			new Thread(new Runnable() {
				public void run() {
					try {
						String output;
						while ((output = stdout.readLine()) != null) {
							if (getServerState() == ServerState.STARTING && doVerifyServerRunningFromStdout(output)) {
								setServerState(ServerState.RUNNING);
							}
							if (DebugUtil.SERVER_TRACING) {
								System.out.println("SERVER: " + output); //$NON-NLS-1$
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
						String output;
						while ((output = stderr.readLine()) != null) {
							if (getServerState() == ServerState.STARTING && doVerifyServerRunningFromStderr(output)) {
								setServerState(ServerState.RUNNING);
							}
							if (DebugUtil.SERVER_TRACING) {
								System.err.println("SERVER: " + output); //$NON-NLS-1$
							}
						}
					} catch (IOException e) {
						// Ignore
					}
				}
			}, "dstore server stderr").start(); //$NON-NLS-1$

			/*
			 * Wait while running but not canceled.
			 */
			while (!fRemoteProcess.isCompleted() && !monitor.isCanceled()) {
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

			setServerState(ServerState.FINISHED);

			/*
			 * Check if process terminated successfully (if not canceled).
			 */
			if (fRemoteProcess.exitValue() != 0) {
				if (!monitor.isCanceled()) {
					throw new CoreException(new Status(IStatus.ERROR, PTPRemoteCorePlugin.PLUGIN_ID, NLS.bind(
							"Server finished with exit code {0}", fRemoteProcess.exitValue()))); //$NON-NLS-1$
				}
			}
			return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
		} catch (CoreException e) {
			setServerState(ServerState.FINISHED);
			return e.getStatus();
		} catch (IOException e) {
			setServerState(ServerState.FINISHED);
			return new Status(IStatus.ERROR, PTPRemoteCorePlugin.PLUGIN_ID, "Failed to start server", e); //$NON-NLS-1$
		} finally {
			synchronized (this) {
				fRemoteProcess = null;
				doFinishServer();
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
