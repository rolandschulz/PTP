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

import java.util.Map;

import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.internal.rdt.sync.core.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
import org.eclipse.ptp.rdt.sync.core.handlers.IMissingConnectionHandler;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.RemoteServices;

/**
 * Class for build information that will be mapped to a specific service configuration. Utility methods for reading and writing
 * the information to a preference node are provided.
 */
public class BuildScenario {
	private static final String ATTR_SYNC_PROVIDER = "sync-provider"; //$NON-NLS-1$
	private static final String ATTR_REMOTE_CONNECTION_ID = "remote-connection-id"; //$NON-NLS-1$
	private static final String ATTR_LOCATION = "location"; //$NON-NLS-1$
	private static final String ATTR_REMOTE_SERVICES_ID = "remote-services-id"; //$NON-NLS-1$

	private String fConfigId;

	/**
	 * Load data from a map into a new build scenario.
	 * 
	 * @param map
	 * @return a new build scenario or null if one of the values is not found or if something goes wrong while trying to find the
	 *         specified IRemoteConnection.
	 */
	public static BuildScenario loadScenario(Map<String, String> map, String configId) {
		String sp = map.get(ATTR_SYNC_PROVIDER);
		String rc = map.get(ATTR_REMOTE_CONNECTION_ID);
		String rs = map.get(ATTR_REMOTE_SERVICES_ID);
		String l = map.get(ATTR_LOCATION);
		if (rc == null || l == null || rs == null) { // null is okay for sync provider
			return null;
		}
		BuildScenario scenario = new BuildScenario(sp, rc, rs, l);
		scenario.setConfigId(configId);
		return scenario;
	}

	/**
	 * Get the configuration ID associated with this scenario
	 * 
	 * @return
	 */
	public String getConfigId() {
		return fConfigId;
	}

	/**
	 * Set the configuration ID associated with this scenario
	 * 
	 * @param configId
	 */
	public void setConfigId(String configId) {
		fConfigId = configId;
	}

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

	private final String syncProvider;
	private final String remoteConnection;
	private final IRemoteServices remoteServices;
	private final String location;

	/**
	 * Create a new build scenario
	 * 
	 * @param sp
	 *            Name of sync provider
	 * @param rc
	 *            Remote connection to use - cannot be null
	 * @param l
	 *            Location (directory) on remote host
	 */
	public BuildScenario(String sp, IRemoteConnection rc, String l) {
		syncProvider = sp;
		remoteConnection = rc.getName();
		remoteServices = rc.getRemoteServices();
		location = l;
	}

	/**
	 * Create a new build scenario
	 * 
	 * @param sp
	 *            Name of sync provider
	 * @param rc
	 *            Name of remote connection
	 * @param rs
	 *            Name of remote service - must be a valid service
	 * @param l
	 *            Location (directory) on remote host
	 */
	public BuildScenario(String sp, String rc, String rs, String l) {
		syncProvider = sp;
		remoteConnection = rc;
		remoteServices = RemoteServices.getRemoteServices(rs);
		if (remoteServices == null) {
			throw new IllegalArgumentException(Messages.BuildScenario_0 + rs);
		}
		location = l;
	}

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
		BuildScenario other = (BuildScenario) obj;
		if (location == null && other.location != null) {
			return false;
		}
		if (!location.equals(other.location)) {
			return false;
		}
		if (remoteConnection == null && other.remoteConnection != null) {
			return false;
		}
		if (!remoteConnection.equals(other.remoteConnection)) {
			return false;
		}
		if (remoteServices == null && other.remoteServices != null) {
			return false;
		}
		if (!remoteServices.equals(other.remoteServices)) {
			return false;
		}
		if (syncProvider == null && other.syncProvider != null) {
			return false;
		}
		if (!syncProvider.equals(other.syncProvider)) {
			return false;
		}
		return true;
	}

	/**
	 * Get the remote location
	 * 
	 * @return location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Get location (directory), resolved in terms of the passed project
	 * 
	 * @param project
	 * @return location
	 */
	public String getLocation(IProject project) {
		return resolveString(project, location);
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
		IRemoteConnection conn = remoteServices.getConnectionManager().getConnection(remoteConnection);
		if (conn == null) {
			IMissingConnectionHandler mcHandler = SyncManager.getDefaultMissingConnectionHandler();
			if (mcHandler != null) {
				mcHandler.handle(remoteServices, remoteConnection);
				conn = remoteServices.getConnectionManager().getConnection(remoteConnection);
			}
		}

		if (conn == null) {
			throw new MissingConnectionException(remoteConnection);
		} else {
			return conn;
		}
	}

	/**
	 * Get remote provider
	 * 
	 * @return remote provider - never null
	 */
	public IRemoteServices getRemoteProvider() {
		return remoteServices;
	}

	/**
	 * Get sync provider name
	 * 
	 * @return sync provider name
	 */
	public String getSyncProvider() {
		return syncProvider;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + ((remoteConnection == null) ? 0 : remoteConnection.hashCode());
		result = prime * result + ((remoteServices == null) ? 0 : remoteServices.hashCode());
		result = prime * result + ((syncProvider == null) ? 0 : syncProvider.hashCode());
		return result;
	}

	/**
	 * Store scenario in the given map
	 * 
	 * @param map
	 */
	public void saveScenario(Map<String, String> map) {
		if (syncProvider != null) {
			map.put(ATTR_SYNC_PROVIDER, syncProvider);
		}
		map.put(ATTR_REMOTE_CONNECTION_ID, remoteConnection);
		map.put(ATTR_LOCATION, location);
		map.put(ATTR_REMOTE_SERVICES_ID, remoteServices.getId());
	}
}