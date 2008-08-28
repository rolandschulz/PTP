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



public interface IRemoteConnectionManager {	
	/**
	 * Register a listener that will be notified when a connection's status changes.
	 * 
	 * @param listener
	 */
	public void addConnectionChangeListener(IRemoteConnectionChangeListener listener);
	
	/**
	 * Notify all listeners when a connection's status changes.
	 * 
	 * @param event
	 */
	public void fireConnectionChangeEvent(IRemoteConnectionChangeEvent event);
	
	/**
	 * Find a remote connection given its name
	 * 
	 * @param name
	 * @return remote connection
	 */
	public IRemoteConnection getConnection(String name);

	/**
	 * Get known connections
	 * 
	 * @return connections that we know about
	 */
	public IRemoteConnection[] getConnections();
	
	/**
	 * Remove a connection and all resources associated with it
	 * 
	 * @param connection connection to remove
	 */
	public void removeConnection(IRemoteConnection connection);

	/**
	 * Remove a listener that will be notified when a connection's status changes.
	 * 
	 * @param listener
	 */
	public void removeConnectionChangeListener(IRemoteConnectionChangeListener listener);
}
