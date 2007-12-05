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

import java.util.List;

import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteConnectionManager;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.IRemoteServicesDelegate;


public class RemoteToolsServices implements IRemoteServicesDelegate {
	private static RemoteToolsServices instance = new RemoteToolsServices();
	private static RemoteToolsConnectionManager connMgr = null;

	/**
	 * Get shared instance of this class
	 * 
	 * @return instance
	 */
	public static RemoteToolsServices getInstance() {
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteServicesDelegate#getConnectionManager()
	 */
	public IRemoteConnectionManager getConnectionManager() {
		return connMgr;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteServicesDelegate#getFileManager(org.eclipse.ptp.remote.IRemoteConnection)
	 */
	public IRemoteFileManager getFileManager(IRemoteConnection conn) {
		if (!(conn instanceof RemoteToolsConnection)) {
			return null;
		}
		return new RemoteToolsFileManager((RemoteToolsConnection)conn);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteServicesDelegate#getProcessBuilder(org.eclipse.ptp.remote.IRemoteConnection, java.util.List)
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, List<String>command) {
		return new RemoteToolsProcessBuilder((RemoteToolsConnection)conn, command);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteServicesDelegate#getProcessBuilder(org.eclipse.ptp.remote.IRemoteConnection, java.lang.String[])
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, String... command) {
		return new RemoteToolsProcessBuilder((RemoteToolsConnection)conn, command);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteServicesDelegate#initialize()
	 */
	public boolean initialize() {
		if (connMgr == null) {
			connMgr = new RemoteToolsConnectionManager();
		}
		return true;
	}
}
