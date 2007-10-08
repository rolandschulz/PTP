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
package org.eclipse.ptp.remote;

import java.net.URI;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ptp.remote.exception.RemoteConnectionException;

public interface IRemoteConnection {
	/**
	 * Forward local port localPort to remote port remotePort on remote machine fwdAddress. If this
	 * connection is not to fwdAddress, the port will be routed via the connection machine to
	 * fwdAddress. 
	 * 
	 * @param localPort local port to forward
	 * @param remoteAddress address of remote machine
	 * @param remotePort remote port on remote machine
	 * @throws RemoteConnectionException
	 */
	public void forwardLocalTCPPort(int localPort, String fwdAddress, int fwdPort) throws RemoteConnectionException;
	
	/**
	 * Forward remote port remotePort to port fwdPort on machine fwdAddress. If fwdAddress is not the
	 * local machine, the port will be routed via the local machine to fwdAddress.
	 * 
	 * @param remotePort remote port to forward
	 * @param fwdAddress address of recipient machine
	 * @param fwdPort port on recipient machine
	 * @throws RemoteConnectionException
	 */
	public void forwardRemoteTCPPort(int remotePort, String fwdAddress, int fwdPort) throws RemoteConnectionException;

	/**
	 * Gets the implementation dependent hostname for this connection
	 * 
	 * return hostname
	 */
	public String getHostname();

	/**
	 * Get unique name for this connection.
	 * 
	 * @return connection name
	 */
	public String getName();
	
	/**
	 * Gets the username for this connection
	 * 
	 * return username
	 */
	public String getUsername();
	
	/**
	 * @param hostname
	 */
	public void setHostname(String hostname);
	
	/**
	 * @param username
	 */
	public void setUsername(String username);

	/**
	 * Test if this connection supports forwarding of TCP connections
	 * 
	 * @return true if TCP port forwarding is supported
	 */
	public boolean supportsTCPPortForwarding();
	
	/**
	 * Convert URI to a remote path. This path is suitable for
	 * direct file operations <i>on the remote system</i>.
	 * 
	 * @return IPath representing the remote path
	 */
	public IPath toPath(URI uri);
	
	/**
	 * Convert remote path to equivalent URI. This URI is suitable
	 * for EFS operations <i>on the local system</i>.
	 * 
	 * @param path path on remote system
	 * @return URI representing path on remote system
	 */
	public URI toURI(IPath path);
}
