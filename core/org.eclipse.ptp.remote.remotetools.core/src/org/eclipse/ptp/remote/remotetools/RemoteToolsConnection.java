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
package org.eclipse.ptp.remote.remotetools;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.exception.UnableToForwardPortException;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;

public class RemoteToolsConnection implements IRemoteConnection {
	private org.eclipse.ptp.remotetools.core.IRemoteConnection conn;
	private String hostName;
	private String userName;

	private IRemoteExecutionManager exeMgr = null;

	public RemoteToolsConnection(org.eclipse.ptp.remotetools.core.IRemoteConnection conn, String hostName, String userName) {
		this.conn = conn;
		this.hostName = hostName;
		this.userName = userName;
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
	 * @return
	 * @throws org.eclipse.ptp.remotetools.exception.RemoteConnectionException 
	 */
	public IRemoteExecutionManager getExecutionManager() throws org.eclipse.ptp.remotetools.exception.RemoteConnectionException {
		if (!conn.isConnected()) {
			conn.connect();
		}
		if (exeMgr == null) {
			exeMgr = conn.createRemoteExecutionManager();
		}
		return exeMgr;
	}

	public String getHostname() {
		return hostName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#getName()
	 */
	public String getName() {
		return userName + "@" + hostName;
	}
	
	public String getUsername() {
		return userName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#setHostname(java.lang.String)
	 */
	public void setHostname(String hostName) {
		this.hostName = hostName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#setUsername(java.lang.String)
	 */
	public void setUsername(String userName) {
		this.userName = userName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#supportsTCPPortForwarding()
	 */
	public boolean supportsTCPPortForwarding() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#toPath(java.net.URI)
	 */
	public IPath toPath(URI uri) {
		return new Path(uri.getPath());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#toURI(org.eclipse.core.runtime.IPath)
	 */
	public URI toURI(IPath path) {
		try {
			return new URI("remotetools", getHostname(), path.toPortableString(), null); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			return null;
		}
	}
}
