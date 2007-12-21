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

import java.text.MessageFormat;

import org.eclipse.ptp.remotetools.core.IRemotePortForwarding;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.PortForwardingException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;

/**
 * Implementation of {@link IRemotePortForwarding} for SSH. Contains information
 * about from and to where the port forwarding was established. The information
 * is valid while the forwarding is active.
 * 
 * When creating the forwarding, the constructor assigns an execution manager as
 * owner of the port forwarding. When the port forwarding is turned off, then it
 * will is considered "inactive", it will have no execution manager anymore as
 * owner and the information won't be valid anymore.
 * The forwarding must be notified by that it was turned off by Remote Tools calling {@link #releaseOwner()}.
 * 
 * If the owner wasn't removed when turning off the connection, the an
 * application that stores a reference to the forwarding, would also indirectly
 * reference the execution manager, the connection and all data structure.
 * This would prevent garbage collection of connections that are not used anymore.
 * 
 * All methods have package visibility to allows internal access without getter/setters.
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class RemotePortForwarding implements IRemotePortForwarding {
	/**
	 * The execution manager that created and owns the port forwarding.
	 * When <code>null</code>, signals that the forwarding was turned off and that the data is not valid anymore.
	 */
	ExecutionManager owner;
	
	/** The port on the host where the remote port is forwarded to. */
	int localPort;
	/** The host where the remote port is forwarded to. */
	String localAddress;
	/** The remote port that is being forwarded. */
	int remotePort;
	
	/**
	 * Creates a remote port forwarding that is owned by the execution manager.
	 * <p>
	 * Used by the SSH implementation of Remote Tools.
	 * 
	 * @param owner
	 *            The execution manager that owns the port forwarding.
	 * @param localPort
	 *            The port on the host where the remote port is forwarded to.
	 * @param localAddress
	 *            The host where the remote port is forwarded to.
	 * @param remotePort
	 *            The remote port that is being forwarded.
	 */
	RemotePortForwarding(ExecutionManager owner, int localPort,
			String localAddress, int remotePort) {
		super();
		this.owner = owner;
		this.localPort = localPort;
		this.localAddress = localAddress;
		this.remotePort = remotePort;
	}

	/**
	 * Notifies that the forwarding was turned off.
	 * <p>
	 * Used by the SSH implementation of Remote Tools.
	 */
	void releaseOwner() {
		this.owner = null;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemotePortForwarding#getLocalPort()
	 */
	public int getLocalPort() {
		return localPort;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemotePortForwarding#getLocalAddress()
	 */
	public String getLocalAddress() {
		return localAddress;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemotePortForwarding#getRemotePort()
	 */
	public int getRemotePort() {
		return remotePort;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemotePortForwarding#release()
	 */
	public void release() throws RemoteConnectionException,
			PortForwardingException, CancelException {
		if (owner == null) {
			throw new PortForwardingException(PortForwardingException.REMOTE_FORWARDING_NOT_ATIVE);
		}
		owner.getPortForwardingTools().releaseForwarding(this);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return MessageFormat.format("remote port {0} to local {1}:{2}", new Object[] { new Integer(remotePort), localAddress, new Integer(localPort)});
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IPortForwarding#isActive()
	 */
	public boolean isActive() {
		return owner != null;
	}
}
