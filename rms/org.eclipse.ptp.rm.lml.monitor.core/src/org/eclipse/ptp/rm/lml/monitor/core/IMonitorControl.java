/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.lml.monitor.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @since 6.0
 */
public interface IMonitorControl {
	/**
	 * Dispose of control and any saved state.
	 */
	public void dispose();

	/**
	 * Get the configuration name for this control
	 * 
	 * @return configuration name
	 */
	public String getConfigurationName();

	/**
	 * Get the connection name for the control
	 * 
	 * @return connection name
	 */
	public String getConnectionName();

	/**
	 * Get an ID that uniquely identifies this control. The ID is unique for
	 * each tuple <remoteServicesId, connectionName, systemType>
	 * 
	 * @return unique ID
	 */
	public String getControlId();

	/**
	 * Get the ID of the remote services used by the control for remote
	 * connections
	 * 
	 * @return remote services ID
	 */
	public String getRemoteServicesId();

	/**
	 * Get the system type for this control
	 * 
	 * @return system type
	 */
	public String getSystemType();

	/**
	 * Check if the control is active. An active control is connected to the
	 * remote system and is polling the system for updates.
	 * 
	 * @return true if the control is active
	 */
	public boolean isActive();

	/**
	 * Check if remote caching is active.
	 * If so, the remote component will try to use cached data,
	 * otherwise a data update is enforced.
	 * 
	 * @return true, if caching is active, false otherwise
	 */
	public boolean isCacheActive();

	/**
	 * Load persisted control information.
	 * 
	 * @return true if the control was active when saved
	 */
	public boolean load() throws CoreException;

	/**
	 * Let the control refresh its data. This will typically cause a
	 * command to be run on the target system.
	 */
	public void refresh();

	/**
	 * Save control data to persisted store.
	 */
	public void save();

	/**
	 * Enable or disable remote caching.
	 * If caching is activated, the remote component
	 * tries to use cached status data possibly
	 * collected by another user. With caching deactivated
	 * every user runs the remote data retrieval
	 * separately.
	 * 
	 * @param active
	 *            true for enabling caching, false otherwise
	 */
	public void setCacheActive(boolean active);

	/**
	 * Set the configuration name for the control
	 * 
	 * @param name
	 *            configuration name
	 */
	public void setConfigurationName(String name);

	/**
	 * Set the connection name for the control
	 * 
	 * @param connName
	 */
	public void setConnectionName(String connName);

	/**
	 * Set the ID of the remote services used by the control for remote
	 * connections
	 * 
	 * @param id
	 *            remote services ID
	 */
	public void setRemoteServicesId(String id);

	/**
	 * Start the control. This will open a connection to the target system (if
	 * necessary) and start the monitoring job.
	 * 
	 * @param monitor
	 *            progress monitor
	 * @throws CoreException
	 */
	public void start(IProgressMonitor monitor) throws CoreException;

	/**
	 * Stop the control. Will terminate any running jobs.
	 * 
	 * @throws CoreException
	 */
	public void stop() throws CoreException;

}
