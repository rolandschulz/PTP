/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.pe.core.rmsystem;

import org.eclipse.ptp.rm.remote.core.IRemoteResourceManagerConfiguration;

public interface IPEResourceManagerConfiguration extends IRemoteResourceManagerConfiguration {

	/**
	 * Get flag indicating LoadLeveler is used to allocate nodes
	 * 
	 * @return the LoadLeveler flag
	 */
	public String getUseLoadLeveler();

	/**
	 * Get flag indicating the miniproxy is to run following main proxy shutdown
	 * 
	 * @return Miniproxy flag
	 */
	public String getRunMiniproxy();

	/**
	 * Get the proxy debug level
	 * 
	 * @return debug level
	 */
	public String getDebugLevel();

	/**
	 * Get the flag indicating proxy should be suspended at startup (for debugging)
	 * 
	 * @return the suspend proxy flag
	 */
	public String getSuspendProxy();

	/**
	 * Set flag indicating whether LoadLeveler is used to allocate nodes
	 * @param useLoadLeveler - flag inidcating LoadLeveler used to allocate nodes
	 */
	public void setUseLoadLeveler(String useLoadLeveler);

	/**
	 * Set flag indicating whether miniproxy should run following proxy shutdown
	 * @param runMiniproxy Flag indicating miniproxy should run
	 */
	public void setRunMiniproxy(String runMiniproxy);

	/**
	 * Set the debug level for the proxy
	 * @param debugLevel Debug level
	 */
	public void setDebugLevel(String debugLevel);

	/**
	 * Set flag indicating whether proxy should be suspended at startup (for debugging)
	 * @param suspendProxy proxy suspension flag
	 */
	public void setSuspendProxy(String suspendProxy);

	/**
	 * Get LoadLeveler run mode (local, multicluster, default)
	 * @return the loadLevelerMode
	 */
	public String getLoadLevelerMode();

	/**
	 * Set LoadLeveler run mode (local, multicluster, default)
	 * @param loadLevelerMode the loadLevelerMode to set
	 */
	public void setLoadLevelerMode(String loadLevelerMode);

	/**
	 * Get the minimum interval to poll LoadLeveler for node status
	 * @return the nodePollMinInterval
	 */
	public String getNodeMinPollInterval();

	/**
	 * Set the minimum interval to poll LoadLeveler for node status
	 * @param nodePollMinInterval the nodePollMinInterval to set
	 */
	public void setNodeMinPollInterval(String nodeMinPollInterval);

	/**
	 * Get the maximum interval to poll LoadLeveler for node status
	 * @return the nodePollMaxInterval
	 */
	public String getNodeMaxPollInterval();

	/**
	 * Set the maximum interval to poll LoadLeveler for node status
	 * @param nodePollMaxInterval the nodePollMaxInterval to set
	 */
	public void setNodeMaxPollInterval(String nodeMaxPollInterval);

	/**
	 * Get the interval to poll LoadLeveler for job status
	 * @return the jobPollInterval
	 */
	public String getJobPollInterval();

	/**
	 * Set the interval to poll LoadLeveler for job status
	 * @param jobPollInterval the jobPollInterval to set
	 */
	public void setJobPollInterval(String jobPollInterval);

	/**
	 * Get the alternate library path for the LoadLeveler API library
	 * @return the libraryOverride
	 */
	public String getLibraryOverride();

	/**
	 * Set the alternate library path for the LoadLeveler API library
	 * @param libraryOverride the libraryOverride to set
	 */
	public void setLibraryOverride(String libraryOverride);

}