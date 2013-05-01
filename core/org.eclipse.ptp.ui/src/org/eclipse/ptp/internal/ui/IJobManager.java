/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.ui;

import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.internal.ui.listeners.IJobChangedListener;
import org.eclipse.ptp.internal.ui.model.IElementHandler;

public interface IJobManager extends IElementManager {
	/**
	 * Add job listener
	 * 
	 * @param jobListener
	 */
	public void addJobChangedListener(IJobChangedListener jobListener);

	/**
	 * Create an element handler for the job
	 * 
	 * @param job
	 *            job
	 * @return element handler for the job
	 * @since 7.0
	 */
	public IElementHandler createElementHandler(IJobStatus job);

	/**
	 * Find a job give its ID.
	 * 
	 * @param jobId
	 *            ID of job to find
	 * @return job with corresponding ID
	 * @since 7.0
	 */
	public IJobStatus findJobById(String jobId);

	/**
	 * Fire job event when job is changed
	 * 
	 * @param type
	 *            job change type or remove type
	 * @param cur_jid
	 * @param pre_jid
	 */
	public void fireJobChangedEvent(int type, String cur_jid, String pre_jid);

	/**
	 * Get the currently selected job.
	 * 
	 * @return currently selected job
	 * @since 7.0
	 */
	public IJobStatus getJob();

	/**
	 * Get Jobs that we know about.
	 * 
	 * @return jobs
	 * @since 7.0
	 */
	public IJobStatus[] getJobs();

	/**
	 * Test if there is at least one completed job.
	 * 
	 * @return true if there is a completed job
	 */
	public boolean hasStoppedJob();

	/**
	 * @since 7.0
	 */
	public void initialize();

	/**
	 * Remove all jobs that have completed from the view
	 * 
	 * @since 7.0
	 */
	public void removeAllCompletedJobs();

	/**
	 * Remove job listener
	 * 
	 * @param jobListener
	 */
	public void removeJobChangedListener(IJobChangedListener jobListener);

	/**
	 * Set the current job
	 * 
	 * @param job
	 *            the current job to set
	 * @since 7.0
	 */
	public void setJob(IJobStatus job);
}
