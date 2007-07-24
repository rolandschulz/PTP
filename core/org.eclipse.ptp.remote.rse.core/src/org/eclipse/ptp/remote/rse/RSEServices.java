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

import java.util.List;

import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteConnectionManager;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.IRemoteServicesDelegate;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.ui.RSEUIPlugin;


public class RSEServices implements IRemoteServicesDelegate {
	private ISystemRegistry registry;
	private IRemoteConnectionManager connMgr;
	private IRemoteFileManager fileMgr;

	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, List<String>command) {
		return new RSEProcessBuilder(conn, command);
	}
	
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, String... command) {
		return new RSEProcessBuilder(conn, command);
	}
	
	public IRemoteConnectionManager getConnectionManager() {
		return connMgr;
	}
	
	public IRemoteFileManager getFileManager() {
		return fileMgr;
	}
	
	public boolean initialize() {
		if (!RSEUIPlugin.isTheSystemRegistryActive()) {
			return false;
		}
		
		registry = RSECorePlugin.getDefault().getSystemRegistry();
		if (registry == null) {
			return false;
		}
		
		int timeout = 0;
		
		while (!RSECorePlugin.getThePersistenceManager().isRestoreComplete() && timeout < 5) {
			System.out.println("waiting for restore...");
			try {
				Thread.sleep(1000);
				timeout++;
			} catch (InterruptedException e) {
				// Ignore
			}
		}
		
		if (timeout == 5) {
			return false;
		}

		connMgr = new RSEConnectionManager(registry);
		fileMgr = new RSEFileManager();
		return true;
	}
}
