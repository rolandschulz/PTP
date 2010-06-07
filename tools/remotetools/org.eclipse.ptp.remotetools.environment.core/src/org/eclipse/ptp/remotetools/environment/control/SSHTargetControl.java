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

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.remotetools.RemotetoolsPlugin;
import org.eclipse.ptp.remotetools.core.AuthToken;
import org.eclipse.ptp.remotetools.core.IRemoteConnection;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.KeyAuthToken;
import org.eclipse.ptp.remotetools.core.PasswdAuthToken;
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
	 * Set of jobs running on the remote target environment.
	 */
	private final Map<ITargetJob, TargetControlledJob> remoteJobs = new HashMap<ITargetJob, TargetControlledJob>();

	/**
	 * A connection (using ssh) to the remote target environment.
	 */
	private IRemoteConnection remoteConnection = null;

	/**
	 * Listeners notified when jobs ared added/removed from the target
	 * environment.
	 */
	private final Set<ITargetControlJobListener> jobListeners = new HashSet<ITargetControlJobListener>();

	/**
	 * Exception to terminate the environment.
	 */
	private CoreException pendingException = null;

	/**
	 * Return a map containing the available cipher types ids and their
	 * respective names
	 * 
	 * @return a Map object containing cipher type ids and cipher type names
	 */
	public static Map getCipherTypesMap() {
		// This method decouples the map from the RemotetoolsPlugin
		// so its safe to use it on the interface of each environment
		// that uses this plugin.

		HashMap cipherTypesMap = new HashMap(RemotetoolsPlugin.getCipherTypesMap());

		return cipherTypesMap;
	}

	/**
	 * A set of parameters used for authentication with SSH.
	 * 
	 * @author Daniel Felix Ferber
	 * @since 1.2
	 */
	protected class SSHParameters {
		public String hostname;
		public int port;
		public String user;
		public String password;
		public String keyPath;
		public String passphrase;
		public boolean isPasswdAuth;
		public String cipherType;
		public int timeout = 0;

		/**
		 * Constructor for password based authentication and assuming timeout =
		 * 0
		 * 
		 * @param hostname
		 * @param port
		 * @param user
		 * @param password
		 */
		public SSHParameters(String hostname, int port, String user, String password) {
			this(hostname, port, user, password, RemotetoolsPlugin.CIPHER_DEFAULT, 0);
		}

		/**
		 * Constructor for password based authentication
		 * 
		 * @param hostname
		 * @param port
		 * @param user
		 * @param password
		 * @param cipherType
		 *            Choose among the supported ciphers
		 * @param timeout
		 */
		public SSHParameters(String hostname, int port, String user, String password, String cipherType, int timeout) {
			super();
			this.hostname = hostname;
			this.port = port;
			this.user = user;
			this.password = password;
			this.timeout = timeout;
			this.cipherType = cipherType;
			isPasswdAuth = true;
		}

		/**
		 * Constructor for public/private key based authentication
		 * 
		 * @param hostname
		 * @param port
		 * @param user
		 * @param password
		 * @param cipherType
		 *            Choose among the supported ciphers
		 * @param timeout
		 */
		public SSHParameters(String hostname, int port, String user, String keyPath, String passphrase, String cipherType,
				int timeout) {
			super();
			this.hostname = hostname;
			this.port = port;
			this.user = user;
			this.keyPath = keyPath;
			this.passphrase = passphrase;
			this.timeout = timeout;
			this.cipherType = cipherType;
			isPasswdAuth = false;
		}

		/**
		 * Constructor for public/private key based authentication assuming
		 * timeout = 0
		 * 
		 * @param hostname
		 * @param port
		 * @param user
		 * @param password
		 */
		public SSHParameters(String hostname, int port, String user, String keyPath, String passphrase) {
			this(hostname, port, user, keyPath, passphrase, RemotetoolsPlugin.CIPHER_DEFAULT, 0);
		}
	}

	/**
	 * The parameters used by the connection to the remote target environment.
	 */
	SSHParameters sshParameters;

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

	protected synchronized void notifyException(CoreException e) {
		pendingException = e;
	}

	/**
	 * Add a new job listener.
	 */
	public synchronized void addJobListener(ITargetControlJobListener listener) {
		jobListeners.add(listener);
	}

	/**
	 * Remove a job listener.
	 */
	public synchronized void removeJobListener(ITargetControlJobListener listener) {
		jobListeners.remove(listener);
	}

	/**
	 * Create a waiting job on the
	 */
	public synchronized void startJob(ITargetJob job) throws CoreException {
		if ((query() != ITargetStatus.PAUSED) && (query() != ITargetStatus.RESUMED)) {
			throw new CoreException(new Status(IStatus.ERROR, getPluginId(), 0, Messages.SSHTargetControl_0, null));
		}
		TargetControlledJob controlledJob = new TargetControlledJob(this, job);
		remoteJobs.put(job, controlledJob);
		controlledJob.start();
	}

	protected synchronized void notifyStartingJob(ITargetJob job) {
		/** Notify listeners. */
		for (ITargetControlJobListener listener : jobListeners) {
			listener.beforeJobStart(job);
		}
		/** Guarantee that the target is operational. */
		try {
			resume(null);
		} catch (CoreException e) {
			// Ignore
		}
	}

	protected synchronized void notifyFinishedJob(ITargetJob job) {
		/** Notify listeners. */
		for (ITargetControlJobListener listener : jobListeners) {
			listener.afterJobFinish(job);
		}
		/** Remove job from list of jobs. */
		remoteJobs.remove(job);
	}

	/**
	 * Returns the number of jobs running on the remote host.
	 */
	public synchronized int getJobCount() {
		return remoteJobs.size();
	}

	/**
	 * Returns a list of jobs running on the remote target environment.
	 */
	public synchronized ITargetJob[] getJobs() {
		ITargetJob[] array = new ITargetJob[remoteJobs.size()];
		remoteJobs.keySet().toArray(array);
		return array;
	}

	/**
	 * Set the connection parameters to be used by
	 * {@link #create(IProgressMonitor)}.
	 * 
	 * @param parameters
	 */
	protected synchronized void setConnectionParameters(SSHParameters parameters) {
		this.sshParameters = parameters;
	}

	/**
	 * Create the remote target environment by opening a SSH connection to it.
	 * First,
	 * {@link #setConnectionParameters(org.eclipse.ptp.remotetools.environment.control.SSHTargetControl.SSHParameters)
	 * must be called.
	 */
	public boolean create(IProgressMonitor monitor) throws CoreException {
		Assert.isNotNull(sshParameters, "missing ssh parameters"); //$NON-NLS-1$
		try {
			if (monitor.isCanceled()) {
				disconnect();
				throw new CoreException(new Status(IStatus.CANCEL, getPluginId(), 0, Messages.SSHTargetControl_1, null));
			}
			kill(monitor);
			connect(monitor);
			if (monitor.isCanceled()) {
				disconnect();
				throw new CoreException(new Status(IStatus.CANCEL, getPluginId(), 0, Messages.SSHTargetControl_1, null));
			}
			return true;
		} catch (RemoteConnectionException e) {
			disconnect();
			String message = e.getMessage();
			Throwable t = e.getCause();
			if (t != null) {
				message += ": " + t.getMessage(); //$NON-NLS-1$
			}
			throw new CoreException(new Status(IStatus.ERROR, getPluginId(), message));
		}
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
		Assert.isNotNull(sshParameters, "missing ssh parameters"); //$NON-NLS-1$
		/*
		 * Try to connect, else undo the connection.
		 */
		AuthToken authToken;
		if (sshParameters.isPasswdAuth) {
			authToken = new PasswdAuthToken(sshParameters.user, sshParameters.password);
		} else {
			authToken = new KeyAuthToken(sshParameters.user, new File(sshParameters.keyPath), sshParameters.passphrase);
		}

		if (remoteConnection == null) {
			remoteConnection = RemotetoolsPlugin.createSSHConnection();
		}
		try {
			remoteConnection.connect(authToken, sshParameters.hostname, sshParameters.port, sshParameters.cipherType,
					sshParameters.timeout, monitor);
		} catch (RemoteConnectionException e) {
			disconnect();
			throw e;
		}
	}

	/**
	 * Returns the plugin ID.
	 * 
	 * @return
	 */
	protected abstract String getPluginId();

	/**
	 * Destroy the remote target environment, by closing the SSH connection to
	 * it.
	 */
	public boolean kill(IProgressMonitor monitor) throws CoreException {
		/*
		 * Try to gracefully terminate all running jobs. Might this fail, then
		 * guarantee to disconnect.
		 */
		try {
			terminateJobs(monitor);
		} finally {
			disconnect();
		}
		return true;
	}

	public boolean executeRemoteCommand(IProgressMonitor monitor, String command, String[] args) throws CoreException {
		if (remoteConnection == null) {
			throw new CoreException(new Status(IStatus.ERROR, getPluginId(), Messages.SSHTargetControl_5, null));
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
			throw new CoreException(new Status(IStatus.ERROR, getPluginId(), 0, Messages.SSHTargetControl_2, e));
		} catch (CancelException e) {
			throw new CoreException(new Status(IStatus.ERROR, getPluginId(), 0, Messages.SSHTargetControl_3, e));
		} catch (RemoteExecutionException e) {
			throw new CoreException(new Status(IStatus.ERROR, getPluginId(), 0, Messages.SSHTargetControl_4, e));
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

	/**
	 * Checks if the connection to the remote target environment is alive.
	 * 
	 * @return
	 */
	protected boolean isConnected() {
		return (remoteConnection != null) && (remoteConnection.isConnected());
	}

	protected synchronized void terminateJobs(IProgressMonitor monitor) {
		/*
		 * Issue each job to terminate gracefully.
		 */
		for (ITargetJob job : remoteJobs.keySet()) {
			TargetControlledJob controlledJob = remoteJobs.get(job);
			controlledJob.cancelExecution();
		}

		/*
		 * Wait until all jobs have terminated.
		 */
		while (getJobCount() > 0) {
			try {
				wait(500);
			} catch (InterruptedException e) {
				return;
			}
			if (monitor != null) {
				if (monitor.isCanceled()) {
					return;
				}
			}
		}
	}

	/**
	 * Guarantees that the connection to the remote host is closed and releases
	 * ressources allocated to the connection.
	 */
	protected synchronized void disconnect() {
		/*
		 * Any still running jobs will fail.
		 */
		remoteJobs.clear();
		if (remoteConnection != null) {
			remoteConnection.disconnect();
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

	public IRemoteConnection getConnection() {
		Assert.isTrue(false, "this method is not supported"); //$NON-NLS-1$
		return remoteConnection;
	}

}
