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

import java.util.List;

public interface IRemoteServices extends IRemoteServicesDescriptor {
	/**
	 * Get a connection manager for managing remote connections. 
	 * 
	 * @return connection manager or null if services are not initialized
	 */
	public IRemoteConnectionManager getConnectionManager();
	
	/**
	 * Gets the directory separator on the target system.
	 * 
	 * @return String
	 */
	public String getDirectorySeparator(IRemoteConnection conn);
	
	/**
	 * Get a file manager for managing remote files
	 * 
	 * @param conn connection to use for managing files
	 * @return file manager or null if services are not initialized
	 */
	public IRemoteFileManager getFileManager(IRemoteConnection conn);
	
	/**
	 * Get a process builder for creating remote processes
	 * 
	 * @param conn connection to use for creating remote processes
	 * @return process builder or null if services are not initialized
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, List<String>command);
	
	/**
	 * Get a process builder for creating remote processes
	 * 
	 * @param conn connection to use for creating remote processes
	 * @return process builder or null if services are not initialized
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, String... command);
	
	/**
	 * Start initializing the remote service.
	 */
	public void initialize();
	
	/**
	 * Check if the remote service is initialized
	 * 
	 * @return true if successfully initialized, false otherwise
	 */
	public boolean isInitialized();
}
