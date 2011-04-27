/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.environment.control;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.remotetools.RemotetoolsPlugin;
import org.eclipse.ptp.remotetools.core.IAuthInfo;
import org.eclipse.ptp.remotetools.core.IRemoteConnection;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
import org.eclipse.ptp.remotetools.environment.core.messages.Messages;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;

/**
 * Standard implementation of <code>IRemoteJobController</code>. Presumes a
 * target environment that accepts SSH connections.
 * 
 * @see ITargetJobController
 * @author Daniel Felix Ferber
 * @since 1.2
 */
public abstract class SSHTargetControl implements ITargetControl {
	/**
	 * Return a map containing the available cipher types ids and their
	 * respective names
	 * 
	 * @return a Map object containing cipher type ids and cipher type names
	 */
	public static Map<String, String> getCipherTypesMap() {
		// This method decouples the map from the RemotetoolsPlugin
		// so its safe to use it on the interface of each environment
		// that uses this plugin.

		HashMap<String, String> cipherTypesMap = new HashMap<String, String>(RemotetoolsPlugin.getCipherTypesMap());

		return cipherTypesMap;
	}

	/**
	 * A connection (using ssh) to the remote target environment.
	 */
	private IRemoteConnection remoteConnection = null;

	/**
	 * Exception to terminate the environment.
	 */
	private CoreException pendingException = null;

	/**
	 * The parameters used by the connection to the remote target environment.
	 * 
	 * @since 2.0
	 */
	private ITargetConfig fConfig = null;
	private IAuthInfo fAuthInfo = null;

	/**
	 * Create the remote target environment by opening a SSH connection to it.
	 * First,
	 * {@link #setConnectionParameters(org.eclipse.ptp.remotetools.environment.control.SSHTargetControl.SSHParameters)
	 * must be called.
	 */
	public boolean create(IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(fAuthInfo, "missing ssh parameters"); //$NON-NLS-1$
		try {
			if (monitor.isCanceled()) {
				disconnect();
				throw new CoreException(new Status(IStatus.CANCEL, EnvironmentPlugin.getUniqueIdentifier(), 0,
						Messages.SSHTargetControl_1, null));
			}
			kill();
			connect(monitor);
			if (monitor.isCanceled()) {
				disconnect();
				throw new CoreException(new Status(IStatus.CANCEL, EnvironmentPlugin.getUniqueIdentifier(), 0,
						Messages.SSHTargetControl_1, null));
			}
			return true;
		} catch (RemoteConnectionException e) {
			disconnect();
			String message = e.getMessage();
			Throwable t = e.getCause();
			if (t != null) {
				message += ": " + t.getMessage(); //$NON-NLS-1$
			}
			throw new CoreException(new Status(IStatus.ERROR, EnvironmentPlugin.getUniqueIdentifier(), message));
		}
	}

	/**
	 * Informs how to connect to a socket that is being listened on the remote
	 * target environment. If necessary, create a tunnel that forwards the
	 * remote port.
	 * 
	 * @return A {@link TargetSocket} object that encapsulates information where
	 *         to connect in order to connect to the remote tunnel.
	 */
	public TargetSocket createTargetSocket(int port) throws CoreException {
		/*
		 * Dummy implementation that assumes that the remote port is mapped to
		 * the same local port.
		 */
		TargetSocket test = new TargetSocket();
		test.host = "localhost"; //$NON-NLS-1$
		test.port = port;
		return test;
	}

	public boolean executeRemoteCommand(IProgressMonitor monitor, String command, String[] args) throws CoreException {
		if (remoteConnection == null) {
			throw new CoreException(new Status(IStatus.ERROR, EnvironmentPlugin.getUniqueIdentifier(), Messages.SSHTargetControl_5,
					null));
		}

		for (int i = 0; i < args.length; i++) {
			command += (" " + args[i]); //$NON-NLS-1$
		}

		try {
			IRemoteExecutionManager executionManager = remoteConnection.createRemoteExecutionManager();
			executionManager.getExecutionTools().executeWithExitValue(command);
			executionManager.close();
			return true;
		} catch (RemoteConnectionException e) {
			throw new CoreException(new Status(IStatus.ERROR, EnvironmentPlugin.getUniqueIdentifier(), 0,
					Messages.SSHTargetControl_2, e));
		} catch (CancelException e) {
			throw new CoreException(new Status(IStatus.ERROR, EnvironmentPlugin.getUniqueIdentifier(), 0,
					Messages.SSHTargetControl_3, e));
		} catch (RemoteExecutionException e) {
			throw new CoreException(new Status(IStatus.ERROR, EnvironmentPlugin.getUniqueIdentifier(), 0,
					Messages.SSHTargetControl_4, e));
		}
	}

	public IRemoteConnection getConnection() {
		Assert.isTrue(false, "this method is not supported"); //$NON-NLS-1$
		return remoteConnection;
	}

	/**
	 * Destroy the remote target environment, by closing the SSH connection to
	 * it.
	 * 
	 * @since 2.0
	 */
	public void kill() throws CoreException {
		disconnect();
	}

	/**
	 * Create the SSH connection to the remote target environment. First,
	 * {@link #setConnectionParameters(org.eclipse.ptp.remotetools.environment.control.SSHTargetControl.SSHParameters)}
	 * must be called.
	 * 
	 * @param monitor
	 *            progress monitor
	 * @throws RemoteConnectionException
	 */
	protected synchronized void connect(IProgressMonitor monitor) throws RemoteConnectionException {
		Assert.isNotNull(fAuthInfo, "missing ssh parameters"); //$NON-NLS-1$
		/*
		 * Try to connect, else undo the connection.
		 */

		if (remoteConnection == null) {
			remoteConnection = RemotetoolsPlugin.createSSHConnection();
		}
		try {
			remoteConnection.connect(fAuthInfo, fConfig.getConnectionAddress(), fConfig.getConnectionPort(),
					fConfig.getCipherType(), fConfig.getConnectionTimeout() * 1000, monitor);
		} catch (RemoteConnectionException e) {
			disconnect();
			throw e;
		}
	}

	/**
	 * Create a remote execution manager that may be used to do operations on
	 * the remote target environment.
	 * 
	 * @return
	 * @throws RemoteConnectionException
	 */
	protected IRemoteExecutionManager createRemoteExecutionManager() throws RemoteConnectionException {
		return remoteConnection.createRemoteExecutionManager();
	}

	/**
	 * Guarantees that the connection to the remote host is closed and releases
	 * ressources allocated to the connection.
	 */
	protected synchronized void disconnect() {
		if (remoteConnection != null) {
			remoteConnection.disconnect();
		}
	}

	/**
	 * Checks if the connection to the remote target environment is alive.
	 * 
	 * @return
	 */
	protected boolean isConnected() {
		return (remoteConnection != null) && (remoteConnection.isConnected());
	}

	protected synchronized void notifyException(CoreException e) {
		pendingException = e;
	}

	/**
	 * Set the connection parameters to be used by
	 * {@link #create(IProgressMonitor)}.
	 * 
	 * @param hostname
	 * @param port
	 * @param authInfo
	 * @param cipherType
	 * @param timeout
	 * @since 2.0
	 */
	protected synchronized void setConnectionParameters(ITargetConfig config, IAuthInfo authInfo) {
		fConfig = config;
		fAuthInfo = authInfo;
	}

	/**
	 * Facility method that raises a SimulatorException with the last error
	 * detected from and observing thread.
	 * 
	 * @throws SimulatorException
	 */
	protected synchronized void throwPendingException() throws CoreException {
		if (pendingException != null) {
			throw pendingException;
		}
	}

}
