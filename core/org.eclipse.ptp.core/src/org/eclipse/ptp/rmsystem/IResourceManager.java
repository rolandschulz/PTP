/*******************************************************************************
 * Copyright (c) 2005, 2006, 2007, 2011 Los Alamos National Security, LLC, and others.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy.  The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rmsystem;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @since 5.0
 */
public interface IResourceManager extends IResourceManagerControl, IResourceManagerMonitor, IAdaptable {
	/**
	 * State indicating resource manager is stopped
	 */
	public static final String STOPPED_STATE = "STOPPED"; //$NON-NLS-1$
	/**
	 * State indicating resource manager is started
	 */
	public static final String STARTED_STATE = "STARTED"; //$NON-NLS-1$
	/**
	 * State indicating resource manager is in the process of starting
	 */
	public static final String STARTING_STATE = "STARTING"; //$NON-NLS-1$
	/**
	 * State indicating a resource manager error condition
	 */
	public static final String ERROR_STATE = "ERROR"; //$NON-NLS-1$

	/**
	 * Safely dispose of this Resource Manager.
	 */
	public void dispose();

	/**
	 * Get the configuration associated with this resource manager.
	 * 
	 * @return resource manager configuration
	 */
	public IResourceManagerConfiguration getConfiguration();

	/**
	 * Get the controller part of this resource manager
	 * 
	 * @return resource manager control
	 */
	public IResourceManagerControl getControl();

	/**
	 * Get a string description of this RM
	 * 
	 * @return string describing the RM
	 * @since 5.0
	 */
	public String getDescription();

	/**
	 * Get the monitor part of this resource manager
	 * 
	 * @return resource manager monitor
	 */
	public IResourceManagerMonitor getMonitor();

	/**
	 * Get the name of this RM
	 * 
	 * @return string name of the RM
	 * @since 5.0
	 */
	public String getName();

	/**
	 * Returns the extension point id of the resource manager
	 * 
	 * @return the extension point id of the resource manager
	 * @since 5.0
	 */
	public String getResourceManagerId();

	/**
	 * Get the state of this RM
	 * 
	 * @return state value representing the state of the RM
	 * @since 5.0
	 */
	public String getState();

	/**
	 * Get a unique name that can be used to identify this resource manager
	 * persistently between PTP invocations. Used by the
	 * ResourceManagerPersistence.
	 * 
	 * @return string representing a unique name for the resource manager
	 * @since 5.0
	 */
	public String getUniqueName();

	/**
	 * Set the state of this RM
	 * 
	 * @param state
	 *            value representing the state of the RM
	 * @since 5.0
	 */
	public void setState(String state);

	/**
	 * Start up the resource manager. This could potentially take a long time
	 * (or forever), particularly if the RM is located on a remote system.
	 * 
	 * Callers can assume that the operation was successful if no exception is
	 * thrown and the monitor was not cancelled. However, the resource manager
	 * may still fail later due to some other condition.
	 * 
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts null, indicating that no progress
	 *            should be reported and that the operation cannot be cancelled.
	 * @throws CoreException
	 *             this exception is thrown if the resource manager fails to
	 *             start
	 * @since 5.0
	 */
	public void start(IProgressMonitor monitor) throws CoreException;

	/**
	 * Stop the resource manager.
	 * 
	 * @throws CoreException
	 *             this exception is thrown if the stop command fails
	 * @since 5.0
	 */
	public void stop() throws CoreException;
}
