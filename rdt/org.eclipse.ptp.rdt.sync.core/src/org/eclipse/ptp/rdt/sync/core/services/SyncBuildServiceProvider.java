/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core.services;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.internal.rdt.sync.core.RDTSyncCorePlugin;
import org.eclipse.ptp.internal.rdt.sync.core.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.RemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.services.core.ServiceProvider;

/**
 * A build service provider that uses the Remote Tools API to provide execution
 * services.
 * 
 * @author crecoskie
 */
public class SyncBuildServiceProvider extends ServiceProvider {

	public static final String REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_PROVIDER_ID = "SyncBuildServiceProvider.remoteToolsProviderID"; //$NON-NLS-1$
	public static final String REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_CONNECTION_NAME = "SyncBuildServiceProvider.remoteToolsConnectionName"; //$NON-NLS-1$
	public static final String REMOTE_BUILD_SERVICE_PROVIDER_CONFIG_LOCATION = "SyncBuildServiceProvider.configLocation"; //$NON-NLS-1$

	public static final String ID = "org.eclipse.ptp.rdt.sync.core.BuildServiceProvider"; //$NON-NLS-1$
	public static final String NAME = Messages.SyncBuildServiceProvider_name;
	public static final String DEFAULT_CONFIG_DIR_NAME = Messages.SyncBuildServiceProvider_configDir;

	private static String getDefaultPath(IRemoteServices remoteServices, IRemoteConnection connection) {
		if (remoteServices == null || connection == null) {
			return null;
		}
		// get the user's home directory
		String homeDir = connection.getProperty(IRemoteConnection.USER_HOME_PROPERTY);
		if (homeDir != null) {
			IPath path = new Path(homeDir);
			path = path.append(DEFAULT_CONFIG_DIR_NAME);
			return path.toString();
		}
		return null;
	}

	private IRemoteConnection fRemoteConnection = null;

	/**
	 */
	public String getConfigLocation() {
		return getString(REMOTE_BUILD_SERVICE_PROVIDER_CONFIG_LOCATION, getDefaultPath(getRemoteServices(), getConnection()));
	}

	@Override
	public String getConfigurationString() {
		if (getRemoteServices() != null && isConfigured()) {
			return getRemoteServices().getName() + ": " + getRemoteConnectionName(); //$NON-NLS-1$
		}
		return null;
	}

	private IRemoteConnection getConnection() {
		if (fRemoteConnection == null && getRemoteConnectionName() != null) {
			IRemoteServices services = getRemoteServices();
			if (services != null) {
				IRemoteConnectionManager manager = services.getConnectionManager();
				if (manager != null) {
					fRemoteConnection = manager.getConnection(getRemoteConnectionName());

					if (fRemoteConnection != null && !fRemoteConnection.isOpen()) {
						try {
							fRemoteConnection.open(new NullProgressMonitor());
						} catch (RemoteConnectionException e) {
							RDTSyncCorePlugin.log(e);
							return null;
						}
					}
				}
			}
		}
		return fRemoteConnection;
	}

	/**
	 * Get the remote connection name
	 * 
	 * @return remote connection name or null if provider has not been
	 *         configured
	 */
	public String getRemoteConnectionName() {
		return getString(REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_CONNECTION_NAME, null);
	}

	private IRemoteServices getRemoteServices() {
		return RemoteServices.getRemoteServices(getRemoteToolsProviderID());
	}

	/**
	 * Gets the ID of the Remote Tools provider that this provider uses for its
	 * execution services.
	 * 
	 * @return remote tools provider ID
	 */
	private String getRemoteToolsProviderID() {
		return getString(REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_PROVIDER_ID, null);
	}

	@Override
	public boolean isConfigured() {
		return (getRemoteToolsProviderID() != null && getRemoteConnectionName() != null);
	}

	/**
	 */
	public void setConfigLocation(String configLocation) {
		putString(REMOTE_BUILD_SERVICE_PROVIDER_CONFIG_LOCATION, configLocation);
	}

	/**
	 * Sets the connection that this provider should use for its execution
	 * services.
	 * 
	 * @param connection
	 */
	public void setRemoteToolsConnection(IRemoteConnection connection) {
		fRemoteConnection = connection;
		String name = ""; //$NON-NLS-1$
		if (connection != null) {
			name = connection.getName();
		}
		putString(REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_CONNECTION_NAME, name);
		setRemoteToolsProviderID(connection.getRemoteServices().getId());
	}

	/**
	 * Sets the ID of the Remote Tools provider that this provider should use
	 * for its execution services.
	 * 
	 * @param id
	 */
	protected void setRemoteToolsProviderID(String id) {
		putString(REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_PROVIDER_ID, id);
	}

	@Override
	public String toString() {
		return "SyncBuildServiceProvider(" + getRemoteConnectionName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
