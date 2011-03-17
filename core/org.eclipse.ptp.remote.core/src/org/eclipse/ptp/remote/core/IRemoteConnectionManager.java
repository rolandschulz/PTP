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

import java.net.URI;

import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;

public interface IRemoteConnectionManager {
	/**
	 * Gets the remote connection corresponding to the supplied name.
	 * 
	 * @param name
	 *            name of the connection (as returned by
	 *            {@link IRemoteConnection#getName()})
	 * @return remote connection or null if no connection exists
	 */
	public IRemoteConnection getConnection(String name);

	/**
	 * Gets the remote connection corresponding to the supplied URI.
	 * 
	 * @param uri
	 *            URI containing a schema for this remote connection
	 * @return remote connection or null if no connection exists or the schema
	 *         is incorrect
	 * @since 4.0
	 */
	public IRemoteConnection getConnection(URI uri);

	/**
	 * Get all the connections for this service provider.
	 * 
	 * @return connections that we know about
	 */
	public IRemoteConnection[] getConnections();

	/**
	 * Creates a new remote connection named with supplied name. The connection
	 * attributes will be the default for the implementation.
	 * 
	 * @param name
	 *            name of the connection
	 * @return a new connection or null if the creation failed for some reason
	 * @since 5.0
	 */
	public IRemoteConnection newConnection(String name) throws RemoteConnectionException;

	/**
	 * Remove a connection and all resources associated with it.
	 * 
	 * @param connection
	 *            connection to remove
	 */
	public void removeConnection(IRemoteConnection connection);
}
