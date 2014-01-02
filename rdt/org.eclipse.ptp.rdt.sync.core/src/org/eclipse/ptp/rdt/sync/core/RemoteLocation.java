/*******************************************************************************
 * Copyright (c) 2013 The University of Tennessee and others.
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
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;

/**
 * Class for defining and handling a  "remote location" (primarily a host and directory pair)
 * Equality is also well-defined so that the class is useful for indexing.
 *
 * @since 4.0
 */
public class RemoteLocation {
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

	private String fRemoteServicesId;
	private String fConnectionName;
	private String fDirectory;

	private IRemoteServices fRemoteServices;
	private IRemoteConnection fConnection;

	/**
	 * Create new RemoteLocation object with all values set to null. Clients should use setters to finish creating object.
	 */
	public RemoteLocation() {
	}
	/**
	 * Copy constructor
	 * @param rl
	 * 			remote location to copy - cannot be null
	 */
	public RemoteLocation(RemoteLocation rl) {
		fRemoteServicesId = rl.fRemoteServicesId;
		fConnectionName = rl.fConnectionName;
		fDirectory = rl.fDirectory;
		fRemoteServices = rl.fRemoteServices;
		fConnection = rl.fConnection;
	}

	/**
	 * Get name of connection to remote
	 * @return connection name
	 */
	public String getConnectionName() {
		return fConnectionName;
	}

	/**
	 * Get the raw remote location unresolved
	 * 
	 * @return remote directory
	 */
	public String getDirectory() {
		return fDirectory;
	}

	/**
	 * Get location (directory), resolved in terms of the passed project
	 *
	 * @param project
	 * @return remote directory
	 */
	public String getDirectory(IProject project) {
		return resolveString(project, fDirectory);
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
	public IRemoteConnection getConnection() throws MissingConnectionException {
		if (fRemoteServices == null) {
			fRemoteServices = RemoteServices.getRemoteServices(fRemoteServicesId);
			fConnection = null;
		}

		if (fConnection == null) {
			fConnection = fRemoteServices.getConnectionManager().getConnection(fConnectionName);
			if (fConnection == null) {
				IMissingConnectionHandler mcHandler = SyncManager.getDefaultMissingConnectionHandler();
				if (mcHandler != null) {
					mcHandler.handle(fRemoteServices, fConnectionName);
					fConnection = fRemoteServices.getConnectionManager().getConnection(fConnectionName);
				}
			}
		}

		if (fConnection == null) {
			throw new MissingConnectionException(fConnectionName);
		}

		return fConnection;
	}

	/**
	 * Get the remote services ID
	 * 
	 * @return remote services ID
	 */
	public String getRemoteServicesId() {
		return fRemoteServicesId;
	}
	
	/**
	 * Set the remote connection
	 * 
	 * @param connection
	 */
	public void setConnection(IRemoteConnection connection) {
		fRemoteServices = connection.getRemoteServices();
		fRemoteServicesId = connection.getRemoteServices().getId();
		fConnectionName = connection.getName();
		fConnection = connection;
	}

	/**
	 * Set the connection name
	 * 
	 * @param connectionName
	 */
	public void setConnectionName(String connectionName) {
		fConnectionName = connectionName;
		fConnection = null;
	}

	/**
	 * Set the sync location
	 * 
	 * @param location
	 */
	public void setLocation(String location) {
		fDirectory = location;
	}
	
	/**
	 * Set the remote services ID
	 * 
	 * @param remoteServicesId
	 */
	public void setRemoteServicesId(String remoteServicesId) {
		fRemoteServicesId = remoteServicesId;
		fRemoteServices = null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fConnectionName == null) ? 0 : fConnectionName.hashCode());
		result = prime * result
				+ ((fDirectory == null) ? 0 : fDirectory.hashCode());
		result = prime
				* result
				+ ((fRemoteServicesId == null) ? 0 : fRemoteServicesId
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemoteLocation other = (RemoteLocation) obj;
		if (fConnectionName == null) {
			if (other.fConnectionName != null)
				return false;
		} else if (!fConnectionName.equals(other.fConnectionName))
			return false;
		if (fDirectory == null) {
			if (other.fDirectory != null)
				return false;
		} else if (!fDirectory.equals(other.fDirectory))
			return false;
		if (fRemoteServicesId == null) {
			if (other.fRemoteServicesId != null)
				return false;
		} else if (!fRemoteServicesId.equals(other.fRemoteServicesId))
			return false;
		return true;
	}
}
