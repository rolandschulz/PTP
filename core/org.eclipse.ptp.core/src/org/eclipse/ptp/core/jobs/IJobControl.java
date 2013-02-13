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
package org.eclipse.ptp.core.jobs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * @since 6.0
 */
public interface IJobControl {
	/**
	 * Control operation to suspend a job
	 */
	public static final String SUSPEND_OPERATION = "SUSPEND"; //$NON-NLS-1$
	/**
	 * Control operation to resume a suspended job
	 */
	public static final String RESUME_OPERATION = "RESUME"; //$NON-NLS-1$
	/**
	 * Control operation to put a job on hold
	 */
	public static final String HOLD_OPERATION = "HOLD"; //$NON-NLS-1$
	/**
	 * Control operation to release a job from hold
	 */
	public static final String RELEASE_OPERATION = "RELEASE"; //$NON-NLS-1$
	/**
	 * Control operation to terminate a job
	 */
	public static final String TERMINATE_OPERATION = "TERMINATE"; //$NON-NLS-1$
	/**
	 * Rerun the job. Note that this is currently only used to resend input to a job that is already in running state.
	 * 
	 * @since 7.0
	 */
	public static final String RERUN_OPERATION = "RERUN"; //$NON-NLS-1$

	/**
	 * Perform control operation on job.
	 * 
	 * @param jobId
	 *            job ID representing the job to be canceled.
	 * @param operation
	 *            operation to perform on the job
	 * @param monitor
	 *            progress monitor for monitoring operation
	 * @throws CoreException
	 * @since 5.0
	 */
	public void control(String jobId, String operation, IProgressMonitor monitor) throws CoreException;

	/**
	 * Get the ID of this job controller
	 */
	public String getControlId();

	/**
	 * Get the status of the job. The could potentially be a long running operation. If the progress monitor is canceled, the method
	 * will return an undetermined status. <br>
	 * <br>
	 * Note that this call may be throttled using a predetermined timeout by the rsource manager implementation. To avoid the
	 * throttle, set the force flag to true.
	 * 
	 * @param jobId
	 *            ID of job used to obtain status
	 * @param force
	 *            if true, tells the resource manager to ignore the throttling timeout.
	 * @param monitor
	 *            progress monitor for monitoring or canceling the operation
	 * @return status of the job or undetermined status if the progress monitor is canceled
	 */
	public IJobStatus getJobStatus(String jobId, boolean force, IProgressMonitor monitor) throws CoreException;

	/**
	 * Get the status of the job. The could potentially be a long running operation. If the progress monitor is canceled, the method
	 * will return an undetermined status. <br>
	 * <br>
	 * Note that this call may be throttled using a predetermined timeout by the rsource manager implementation. To avoid the
	 * throttle, use the overloaded method.<br>
	 * <br>
	 * 
	 * This method is equivalent to {@link #getJobStatus(String, boolean, IProgressMonitor)} with flag set to false.
	 * 
	 * @param jobId
	 *            ID of job used to obtain status
	 * @param monitor
	 *            progress monitor for monitoring or canceling the operation
	 * @return status of the job or undetermined status if the progress monitor is canceled
	 */
	public IJobStatus getJobStatus(String jobId, IProgressMonitor monitor) throws CoreException;

	/**
	 * Submit a job for execution
	 * 
	 * @param launchConfig
	 *            job launch configuration
	 * @param mode
	 *            job launch mode
	 * @param monitor
	 *            progress monitor
	 * @return job ID that can be used to identify the job
	 * @throws CoreException
	 * @since 7.0
	 */
	public String submitJob(ILaunchConfiguration launchConfig, String launchMode, IProgressMonitor monitor) throws CoreException;
}
