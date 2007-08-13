/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote.rse;

import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.exception.UnableToForwardPortException;
import org.eclipse.rse.core.model.IHost;

public class RSEConnection implements IRemoteConnection {
	private IHost rseHost;

	public RSEConnection(IHost host) {
		rseHost = host;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardLocalTCPPort(int, java.lang.String, int)
	 */
	public void forwardLocalTCPPort(int localPort, String fwdAddress,
			int fwdPort) throws RemoteConnectionException {
		throw new UnableToForwardPortException("Port forwarding not supported");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#forwardRemoteTCPPort(int, java.lang.String, int)
	 */
	public void forwardRemoteTCPPort(int remotePort, String fwdAddress,
			int fwdPort) throws RemoteConnectionException {
		throw new UnableToForwardPortException("Port forwarding not supported");
	}
	
	/**
	 * Get RSE host object 
	 * 
	 * @return IHost
	 */
	public IHost getHost() {
		return rseHost;
	}
	
	public String getHostname() {
		return rseHost.getHostName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#getName()
	 */
	public String getName() {
		return rseHost.getAliasName();
	}
	
	public String getUsername() {
		return rseHost.getDefaultUserId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#setHostname(java.lang.String)
	 */
	public void setHostname(String hostname) {
		rseHost.setHostName(hostname);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#setUsername(java.lang.String)
	 */
	public void setUsername(String username) {
		rseHost.setDefaultUserId(username);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#supportsTCPPortForwarding()
	 */
	public boolean supportsTCPPortForwarding() {
		return false;
	}
}
