/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core;

import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
import org.eclipse.ptp.rdt.sync.core.handlers.IMissingConnectionHandler;
import org.eclipse.ptp.rdt.sync.core.services.ISynchronizeService;
import org.eclipse.ptp.rdt.sync.core.services.SynchronizeServiceRegistry;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.RemoteServices;

/**
 * Class to encapsulate information about syncing a project
 */
public class SyncConfig implements Comparable<SyncConfig> {
	/**
	 * Utility function to resolve a string based on path variables for a certain project. Unless string is in the form:
	 * ${path_variable:/remainder}, where "path_variable" is a path variable defined for the project, the original string
	 * is returned unchanged.
	 * 
	 * The Eclipse platform should provide a standard mechanism for doing this, but various combinations of URIUtil and
	 * PathVariableManager methods failed.
	 * 
	 * @param project
	 * @param path
	 * @return resolved string
	 */
	public static String resolveString(IProject project, String path) {
		// Check basic syntax
		if (!path.startsWith("${") || !path.endsWith("}")) { //$NON-NLS-1$ //$NON-NLS-2$
			return path;
		}

		String newPath = path.substring(2, path.length() - 1);

		// Extract variable's value
		String variable = newPath.split(":")[0]; //$NON-NLS-1$
		IPathVariableManager pvm = project.getPathVariableManager();
		String value = pvm.getURIValue(variable.toUpperCase()).toString();
		if (value == null) {
			return path;
		}

		// Build and return new path
		value = value.replaceFirst("file:", ""); //$NON-NLS-1$ //$NON-NLS-2$
		if (value.endsWith("/") || value.endsWith("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
			value = value.substring(0, path.length() - 1);
		}
		return newPath.replaceFirst(variable + ":*", value); //$NON-NLS-1$
	}

	private String fData;
	private String fConfigName;
	private String fSyncProviderId;
	private String fConnectionName;
	private String fRemoteServicesId;
	private String fLocation;
	private boolean fIsActive;
	private boolean fSyncOnPreBuild = true;
	private boolean fSyncOnPostBuild = true;
	private boolean fSyncOnSave = true;

	private IRemoteServices fRemoteServices;
	private IRemoteConnection fRemoteConnection;
	private ISynchronizeService fSyncService;

	/**
	 * Create a new sync configuration
	 * 
	 * @param configName
	 *            Name of this configuration
	 * @param syncProviderId
	 *            ID of sync provider
	 * @param conn
	 *            Remote connection to use - cannot be null
	 * @param location
	 *            Location (directory) on remote host
	 */
	public SyncConfig(String configName, String syncProviderId, IRemoteConnection conn, String location) {
		fConfigName = configName;
		fSyncProviderId = syncProviderId;
		fConnectionName = conn.getName();
		fRemoteServicesId = conn.getRemoteServices().getId();
		fLocation = location;
	}

	/**
	 * Create a new sync configuration
	 * 
	 * @param configName
	 *            Name of this configuration
	 * @param syncProviderId
	 *            ID of sync provider
	 * @param connName
	 *            Name of remote connection
	 * @param remoteServicesId
	 *            ID of remote service - must be a valid service
	 * @param location
	 *            Location (directory) on remote host
	 */
	public SyncConfig(String configName, String syncProviderId, String connName, String remoteServicesId, String location) {
		fConfigName = configName;
		fSyncProviderId = syncProviderId;
		fConnectionName = connName;
		fRemoteServicesId = remoteServicesId;
		fLocation = location;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SyncConfig config) {
		return getConfigName().compareTo(config.getConfigName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SyncConfig other = (SyncConfig) obj;
		if (!fConfigName.equals(other.fConfigName)) {
			return false;
		}
		return true;
	}

	/**
	 * @return config name
	 */
	public String getConfigName() {
		return fConfigName;
	}

	/**
	 * @return remote services ID
	 */
	public String getConnectionName() {
		return fConnectionName;
	}

	/**
	 * Get the data associated with this configuration
	 * 
	 * @return
	 */
	public String getData() {
		return fData;
	}

	/**
	 * Get the remote fLocation
	 * 
	 * @return fLocation
	 */
	public String getLocation() {
		return fLocation;
	}

	/**
	 * Get fLocation (directory), resolved in terms of the passed project
	 * 
	 * @param project
	 * @return fLocation
	 */
	public String getLocation(IProject project) {
		return resolveString(project, fLocation);
	}

	/**
	 * Get remote connection. If connection is missing, this function calls the missing-connection handler. Thus, after catching
	 * the exception, callers can assume user has already been notified and given an opportunity to define the connection. So
	 * callers only need to worry about recovering gracefully.
	 * 
	 * @return remote connection - never null
	 * 
	 * @throws MissingConnectionException
	 *             if no connection with the stored name exist. This can happen for various reasons:
	 *             1) The connection was renamed
	 *             2) The connection was deleted
	 *             3) The connection never existed, such as when a project is imported to a different workspace
	 */
	public IRemoteConnection getRemoteConnection() throws MissingConnectionException {
		if (fRemoteServices == null) {
			fRemoteServices = RemoteServices.getRemoteServices(fRemoteServicesId);
			fRemoteConnection = null;
		}

		if (fRemoteConnection == null) {
			fRemoteConnection = fRemoteServices.getConnectionManager().getConnection(fConnectionName);
			if (fRemoteConnection == null) {
				IMissingConnectionHandler mcHandler = SyncManager.getDefaultMissingConnectionHandler();
				if (mcHandler != null) {
					mcHandler.handle(fRemoteServices, fConnectionName);
					fRemoteConnection = fRemoteServices.getConnectionManager().getConnection(fConnectionName);
				}
			}
		}

		if (fRemoteConnection == null) {
			throw new MissingConnectionException(fConnectionName);
		}

		return fRemoteConnection;
	}

	/**
	 * @return remote services ID
	 */
	public String getRemoteServicesId() {
		return fRemoteServicesId;
	}

	/**
	 * @return
	 */
	public boolean isSyncOnPostBuild() {
		return fSyncOnPostBuild;
	}

	/**
	 * @return
	 */
	public boolean isSyncOnPreBuild() {
		return fSyncOnPreBuild;
	}

	/**
	 * @return
	 */
	public boolean isSyncOnSave() {
		return fSyncOnSave;
	}

	/**
	 * Get sync provider ID
	 * 
	 * @return sync provider ID
	 */
	public String getSyncProviderId() {
		return fSyncProviderId;
	}

	/**
	 * @return sync service
	 */
	public ISynchronizeService getSyncService() {
		if (fSyncService == null) {
			fSyncService = SynchronizeServiceRegistry.getSynchronizeServiceDescriptor(getSyncProviderId()).getService();
			if (fSyncService == null) {
				throw new RuntimeException("Unable to locate sync service"); //$NON-NLS-1$
			}
		}
		return fSyncService;
	}

	@Override
	public int hashCode() {
		return fConfigName.hashCode();
	}

	/**
	 * @return true if this config is active
	 */
	public boolean isActive() {
		return fIsActive;
	}

	/**
	 * Set this config as active for the project. Clients should not call this method directly. Use
	 * {@link SyncConfigManager#setActive(IProject, SyncConfig)} instead.
	 * 
	 * @param active
	 */
	public void setActive(boolean active) {
		fIsActive = active;
	}

	/**
	 * @param configName
	 */
	public void setConfigName(String configName) {
		fConfigName = configName;
	}

	/**
	 * @param connectionName
	 */
	public void setConnectionName(String connectionName) {
		fConnectionName = connectionName;
		fRemoteConnection = null;
	}

	/**
	 * Set the data associated with this configuration
	 * 
	 * @param data
	 */
	public void setData(String data) {
		fData = data;
	}

	/**
	 * @param location
	 */
	public void setLocation(String location) {
		fLocation = location;
	}

	/**
	 * @param remoteServicesId
	 */
	public void setRemoteServicesId(String remoteServicesId) {
		fRemoteServicesId = remoteServicesId;
		fRemoteServices = null;
	}

	/**
	 * @param syncOnPostBuild
	 */
	public void setSyncOnPostBuild(boolean syncOnPostBuild) {
		fSyncOnPostBuild = syncOnPostBuild;
	}

	/**
	 * @param syncOnPreBuild
	 */
	public void setSyncOnPreBuild(boolean syncOnPreBuild) {
		fSyncOnPreBuild = syncOnPreBuild;
	}

	/**
	 * @param syncOnSave
	 */
	public void setSyncOnSave(boolean syncOnSave) {
		fSyncOnSave = syncOnSave;
	}

	/**
	 * @param syncProvider
	 */
	public void setSyncProviderId(String syncProviderId) {
		fSyncProviderId = syncProviderId;
		fSyncService = null;
	}
}