package org.eclipse.ptp.core.rm;

import java.util.Set;

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
