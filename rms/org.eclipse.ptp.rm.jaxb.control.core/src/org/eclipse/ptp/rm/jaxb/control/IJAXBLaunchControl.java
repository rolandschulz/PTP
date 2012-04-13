/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManager;

/**
 * JAXB Launch Control interface.
 * 
 */
public interface IJAXBLaunchControl extends IJAXBJobControl {

	public void setConnectionName(String connName);

	public void setRemoteServicesId(String id);

	/**
	 * Resets internal (in-memory) data objects.
	 * 
	 * @param all
	 *            if false, clears only the data tree and map, not the connections.
	 * 
	 * @since 5.0
	 */
	public void clearReferences(boolean all);

	/**
	 * Safely dispose of this Resource Manager.
	 */
	public void dispose();

	/**
	 * @return state of resource manager
	 */
	public String getState();

	/**
	 * Initialize the resource manager. The resource manager is ready to be used after initialization, however it must be started
	 * before any control operations can be performed.
	 * 
	 * @param monitor
	 *            progress monitor
	 * @throws CoreException
	 */
	public void initialize(IProgressMonitor monitor) throws CoreException;

	/**
	 * Runs an action command.
	 * 
	 * @param action
	 *            name of action or command
	 * @param resetValue
	 *            name of property or attribute
	 * @param configuration
	 *            current values
	 * @return result of the action on resetValue, if any
	 * 
	 */
	public Object runActionCommand(String action, String resetValue, ILaunchConfiguration configuration) throws CoreException;

	/**
	 * @param url
	 *            of the JAXB configuration for this resource manager.
	 * 
	 * @since 5.0
	 */
	public void setRMConfigurationURL(URL url);

	/**
	 * Start the resource manager. Clients should not call this directly. Call {@link IResourceManager#start(IProgressMonitor)}
	 * instead.
	 * 
	 * @param monitor
	 *            progress monitor
	 * @throws CoreException
	 *             this exception is thrown if the start command fails
	 */
	public void start(IProgressMonitor monitor) throws CoreException;

	/**
	 * Stop the resource manager. Clients should not call this directly. Call {@link IResourceManager#stop()} instead.
	 * 
	 * @throws CoreException
	 *             this exception is thrown if the stop command fails
	 * @since 5.0
	 */
	public void stop() throws CoreException;
}
