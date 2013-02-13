/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.core;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author arossi
 * 
 */
public interface ICommandJobStatusMap extends Runnable {
	/**
	 * Add job status information to the map.
	 * 
	 * @param jobId
	 *            either internal UUID or scheduler id for the job.
	 * @param status
	 *            object containing status info and stream proxy
	 * @return true if added, false if the entry already exists
	 */
	public boolean addJobStatus(String jobId, ICommandJobStatus status);

	/**
	 * Cancel the job.
	 * 
	 * @param jobId
	 *            either internal UUID or scheduler id for the job.
	 * @return object containing status info and stream proxy
	 */
	public ICommandJobStatus cancel(String jobId);

	/**
	 * Dispose of any map resources
	 */
	public void dispose();

	/**
	 * Get the job status information for the job.
	 * 
	 * @param jobId
	 *            either internal UUID or scheduler id for the job.
	 * @return object containing status info and stream proxy
	 */
	public ICommandJobStatus getStatus(String jobId);

	/**
	 * Initialize the map. Must be called prior to any other methods.
	 */
	public void initialize();

	/**
	 * Check if there are any jobs in the map.
	 * 
	 * @return true if there are no jobs in the map.
	 */
	public boolean isEmpty();

	/**
	 * Notify the map that the job has terminated.
	 * 
	 * @param jobId
	 *            either internal UUID or scheduler id for the job.
	 * @param monitor
	 *            progress monitor
	 * @return object containing status info and stream proxy
	 */
	public ICommandJobStatus terminated(String jobId, IProgressMonitor monitor);
}
