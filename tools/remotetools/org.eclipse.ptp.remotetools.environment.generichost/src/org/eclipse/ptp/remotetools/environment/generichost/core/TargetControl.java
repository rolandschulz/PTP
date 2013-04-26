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
package org.eclipse.ptp.remotetools.environment.generichost.core;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.remotetools.core.IAuthInfo;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.environment.control.ITargetConfig;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.control.ITargetStatus;
import org.eclipse.ptp.remotetools.environment.control.SSHTargetControl;
import org.eclipse.ptp.remotetools.environment.generichost.Activator;
import org.eclipse.ptp.remotetools.environment.generichost.messages.Messages;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.internal.common.Debug;

/**
 * Controls an instance of a target created from the Environment.
 * 
 * @author Daniel Felix Ferber
 * @since 1.4
 */
public class TargetControl extends SSHTargetControl implements ITargetControl {
	/**
	 * Configuration provided to the target control.
	 */
	private final ITargetConfig fTargetConfig;

	/**
	 * BackReference to the target element
	 */
	private IRemoteExecutionManager executionManager;

	/**
	 * Current connection state.
	 */
	private int state;

	private static final int NOT_OPERATIONAL = 1;
	private static final int CONNECTING = 2;
	private static final int CONNECTED = 3;
	private static final int DISCONNECTING = 4;

	private final IAuthInfo fAuthInfo;

	/**
	 * Creates a target control.
	 * 
	 * @param attributes
	 *            Configuration attributes
	 * @param element
	 *            Name for the target (displayed in GUI)
	 * @throws CoreException
	 *             Some attribute is not valid
	 */
	public TargetControl(ITargetConfig config, IAuthInfo authInfo) throws CoreException {
		super();
		state = NOT_OPERATIONAL;
		fTargetConfig = config;
		fAuthInfo = authInfo;
	}

	/**
	 * Connect to the remote target.. On every error or possible failure, an
	 * exception (CoreException) is thrown, whose (multi)status describe the
	 * error(s) that prevented creating the target control.
	 * 
	 * @param monitor
	 *            Progress indicator or <code>null</code>
	 * @return Always true.
	 * @throws CoreException
	 *             Some attribute is not valid, the simulator cannot be
	 *             launched, the ssh failed to connect.
	 */
	@Override
	public boolean create(IProgressMonitor monitor) throws CoreException {
		return create(fAuthInfo, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.environment.control.ITargetControl#create(org.eclipse.ptp.remotetools.core.IAuthInfo,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean create(IAuthInfo authInfo, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(Messages.TargetControl_create_MonitorConnecting, 1);

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		Debug.setDebug(store.getBoolean("logging")); //$NON-NLS-1$

		/*
		 * Connect to the remote temote target
		 */
		setConnectionParameters(fTargetConfig, authInfo);

		try {
			setState(CONNECTING);

			super.create(monitor);

			if (monitor.isCanceled()) {
				disconnect();
				setState(NOT_OPERATIONAL);
				monitor.done();
				return true;
			}

			setState(CONNECTED);

			monitor.worked(1);
		} catch (CoreException e) {
			disconnect();
			setState(NOT_OPERATIONAL);
			monitor.done();
			throw e;
		}
		try {
			executionManager = super.createRemoteExecutionManager();
		} catch (RemoteConnectionException e) {
			disconnect();
			setState(NOT_OPERATIONAL);
			throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), e.getMessage()));
		}
		monitor.done();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.environment.control.ITargetControl#
	 * createExecutionManager()
	 */
	public IRemoteExecutionManager createExecutionManager() throws RemoteConnectionException {
		if (!isConnected()) {
			throw new RemoteConnectionException(Messages.TargetControl_Connection_is_not_open);
		}
		return super.createRemoteExecutionManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.environment.control.SSHTargetControl#
	 * createTargetSocket(int)
	 */
	@Override
	public TargetSocket createTargetSocket(int port) {
		Assert.isTrue(isConnected());
		TargetSocket socket = new TargetSocket();
		socket.host = fTargetConfig.getConnectionAddress();
		socket.port = port;
		return socket;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.environment.control.ITargetControl#destroy()
	 */
	public void destroy() throws CoreException {
		disconnect();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.environment.control.ITargetControl#getConfig
	 * ()
	 */
	public ITargetConfig getConfig() {
		return fTargetConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.environment.control.ITargetControl#
	 * getExecutionManager()
	 */
	public IRemoteExecutionManager getExecutionManager() {
		return executionManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.environment.control.SSHTargetControl#kill()
	 */
	@Override
	public void kill() throws CoreException {
		try {
			setState(DISCONNECTING);
			super.kill();
		} finally {
			setState(NOT_OPERATIONAL);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.environment.control.ITargetControl#query()
	 */
	public synchronized int query() {
		switch (state) {
		case NOT_OPERATIONAL:
			return ITargetStatus.STOPPED;
		case CONNECTING:
		case DISCONNECTING:
			return ITargetStatus.STARTED;
		case CONNECTED:
			if (isConnected()) {
				return ITargetStatus.RESUMED;
			} else {
				return ITargetStatus.STARTED;
			}
		default:
			return ITargetStatus.STOPPED;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.environment.control.ITargetControl#resume
	 * (org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean resume(IProgressMonitor monitor) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), 0,
				Messages.TargetControl_resume_CannotResume, null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.environment.control.ITargetControl#stop(org
	 * .eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean stop(IProgressMonitor monitor) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, Activator.getUniqueIdentifier(), 0,
				Messages.TargetControl_stop_CannotPause, null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.environment.control.ITargetControl#
	 * updateConfiguration()
	 */
	public void updateConfiguration() throws CoreException {
		// Nothing required
	}

	/**
	 * @param state
	 */
	private synchronized void setState(int state) {
		this.state = state;
	}
}
