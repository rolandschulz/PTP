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
package org.eclipse.ptp.remote.rse.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;


public class RSEConnectionManager implements IRemoteConnectionManager {
	private IFileSystem fileSystem = null;
	
	private final ISystemRegistry registry;
	private final Map<String, RSEConnection> connections = new HashMap<String, RSEConnection>();
	
	public RSEConnectionManager(ISystemRegistry registry) {
		this.registry = registry;
		try {
			this.fileSystem = EFS.getFileSystem("rse"); //$NON-NLS-1$
		} catch (CoreException e) {
			// Could not find the rse filesystem!
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#getConnection(java.lang.String)
	 */
	public IRemoteConnection getConnection(String name) {
		refreshConnections();
		if (name != null) {
			return connections.get(name);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#getConnections()
	 */
	public IRemoteConnection[] getConnections() {
		refreshConnections();
		return connections.values().toArray(new IRemoteConnection[connections.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#newConnection(java.lang.String, java.util.Map)
	 */
	public IRemoteConnection newConnection(String name,
			Map<String, String> attributes) throws RemoteConnectionException {
		// TODO implement
		return null;
	}
	
	/**
	 * Check for new connections
	 */
	public void refreshConnections() {
		if (fileSystem != null) {
			Map<String, RSEConnection> newConns = new HashMap<String, RSEConnection>();
			IHost[] hosts = registry.getHostsBySubSystemConfigurationCategory("shells"); //$NON-NLS-1$
			for (IHost host : hosts) {
				RSEConnection conn = connections.get(host);
				if (conn == null) {
					conn = new RSEConnection(host, fileSystem);
					if (!conn.initialize()) {
						continue;
					}
				}
				newConns.put(host.getAliasName(), conn);
			}
			connections.clear();
			connections.putAll(newConns);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#removeConnection(org.eclipse.ptp.remote.core.IRemoteConnection)
	 */
	public void removeConnection(IRemoteConnection conn) {
		if (conn instanceof RSEConnection) {
			((RSEConnection)conn).dispose();
		}
		connections.remove(conn.getName());
	}
}
