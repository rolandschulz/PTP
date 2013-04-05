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
package org.eclipse.ptp.rdt.ui.serviceproviders;

import java.net.URI;

import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.internal.rdt.ui.RSEUtils;
import org.eclipse.ptp.rdt.core.activator.Activator;
import org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider;
import org.eclipse.ptp.rdt.ui.messages.Messages;
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
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the RDT team.
 * 
 * @author crecoskie
 */
public class RemoteBuildServiceProvider extends ServiceProvider implements IRemoteExecutionServiceProvider {

	public static final String REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_PROVIDER_ID = "RemoteBuildServiceProvider.remoteToolsProviderID"; //$NON-NLS-1$
	public static final String REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_CONNECTION_NAME = "RemoteBuildServiceProvider.remoteToolsConnectionName"; //$NON-NLS-1$
	/**
	 * @since 2.0
	 */
	public static final String REMOTE_BUILD_SERVICE_PROVIDER_CONFIG_LOCATION = "RemoteBuildServiceProvider.configLocation"; //$NON-NLS-1$

	public static final String ID = "org.eclipse.ptp.rdt.ui.RemoteBuildServiceProvider"; //$NON-NLS-1$
	public static final String SERVICE_ID = "org.eclipse.ptp.rdt.core.BuildService"; //$NON-NLS-1$
	public static final String NAME = Messages.getString("RemoteBuildServiceProvider.0"); //$NON-NLS-1$

	private static String getDefaultPath(IRemoteServices remoteServices, IRemoteConnection connection) {
		if (remoteServices == null || connection == null) {
			return null;
		}
		// get the user's home directory
		String homeDir = connection.getProperty(IRemoteConnection.USER_HOME_PROPERTY);
		if (homeDir != null) {
			IFileStore homeStore = remoteServices.getFileManager(connection).getResource(homeDir);
			URI uri = homeStore.toURI();
			String pathString = EFSExtensionManager.getDefault().getPathFromURI(uri);
			IPath path = new Path(pathString);
			path = path.append(RSEUtils.DEFAULT_CONFIG_DIR_NAME);
			return path.toString();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider
	 * #getConfigLocation()
	 */
	/**
	 * @since 2.0
	 */
	public String getConfigLocation() {
		return getString(REMOTE_BUILD_SERVICE_PROVIDER_CONFIG_LOCATION, getDefaultPath(getRemoteServices(), getConnection()));
	}

	/**
	 * @since 2.0
	 */
	public void setConfigLocation(String configLocation) {
		putString(REMOTE_BUILD_SERVICE_PROVIDER_CONFIG_LOCATION, configLocation);
	}

	@Override
	public String getConfigurationString() {
		if (getRemoteServices() != null && isConfigured()) {
			return getRemoteServices().getName() + ": " + getRemoteConnectionName(); //$NON-NLS-1$
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider
	 * #getConnection()
	 */
	public IRemoteConnection getConnection() {
		IRemoteConnection conn = null;
		if (getRemoteConnectionName() != null) {
			IRemoteServices services = getRemoteServices();
			if (services != null) {
				IRemoteConnectionManager manager = services.getConnectionManager();
				if (manager != null) {
					conn = manager.getConnection(getRemoteConnectionName());

					if (conn != null && !conn.isOpen()) {
						try {
							conn.open(new NullProgressMonitor());
						} catch (RemoteConnectionException e) {
							Activator.log(e);
							return null;
						}
					}
				}
			}
		}
		return conn;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider
	 * #getRemoteServices()
	 */
	public IRemoteServices getRemoteServices() {
		return RemoteServices.getRemoteServices(getRemoteToolsProviderID());
	}

	/**
	 * Gets the ID of the Remote Tools provider that this provider uses for its
	 * execution services.
	 * 
	 * @return remote tools provider ID
	 */
	public String getRemoteToolsProviderID() {
		return getString(REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_PROVIDER_ID, null);
	}

	public boolean isConfigured() {
		return (getRemoteToolsProviderID() != null && getRemoteConnectionName() != null);
	}

	/**
	 * Sets the connection that this provider should use for its execution
	 * services.
	 * 
	 * @param connection
	 */
	public void setRemoteToolsConnection(IRemoteConnection connection) {
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
		return "RemoteBuildServiceProvider(" + getRemoteConnectionName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
