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

/**
 * Launch Control interface.
 * 
 */
public interface ILaunchController extends IJobController {

	/**
	 * Safely dispose of this controller.
	 */
	public void dispose();

	/**
	 * Initialize the controller. The controller is ready to be used after initialization, however it must be started before any
	 * control operations can be performed.
	 * 
	 * @param monitor
	 *            progress monitor
	 * @throws CoreException
	 */
	public void initialize(IProgressMonitor monitor) throws CoreException;

	/**
	 * Check if controller is initialized.
	 * 
	 * @return true if controller is initialized
	 */
	public boolean isInitialized();

	/**
	 * Set the connection name for this control
	 * 
	 * @param connName
	 */
	public void setConnectionName(String connName);

	/**
	 * Set the remote services ID for this control
	 * 
	 * @param id
	 */
	public void setRemoteServicesId(String id);

	/**
	 * @param url
	 *            of the JAXB configuration for this controller.
	 * 
	 * @since 5.0
	 */
	public void setRMConfigurationURL(URL url);

	/**
	 * Start the launch control. This must be called before any other operations.
	 * 
	 * @param monitor
	 *            progress monitor
	 * @throws CoreException
	 *             this exception is thrown if the start command fails
	 */
	public void start(IProgressMonitor monitor) throws CoreException;

	/**
	 * Stop the the launch control.
	 * 
	 * @throws CoreException
	 *             this exception is thrown if the stop command fails
	 * @since 5.0
	 */
	public void stop() throws CoreException;
}
