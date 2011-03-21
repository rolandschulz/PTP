/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.core.rservices;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;

/**
 * Manages existing SMOA connections
 */
public class SMOAConnectionManager implements IRemoteConnectionManager {

	/** Parent Remote Services */
	private final SMOARemoteServices remoteServices;

	/** Map keeping connection under it's name */
	private final Map<String, SMOAConnection> connections = new HashMap<String, SMOAConnection>();

	public SMOAConnectionManager(SMOARemoteServices remoteServices) {
		this.remoteServices = remoteServices;
	}

	public SMOAConnection getConnection(String name) {
		return connections.get(name);
	}

	public SMOAConnection getConnection(URI uri) {
		return getConnection(uri.toString());
	}

	/**
	 * In order to prevent using this Remote Services in other RM's, the
	 * official connection list is always empty
	 */
	public SMOAConnection[] getConnections() {
		// return connections.values().toArray(new
		// SMOAConnection[connections.size()]);
		return new SMOAConnection[0];
	}

	/**
	 * Retrieves all successful connections
	 */
	public Map<String, SMOAConnection> getOpenConnections() {
		final Map<String, SMOAConnection> map = new TreeMap<String, SMOAConnection>();
		for (final Entry<String, SMOAConnection> entry : connections.entrySet()) {
			if (entry.getValue().isOpen()) {
				map.put(entry.getKey(), entry.getValue());
			}
		}
		return map;
	}

	/**
	 * Creates a connection, does not open it.
	 */
	public SMOAConnection newConnection(String name) throws RemoteConnectionException {

		if (connections.containsKey(name)) {
			throw new RemoteConnectionException(Messages.SMOAConnectionManager_DuplicatedConnection);
		}

		final SMOAConnection connection = new SMOAConnection(remoteServices, name);

		connections.put(name, connection);

		return connection;
	}

	public void removeConnection(IRemoteConnection connection) {
		connection.close();
		connections.remove(connection.getName());
	}

}
