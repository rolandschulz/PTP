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
package org.eclipse.ptp.remote.rse.core;

import java.util.List;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.IRemoteServicesDescriptor;
import org.eclipse.ptp.remote.rse.core.messages.Messages;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemRegistry;


public class RSEServices implements IRemoteServices {
	private ISystemRegistry registry = null;
	private IRemoteConnectionManager connMgr = null;
	
	private final IRemoteServicesDescriptor fDescriptor;
	
	public RSEServices(IRemoteServicesDescriptor descriptor) {
		fDescriptor = descriptor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#getId()
	 */
	public String getId() {
		return fDescriptor.getId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#getName()
	 */
	public String getName() {
		return fDescriptor.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#getScheme()
	 */
	public String getScheme() {
		return fDescriptor.getScheme();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteServicesDelegate#getConnectionManager()
	 */
	public IRemoteConnectionManager getConnectionManager() {
		if (!isInitialized()) {
			return null;
		}
		if (connMgr == null) {
			connMgr = new RSEConnectionManager(registry);
		}
		return connMgr;
	}

	public String getDirectorySeparator(IRemoteConnection conn) {
		if (!(conn instanceof RSEConnection)) {
			return null;
		}
		
		RSEConnection rseConnection = (RSEConnection) conn;
		IRSESystemType systemType = rseConnection.getHost().getSystemType();
		
		if(systemType.isWindows()) {
			return "\\"; //$NON-NLS-1$
		}
		
		else {
			return "/"; //$NON-NLS-1$
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteServicesDelegate#getFileManager(org.eclipse.ptp.remote.IRemoteConnection)
	 */
	public IRemoteFileManager getFileManager(IRemoteConnection conn) {
		if (!isInitialized()) {
			return null;
		}
		
		if (!(conn instanceof RSEConnection)) {
			return null;
		}
		return new RSEFileManager((RSEConnection)conn);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteServicesDelegate#getProcessBuilder(org.eclipse.ptp.remote.IRemoteConnection, java.util.List)
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, List<String>command) {
		if (!isInitialized()) {
			return null;
		}
		
		return new RSEProcessBuilder(conn, command);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteServicesDelegate#getProcessBuilder(org.eclipse.ptp.remote.IRemoteConnection, java.lang.String[])
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, String... command) {
		if (!isInitialized()) {
			return null;
		}
		
		return new RSEProcessBuilder(conn, command);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteServicesDelegate#initialize()
	 */
	public void initialize() {
		if (registry == null) {
			try {
				RSECorePlugin.waitForInitCompletion();
			} catch (InterruptedException e) {
				RSEAdapterCorePlugin.log(e);
			}
			if (RSECorePlugin.isTheSystemRegistryActive()) {
				registry = RSECorePlugin.getTheSystemRegistry();
			} else {
				RSEAdapterCorePlugin.log(Messages.RSEServices_0);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#isInitialized()
	 */
	public boolean isInitialized() {
		initialize();
		if (registry == null) {
			return false;
		}
		return true;
	}
}
