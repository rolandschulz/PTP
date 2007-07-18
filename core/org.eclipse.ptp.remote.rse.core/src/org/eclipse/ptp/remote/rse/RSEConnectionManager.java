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

	private IRemoteConnection current = null;
	private SystemNewConnectionAction action;
	public RSEConnectionManager(ISystemRegistry registry) {
		this.registry = registry;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#getConnection()
	 */
	public IRemoteConnection getConnection() {
		return current;
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
		if (registry != null) {
			IHost[] hosts = registry.getHostsBySubSystemConfigurationCategory("shells"); //$NON-NLS-1$
			IRemoteConnection[] conns = new RSEConnection[hosts.length];
			for (int i = 0; i < hosts.length; i++) {
				conns[i] = new RSEConnection(hosts[i]);
			}
			return conns;
		}
		return new RSEConnection[0];
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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#supportsNewConnections()
	 */
	public boolean supportsNewConnections() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#setConnection(org.eclipse.ptp.remote.IRemoteConnection)
	 */
	public void setConnection(IRemoteConnection conn) {
		current = conn;
	}
}
