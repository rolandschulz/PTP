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

import java.util.Set;

/**
 * @since 5.0
 */
public interface IResourceManagerListener {
	/**
	 * Accept notification when one or more jobs change status
	 * 
	 * @param jobStatus
	 *            new status of each job
	 * @param jobInfo
	 *            job information for jobs. Only valid for jobs that have
	 *            changed status to DONE or FAILED.
	 */
	public void handleJobStatusChange(Set<IResourceManager.JobStatus> jobStatus, Set<JobInfo> jobInfo);

	/**
	 * Accept notification of resource manager status change
	 * 
	 * @param status
	 *            new status of resource manager session
	 */
	public void handleSessionStatusChange(IResourceManager.SessionStatus status);
}
