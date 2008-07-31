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
package org.eclipse.ptp.remote.ui;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;


public interface IRemoteUIServicesDelegate {
	/**
	 * Get a UI file manager for managing remote files
	 * 
	 * @param fileMgr remote file manager
	 * @return UI file manager or null if no file manager operations are supported
	 */
	public IRemoteUIFileManager getUIFileManager(IRemoteConnection connection);
	
	/**
	 * Get a UI connection manager for managing connections
	 * 
	 * @return UI connection manager or null if no connection manager operations are supported
	 */
	public IRemoteUIConnectionManager getUIConnectionManager(IRemoteConnectionManager mgr);
}
