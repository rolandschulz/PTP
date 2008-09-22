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
package org.eclipse.ptp.remote.internal.ui;

import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServicesDelegate;

public class LocalUIServices implements IRemoteUIServicesDelegate {
	private static LocalUIServices instance = new LocalUIServices();
	private static LocalUIFileManager fileMgr = null;

	/**
	 * Get shared instance of this class
	 * 
	 * @return instance
	 */
	public static LocalUIServices getInstance(IRemoteServices services) {
		fileMgr = new LocalUIFileManager();
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIServicesDelegate#getUIConnectionManager(org.eclipse.ptp.remote.core.IRemoteConnectionManager)
	 */
	public IRemoteUIConnectionManager getUIConnectionManager() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIServicesDelegate#getUIFileManager(org.eclipse.ptp.remote.core.IRemoteConnection)
	 */
	public IRemoteUIFileManager getUIFileManager() {
		return fileMgr;
	}
}
