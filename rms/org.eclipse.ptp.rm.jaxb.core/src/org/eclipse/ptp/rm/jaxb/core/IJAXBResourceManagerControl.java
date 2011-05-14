/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.remote.core.RemoteServicesDelegate;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;

/**
 * Allows sharing of control-specific data among (internal) elements.
 * 
 * @author arossi
 * 
 */
public interface IJAXBResourceManagerControl extends IResourceManagerControl {

	/**
	 * @return whether the launch environment should be appended to (or replace)
	 *         the environment for a given command execution.
	 */
	public boolean getAppendEnv();

	/**
	 * @return resource manager environment
	 */
	public IVariableMap getEnvironment();

	/**
	 * @return the user-defined environment (from the Environment Tab)
	 */
	public Map<String, String> getLaunchEnv();

	/**
	 * connection information for this resource manager
	 * 
	 * @param monitor
	 * @return
	 */
	public RemoteServicesDelegate getRemoteServicesDelegate(IProgressMonitor monitor);

	/**
	 * @return state of resource manager
	 */
	public String getState();

	/**
	 * For callbacks to the resource manager from internal jobs.
	 */
	public void jobStateChanged(String jobId);
}
