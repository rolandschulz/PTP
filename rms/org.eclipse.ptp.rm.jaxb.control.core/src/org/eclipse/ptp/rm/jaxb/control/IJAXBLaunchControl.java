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
 * JAXB Launch Control interface.
 * 
 */
public interface IJAXBLaunchControl extends IJAXBJobControl {

	public void setConnectionName(String connName);

	public void setRemoteServicesId(String id);

	/**
	 * Safely dispose of this Resource Manager.
	 */
	public void dispose();

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
	 * @param url
	 *            of the JAXB configuration for this resource manager.
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
