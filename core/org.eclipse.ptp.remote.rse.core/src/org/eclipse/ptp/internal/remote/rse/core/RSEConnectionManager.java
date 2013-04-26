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
package org.eclipse.ptp.internal.remote.rse.core;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;

public class RSEConnectionManager implements IRemoteConnectionManager {
	private final ISystemRegistry fRegistry;
	private final IRemoteServices fRemoteServices;
	private final Map<String, RSEConnection> fConnections = new HashMap<String, RSEConnection>();

	/**
	 * @since 4.0
	 */
	public RSEConnectionManager(ISystemRegistry registry, IRemoteServices services) {
		fRegistry = registry;
		fRemoteServices = services;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.IRemoteConnectionManager#getConnection(java.lang
	 * .String)
	 */
	public IRemoteConnection getConnection(String name) {
		refreshConnections();
		if (name != null) {
			IRemoteConnection connection = fConnections.get(name);

			// RSE may have upper cased the name on us
			if (connection == null) {
				connection = fConnections.get(name.toLowerCase());
			}

			if (connection == null) {
				connection = fConnections.get(name.toUpperCase());
			}

			return connection;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnectionManager#getConnection(java
	 * .net.URI)
	 */
	/**
	 * @since 4.0
	 */
	public IRemoteConnection getConnection(URI uri) {
		/*
		 * See org.eclipse.rse.internal.efs.RSEFileSystem for definition
		 */
		String name = uri.getQuery();
		if (name == null) {
			name = uri.getHost();
		}
		return getConnection(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#getConnections()
	 */
	public IRemoteConnection[] getConnections() {
		refreshConnections();
		return fConnections.values().toArray(new IRemoteConnection[fConnections.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnectionManager#newConnection(java
	 * .lang.String)
	 */
	/**
	 * @since 5.0
	 */
	public IRemoteConnection newConnection(String name) throws RemoteConnectionException {
		// TODO implement
		return null;
	}

	/**
	 * Check for new fConnections
	 */
	public void refreshConnections() {
		Map<String, RSEConnection> newConns = new HashMap<String, RSEConnection>();
		IHost[] hosts = fRegistry.getHostsBySubSystemConfigurationCategory("shells"); //$NON-NLS-1$
		for (IHost host : hosts) {
			RSEConnection conn = fConnections.get(host);
			if (conn == null) {
				conn = new RSEConnection(host, fRemoteServices);
				if (!conn.initialize()) {
					continue;
				}
			}
			newConns.put(host.getAliasName(), conn);
		}
		fConnections.clear();
		fConnections.putAll(newConns);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteConnectionManager#removeConnection
	 * (org.eclipse.ptp.remote.core.IRemoteConnection)
	 */
	public void removeConnection(IRemoteConnection conn) {
		if (conn instanceof RSEConnection) {
			((RSEConnection) conn).dispose();
		}
		fConnections.remove(conn.getName());
	}
}
