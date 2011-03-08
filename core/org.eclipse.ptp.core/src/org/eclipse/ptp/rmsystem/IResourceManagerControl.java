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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * @since 5.0
 */
public interface IResourceManagerControl {
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
	 * Safely dispose of this Resource Manager.
	 */
	public void dispose();

	/**
	 * Get the status of the job
	 * 
	 * @param jobId
	 *            ID of job used to obtain status
	 * @return status of the job
	 */
	public IJobStatus getJobStatus(String jobId);

	/**
	 * Start the resource manager. Clients should not call this directly. Call
	 * {@link IResourceManager#start(IProgressMonitor)} instead.
	 * 
	 * @param monitor
	 *            progress monitor
	 * @throws CoreException
	 *             this exception is thrown if the start command fails
	 */
	public void start(IProgressMonitor monitor) throws CoreException;

	/**
	 * Stop the resource manager. Clients should not call this directly. Call
	 * {@link IResourceManager#stop()} instead.
	 * 
	 * @throws CoreException
	 *             this exception is thrown if the stop command fails
	 * @since 5.0
	 */
	public void stop() throws CoreException;

	/**
	 * Stop the resource manager.
	 * 
	 * @throws CoreException
	 *             this exception is thrown if the stop command fails
	 */
	public String submitJob(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException;
}
