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

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.internal.core.events.JobAddedEvent;
import org.eclipse.ptp.internal.core.events.JobChangedEvent;

/**
 * Job manager for resource manager framework.
 * 
 * @since 6.0
 */
public class JobManager {

	private static final JobManager fInstance = new JobManager();

	public static JobManager getInstance() {
		return fInstance;
	}

	private final ListenerList fJobListeners = new ListenerList();

	private JobManager() {
	}

	public void addListener(IJobListener listener) {
		fJobListeners.add(listener);
	}

	/**
	 * Notify listeners when a job has been added.
	 * 
	 * @param jobId
	 *            ID of job that has been added
	 * @param jobStatus
	 *            status of the job
	 * @since 5.0
	 */
	public void fireJobAdded(IJobStatus jobStatus) {
		IJobAddedEvent e = new JobAddedEvent(jobStatus);

		for (Object listener : fJobListeners.getListeners()) {
			((IJobListener) listener).handleEvent(e);
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
		IJobChangedEvent e = new JobChangedEvent(jobStatus);

		for (Object listener : fJobListeners.getListeners()) {
			((IJobListener) listener).handleEvent(e);
		}
	}

	public void removeListener(IJobListener listener) {
		fJobListeners.remove(listener);
	}
}