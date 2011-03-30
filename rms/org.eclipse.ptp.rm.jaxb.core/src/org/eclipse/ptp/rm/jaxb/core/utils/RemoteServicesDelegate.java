/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.utils;

import java.net.URI;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;

public class RemoteServicesDelegate implements IJAXBNonNLSConstants {
	private final IRemoteServices remoteServices;
	private final IRemoteServices localServices;
	private final IRemoteConnectionManager remoteConnectionManager;
	private final IRemoteConnectionManager localConnectionManager;
	private final IRemoteConnection remoteConnection;
	private final IRemoteConnection localConnection;
	private final IRemoteFileManager remoteFileManager;
	private final IRemoteFileManager localFileManager;
	private final URI localHome;
	private final URI remoteHome;

	public RemoteServicesDelegate(String remoteServicesId, String remoteConnectionName) {
		localServices = PTPRemoteCorePlugin.getDefault().getDefaultServices();
		assert (localServices != null);
		localConnectionManager = localServices.getConnectionManager();
		assert (localConnectionManager != null);
		/*
		 * Since it's a local service, it doesn't matter which parameter is
		 * passed
		 */
		localConnection = localConnectionManager.getConnection(ZEROSTR);
		assert (localConnection != null);
		localFileManager = localServices.getFileManager(localConnection);
		assert (localFileManager != null);

		if (remoteServicesId != null) {
			remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(remoteServicesId, new NullProgressMonitor());
			assert (null != remoteServices);
			remoteConnectionManager = remoteServices.getConnectionManager();
			assert (null != remoteConnectionManager);
			remoteConnection = remoteConnectionManager.getConnection(remoteConnectionName);
			assert (null != remoteConnection);
			remoteFileManager = remoteServices.getFileManager(remoteConnection);
			assert (null != remoteFileManager);
		} else {
			remoteServices = localServices;
			remoteConnectionManager = localConnectionManager;
			remoteConnection = localConnection;
			remoteFileManager = localFileManager;
		}

		localHome = localFileManager.toURI(localConnection.getWorkingDirectory());
		remoteHome = remoteFileManager.toURI(remoteConnection.getWorkingDirectory());
	}

	public IRemoteConnection getLocalConnection() {
		return localConnection;
	}

	public IRemoteConnectionManager getLocalConnectionManager() {
		return localConnectionManager;
	}

	public IRemoteFileManager getLocalFileManager() {
		return localFileManager;
	}

	public URI getLocalHome() {
		return localHome;
	}

	public IRemoteServices getLocalServices() {
		return localServices;
	}

	public IRemoteConnection getRemoteConnection() {
		return remoteConnection;
	}

	public IRemoteConnectionManager getRemoteConnectionManager() {
		return remoteConnectionManager;
	}

	public IRemoteFileManager getRemoteFileManager() {
		return remoteFileManager;
	}

	public URI getRemoteHome() {
		return remoteHome;
	}

	public IRemoteServices getRemoteServices() {
		return remoteServices;
	}

}
