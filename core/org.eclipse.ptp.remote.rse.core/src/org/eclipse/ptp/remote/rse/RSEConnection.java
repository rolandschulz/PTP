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
import org.eclipse.rse.core.model.IHost;

public class RSEConnection implements IRemoteConnection {
	private IHost rseHost;
	
	public RSEConnection(IHost host) {
		rseHost = host;
	}
	
	/**
	 * Get RSE host object 
	 * 
	 * @return IHost
	 */
	public IHost getHost() {
		return rseHost;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#getName()
	 */
	public String getName() {
		return rseHost.getAliasName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#setHostname(java.lang.String)
	 */
	public void setHostname(String hostname) {
		rseHost.setHostName(hostname);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#setPassword(java.lang.String)
	 */
	public void setPassword(String password) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnection#setUsername(java.lang.String)
	 */
	public void setUsername(String username) {
		rseHost.setDefaultUserId(username);
	}
}
