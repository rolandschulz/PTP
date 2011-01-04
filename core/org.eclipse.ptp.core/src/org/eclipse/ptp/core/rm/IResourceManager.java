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
package org.eclipse.ptp.core.rm;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerListener;
import org.eclipse.ptp.core.rm.exceptions.ResourceManagerException;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;

public interface IResourceManager {
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

	public enum JobStatus {
		/*
		 * job status cannot be determined
		 */
		UNDETERMINED,
		/*
		 * job is queued and waiting to be scheduled
		 */
		QUEUED_ACTIVE,
		/*
		 * job has been placed on hold by the system or the administrator
		 */
		SYSTEM_ON_HOLD,
		/*
		 * job has been placed on hold by a user
		 */
		USER_ON_HOLD,
		/*
		 * job has been placed on hold by both the system or administrator and a
		 * user
		 */
		USER_SYSTEM_ON_HOLD,
		/*
		 * job has been scheduled and is running
		 */
		RUNNING,
		/*
		 * job has been suspended by the system or administrator
		 */
		SYSTEM_SUSPENDED,
		/*
		 * job has been suspended by a user
		 */
		USER_SUSPENDED,
		/*
		 * job has been suspended by both the system or administrator and a user
		 */
		USER_SYSTEM_SUSPENDED,
		/*
		 * job finished normally
		 */
		DONE,
		/*
		 * job exited abnormally before finishing
		 */
		FAILED
	};

	public enum SessionStatus {
		/*
		 * session is stopped
		 */
		STOPPED,
		/*
		 * session is started
		 */
		STARTED
	}

	/**
	 * Add a listener for resource manager events.
	 * 
	 * @param listener
	 *            listener to add to the list of listeners
	 */
	public void addListener(IResourceManagerListener listener);

	/**
	 * Perform a control the job. The action this takes depends on the operation
	 * argument.
	 * 
	 * @param job
	 *            job id representing the job to be controlled.
	 * @param operation
	 *            operation to perform on the job
	 * @param monitor
	 *            progress monitor for reporting progress to the user. It is the
	 *            caller's responsibility to call done() on the given monitor.
	 *            Accepts null, indicating that no progress should be reported.
	 * @throws ResourceManagerException
	 */
	public void control(String job, JobControlOperation operation, IProgressMonitor monitor) throws ResourceManagerException;

	/**
	 * Create a new empty job template.
	 * 
	 * @return new empty job template
	 */
	public IJobTemplate createJobTemplate() throws ResourceManagerException;

	/**
	 * Deallocate resources used by a job template.
	 * 
	 * @param jobTemplate
	 */
	public void deleteJobTemplate(IJobTemplate jobTemplate) throws ResourceManagerException;

	/**
	 * Get the configuration for this RM
	 * 
	 * @return configuration for the RM
	 */
	public IResourceManagerConfiguration getConfiguration();

	/**
	 * Get a string description of this RM
	 * 
	 * @return string describing the RM
	 */
	public String getDescription();

	/**
	 * Get the status of the job
	 * 
	 * @param job
	 *            job used to obtain status
	 * @return status of the job
	 */
	public JobStatus getJobStatus(String job) throws ResourceManagerException;

	/**
	 * Get the name of this RM
	 * 
	 * @return string name of the RM
	 */
	public String getName();

	/**
	 * Returns the id of the resource manager
	 * 
	 * @return the id of the resource manager
	 */
	public String getResourceManagerId();

	/**
	 * Get the status of the resource manager session
	 * 
	 * @return status of resource manager session
	 */
	public SessionStatus getSessionStatus();

	/**
	 * Get a unique name that can be used to identify this resource manager
	 * persistently between PTP invocations. Used by the
	 * ResourceManagerPersistence.
	 * 
	 * @return string representing a unique name for the resource manager
	 */
	public String getUniqueName();

	/**
	 * Remove listener for events relating to this resource manager
	 * 
	 * @param listener
	 *            listener to remove
	 */
	public void removeListener(IResourceManagerListener listener);

	/**
	 * Submit a job. The job template must contain the appropriate attributes
	 * for a successful job launch (e.g. the queue, etc.).
	 * 
	 * The method will return after confirmation that the job has been
	 * submitted.
	 * 
	 * @param jobTemplate
	 *            job template used to submit the job.
	 * @param monitor
	 *            progress monitor for reporting progress to the user. It is the
	 *            caller's responsibility to call done() on the given monitor.
	 *            Accepts null, indicating that no progress should be reported.
	 * @return a job id representing the submitted job
	 * @throws ResourceManagerException
	 */
	public String runJob(IJobTemplate jobTemplate, IProgressMonitor monitor) throws ResourceManagerException;

	/**
	 * Set the configuration for this RM
	 * 
	 * @param config
	 *            new configuration for the RM
	 */
	public void setConfiguration(IResourceManagerConfiguration config);

	/**
	 * Set the status for this RM
	 * 
	 * @param status
	 *            new status for the RM
	 */
	public void setStatus(SessionStatus status);

	/**
	 * Shutdown the resource manager.
	 * 
	 * @throws CoreException
	 *             this exception is thrown if the shutdown command fails
	 */
	public void shutdown() throws CoreException;

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
	 */
	public void startup(IProgressMonitor monitor) throws CoreException;
}
