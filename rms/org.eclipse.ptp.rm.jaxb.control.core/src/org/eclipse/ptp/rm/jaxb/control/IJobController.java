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

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.jobs.IJobControl;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.rm.jaxb.control.internal.ICommandJob;
import org.eclipse.ptp.rm.jaxb.control.internal.ICommandJobStatusMap;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;

/**
 * Job Controller interface.
 * 
 */
public interface IJobController extends IJobControl {
	/**
	 * @return whether the launch environment should be appended to (or replace) the environment for a given command execution.
	 */
	public boolean getAppendEnv();

	/**
	 * Get the control configuration
	 * 
	 * @return configuration
	 */
	public ResourceManagerData getConfiguration();

	/**
	 * Get the connection used to control the job
	 * 
	 * @return remote connection name
	 * @since 6.0
	 */
	public String getConnectionName();

	/**
	 * @return variable resolver environment
	 */
	public IVariableMap getEnvironment();

	/**
	 * @return
	 */
	public ICommandJob getInteractiveJob();

	/**
	 * @return the user-defined environment (from the Environment Tab)
	 */
	public Map<String, String> getLaunchEnv();

	/**
	 * Get the remote services provider used to control the job
	 * 
	 * @return remote services provider
	 * @since 6.0
	 */
	public String getRemoteServicesId();

	/**
	 * @return
	 */
	public ICommandJobStatusMap getStatusMap();

	/**
	 * For callbacks to the resource manager from internal jobs.
	 */
	public void jobStateChanged(String jobId, IJobStatus status);

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
	 * @param interactiveJob
	 */
	public void setInteractiveJob(ICommandJob interactiveJob);

}
