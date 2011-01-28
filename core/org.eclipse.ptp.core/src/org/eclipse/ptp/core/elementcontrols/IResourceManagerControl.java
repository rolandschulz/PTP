/*******************************************************************************
 * Copyright (c) 2005, 2006, 2007 Los Alamos National Security, LLC.
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
package org.eclipse.ptp.core.elementcontrols;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;

public interface IResourceManagerControl extends IAdaptable {
	/**
	 * @since 5.0
	 */
	public enum JobControlOperation {
		/*
		 * stop a job
		 */
		SUSPEND,
		/*
		 * restart a suspended job
		 */
		RESUME,
		/*
		 * put a job on hold
		 */
		HOLD,
		/*
		 * release a job from hold
		 */
		RELEASE,
		/*
		 * kill a job
		 */
		TERMINATE
	};

	/**
	 * Perform control operation on job.
	 * 
	 * @param job
	 *            job object representing the job to be canceled.
	 * @param operation
	 *            operation to perform on the job
	 * @param monitor
	 *            progress monitor for monitoring operation
	 * @throws CoreException
	 * @since 5.0
	 */
	public void control(IPJob job, JobControlOperation operation, IProgressMonitor monitor) throws CoreException;

	/**
	 * Safely dispose of this Resource Manager.
	 */
	public void dispose();

	/**
	 * Get the attribute definition corresponding to the attrId. This will only
	 * check for attribute definitions that the RM knows about.
	 * 
	 * @param attrId
	 *            ID of the attribute definition
	 * @return the attribute definition corresponding to the attribute
	 *         definition ID
	 * @since 5.0
	 */
	public IAttributeDefinition<?, ?, ?> getAttributeDefinition(String attrId);

	/**
	 * Get the configuration associated with this resource manager.
	 * 
	 * @return resource manager configuration
	 */
	public IResourceManagerConfiguration getConfiguration();

	/**
	 * Get a string description of this RM
	 * 
	 * @return string describing the RM
	 * @since 5.0
	 */
	public String getDescription();

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
	public ResourceManagerAttributes.State getState();

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
	 * Set the configuration for this resource manager. This will replace the
	 * existing configuration with a new configuration. The method is
	 * responsible for dealing with any saved state that needs to be cleaned up.
	 * 
	 * @param config
	 *            the new configuration
	 */
	public void setConfiguration(IResourceManagerConfiguration config);

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

	/**
	 * Submit a job. The attribute manager must contain the appropriate
	 * attributes for a successful job launch (e.g. the queue, etc.).
	 * 
	 * The method will return after an INewJobEvent has been received confirming
	 * that the job has been submitted.
	 * 
	 * @param configuration
	 *            launch configuration used to submit the job
	 * @param attrMgr
	 *            attribute manager containing the job launch attributes
	 * @param monitor
	 *            progress monitor for monitoring job submission.
	 * @return a job object representing the submitted job
	 * @throws CoreException
	 *             if the job submission fails or was canceled
	 * @since 5.0
	 */
	public IPJob submitJob(ILaunchConfiguration configuration, AttributeManager attrMgr, IProgressMonitor monitor)
			throws CoreException;
}
