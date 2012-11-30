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
package org.eclipse.ptp.ui;

import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.ui.listeners.IJobChangedListener;
import org.eclipse.ptp.ui.model.IElementHandler;

public interface IJobManager extends IElementManager {
	/**
	 * Add job listener
	 * 
	 * @param jobListener
	 */
	public void addJobChangedListener(IJobChangedListener jobListener);

	/**
	 * Add a process to the view.
	 * 
	 * @param job
	 * @param procJobRank
	 * @since 4.0
	 */
	public void addProcess(IPJob job, int procJobRank);

	/**
	 * Create an element handler for the job
	 * 
	 * @param job
	 *            job
	 * @return element handler for the job
	 */
	public IElementHandler createElementHandler(IPJob job);

	/**
	 * Find a job give its ID.
	 * 
	 * @param jobId
	 *            ID of job to find
	 * @return job with corresponding ID
	 */
	public IPJob findJobById(String jobId);

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
	 */
	public IPJob getJob();

	/**
	 * Get Jobs that we know about.
	 * 
	 * @return jobs
	 */
	public IPJob[] getJobs();

	/**
	 * Get the currently selected queue.
	 * 
	 * @return currently selected queue
	 */
	public IPQueue getQueue();

	/**
	 * Test if there is at least one completed job.
	 * 
	 * @return true if there is a completed job
	 */
	public boolean hasStoppedJob();

	/**
	 * Remove all jobs that have completed from the view
	 */
	public void removeAllStoppedJobs();

	/**
	 * Remove job from view.
	 * 
	 * @param job
	 */
	public void removeJob(IPJob job);

	/**
	 * Remove job listener
	 * 
	 * @param jobListener
	 */
	public void removeJobChangedListener(IJobChangedListener jobListener);

	/**
	 * Remove a process from the view.
	 * 
	 * @param job
	 * @param procJobRank
	 * @since 4.0
	 */
	public void removeProcess(IPJob job, int procJobRank);

	/**
	 * Set the current job
	 * 
	 * @param job
	 *            the current job to set
	 */
	public void setJob(IPJob job);
}
