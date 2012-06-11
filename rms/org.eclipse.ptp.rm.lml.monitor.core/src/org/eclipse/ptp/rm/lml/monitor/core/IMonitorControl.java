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
	 * Dispose of monitor and any saved state.
	 */
	public void dispose();

	/**
	 * Get the connection name for the monitor
	 * 
	 * @return connection name
	 */
	public String getConnectionName();

	/**
	 * Get an ID that uniquely identifies this monitor. The ID is unique for each tuple <remoteServicesId, connectionName,
	 * systemType>
	 * 
	 * @return unique ID
	 */
	public String getMonitorId();

	/**
	 * Get the ID of the remote services used by the monitor for remote connections
	 * 
	 * @return remote services ID
	 */
	public String getRemoteServicesId();

	/**
	 * Get the system type for this monitor
	 * 
	 * @return system type
	 */
	public String getSystemType();

	/**
	 * Check if the monitor is active. An active monitor is connected to the remote system and is polling the system for updates.
	 * 
	 * @return true if the monitor is active
	 */
	public boolean isActive();

	/**
	 * Load persisted monitor information.
	 * 
	 * @return true if the monitor was active when saved
	 */
	public boolean load() throws CoreException;

	/**
	 * Force the monitor to refresh its data. This will typically cause a command to be run on the target system.
	 */
	public void refresh();

	/**
	 * Save monitor data to persisted store.
	 */
	public void save();

	/**
	 * Set the connection name for the monitor
	 * 
	 * @param connName
	 */
	public void setConnectionName(String connName);

	/**
	 * Set the ID of the remote services used by the monitor for remote connections
	 * 
	 * @param id
	 *            remote services ID
	 */
	public void setRemoteServicesId(String id);

	/**
	 * Set the system type for the monitor
	 * 
	 * @param type
	 *            system type
	 */
	public void setSystemType(String type);

	/**
	 * Start the monitor. This will open a connection to the target system (if necessary) and start the monitoring job.
	 * 
	 * @param monitor
	 *            progress monitor
	 * @throws CoreException
	 */
	public void start(IProgressMonitor monitor) throws CoreException;

	/**
	 * Stop the monitor. Will terminate any running jobs.
	 * 
	 * @throws CoreException
	 */
	public void stop() throws CoreException;

}
