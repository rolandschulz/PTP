/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ptp.rmsystem;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.listeners.IJobListener;

/**
 * @since 5.0
 */
public interface IResourceManagerMonitor {
	/**
	 * Notify monitor that job should be treated specially.
	 * 
	 * @param jobId
	 *            ID of job to be treated specially
	 * @param status
	 *            current status of job
	 */
	public void addJob(String jobId, IJobStatus status);

	/**
	 * Add a listener for job events
	 * 
	 * @param listener
	 */
	public void addJobListener(IJobListener listener);

	/**
	 * Safely dispose of this resource manager monitor.
	 */
	public void dispose();

	/**
	 * Get the configuration associated with this resource manager monitor.
	 * 
	 * @return resource manager configuration
	 */
	public IResourceManagerComponentConfiguration getMonitorConfiguration();

	/**
	 * Notify monitor that job should no longer be treated specially
	 * 
	 * @param jobId
	 *            ID of job to remove
	 */
	public void removeJob(String jobId);

	/**
	 * Remove a listener for job events
	 * 
	 * @param listener
	 */
	public void removeJobListener(IJobListener listener);

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
	 * Stop the resource manager. Call {@link IResourceManager#stop()} instead.
	 * 
	 * @throws CoreException
	 *             this exception is thrown if the stop command fails
	 */
	public void stop() throws CoreException;

	/**
	 * Notify monitor that status of job has changed.
	 * 
	 * @param jobId
	 *            ID of job to be updated
	 * @param status
	 *            new status of job
	 */
	public void updateJob(String jobId, IJobStatus status);

}
