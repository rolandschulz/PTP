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
package org.eclipse.ptp.remote.remotetools.core;

import java.util.List;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServicesDelegate;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;


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
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDelegate#getConnectionManager()
	 */
	public IRemoteConnectionManager getConnectionManager() {
		return connMgr;
	}
	
	public String getDirectorySeparator(IRemoteConnection conn) {
		// dunno if there is a way to do this for Remote Tools... just return the forward slash
		return "/"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDelegate#getFileManager(org.eclipse.ptp.remote.core.IRemoteConnection)
	 */
	public IRemoteFileManager getFileManager(IRemoteConnection conn) {
		if (!(conn instanceof RemoteToolsConnection)) {
			return null;
		}
		RemoteToolsConnection remConn = (RemoteToolsConnection)conn;
		IRemoteExecutionManager mgr;
		try {
			mgr = remConn.createExecutionManager();
		} catch (RemoteConnectionException e) {
			return null;
		}
		return new RemoteToolsFileManager(remConn, mgr);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDelegate#getProcessBuilder(org.eclipse.ptp.remote.core.IRemoteConnection, java.util.List)
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, List<String>command) {
		return new RemoteToolsProcessBuilder((RemoteToolsConnection)conn, command);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDelegate#getProcessBuilder(org.eclipse.ptp.remote.core.IRemoteConnection, java.lang.String[])
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, String... command) {
		return new RemoteToolsProcessBuilder((RemoteToolsConnection)conn, command);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDelegate#getServicesExtension(org.eclipse.ptp.remote.core.IRemoteConnection, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getServicesExtension(IRemoteConnection conn, Class extension) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDelegate#initialize()
	 */
	public void initialize() {
		if (connMgr == null) {
			connMgr = new RemoteToolsConnectionManager();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDelegate#isInitialized()
	 */
	public boolean isInitialized() {
		initialize();
		return connMgr != null;
	}
}
