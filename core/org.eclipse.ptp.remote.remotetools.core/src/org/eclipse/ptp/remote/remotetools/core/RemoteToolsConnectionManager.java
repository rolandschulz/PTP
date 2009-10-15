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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.remotetools.core.environment.ConfigFactory;
import org.eclipse.ptp.remote.remotetools.core.environment.PTPTargetControl;
import org.eclipse.ptp.remote.remotetools.core.messages.Messages;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
import org.eclipse.ptp.remotetools.environment.core.ITargetElement;
import org.eclipse.ptp.remotetools.environment.core.TargetEnvironmentManager;
import org.eclipse.ptp.remotetools.environment.core.TargetTypeElement;


public class RemoteToolsConnectionManager implements IRemoteConnectionManager {
	private TargetTypeElement remoteHost = null;
	private Map<String, IRemoteConnection> connections = new HashMap<String, IRemoteConnection>();
	
	public RemoteToolsConnectionManager() {
		TargetEnvironmentManager targetMgr = EnvironmentPlugin.getDefault().getTargetsManager();
		for (Object obj : targetMgr.getTypeElements()) {
			TargetTypeElement element = (TargetTypeElement)obj;
			if (element.getName().equals("Remote Host")) { //$NON-NLS-1$
				remoteHost = element;
				break;
			}
		}
		refreshConnections();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#getConnection(java.lang.String)
	 */
	public IRemoteConnection getConnection(String name) {
		refreshConnections();
		return connections.get(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#getConnections()
	 */
	public IRemoteConnection[] getConnections() {
		refreshConnections();
		return connections.values().toArray(new IRemoteConnection[connections.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#removeConnection(org.eclipse.ptp.remote.core.IRemoteConnection)
	 */
	public void removeConnection(IRemoteConnection conn) {
		connections.remove(conn);
	}

	/**
	 * Refresh the list of connections that we know about. Deals with connection that are added or deleted
	 * by another entity.
	 */
	private void refreshConnections() {
		if (remoteHost != null) {
			Map<String, IRemoteConnection> newConns = new HashMap<String, IRemoteConnection>();
			for (Object obj : remoteHost.getElements()) {
				ITargetElement element = (ITargetElement)obj;
				IRemoteConnection conn = connections.get(element.getName());
				if (conn == null) {
					try {
						conn = createConnection(element);
					} catch (RemoteConnectionException e) {
					}
				}
				newConns.put(element.getName(), conn);
			}
			connections.clear();
			connections.putAll(newConns);
		}
	}
	
	/**
	 * Create a connection using information from the target element.
	 * 
	 * @param element target element
	 * @return new remote tools connection
	 * @throws RemoteConnectionException
	 */
	private IRemoteConnection createConnection(ITargetElement element) throws RemoteConnectionException {
		String address = (String) element.getAttributes().get(ConfigFactory.ATTR_CONNECTION_ADDRESS);
		String user = (String) element.getAttributes().get(ConfigFactory.ATTR_LOGIN_USERNAME);
		if (address == null) {
			throw new RemoteConnectionException(Messages.RemoteToolsConnectionManager_1);
		}
		if (user == null) {
			throw new RemoteConnectionException(Messages.RemoteToolsConnectionManager_2);
		}
		try {
			RemoteToolsConnection conn = new RemoteToolsConnection(element.getName(), address, user, (PTPTargetControl)element.getControl());
			((PTPTargetControl)element.getControl()).setConnection(conn);
			return conn;
		} catch (CoreException e) {
			throw new RemoteConnectionException(e.getMessage());
		}
	}
}
