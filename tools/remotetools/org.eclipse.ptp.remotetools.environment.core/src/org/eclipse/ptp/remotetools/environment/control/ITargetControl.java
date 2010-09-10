/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.environment.control;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;

/**
 * Controls an instance of a target created from the Environment.
 * 
 * @author Ricardo M. Matinata, Daniel Felix Ferber, Richard Maciel
 * @since 1.1
 */
public interface ITargetControl {
	public class TargetSocket {
		public String host;
		public int port;
	}

	/**
	 * Add a new job listener.
	 */
	public void addJobListener(ITargetControlJobListener listener);

	/**
	 * Called to create an instance of the target.
	 * 
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	public boolean create(IProgressMonitor monitor) throws CoreException;

	/**
	 * Create a new execution manager. This is required for script execution
	 * because KillableExecution closes the channel after execution.
	 * 
	 * @return new execution manager
	 * @since 2.0
	 */
	public IRemoteExecutionManager createExecutionManager() throws RemoteConnectionException;

	/**
	 * Create a bridge to a listening socket on the target environment.
	 * 
	 * @throws CoreException
	 */
	public TargetSocket createTargetSocket(int port) throws CoreException;

	/**
	 * Called to remove information associated with the instance of the target
	 * because the it was removed.
	 * 
	 * @throws CoreException
	 */
	public void destroy() throws CoreException;

	/**
	 * Get the target configuration information
	 * 
	 * @return ITargetConfig
	 * @since 2.0
	 */
	public ITargetConfig getConfig();

	/**
	 * Get the main execution manager for this control.
	 * 
	 * @return execution manager
	 * @since 2.0
	 */
	public IRemoteExecutionManager getExecutionManager();

	/**
	 * Returns how many jobs are being managed by the control.
	 */
	public int getJobCount();

	/**
	 * Returns an array of all jobs running remotely. All jobs are returned,
	 * regardless if WAITING or RUNNING. Finished jobs are automatically removed
	 * from the list.
	 * 
	 * @return The array of remote jobs.
	 */
	public ITargetJob[] getJobs();

	/**
	 * Called to halt the instance of the target.
	 * 
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	public boolean kill(IProgressMonitor monitor) throws CoreException;

	/**
	 * Query the status of the target connection
	 * 
	 * @return status of connection as specified by {@link ITargetStatus}
	 */
	public int query();

	/**
	 * Remove a job listener.
	 */
	public void removeJobListener(ITargetControlJobListener listener);

	public boolean resume(IProgressMonitor monitor) throws CoreException;

	/**
	 * Adds a new job to the set of running jobs on the environment. The job
	 * will begin execution as soon as possible after being added.
	 * 
	 * @param job
	 *            The job to be added and executed.
	 * @return The job encapsulated as a IRemoteControlledJob
	 * @throws CoreException
	 */
	public void startJob(ITargetJob job) throws CoreException;

	public boolean stop(IProgressMonitor monitor) throws CoreException;

	/**
	 * This method is a callback called when the ITargetControl implementing
	 * class must update its configuration because an external change occurred.
	 * 
	 * @throws CoreException
	 */
	public void updateConfiguration() throws CoreException;
}