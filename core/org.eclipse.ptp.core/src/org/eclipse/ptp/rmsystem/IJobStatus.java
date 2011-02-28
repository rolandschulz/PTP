/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rmsystem;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IStreamsProxy;

/**
 * @since 5.0
 */
public interface IJobStatus {
	/*********** PRIMARY JOB STATES ********/
	/**
	 * Job status cannot be determined
	 */
	public static String UNDETERMINED = "UNDETERMINED"; //$NON-NLS-1$
	/**
	 * Job has been submitted but has not yet begun execution
	 */
	public static String SUBMITTED = "SUBMITTED"; //$NON-NLS-1$
	/**
	 * Job has been scheduled and is running.
	 */
	public static String RUNNING = "RUNNING"; //$NON-NLS-1$
	/**
	 * Running job has been suspended.
	 */
	public static String SUSPENDED = "SUSPENDED"; //$NON-NLS-1$
	/**
	 * Job has completed execution.
	 */
	public static String COMPLETED = "COMPLETED"; //$NON-NLS-1$

	/*********** JOB STATE DETAIL ************/
	/**
	 * Job is queued and waiting to be scheduled. Jobs in this state have not
	 * yet run and are considered "SUBMITTED".
	 */
	public static String QUEUED_ACTIVE = "QUEUED_ACTIVE"; //$NON-NLS-1$
	/**
	 * Job has been placed on hold by the system or the administrator. Jobs in
	 * this state have not yet run and are considered "SUBMITTED".
	 */
	public static String SYSTEM_ON_HOLD = "SYSTEM_ON_HOLD"; //$NON-NLS-1$
	/**
	 * Job has been placed on hold by a user. Jobs in this state have not yet
	 * run and are considered "SUBMITTED".
	 */
	public static String USER_ON_HOLD = "USER_ON_HOLD"; //$NON-NLS-1$
	/**
	 * Job has been placed on hold by both the system or administrator and a
	 * user. Jobs in this state have not yet run and are considered "SUBMITTED".
	 */
	public static String USER_SYSTEM_ON_HOLD = "USER_SYSTEM_ON_HOLD"; //$NON-NLS-1$
	/**
	 * Running job has been suspended by the system or administrator. Jobs in
	 * this state have begun execution and are considered "SUSPENDED".
	 */
	public static String SYSTEM_SUSPENDED = "SYSTEM_SUSPENDED"; //$NON-NLS-1$
	/**
	 * Running job has been suspended by a user. Jobs in this state have begun
	 * execution and are considered "SUSPENDED".
	 */
	public static String USER_SUSPENDED = "USER_SUSPENDED"; //$NON-NLS-1$
	/**
	 * Running job has been suspended by both the system or administrator and a
	 * user. Jobs in this state have begun execution and are considered
	 * "SUSPENDED".
	 */
	public static String USER_SYSTEM_SUSPENDED = "USER_SYSTEM_SUSPENDED"; //$NON-NLS-1$
	/**
	 * Job exited abnormally before finishing. Jobs in this state have completed
	 * execution and are considered "COMPLETED".
	 */
	public static String FAILED = "FAILED"; //$NON-NLS-1$

	/**
	 * Get the job ID of the job this status is for
	 * 
	 * @return job ID
	 */
	public String getJobId();

	/**
	 * Get the launch configuration used to launch this job
	 * 
	 * @return
	 */
	public ILaunchConfiguration getLaunchConfiguration();

	/**
	 * Get job state. This is the primary state of the job, and is one of
	 * UNDETERMINED, SUBMITTED, RUNNING, SUSPENDED or COMPLETED.
	 * 
	 * Note that UNDERTERMINED will only be returned if no other valid job state
	 * has been reach. Implementations MUST return the last valid job state if
	 * one is available.
	 * 
	 * @return state of the job
	 */
	public String getState();

	/**
	 * Get job state detail. This either returns the same state as
	 * {@link IJobStatus#getState()} or provides additional detail on the state
	 * of the job.
	 * 
	 * @return detailed state of the job
	 */
	public String getStateDetail();

	/**
	 * @return
	 */
	public IStreamsProxy getStreamsProxy();
}
