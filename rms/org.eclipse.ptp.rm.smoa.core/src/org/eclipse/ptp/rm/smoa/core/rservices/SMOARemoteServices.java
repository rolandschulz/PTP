/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.core.rservices;

import java.util.List;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;

/**
 * SMOARemoteServices provide services for managing connection and files, as
 * well as hold SMOA objects need for task submission.
 * 
 * But for file managing and creating remote processes one may supply any other
 * services, in this case SMOARemoteService will just serve as proxy.
 */
public class SMOARemoteServices implements IRemoteServices {

	private final String remoteServices_ID;
	private final String remoteServices_NAME;
	private final String remoteServices_SCHEME;

	private SMOAConnectionManager connectionManager = null;

	public SMOARemoteServices(String id, String name) {
		remoteServices_ID = id;
		remoteServices_NAME = name;
		remoteServices_SCHEME = "scheme"; //$NON-NLS-1$
	}

	public SMOAConnectionManager getConnectionManager() {
		if (connectionManager == null) {
			connectionManager = new SMOAConnectionManager(this);
		}
		return connectionManager;
	}

	public IRemoteFileManager getFileManager(IRemoteConnection conn) {
		if (!(conn instanceof SMOAConnection)) {
			throw new IllegalArgumentException("SMOA got a " //$NON-NLS-1$
					+ conn.getClass().getCanonicalName() + " connection"); //$NON-NLS-1$
		}
		final SMOAConnection c = (SMOAConnection) conn;

		if (!c.isOpen()) {
			return null;
		}

		final IRemoteConnection fileServices = c.getFileConnection();
		if (fileServices != null) {
			return fileServices.getRemoteServices()
					.getFileManager(fileServices);
		}

		IRemoteFileManager fileManager = c.getFileManager();

		if (fileManager == null) {
			fileManager = new SMOAFileManager(c);
			c.setFileManager(fileManager);
		}

		return fileManager;
	}

	public String getId() {
		return remoteServices_ID;
	}

	public String getName() {
		return remoteServices_NAME;
	}

	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn,
			List<String> command) {

		if (!(conn instanceof SMOAConnection)) {
			throw new IllegalArgumentException("SMOA got a " //$NON-NLS-1$
					+ conn.getClass().getCanonicalName() + " connection"); //$NON-NLS-1$
		}
		final SMOAConnection c = (SMOAConnection) conn;
		final IRemoteConnection fileServices = c.getFileConnection();
		if (fileServices != null) {
			return fileServices.getRemoteServices().getProcessBuilder(
					fileServices, command);
		}

		return null;
	}

	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn,
			String... command) {

		if (!(conn instanceof SMOAConnection)) {
			throw new IllegalArgumentException("SMOA got a " //$NON-NLS-1$
					+ conn.getClass().getCanonicalName() + " connection"); //$NON-NLS-1$
		}
		final SMOAConnection c = (SMOAConnection) conn;
		final IRemoteConnection fileServices = c.getFileConnection();
		if (fileServices != null) {
			return fileServices.getRemoteServices().getProcessBuilder(
					fileServices, command);
		}

		return null;
	}

	public String getScheme() {
		return remoteServices_SCHEME;
	}

	public void initialize() {
	}

	public boolean isInitialized() {
		return true;
	}

}
