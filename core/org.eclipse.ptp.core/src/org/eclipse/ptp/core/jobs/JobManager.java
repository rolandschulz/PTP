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

import java.util.HashMap;
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

	private final Map<String, ListenerList> fJobListeners = new HashMap<String, ListenerList>();

	private JobManager() {
	}

	public void addListener(String qualifier, IJobListener listener) {
		ListenerList listeners = fJobListeners.get(qualifier);
		if (listeners == null) {
			listeners = new ListenerList();
			fJobListeners.put(qualifier, listeners);
		}
		listeners.add(listener);
	}

	public void addListener(IJobListener listener) {
		ListenerList listeners = fJobListeners.get(ALL_JOBS);
		if (listeners == null) {
			listeners = new ListenerList();
			fJobListeners.put(ALL_JOBS, listeners);
		}
		listeners.add(listener);
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
	}

	/**
	 * @param listener
	 */
	public void removeListener(IJobListener listener) {
		for (ListenerList listeners : fJobListeners.values()) {
			listeners.remove(listener);
		}
	}

	/**
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