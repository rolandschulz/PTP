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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.remote.exception.RemoteConnectionException;

public interface IRemoteConnection {
	/**
	 * Close the connection. Must be called to terminate the connection.
	 */
	public void close(IProgressMonitor monitor);
	
	/**
	 * Forward local port localPort to remote port fwdPort on remote machine fwdAddress. If this
	 * IRemoteConnection is not to fwdAddress, the port will be routed via the connection machine to
	 * fwdAddress. 
	 * 
	 * @param localPort local port to forward
	 * @param remoteAddress address of remote machine
	 * @param remotePort remote port on remote machine
	 * @throws RemoteConnectionException
	 */
	public void forwardLocalPort(int localPort, String fwdAddress, int fwdPort) throws RemoteConnectionException;

	/**
	 * Forward remote port remotePort to port fwdPort on machine fwdAddress. When a connection is made to remotePort
	 * on the remote machine, it is forwarded via this IRemoteConnection to fwdPort on machine fwdAddress.
	 * 
	 * @param remotePort remote port to forward
	 * @param fwdAddress address of recipient machine
	 * @param fwdPort port on recipient machine
	 * @throws RemoteConnectionException
	 */
	public void forwardRemotePort(int remotePort, String fwdAddress, int fwdPort) throws RemoteConnectionException;

	/**
	 * Forward a local port to remote port fwdPort on remote machine fwdAddress. The local port is chosen
	 * dynamically and returned by the method. If this IRemoteConnection is not to fwdAddress, the port will 
	 * be routed via the connection machine to fwdAddress. 
	 * 
	 * @param fwdAddress
	 * @param fwdPort
	 * @param monitor
	 * @return local port number
	 * @throws RemoteConnectionException
	 */
	public int forwardLocalPort(String fwdAddress, int fwdPort, IProgressMonitor monitor) throws RemoteConnectionException;
	
	/**
	 * Forward a remote port to port remotePort on remote machine fwdAddress. The remote port is chosen
	 * dynamically and returned by the method. When a connection is made to this port
	 * on the remote machine, it is forwarded via this IRemoteConnection to fwdPort on machine fwdAddress.
	 * 
	 * @param fwdAddress
	 * @param fwdPort
	 * @param monitor
	 * @return remote port number
	 * @throws RemoteConnectionException
	 */
	public int forwardRemotePort(String fwdAddress, int fwdPort, IProgressMonitor monitor) throws RemoteConnectionException;

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
	 * Test if the connection is open.
	 * 
	 * @return true if connection is open.
	 */
	public boolean isOpen();

	/**
	 * Open the connection. Must be called before the connection can be used.
	 * 
	 * @throws RemoteConnectionException
	 */
	public void open(IProgressMonitor monitor) throws RemoteConnectionException;
	
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
