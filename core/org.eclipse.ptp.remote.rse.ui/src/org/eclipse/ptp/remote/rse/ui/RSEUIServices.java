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

import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;


public class RSEUIServices implements IRemoteUIServices {
	private static RSEUIServices fInstance = null;
	
	private final IRemoteServices fServices;

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIServicesDescriptor#getId()
	 */
	public String getId() {
		return fServices.getId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIServicesDescriptor#getName()
	 */
	public String getName() {
		return fServices.getName();
	}

	/**
	 * Get shared instance of this class
	 * 
	 * @return instance
	 */
	public static RSEUIServices getInstance(IRemoteServices services) {
		if (fInstance == null) {
			fInstance = new RSEUIServices(services);
		}
		return fInstance;
	}
	
	public RSEUIServices(IRemoteServices services) {
		fServices = services;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIServicesDescriptor#getUIConnectionManager()
	 */
	public IRemoteUIConnectionManager getUIConnectionManager() {
		return new RSEUIConnectionManager(fServices);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIServicesDescriptor#getUIFileManager()
	 */
	public IRemoteUIFileManager getUIFileManager() {
		return new RSEUIFileManager(fServices);
	}
}
