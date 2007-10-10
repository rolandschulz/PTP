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
package org.eclipse.ptp.remote.rse;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteConnectionManager;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.ui.actions.SystemNewConnectionAction;
import org.eclipse.swt.widgets.Shell;


public class RSEConnectionManager implements IRemoteConnectionManager {
	private ISystemRegistry registry;
	private SystemNewConnectionAction action;
	private Map<IHost, IRemoteConnection> connections = null;
	
	public RSEConnectionManager(ISystemRegistry registry) {
		this.registry = registry;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#getConnection(java.lang.String)
	 */
	public IRemoteConnection getConnection(String name) {
		for (IRemoteConnection conn : getConnections()) {
			IHost host = ((RSEConnection)conn).getHost();
			if (host.getName().equals(name)) {
				return conn;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#getConnections()
	 */
	public IRemoteConnection[] getConnections() {
		if (connections == null && registry != null) {
			IHost[] hosts = registry.getHostsBySubSystemConfigurationCategory("shells"); //$NON-NLS-1$
			connections = new HashMap<IHost,IRemoteConnection>();
			for (IHost host : hosts) {
				connections.put(host, new RSEConnection(host));
			}
		}
		return connections.values().toArray(new IRemoteConnection[connections.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#newConnection()
	 */
	public void newConnection(Shell shell) {
		if (action == null) {
 			action = new SystemNewConnectionAction(shell, false, false, null);
 	   		IRSESystemType systemType = RSECorePlugin.getTheCoreRegistry().getSystemTypeById(IRSESystemType.SYSTEMTYPE_SSH_ONLY_ID);
			if (systemType != null) {
 	   			action.restrictSystemTypes(new IRSESystemType[] { systemType });
			}
		}
    		
		try 
		{
			action.run();
		} catch (Exception e)
		{
			// Ignore
		}
		
		/*
		 * Check for new connections
		 */
		if (connections != null) {
			IHost[] hosts = registry.getHostsBySubSystemConfigurationCategory("shells"); //$NON-NLS-1$
			for (IHost host : hosts) {
				if (!connections.containsKey(host)) {
					connections.put(host, new RSEConnection(host));
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#supportsNewConnections()
	 */
	public boolean supportsNewConnections() {
		return true;
	}
}
