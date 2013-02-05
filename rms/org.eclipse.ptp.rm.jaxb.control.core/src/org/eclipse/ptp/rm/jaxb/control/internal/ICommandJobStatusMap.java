/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.internal;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author arossi
 * 
 */
public interface ICommandJobStatusMap extends Runnable {
	/**
	 * @param jobId
	 *            either internal UUID or scheduler id for the job.
	 * @param status
	 *            object containing status info and stream proxy
	 * @return true if added, false if the entry already exists
	 */
	public boolean addJobStatus(String jobId, ICommandJobStatus status);

	/**
	 * @param jobId
	 *            either internal UUID or scheduler id for the job.
	 * @return object containing status info and stream proxy
	 */
	public ICommandJobStatus cancel(String jobId);

	/**
	 * @param jobId
	 *            either internal UUID or scheduler id for the job.
	 * @return object containing status info and stream proxy
	 */
	public ICommandJobStatus getStatus(String jobId);

	/**
	 * shuts down the map's internal thread
	 */
	public void dispose();

	/**
	 * @param jobId
	 *            either internal UUID or scheduler id for the job.
	 * @param monitor
	 *            progress monitor
	 * @return object containing status info and stream proxy
	 */
	public ICommandJobStatus terminated(String jobId, IProgressMonitor monitor);

	public void initialize();
}
