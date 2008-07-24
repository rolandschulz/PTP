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
package org.eclipse.ptp.remote.internal.core;

import java.util.List;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServicesDelegate;

public class LocalServices implements IRemoteServicesDelegate {
	public static final String LocalServicesId = "org.eclipse.ptp.remote.core.LocalServices";
	
	private IRemoteConnectionManager connMgr;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDelegate#getProcessBuilder(org.eclipse.ptp.remote.core.IRemoteConnection, java.util.List)
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, List<String>command) {
		return new LocalProcessBuilder(conn, command);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDelegate#getProcessBuilder(org.eclipse.ptp.remote.core.IRemoteConnection, java.lang.String[])
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, String... command) {
		return new LocalProcessBuilder(conn, command);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDelegate#getConnectionManager()
	 */
	public IRemoteConnectionManager getConnectionManager() {
		return connMgr;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDelegate#getFileManager(org.eclipse.ptp.remote.core.IRemoteConnection)
	 */
	public IRemoteFileManager getFileManager(IRemoteConnection conn) {
		if (!(conn instanceof LocalConnection)) {
			return null;
		}
		return new LocalFileManager();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDelegate#initialize()
	 */
	public boolean initialize() {
		connMgr = new LocalConnectionManager();
		return true;
	}
}
