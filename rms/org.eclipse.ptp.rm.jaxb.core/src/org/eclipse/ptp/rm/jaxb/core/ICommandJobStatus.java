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
	 * Cancel the Job process (if interactive).
	 */
	void cancel();

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
	 * @return whether the associated Job was launched interactively or not.
	 */
	boolean isInteractive();

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
	 * Do a monitor wait until the job id arrives.
	 * 
	 * @param uuid
	 *            internal id which the job id will be mapped to.
	 */
	void waitForJobId(String uuid);
}
