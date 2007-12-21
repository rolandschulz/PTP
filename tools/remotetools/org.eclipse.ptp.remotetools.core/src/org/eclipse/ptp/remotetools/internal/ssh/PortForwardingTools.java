/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remotetools.internal.ssh;

import java.util.HashSet;
import java.util.Hashtable;

import org.eclipse.ptp.remotetools.core.IPortForwarding;
import org.eclipse.ptp.remotetools.core.IRemotePortForwarding;
import org.eclipse.ptp.remotetools.core.IRemotePortForwardingTools;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.PortForwardingException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;

/**
 * Implementation of {@link IRemotePortForwardingTools} for SSH.
 * It simply redirects requests to the {@link RemotePortForwardingPool} managed by the {@link Connection}, but filtering the forwardings for the execution manager.
 * @author Daniel Felix Ferber
 */
public class PortForwardingTools implements IRemotePortForwardingTools {
	ExecutionManager executionManager;
	
	PortForwardingTools(ExecutionManager executionManager) {
		this.executionManager = executionManager;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemotePortForwardingTools#forwardRemotePort(int, int)
	 */
	public IRemotePortForwarding forwardRemotePort(int remotePort, int localPort)
			throws RemoteConnectionException, PortForwardingException, CancelException {
		this.executionManager.test();
		this.executionManager.testCancel();
		
		RemotePortForwarding forwarding = this.executionManager.connection.forwardingPool.createRemotePortForwarding(this.executionManager, remotePort,
						null, localPort);
		return forwarding;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemotePortForwardingTools#forwardRemotePort(int, java.lang.String, int)
	 */
	public IRemotePortForwarding forwardRemotePort(int remotePort,
			String localAddress, int localPort)
			throws RemoteConnectionException, PortForwardingException, CancelException {
		this.executionManager.test();
		this.executionManager.testCancel();

		RemotePortForwarding forwarding = this.executionManager.connection.forwardingPool
		.createRemotePortForwarding(this.executionManager, remotePort,
				localAddress, localPort);
		return forwarding;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemotePortForwardingTools#getRemotePortForwarding(int)
	 */
	public IRemotePortForwarding getRemotePortForwarding(int remotePort) throws RemoteConnectionException, PortForwardingException {
		this.executionManager.test();

		RemotePortForwarding forwarding = this.executionManager.connection.forwardingPool.getRemotePortForwarding(remotePort);
		
		/* Return null if there is no forwarding for the remote port. */
		if (forwarding == null) {
			return null;
		}

		/* Return null if there is forwarding for the remote port, but it does not belong the the ExecutionManager. */
		if (forwarding.owner != this.executionManager) {
			return null;
		}
		
		return forwarding;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemotePortForwardingTools#releaseForwarding(org.eclipse.ptp.remotetools.core.IPortForwarding)
	 */
	public void releaseForwarding(IPortForwarding forwarding)
			throws RemoteConnectionException, PortForwardingException {
		assert forwarding != null;
		this.executionManager.test();

		if (forwarding instanceof RemotePortForwarding) {
			this.executionManager.connection.forwardingPool.releaseRemotePortForwarding((RemotePortForwarding) forwarding, this.executionManager);
		} else {
			/* Forwarding is not a valid instance. */
			assert false;
		}
	}
}
