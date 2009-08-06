package org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem;

import org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration;

public interface IMPICH2ResourceManagerConfiguration extends IToolRMConfiguration {
	public static int MPICH2_CAPABILITIES = CAP_LAUNCH | CAP_DISCOVER | CAP_PERIODIC_MONITOR | CAP_REMOTE_INSTALL_PATH;

	/**
	 * Get the version selected when configuring the RM
	 * 
	 * @return string representing the MPICH2 version
	 */
	public String getVersionId();

	/**
	 * Set the version that is selected when configuring the RM
	 * 
	 * @param versionId string representing the MPICH2 version
	 */
	public void setVersionId(String versionId);

}