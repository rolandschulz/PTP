/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
/**
 *
 */
package org.eclipse.ptp.rm.core.rmsystem;

/**
 * @author greg
 * @since 3.0
 */
public abstract class AbstractToolRMConfiguration extends AbstractRemoteResourceManagerConfiguration implements
		IToolRMConfiguration {
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private static final String TAG_LAUNCH_CMD = "launchCmd"; //$NON-NLS-1$
	private static final String TAG_DEBUG_CMD = "debugCmd"; //$NON-NLS-1$
	private static final String TAG_DISCOVER_CMD = "discoverCmd"; //$NON-NLS-1$
	private static final String TAG_PERIODIC_MONITOR_CMD = "periodicMonitorCmd"; //$NON-NLS-1$
	private static final String TAG_PERIODIC_MONITOR_TIME = "periodicMonitorTime"; //$NON-NLS-1$
	private static final String TAG_CONTINUOUS_MONITOR_CMD = "continuousMonitorCmd"; //$NON-NLS-1$
	private static final String TAG_REMOTE_INSTALL_PATH = "remoteInstallPath"; //$NON-NLS-1$
	private static final String TAG_USE_TOOL_DEFAULTS = "useToolDefaults"; //$NON-NLS-1$
	private static final String TAG_USE_INSTALL_DEFAULTS = "useInstallDefaults"; //$NON-NLS-1$
	private static final String TAG_COMMANDS_ENABLED = "commandsEnabled"; //$NON-NLS-1$

	private final int fCapabilities;

	public AbstractToolRMConfiguration(int capabilities) {
		fCapabilities = capabilities;
	}

	/**
	 * Constructor for creating a working copy of the service provider
	 * 
	 * @param provider
	 *            provider we are making a copy from
	 */
	public AbstractToolRMConfiguration(AbstractToolRMConfiguration provider) {
		super(provider);
		fCapabilities = provider.getCapabilities();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getCapabilities()
	 */
	public int getCapabilities() {
		return fCapabilities;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getCommandsEnabled
	 * ()
	 */
	public boolean getCommandsEnabled() {
		return getBoolean(TAG_COMMANDS_ENABLED, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getContinuousMonitorCmd
	 * ()
	 */
	public String getContinuousMonitorCmd() {
		return getString(TAG_CONTINUOUS_MONITOR_CMD, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getDebugCmd()
	 */
	public String getDebugCmd() {
		return getString(TAG_DEBUG_CMD, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getDiscoverCmd()
	 */
	public String getDiscoverCmd() {
		return getString(TAG_DISCOVER_CMD, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getLaunchCmd()
	 */
	public String getLaunchCmd() {
		return getString(TAG_LAUNCH_CMD, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getPeriodicMonitorCmd
	 * ()
	 */
	public String getPeriodicMonitorCmd() {
		return getString(TAG_PERIODIC_MONITOR_CMD, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getPeriodicMonitorTime
	 * ()
	 */
	public int getPeriodicMonitorTime() {
		return getInt(TAG_PERIODIC_MONITOR_TIME, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getRemoteInstallPath
	 * ()
	 */
	public String getRemoteInstallPath() {
		return getString(TAG_REMOTE_INSTALL_PATH, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getUseInstallDefaults
	 * ()
	 */
	public boolean getUseInstallDefaults() {
		return getBoolean(TAG_USE_INSTALL_DEFAULTS, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getUseToolDefaults
	 * ()
	 */
	public boolean getUseToolDefaults() {
		return getBoolean(TAG_USE_TOOL_DEFAULTS, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#hasContinuousMonitorCmd
	 * ()
	 */
	public boolean hasContinuousMonitorCmd() {
		return (fCapabilities & CAP_CONTINUOUS_MONITOR) != 0 && getContinuousMonitorCmd() != null
				&& !getContinuousMonitorCmd().trim().equals(EMPTY_STRING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#hasDebugCmd()
	 */
	public boolean hasDebugCmd() {
		return (fCapabilities & CAP_LAUNCH) != 0 && getDebugCmd() != null && !getDebugCmd().trim().equals(EMPTY_STRING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#hasDiscoverCmd()
	 */
	public boolean hasDiscoverCmd() {
		return (fCapabilities & CAP_DISCOVER) != 0 && getDiscoverCmd() != null && !getDiscoverCmd().trim().equals(EMPTY_STRING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#hasLaunchCmd()
	 */
	public boolean hasLaunchCmd() {
		return (fCapabilities & CAP_LAUNCH) != 0 && getLaunchCmd() != null && !getLaunchCmd().trim().equals(EMPTY_STRING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#hasPeriodicMonitorCmd
	 * ()
	 */
	public boolean hasPeriodicMonitorCmd() {
		return (fCapabilities & CAP_PERIODIC_MONITOR) != 0 && getPeriodicMonitorCmd() != null
				&& !getPeriodicMonitorCmd().trim().equals(EMPTY_STRING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerServiceProvider#
	 * needsDebuggerLaunchHelp()
	 */
	@Override
	public boolean needsDebuggerLaunchHelp() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#setCommandsEnabled
	 * (boolean)
	 */
	public void setCommandsEnabled(boolean commandsEnabled) {
		putBoolean(TAG_COMMANDS_ENABLED, commandsEnabled);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#setContinuousMonitorCmd
	 * (java.lang.String)
	 */
	public void setContinuousMonitorCmd(String continuousMonitorCmd) {
		putString(TAG_CONTINUOUS_MONITOR_CMD, continuousMonitorCmd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#setDebugCmd(java
	 * .lang.String)
	 */
	public void setDebugCmd(String debugCmd) {
		putString(TAG_DEBUG_CMD, debugCmd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#setDiscoverCmd(
	 * java.lang.String)
	 */
	public void setDiscoverCmd(String discoverCmd) {
		putString(TAG_DISCOVER_CMD, discoverCmd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#setLaunchCmd(java
	 * .lang.String)
	 */
	public void setLaunchCmd(String launchCmd) {
		putString(TAG_LAUNCH_CMD, launchCmd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#setPeriodicMonitorCmd
	 * (java.lang.String)
	 */
	public void setPeriodicMonitorCmd(String periodicMonitorCmd) {
		putString(TAG_PERIODIC_MONITOR_CMD, periodicMonitorCmd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#setPeriodicMonitorTime
	 * (int)
	 */
	public void setPeriodicMonitorTime(int periodicMonitorTime) {
		putInt(TAG_PERIODIC_MONITOR_TIME, periodicMonitorTime);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#setRemoteInstallPath
	 * (java.lang.String)
	 */
	public void setRemoteInstallPath(String remoteInstallPath) {
		putString(TAG_REMOTE_INSTALL_PATH, remoteInstallPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#setUseInstallDefaults
	 * (boolean)
	 */
	public void setUseInstallDefaults(boolean useInstallDefaults) {
		putBoolean(TAG_USE_INSTALL_DEFAULTS, useInstallDefaults);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#setUseToolDefaults
	 * (boolean)
	 */
	public void setUseToolDefaults(boolean useToolDefaults) {
		putBoolean(TAG_USE_TOOL_DEFAULTS, useToolDefaults);
	}
}
