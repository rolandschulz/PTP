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
package org.eclipse.ptp.remote.rse.ui;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.rse.core.RSEConnection;
import org.eclipse.ptp.remote.rse.core.RSEServices;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServicesDelegate;


public class RSEUIServices extends RSEServices implements IRemoteUIServicesDelegate {
	private static RSEUIServices instance = new RSEUIServices();
	private IRemoteServices services;

	/**
	 * Get shared instance of this class
	 * 
	 * @return instance
	 */
	public static RSEUIServices getInstance(IRemoteServices services) {
		instance.setServices(services);
		return instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIServicesDelegate#getUIConnectionManager(org.eclipse.ptp.remote.core.IRemoteConnectionManager)
	 */
	public IRemoteUIConnectionManager getUIConnectionManager() {
		return new RSEUIConnectionManager(services);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIServicesDelegate#getUIFileManager(org.eclipse.ptp.remote.core.IRemoteConnection)
	 */
	public IRemoteUIFileManager getUIFileManager(IRemoteConnection connection) {
		return new RSEUIFileManager(services, (RSEConnection)connection);
	}
	
	/**
	 * Set remote services for this provider
	 * 
	 * @param services
	 */
	private void setServices(IRemoteServices services) {
		this.services = services;
	}
}
