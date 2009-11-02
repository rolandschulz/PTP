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

import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;



public interface IRemoteConnectionManager {	
	/**
	 * Gets the remote connection corresponding to the supplied name.
	 * 
	 * @param name name of the connection (as returned by {@link IRemoteConnection#getName()})
	 * @return remote connection or null if no connection exists
	 */
	public IRemoteConnection getConnection(String name);

	/**
	 * Get all the connections for this service provider.
	 * 
	 * @return connections that we know about
	 */
	public IRemoteConnection[] getConnections();
	
	/**
	 * Creates a remote connection using the supplied attributes. The attributes are specific
	 * to the remote service provider and can be obtained using {@link IRemoteConnection#getAttributes()}.
	 * 
	 * @param name name of the connection
	 * @param attributes map containing attributes used to create the connection
	 * @return a new connection or null if the creation failed for some reason
	 */
	public IRemoteConnection newConnection(String name, Map<String, String> attributes) throws RemoteConnectionException;
	
	/**
	 * Remove a connection and all resources associated with it.
	 * 
	 * @param connection connection to remove
	 */
	public void removeConnection(IRemoteConnection connection);
}
