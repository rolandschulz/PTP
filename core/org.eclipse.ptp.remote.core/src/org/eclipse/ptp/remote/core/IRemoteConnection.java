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
package org.eclipse.ptp.remote.core;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;

public interface IRemoteConnection {
	public final static String OS_NAME_PROPERTY = "os.name"; //$NON-NLS-1$
	public final static String OS_VERSION_PROPERTY = "os.version"; //$NON-NLS-1$
	public final static String OS_ARCH_PROPERTY = "os.arch"; //$NON-NLS-1$
	public final static String FILE_SERPARATOR_PROPERTY = "file.separator"; //$NON-NLS-1$
	public final static String PATH_SERPARATOR_PROPERTY = "path.separator"; //$NON-NLS-1$
	public final static String LINE_SERPARATOR_PROPERTY = "line.separator"; //$NON-NLS-1$
	/**
	 * @since 4.0
	 */
	public final static String USER_HOME_PROPERTY = "user.home"; //$NON-NLS-1$

	/**
	 * Register a listener that will be notified when this connection's status
	 * changes.
	 * 
	 * @param listener
	 */
	public void addConnectionChangeListener(IRemoteConnectionChangeListener listener);

	/**
	 * Close the connection. Must be called to terminate the connection.
	 */
	public void close();

	/**
	 * Forward local port localPort to remote port fwdPort on remote machine
	 * fwdAddress. If this IRemoteConnection is not to fwdAddress, the port will
	 * be routed via the connection machine to fwdAddress.
	 * 
	 * @param localPort
	 *            local port to forward
	 * @param fwdAddress
	 *            address of remote machine
	 * @param fwdPort
	 *            remote port on remote machine
	 * @throws RemoteConnectionException
	 */
	public void forwardLocalPort(int localPort, String fwdAddress, int fwdPort) throws RemoteConnectionException;

	/**
	 * Forward a local port to remote port fwdPort on remote machine fwdAddress.
	 * The local port is chosen dynamically and returned by the method. If this
	 * IRemoteConnection is not to fwdAddress, the port will be routed via the
	 * connection machine to fwdAddress.
	 * 
	 * @param fwdAddress
	 * @param fwdPort
	 * @param monitor
	 * @return local port number
	 * @throws RemoteConnectionException
	 */
	public int forwardLocalPort(String fwdAddress, int fwdPort, IProgressMonitor monitor) throws RemoteConnectionException;

	/**
	 * Forward remote port remotePort to port fwdPort on machine fwdAddress.
	 * When a connection is made to remotePort on the remote machine, it is
	 * forwarded via this IRemoteConnection to fwdPort on machine fwdAddress.
	 * 
	 * @param remotePort
	 *            remote port to forward
	 * @param fwdAddress
	 *            address of recipient machine
	 * @param fwdPort
	 *            port on recipient machine
	 * @throws RemoteConnectionException
	 */
	public void forwardRemotePort(int remotePort, String fwdAddress, int fwdPort) throws RemoteConnectionException;

	/**
	 * Forward a remote port to port fwdPort on remote machine fwdAddress. The
	 * remote port is chosen dynamically and returned by the method. When a
	 * connection is made to this port on the remote machine, it is forwarded
	 * via this IRemoteConnection to fwdPort on machine fwdAddress.
	 * 
	 * If fwdAddress is the empty string ("") then the fwdPort will be bound to
	 * any address on all interfaces. Note that this requires enabling the
	 * GatewayPort sshd option on some systems.
	 * 
	 * @param fwdAddress
	 * @param fwdPort
	 * @param monitor
	 * @return remote port number
	 * @throws RemoteConnectionException
	 */
	public int forwardRemotePort(String fwdAddress, int fwdPort, IProgressMonitor monitor) throws RemoteConnectionException;

	/**
	 * Gets the implementation dependent address for this connection
	 * 
	 * return address
	 */
	public String getAddress();

	/**
	 * Get the attributes for the connection. This includes everything needed to
	 * recreate the connection using the
	 * {@link IRemoteConnectionManager#newConnection(Map)} method.
	 * 
	 * NOTE: the attributes do not include any security related information
	 * (e.g. username, key, password, etc.)
	 * 
	 * @return a map containing the connection attributes
	 */
	public Map<String, String> getAttributes();

	/**
	 * Returns an unmodifiable string map view of the remote environment. The
	 * connection must be open prior to calling this method.
	 * 
	 * @return the remote environment
	 */
	public Map<String, String> getEnv();

	/**
	 * Returns the value of an environment variable. The connection must be open
	 * prior to calling this method.
	 * 
	 * @param name
	 *            name of the environment variable
	 * @return value of the environment variable or null if the variable is not
	 *         defined
	 */
	public String getEnv(String name);

	/**
	 * Get unique name for this connection.
	 * 
	 * @return connection name
	 */
	public String getName();

	/**
	 * Gets the remote system property indicated by the specified key. The
	 * connection must be open prior to calling this method.
	 * 
	 * The following keys are supported:
	 * 
	 * <pre>
	 * os.name			Operating system name 
	 * os.arch			Operating system architecture
	 * os.version		Operating system version
	 * file.separator	File separator ("/" on UNIX)
	 * path.separator	Path separator (":" on UNIX)
	 * line.separator	Line separator ("\n" on UNIX)
	 * user.home		Home directory
	 * </pre>
	 * 
	 * @param key
	 *            the name of the property
	 * @return the string value of the property, or null if no property has that
	 *         key
	 */
	public String getProperty(String key);

	/**
	 * Get the remote services provider for this connection.
	 * 
	 * @return remote services provider
	 * @since 4.0
	 */
	public IRemoteServices getRemoteServices();

	/**
	 * Gets the username for this connection
	 * 
	 * return username
	 */
	public String getUsername();

	/**
	 * Get the working directory. Relative paths will be resolved using this
	 * path.
	 * 
	 * The remote connection does not need to be open to use this method,
	 * however a default directory path, rather than the actual working
	 * directory, may be returned in this case.
	 * 
	 * @return String representing the current working directory
	 * @since 4.0
	 */
	public String getWorkingDirectory();

	/**
	 * Test if the connection is open.
	 * 
	 * @return true if connection is open.
	 */
	public boolean isOpen();

	/**
	 * Open the connection. Must be called before the connection can be used.
	 * 
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts null, indicating that no progress
	 *            should be reported and that the operation cannot be cancelled.
	 * @throws RemoteConnectionException
	 */
	public void open(IProgressMonitor monitor) throws RemoteConnectionException;

	/**
	 * Remove a listener that will be notified when this connection's status
	 * changes.
	 * 
	 * @param listener
	 */
	public void removeConnectionChangeListener(IRemoteConnectionChangeListener listener);

	/**
	 * Set the address for this connection
	 * 
	 * @param address
	 */
	public void setAddress(String address);

	/**
	 * Set the name for this connection
	 * 
	 * @param name
	 */
	public void setName(String name);

	/**
	 * Set the username for this connection
	 * 
	 * @param username
	 */
	public void setUsername(String username);

	/**
	 * Set the working directory. Relative paths will be resolved using this
	 * path. The path must be valid and absolute for any changes to be made.
	 * 
	 * @param path
	 *            String representing the current working directory
	 * @since 4.0
	 */
	public void setWorkingDirectory(String path);

	/**
	 * Test if this connection supports forwarding of TCP connections
	 * 
	 * @return true if TCP port forwarding is supported
	 */
	public boolean supportsTCPPortForwarding();
}
