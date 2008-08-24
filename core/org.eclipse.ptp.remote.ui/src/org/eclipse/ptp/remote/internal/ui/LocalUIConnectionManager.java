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

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.swt.widgets.Shell;


public class LocalUIConnectionManager implements IRemoteUIConnectionManager {
	private IRemoteServices services;
	
	public LocalUIConnectionManager(IRemoteServices services) {
		this.services = services;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#newConnection()
	 */
	public IRemoteConnection newConnection(Shell shell) {
		return services.getConnectionManager().getConnections()[0];
	}
}
