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
package org.eclipse.ptp.remote.internal.core;

import java.util.Map;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;


public class LocalConnectionManager implements IRemoteConnectionManager {
	private IRemoteConnection localConnection = new LocalConnection();

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#getConnection(java.lang.String)
	 */
	public IRemoteConnection getConnection(String name) {
		return localConnection;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#getConnections()
	 */
	public IRemoteConnection[] getConnections() {
		return new IRemoteConnection[]{localConnection};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#newConnection(java.lang.String, java.util.Map)
	 */
	public IRemoteConnection newConnection(String name,
			Map<String, String> attributes) throws RemoteConnectionException {
		return localConnection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#removeConnection(org.eclipse.ptp.remote.core.IRemoteConnection)
	 */
	public void removeConnection(IRemoteConnection connection) {
	}
}
