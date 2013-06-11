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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;

/**
 * Job manager for resource manager framework.
 * 
 * @since 6.0
 */
public class JobManager {
	private static final String ALL_JOBS = "ALL_JOBS"; //$NON-NLS-1$
	private static final JobManager fInstance = new JobManager();

	public static JobManager getInstance() {
		return fInstance;
	}

	private final Map<String, IJobStatus> fJobs = Collections.synchronizedMap(new HashMap<String, IJobStatus>());
	private final Map<String, ListenerList> fJobListeners = Collections.synchronizedMap(new HashMap<String, ListenerList>());

	private JobManager() {
	}

	private void addJob(IJobStatus jobStatus) {
		fJobs.put(jobStatus.getControlId() + "+" + jobStatus.getJobId(), jobStatus); //$NON-NLS-1$
	}

	/**
	 * Add a listener for all job events
	 * 
	 * @param listener
	 */
	public void addListener(IJobListener listener) {
		ListenerList listeners = fJobListeners.get(ALL_JOBS);
		if (listeners == null) {
			listeners = new ListenerList();
			fJobListeners.put(ALL_JOBS, listeners);
		}
		listeners.add(listener);
	}

	/**
	 * Add a listener for jobs under the control of a controller identified by qualifier.
	 * 
	 * @param qualifier
	 * @param listener
	 */
	public void addListener(String qualifier, IJobListener listener) {
		ListenerList listeners = fJobListeners.get(qualifier);
		if (listeners == null) {
			listeners = new ListenerList();
			fJobListeners.put(qualifier, listeners);
		}
		listeners.add(listener);
	}

	/**
	 * Locate any completed jobs and remove them from the hash
	 */
	private void cleanUpJobs() {
		List<String> completedJobs = new ArrayList<String>();
		for (IJobStatus job : fJobs.values()) {
			if (job.getState().equals(IJobStatus.COMPLETED)) {
				completedJobs.add(job.getJobId());
			}
		}
		for (String jobId : completedJobs) {
			fJobs.remove(jobId);
		}
	}

	/**
	 * Notify listeners when a job has been added.
	 * 
	 * @param qualifier
	 *            Unique ID for the job manager
	 * @param jobStatus
	 *            status of the job
	 * @since 5.0
	 */
	public void fireJobAdded(IJobStatus jobStatus) {
		addJob(jobStatus);
		ListenerList listeners = fJobListeners.get(jobStatus.getControlId());
		if (listeners != null) {
			for (Object listener : listeners.getListeners()) {
				((IJobListener) listener).jobAdded(jobStatus);
			}
		}
		listeners = fJobListeners.get(ALL_JOBS);
		if (listeners != null) {
			for (Object listener : listeners.getListeners()) {
				((IJobListener) listener).jobAdded(jobStatus);
			}
		}
	}

	/**
	 * Notify listeners when a job has changed.
	 * 
	 * @param jobId
	 *            ID of job that has changed
	 * @since 5.0
	 */
	public void fireJobChanged(IJobStatus jobStatus) {
		ListenerList listeners = fJobListeners.get(jobStatus.getControlId());
		if (listeners != null) {
			for (Object listener : listeners.getListeners()) {
				((IJobListener) listener).jobChanged(jobStatus);
			}
		}
		listeners = fJobListeners.get(ALL_JOBS);
		if (listeners != null) {
			for (Object listener : listeners.getListeners()) {
				((IJobListener) listener).jobChanged(jobStatus);
			}
		}
		cleanUpJobs();
	}

	/**
	 * Find a job given the job controller ID and the job ID. Note, completed jobs are not kept by the JobManager, so it's possible
	 * the job may no longer exist.
	 * 
	 * @return IJobStatus or null
	 * 
	 * @since 7.0
	 */
	public IJobStatus getJob(String controlId, String jobId) {
		return fJobs.get(controlId + "+" + jobId); //$NON-NLS-1$
	}

	/**
	 * Get all the jobs we know about.
	 * 
	 * @return array containing known jobs
	 * @since 7.0
	 */
	public IJobStatus[] getJobs() {
		return fJobs.values().toArray(new IJobStatus[0]);
	}

	/**
	 * Remove the job listener.
	 * 
	 * @param listener
	 */
	public void removeListener(IJobListener listener) {
		for (ListenerList listeners : fJobListeners.values()) {
			listeners.remove(listener);
		}
	}

	/**
	 * Remove the job listener for jobs under the control of a controller identified by qualifier
	 * 
	 * @param qualifier
	 * @param listener
	 */
	public void removeListener(String qualifier, IJobListener listener) {
		ListenerList listeners = fJobListeners.get(qualifier);
		if (listeners != null) {
			listeners.remove(listener);
		}
	}
}