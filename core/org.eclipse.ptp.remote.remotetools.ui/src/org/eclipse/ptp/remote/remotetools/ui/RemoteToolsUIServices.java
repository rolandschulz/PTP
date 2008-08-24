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
package org.eclipse.ptp.remote.remotetools.ui;

import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServicesDelegate;


public class RemoteToolsUIServices implements IRemoteUIServicesDelegate {
	private static RemoteToolsUIServices instance = new RemoteToolsUIServices();
	private IRemoteServices services;

	/**
	 * Get shared instance of this class
	 * 
	 * @return instance
	 */
	public static RemoteToolsUIServices getInstance(IRemoteServices services) {
		instance.setServices(services);
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIServicesDelegate#getUIConnectionManager()
	 */
	public IRemoteUIConnectionManager getUIConnectionManager() {
		return new RemoteToolsUIConnectionManager(services);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIServicesDelegate#getUIFileManager()
	 */
	public IRemoteUIFileManager getUIFileManager() {
		return new RemoteToolsUIFileManager(services);
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
