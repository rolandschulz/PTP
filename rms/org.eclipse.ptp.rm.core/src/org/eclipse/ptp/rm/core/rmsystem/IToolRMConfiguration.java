/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.core.rmsystem;

import org.eclipse.ptp.rm.remote.core.IRemoteResourceManagerConfiguration;

public interface IToolRMConfiguration extends IRemoteResourceManagerConfiguration {
	public static final int CAP_LAUNCH = 1 << 0;
	public static final int CAP_DISCOVER = 1 << 1;
	public static final int CAP_PERIODIC_MONITOR = 1 << 2;
	public static final int CAP_CONTINUOUS_MONITOR = 1 << 3;
	public static final int CAP_REMOTE_INSTALL_PATH = 1 << 4;
	
	public static final int NO_CAP_SET = 0;
	
	/**
	 * Get the capabilities supported by the RM
	 * 
	 * @return supported capabilities
	 */
	public int getCapabilities();
	
	/**
	 * Get a flag that specifies if the command fields are enabled in the RM configuration UI.
	 * 
	 * @return true if command fields are enabled
	 */
	public boolean getCommandsEnabled();

	/**
	 * Get a command that produces continuous system/job monitoring output.
	 * 
	 * @return the continuous monitor command
	 */
	public String getContinuousMonitorCmd();

	/**
	 * Get the command to launch a debug session.
	 * 
	 * @return the command to launch a debug session
	 */
	public String getDebugCmd();

	/**
	 * Get a command that is used to discover the system configuration.
	 * 
	 * @return the discover command
	 */
	public String getDiscoverCmd();

	/**
	 * Get the command to launch an application.
	 * 
	 * @return the command to launch an application
	 */
	public String getLaunchCmd();

	/**
	 * Get a command that can be used to periodically monitor system/job status.
	 * 
	 * @return the periodic monitor command
	 */
	public String getPeriodicMonitorCmd();

	/**
	 * Get the time interval to delay between issuing periodic monitor commands.
	 * 
	 * @return the time interval delay in milliseconds
	 */
	public int getPeriodicMonitorTime();

	/**
	 * Get the path of the remote runtime system installation.
	 * 
	 * @return the path of the remote runtime system installation
	 */
	public String getRemoteInstallPath();

	/**
	 * Get a flag that specifies if the default install path should be used.
	 * 
	 * @return true if the default install path should be used
	 */
	public boolean getUseInstallDefaults();

	/**
	 * Get a flag that specifies if the default tool commands should be used.
	 * 
	 * @return  true if the default tool commands should be used
	 */
	public boolean getUseToolDefaults();

	/**
	 * Test if the RM has a continuous monitor command capability.
	 * 
	 * @return true if the RM has a continuous monitor command capability
	 */
	public boolean hasContinuousMonitorCmd();

	/**
	 * Test if the RM has a debug command capability.
	 * 
	 * @return true if the RM has a debug command capability
	 */
	public boolean hasDebugCmd();

	/**
	 * Test if the RM has a discover command capability.
	 * 
	 * @return true if the RM has a discover command capability
	 */
	public boolean hasDiscoverCmd();

	/**
	 * Test if the RM has a launch command capability.
	 * 
	 * @return true if the RM has a launch command capability
	 */
	public boolean hasLaunchCmd();

	/**
	 * Test if the RM has a periodic monitor command capability.
	 * 
	 * @return true if the RM has a periodic monitor command capability
	 */
	public boolean hasPeriodicMonitorCmd();

	/**
	 * Set a flag that specifies if the command fields are enabled in the RM configuration UI.
	 * 
	 * @param commandsEnabled true if command fields are enabled
	 */
	public void setCommandsEnabled(boolean commandsEnabled);

	/**
	 * Set a command that produces continuous system/job monitoring output.
	 * 
	 * @param continuousMonitorCmd the continuous monitor command
	 */
	public void setContinuousMonitorCmd(String continuousMonitorCmd);

	/**
	 * Set a command to launch a debug session.
	 * 
	 * @param debugCmd the debug command
	 */
	public void setDebugCmd(String debugCmd);

	/**
	 * Set a command that is used to discover the system configuration.
	 * 
	 * @param discoverCmd the discover command
	 */
	public void setDiscoverCmd(String discoverCmd);

	/**
	 * Set a command to launch an application.
	 * 
	 * @param launchCmd the command to launch an application
	 */
	public void setLaunchCmd(String launchCmd);

	/**
	 * Set a command that can be used to periodically monitor system/job status
	 * 
	 * @param continuousMonitorCmd the periodic monitor command
	 */
	public void setPeriodicMonitorCmd(String periodicMonitorCmd);

	/**
	 * Set the time interval to delay between issuing periodic monitor commands.
	 * 
	 * @param periodicMonitorTime the time interval delay in milliseconds
	 */
	public void setPeriodicMonitorTime(int periodicMonitorTime);

	/**
	 * Set the path of the remote runtime system installation.
	 * 
	 * @param remoteInstallPath the path of the remote runtime system installation
	 */
	public void setRemoteInstallPath(String remoteInstallPath);

	/**
	 * Set a flag that specifies if the default install path should be used.
	 * 
	 * @param useInstallDefaults true if the default install path should be used
	 */
	public void setUseInstallDefaults(boolean useInstallDefaults);

	/**
	 * Set a flag that specifies if the default tool commands should be used.
	 * 
	 * @param useToolDefaults true if the default tool commands should be used
	 */
	public void setUseToolDefaults(boolean useToolDefaults);

}