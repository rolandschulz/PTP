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

import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.rmsystem.IJobStatus;

/**
 * CommandJob-specific extension to IJobStatus.
 * 
 * @see org.eclipse.ptp.rmsystem.IJobStatus
 * @author arossi
 * 
 */
public interface ICommandJobStatus extends IJobStatus {
	/**
	 * for throttling requests which may trigger remote calls
	 */
	final long UPDATE_REQUEST_INTERVAL = 30 * 1000;

	/**
	 * @return the current control
	 */
	public IJAXBResourceManagerControl getControl();

	/**
	 * Cancel the Job process (if interactive).
	 */
	boolean cancel();

	/**
	 * Notify all waiting on the job id of its arrival.
	 */
	void cancelWait();

	/**
	 * timestamp of last update request issued to remote resource
	 * 
	 * @return update in milliseconds
	 */
	long getLastUpdateRequest();

	/**
	 * Initialize remote file paths from current env.
	 * 
	 * @param jobId
	 *            for the associated job
	 */
	void initialize(String jobId);

	/**
	 * If there are remote output files, runs the check and joins on those
	 * threads.
	 * 
	 * @param blockForSecs
	 *            will continue trying for this long before returning
	 */
	void maybeWaitForHandlerFiles(int blockForSecs);

	/**
	 * @param process
	 *            if the Job is interactive.
	 */
	void setProcess(IRemoteProcess process);

	/**
	 * @param state
	 *            of the launched Job, not of the submission call.
	 */
	void setState(String state);

	/**
	 * @param time
	 *            in milliseconds of last update request issued to remote
	 *            resource
	 */
	void setUpdateRequestTime(long update);

	/**
	 * @return if the state has changed since the last check.
	 */
	boolean stateChanged();

	/**
	 * Do a monitor wait until the job id arrives.
	 * 
	 * @param uuid
	 *            internal id which the job id will be mapped to.
	 */
	void waitForJobId(String uuid);
}
