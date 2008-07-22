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


public interface IRemoteServicesDelegate {
	/**
	 * Get a connection manager for managing remote connections
	 * 
	 * @return connection manager
	 */
	public IRemoteConnectionManager getConnectionManager();
	
	/**
	 * Get a file manager for managing remote files
	 * 
	 * @param conn connection to use for managing files
	 * @return file manager
	 */
	public IRemoteFileManager getFileManager(IRemoteConnection conn);
	
	/**
	 * Get a process builder for creating remote processes
	 * 
	 * @param conn connection to use for creating remote processes
	 * @return process builder
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, List<String>command);
	
	/**
	 * Get a process builder for creating remote processes
	 * 
	 * @param conn connection to use for creating remote processes
	 * @return process builder
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, String... command);
	
	/**
	 * Initialize the remote service.
	 * 
	 * @return true if successfully initialized, false otherwise
	 */
	public boolean initialize();
}
