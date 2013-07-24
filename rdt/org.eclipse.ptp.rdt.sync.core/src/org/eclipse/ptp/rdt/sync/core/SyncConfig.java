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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
import org.eclipse.remote.core.IRemoteConnection;

/**
 * Class to encapsulate information about syncing a project
 * 
 * @since 3.0
 */
public class SyncConfig implements Comparable<SyncConfig> {


	private String fName;
	private String fSyncProviderId;
	private RemoteLocation remoteLocation;
	private IProject fProject;
	private boolean fSyncOnPreBuild = true;
	private boolean fSyncOnPostBuild = true;
	private boolean fSyncOnSave = true;
	private final Map<String, String> fProperties = new HashMap<String, String>();

	/**
	 * Create a new sync configuration. Should not be called by clients directly. Use
	 * {@link SyncConfigManager#newConfig(String, String, IRemoteConnection, String)} instead.
	 * 
	 * @param name
	 *            Name of this configuration. Must be unique per project.
	 */
	public SyncConfig(String name) {
		fName = name;
		remoteLocation = new RemoteLocation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SyncConfig config) {
		return getName().compareTo(config.getName());
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
		if (!fName.equals(other.fName)) {
			return false;
		}
		return true;
	}

	/**
	 * @return connection name
	 */
	public String getConnectionName() {
		return remoteLocation.getConnectionName();
	}

	/**
	 * Get the keys for all properties set for this configuration.
	 * 
	 * @return configuration properties keys
	 */
	public String[] getKeys() {
		return fProperties.keySet().toArray(new String[0]);
	}

	/**
	 * Get the raw remote location unresolved
	 * @return remote directory
	 */
	public String getLocation() {
		return remoteLocation.getDirectory();
	}

	/**
	 * Get location (directory), resolved in terms of the passed project
	 * TODO: Legacy code. It doesn't make sense to pass in a project different from the one stored. For now, add an assertion to
	 * see if this ever occurs.
	 * 
	 * @param project
	 * @return remote directory
	 */
	public String getLocation(IProject project) {
		assert fProject == project;
		return remoteLocation.getDirectory(project);
	}

	/**
	 * Get the configuration name
	 * 
	 * @return config name
	 */
	public String getName() {
		return fName;
	}

	/**
	 * Get the synchronized project
	 * 
	 * @return
	 */
	public IProject getProject() {
		return fProject;
	}

	/**
	 * Get an arbitrary property for the configuration
	 * 
	 * @param key
	 * @return value
	 */
	public String getProperty(String key) {
		return fProperties.get(key);
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
	 * @since 4.0
	 */
	public IRemoteConnection getRemoteConnection() throws MissingConnectionException {
		return remoteLocation.getConnection();
	}

	/**
	 * Get remote location
	 * @return remote location
	 * @since 4.0
	 */
	public RemoteLocation getRemoteLocation() {
		return remoteLocation;
	}

	/**
	 * Get the remote services ID
	 * @return remote services ID
	 */
	public String getRemoteServicesId() {
		return remoteLocation.getRemoteServicesId();
	}

	/**
	 * Get sync provider ID
	 * 
	 * @return sync provider ID
	 */
	public String getSyncProviderId() {
		return fSyncProviderId;
	}

	@Override
	public int hashCode() {
		return fName.hashCode();
	}

	/**
	 * Check if syncs should occur on post-build
	 * 
	 * @return
	 */
	public boolean isSyncOnPostBuild() {
		return fSyncOnPostBuild;
	}

	/**
	 * Check if syncs should occur on pre-build
	 * 
	 * @return
	 */
	public boolean isSyncOnPreBuild() {
		return fSyncOnPreBuild;
	}

	/**
	 * Check if syncs should occur on saves
	 * 
	 * @return
	 */
	public boolean isSyncOnSave() {
		return fSyncOnSave;
	}

	/**
	 * Set the configuration name
	 * 
	 * @param configName
	 */
	public void setConfigName(String configName) {
		fName = configName;
	}

	/**
	 * Set the remote connection
	 * 
	 * @param connection
	 * @since 4.0
	 */
	public void setConnection(IRemoteConnection connection) {
		remoteLocation.setConnection(connection);
	}

	/**
	 * Set the connection name
	 * 
	 * @param connectionName
	 */
	public void setConnectionName(String connectionName) {
		remoteLocation.setConnectionName(connectionName);
	}

	/**
	 * Set the sync location
	 * 
	 * @param location
	 */
	public void setLocation(String location) {
		remoteLocation.setLocation(location);
	}

	/**
	 * Set the synchronized project
	 * 
	 * @param project
	 */
	public void setProject(IProject project) {
		fProject = project;
	}

	/**
	 * Set an arbitrary property for the configuration
	 * 
	 * @param key
	 * @param value
	 */
	public void setProperty(String key, String value) {
		fProperties.put(key, value);
	}

	/**
	 * Set the remote services ID
	 * 
	 * @param remoteServicesId
	 */
	public void setRemoteServicesId(String remoteServicesId) {
		remoteLocation.setRemoteServicesId(remoteServicesId);
	}

	/**
	 * Set the sync on post-build flag
	 * 
	 * @param syncOnPostBuild
	 */
	public void setSyncOnPostBuild(boolean syncOnPostBuild) {
		fSyncOnPostBuild = syncOnPostBuild;
	}

	/**
	 * Set the sync on pre-build flag
	 * 
	 * @param syncOnPreBuild
	 */
	public void setSyncOnPreBuild(boolean syncOnPreBuild) {
		fSyncOnPreBuild = syncOnPreBuild;
	}

	/**
	 * Set the sync on save flag
	 * 
	 * @param syncOnSave
	 */
	public void setSyncOnSave(boolean syncOnSave) {
		fSyncOnSave = syncOnSave;
	}

	/**
	 * Set the sync provider ID
	 * 
	 * @param syncProvider
	 */
	public void setSyncProviderId(String syncProviderId) {
		fSyncProviderId = syncProviderId;
	}
}