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
package org.eclipse.ptp.remote.remotetools;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteConnectionManager;
import org.eclipse.ptp.remote.remotetools.ui.ConfigFactory;
import org.eclipse.ptp.remote.remotetools.ui.ConfigurationDialog;
import org.eclipse.ptp.remotetools.RemotetoolsPlugin;
import org.eclipse.ptp.remotetools.core.AuthToken;
import org.eclipse.ptp.remotetools.utils.verification.ControlAttributes;
import org.eclipse.swt.widgets.Shell;


public class RemoteToolsConnectionManager implements IRemoteConnectionManager {
	private Map<String, IRemoteConnection> connections = new HashMap<String, IRemoteConnection>();
	
	public RemoteToolsConnectionManager() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#getConnection(java.lang.String)
	 */
	public IRemoteConnection getConnection(String name) {
		return connections.get(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#getConnections()
	 */
	public IRemoteConnection[] getConnections() {
		return connections.values().toArray(new IRemoteConnection[connections.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#newConnection()
	 */
	public void newConnection(Shell shell) {
		ConfigurationDialog dialog = new ConfigurationDialog(shell);
		dialog.open();
		
		ControlAttributes attributes = dialog.getAttributes();
		
		String host = attributes.getString(ConfigFactory.ATTR_CONNECTION_ADDRESS);
		AuthToken auth = dialog.getAuthToken();
		
		org.eclipse.ptp.remotetools.core.IRemoteConnection conn = 
			RemotetoolsPlugin.createSSHConnection(auth, host);
		
		connections.put(host, new RemoteToolsConnection(conn, host, auth.getUsername()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#supportsNewConnections()
	 */
	public boolean supportsNewConnections() {
		return true;
	}
}
