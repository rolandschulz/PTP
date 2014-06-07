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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.remote.core.AbstractRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;

public class RSEConnectionManager extends AbstractRemoteConnectionManager {
	private final ISystemRegistry fRegistry;
	private final Map<String, RSEConnection> fConnections = new HashMap<String, RSEConnection>();

	/**
	 * @since 4.0
	 */
	public RSEConnectionManager(ISystemRegistry registry, IRemoteServices services) {
		super(services);
		fRegistry = registry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.IRemoteConnectionManager#getConnection(java.lang
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
	 * org.eclipse.remote.core.IRemoteConnectionManager#getConnection(java
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
	 * @see org.eclipse.remote.IRemoteConnectionManager#getConnections()
	 */
	public List<IRemoteConnection> getConnections() {
		refreshConnections();
		return new ArrayList<IRemoteConnection>(fConnections.values());
	}

	/**
	 * @since 5.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionManager#newConnection(java.lang.String)
	 */
	public IRemoteConnectionWorkingCopy newConnection(String name) throws RemoteConnectionException {
		// Appears to only be possible through UI
		return null;
	}

	public IRemoteConnection createConnection(IHost host) {
		return new RSEConnection(host, getRemoteServices());
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
				conn = new RSEConnection(host, getRemoteServices());
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
	 * org.eclipse.remote.core.IRemoteConnectionManager#removeConnection
	 * (org.eclipse.remote.core.IRemoteConnection)
	 */
	public void removeConnection(IRemoteConnection conn) {
		if (conn instanceof RSEConnection) {
			((RSEConnection) conn).dispose();
		}
		fConnections.remove(conn.getName());
	}
}
