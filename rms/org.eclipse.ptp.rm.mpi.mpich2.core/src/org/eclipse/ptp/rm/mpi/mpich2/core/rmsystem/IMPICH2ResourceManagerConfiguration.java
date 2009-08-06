package org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem;

import org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration;

public interface IMPICH2ResourceManagerConfiguration extends IToolRMConfiguration {
	public static int MPICH2_CAPABILITIES = CAP_LAUNCH | CAP_DISCOVER | CAP_PERIODIC_MONITOR | CAP_REMOTE_INSTALL_PATH;

	/**
	 * Get the version ID
	 * 
	 * @return version ID
	 */
	public String getVersionId();

	/**
	 * Set the version ID
	 * 
	 * @param versionId
	 */
	public void setVersionId(String versionId);

}