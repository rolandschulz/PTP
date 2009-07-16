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
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;


public class RSEConnectionManager implements IRemoteConnectionManager {
	private IFileSystem fileSystem = null;
	private ISystemRegistry registry;
	private Map<IHost, IRemoteConnection> connections = new HashMap<IHost,IRemoteConnection>();
	
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
		if (name != null) {
			for (IRemoteConnection conn : getConnections()) {
				IHost host = ((RSEConnection)conn).getHost();
				if (host.getName().equals(name)) {
					return conn;
				}
			}
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
	
	/**
	 * Check for new connections
	 */
	public void refreshConnections() {
		if (fileSystem != null) {
			IHost[] hosts = registry.getHostsBySubSystemConfigurationCategory("shells"); //$NON-NLS-1$
			for (IHost host : hosts) {
				if (!connections.containsKey(host)) {
					RSEConnection conn = new RSEConnection(host, fileSystem);
					if (conn.initialize()) {
						connections.put(host, conn);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#removeConnection(org.eclipse.ptp.remote.core.IRemoteConnection)
	 */
	public void removeConnection(IRemoteConnection conn) {
		if (conn instanceof RSEConnection) {
			((RSEConnection)conn).dispose();
		}
		connections.remove(conn);
	}
}
