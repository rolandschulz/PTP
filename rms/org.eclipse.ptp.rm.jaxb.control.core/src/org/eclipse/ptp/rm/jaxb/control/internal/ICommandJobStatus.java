/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.jobs.IPJobStatus;
import org.eclipse.ptp.remote.core.IRemoteProcess;

/**
 * CommandJob-specific extension to IJobStatus.
 * 
 * @see org.eclipse.ptp.core.jobs.IJobStatus
 * @author arossi
 * 
 */
public interface ICommandJobStatus extends IPJobStatus {
	/**
	 * for throttling requests which may trigger remote calls
	 */
	public static final long UPDATE_REQUEST_INTERVAL = 30 * 1000;

	/**
	 * Cancel the Job process (if interactive).
	 * 
	 * @return true if canceled.
	 */
	public boolean cancel();

	/**
	 * Notify all waiting on the job id of its arrival.
	 */
	public void cancelWait();

	/**
	 * timestamp of last update request issued to remote resource
	 * 
	 * @return update in milliseconds
	 */
	public long getLastUpdateRequest();

	/**
	 * Initialize remote file paths from current environment.
	 * 
	 * Initialize must be called immediately after the return of the submit.run() method while the property for the jobId is
	 * pinned and in the environment. Note also that batch variable replacement will not work, as that would not be interpretable
	 * for the RM. One actually needs to configure two separate strings in this case, giving one to the script and one to the
	 * resource manager.
	 * 
	 * @param jobId
	 *            for the associated job
	 */
	public void initialize(String jobId) throws CoreException;

	/**
	 * If there are remote output files, runs the check and joins on those threads.
	 * 
	 * @param blockForSecs
	 *            will continue trying for this long before returning
	 * @param monitor
	 *            progress monitor for potential cancellation
	 */
	public void maybeWaitForHandlerFiles(int blockForSecs, IProgressMonitor monitor);

	/**
	 * @param owner
	 *            user name of the submitter
	 */
	public void setOwner(String owner);

	/**
	 * @param process
	 *            if the Job is interactive.
	 */
	public void setProcess(IRemoteProcess process);

	/**
	 * @param proxy
	 */
	public void setProxy(ICommandJobStreamsProxy proxy);

	/**
	 * If a batch job, the queue submitted to.
	 * 
	 * @param name
	 */
	public void setQueueName(String name);

	/**
	 * @param state
	 *            of the launched Job, not of the submission call.
	 */
	public void setState(String state);

	/**
	 * @param time
	 *            in milliseconds of last update request issued to remote resource
	 */
	public void setUpdateRequestTime(long update);

	/**
	 * @return if the state has changed since the last check.
	 */
	public boolean stateChanged();

	/**
	 * Wait until the jobId has been set on the job id property in the environment.
	 * 
	 * The uuid key for the property containing as its name the resource-specific jobId and as its value the state.
	 * 
	 * The waitUntil state will usually be either SUBMITTED or RUNNING (for interactive)
	 * 
	 * @param uuid
	 *            internal id which the job id will be mapped to.
	 * @param waitUntil
	 *            wait until this state is reached.
	 * @oaram monitor
	 * @throws CoreException
	 */
	public void waitForJobId(String uuid, String waitUntil, IProgressMonitor monitor) throws CoreException;
}
