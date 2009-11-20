/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.subsystems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dstore.core.client.ClientConnection;
import org.eclipse.dstore.core.client.ConnectionStatus;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.rdt.ui.UIPlugin;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;

public class DStoreServerRunner extends Job {
	public enum DStoreServerState {STARTING, RUNNING, FINISHED, ERROR}

	private boolean DEBUG = true;
	
	private List<String> fCommand;

	private String workDir = null;
	private Map<String, String> fEnv = new HashMap<String, String>();
	private DStoreServerState fServerState = DStoreServerState.STARTING;
	private IRemoteServices fRemoteServices;

	private IRemoteConnection fRemoteConnection;
	private IRemoteProcess fRemoteProcess;
	private ClientConnection fDStoreConnection = null;
	private int fPort;
	private static final String fSuccessString = "Server Started Successfully"; //$NON-NLS-1$

	public DStoreServerRunner(IRemoteServices services, IRemoteConnection connection) {
		super("DStoreServerRunner"); //$NON-NLS-1$
		setPriority(Job.LONG);
		setSystem(!DEBUG);
		fRemoteServices = services;
		fRemoteConnection = connection;
	}

	public DataStore getDataStore() {
		if (fDStoreConnection == null) {
			fDStoreConnection = new ClientConnection(fRemoteConnection.getName());
		}
		return fDStoreConnection.getDataStore();
	}

	public synchronized DStoreServerState getServerState() {
		return fServerState;
	}

	public void setCommand(String command) {
		if (command != null) {
			fCommand = Arrays.asList(command.split(" ")); //$NON-NLS-1$
		}
	}

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

	public void setWorkDir(String workDir) {
		this.workDir = workDir;
	}
	
	public synchronized boolean startServer() {
//		if (fServerState == DStoreServerState.RUNNING ||
//				fServerState == DStoreServerState.ERROR) {
		if (fServerState == DStoreServerState.RUNNING) {
			return false;
		}
		if (fServerState == DStoreServerState.FINISHED) {
			setServerState(DStoreServerState.STARTING);
			fPort = 0;
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
		while (getServerState() == DStoreServerState.STARTING) {
			try {
				wait(500);
			} catch (InterruptedException e) {
				if (DStoreServerDefaults.DSTORE_TRACING) {
					System.err.println("DSTORE SERVER: InterruptedException " + e.getLocalizedMessage()); //$NON-NLS-1$
				}
				return false;
			}
		}
		if (fServerState == DStoreServerState.RUNNING) {
			int port;
			try {
				port = fRemoteConnection.forwardLocalPort("localhost", fPort, null); //$NON-NLS-1$
			} catch (RemoteConnectionException e) {
				if (DStoreServerDefaults.DSTORE_TRACING) {
					System.err.println("DSTORE SERVER: port fowarding failed " + e.getLocalizedMessage()); //$NON-NLS-1$
				}
				return false;
			}
			fDStoreConnection.setHost("localhost"); //$NON-NLS-1$
			fDStoreConnection.setPort(Integer.toString(port));
			ConnectionStatus status = fDStoreConnection.connect(null, 0);
			DataStore dataStore = fDStoreConnection.getDataStore();
			dataStore.showTicket(null);
			dataStore.registerLocalClassLoader(getClass().getClassLoader());
			return status.isConnected();
		}
		return false;
	}
	
	@Override
	protected void canceling() {
		terminateServer();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		assert fCommand != null;
		assert fRemoteProcess == null;

		/*
		 * Catch all try...catch
		 */
		try {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			/*
			 * Prepare remote connection.
			 */
			IRemoteFileManager fileManager = fRemoteServices.getFileManager(fRemoteConnection);

			IFileStore directory = null;
			if (workDir != null) {
				directory = fileManager.getResource(workDir);
			}
			IRemoteProcessBuilder builder = fRemoteServices.getProcessBuilder(fRemoteConnection, fCommand);
			if (directory != null) {
				builder.directory(directory);
			}
			
			builder.environment().putAll(fEnv);

			/*
			 * Create process.
			 */
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			
			synchronized (this) {
				fRemoteProcess = builder.start();
			}
			
			if (DStoreServerDefaults.DSTORE_TRACING) {
				final BufferedReader stdout = new BufferedReader(new InputStreamReader(fRemoteProcess.getInputStream()));
				new Thread(new Runnable() {
					public void run() {
						try {
							String output;
							while ((output = stdout.readLine()) != null) {
								System.out.println("DSTORE SERVER stdout: " + output); //$NON-NLS-1$
							}
						} catch (IOException e) {
							// Ignore
						}
					}
				}, "dstore server stdout").start();  //$NON-NLS-1$
			}

			final BufferedReader stderr = new BufferedReader(new InputStreamReader(fRemoteProcess.getErrorStream()));
			new Thread(new Runnable() {
				public void run() {
					try {
						String output;
						while ((output = stderr.readLine()) != null) {
							if (getServerState() == DStoreServerState.STARTING && output.startsWith(fSuccessString)) {
								if ((output = stderr.readLine()) != null && output.matches("^[0-9]+$")) { //$NON-NLS-1$
									fPort = Integer.parseInt(output);
									setServerState(DStoreServerState.RUNNING);
									if (DStoreServerDefaults.DSTORE_TRACING) {
										System.err.println("DSTORE SERVER started on port " + output); //$NON-NLS-1$
									}
								}
							}
							if (DStoreServerDefaults.DSTORE_TRACING) {
								System.err.println("DSTORE SERVER stderr: " + output);  //$NON-NLS-1$
							}
						}
					} catch (IOException e) {
						// Ignore
					}
				}
			}, "dstore server stderr").start();  //$NON-NLS-1$

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

			/*
			 * Check if process terminated successfully (if not canceled).
			 */
			if (fRemoteProcess.exitValue() != 0) {
				if (!monitor.isCanceled()) {
					throw new CoreException(new Status(IStatus.ERROR, UIPlugin.getPluginId(), NLS.bind("DStore server finished with exit code {0}", fRemoteProcess.exitValue()))); //$NON-NLS-1$
				}
			}
			setServerState(DStoreServerState.FINISHED);
			return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
		} catch (CoreException e) {
			setServerState(DStoreServerState.ERROR);
			return e.getStatus();
		} catch (IOException e) {
			setServerState(DStoreServerState.ERROR);
			return new Status(IStatus.ERROR, UIPlugin.getPluginId(), "failed to start dstore server", e); //$NON-NLS-1$
		} finally {
			synchronized (this) {
				fRemoteProcess = null;
				if (fDStoreConnection != null) {
					fDStoreConnection.disconnect();
					fDStoreConnection = null;
				}
			}
		}
	}
	
	protected synchronized void setServerState(DStoreServerState state) {
		if (DStoreServerDefaults.DSTORE_TRACING) {
			System.err.println("DSTORE SERVER new state: " + state.toString()); //$NON-NLS-1$
		}
		fServerState = state;
		this.notifyAll();
	}

	protected synchronized void terminateServer() {
		if (fServerState == DStoreServerState.RUNNING && fRemoteProcess != null) {
			fRemoteProcess.destroy();
		}
	}
}
