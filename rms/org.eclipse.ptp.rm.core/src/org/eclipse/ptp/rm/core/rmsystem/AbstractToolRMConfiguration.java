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

import org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ui.IMemento;

/*
 * TODO: Make this class not extend AbstractRemoteResourceManagerConfiguration/ -
 * Remove references to RemoteConfig - Create new attribute(s) to store remote
 * target, as done on AbstractRemoteResourceManagerConfiguration, but without
 * proxy settings.
 */
/**
 * @author Daniel Felix Ferber
 */
public abstract class AbstractToolRMConfiguration extends
AbstractRemoteResourceManagerConfiguration implements Cloneable, IToolRMConfiguration {
	/**
	 * Static class to hold tool configuration information
	 *
	 * @author dfferber
	 */
	static public class ToolsConfig {
		private RemoteConfig remoteConfig;
		private String launchCmd;
		private String debugCmd;
		private String discoverCmd;
		private String periodicMonitorCmd;
		private int periodicMonitorTime;
		private String continuousMonitorCmd;
		private String remoteInstallPath;
		private boolean useToolDefaults;
		private boolean useInstallDefaults;
		private boolean commandsEnabled;

		public ToolsConfig() {
			this(new RemoteConfig(), null, null, null, null, 0, null, null, true, true, true);
		}

		public ToolsConfig(RemoteConfig remoteConfig, String launchCmd, String debugCmd,
				String discoverCmd, String periodicMonitorCmd,
				int periodicMonitorTime, String continuousMonitorCmd,
				String remoteInstallPath, boolean useToolDefaults,
				boolean useInstallDefaults, boolean commandsEnabled) {
			super();
			this.remoteConfig = remoteConfig;
			this.launchCmd = launchCmd;
			this.debugCmd = debugCmd;
			this.discoverCmd = discoverCmd;
			this.periodicMonitorCmd = periodicMonitorCmd;
			this.periodicMonitorTime = periodicMonitorTime;
			this.continuousMonitorCmd = continuousMonitorCmd;
			this.remoteInstallPath = remoteInstallPath;
			this.useToolDefaults = useToolDefaults;
			this.useInstallDefaults = useInstallDefaults;
			this.commandsEnabled = commandsEnabled;
		}

		public boolean getCommandsEnabled() {
			return commandsEnabled;
		}

		public String getContinuousMonitorCmd() {
			return continuousMonitorCmd;
		}

		public String getDebugCmd() {
			return debugCmd;
		}

		public String getDiscoverCmd() {
			return discoverCmd;
		}

		public String getLaunchCmd() {
			return launchCmd;
		}

		public String getPeriodicMonitorCmd() {
			return periodicMonitorCmd;
		}

		public int getPeriodicMonitorTime() {
			return periodicMonitorTime;
		}

		public RemoteConfig getRemoteConfig() {
			return remoteConfig;
		}

		public String getRemoteInstallPath() {
			return remoteInstallPath;
		}

		public boolean getUseInstallDefaults() {
			return useInstallDefaults;
		}

		public boolean getUseToolDefaults() {
			return useToolDefaults;
		}

		public void setCommandsEnabled(boolean commandsEnabled) {
			this.commandsEnabled = commandsEnabled;
		}

		public void setContinuousMonitorCmd(String continuousMonitorCmd) {
			this.continuousMonitorCmd = continuousMonitorCmd;
		}

		public void setDebugCmd(String debugCmd) {
			this.debugCmd = debugCmd;
		}

		public void setDiscoverCmd(String discoverCmd) {
			this.discoverCmd = discoverCmd;
		}

		public void setLaunchCmd(String launchCmd) {
			this.launchCmd = launchCmd;
		}

		public void setPeriodicMonitorCmd(String periodicMonitorCmd) {
			this.periodicMonitorCmd = periodicMonitorCmd;
		}

		public void setPeriodicMonitorTime(int periodicMonitorTime) {
			this.periodicMonitorTime = periodicMonitorTime;
		}

		public void setRemoteConfig(RemoteConfig remoteConfig) {
			this.remoteConfig = remoteConfig;
		}

		public void setRemoteInstallPath(String remoteInstallPath) {
			this.remoteInstallPath = remoteInstallPath;
		}

		public void setUseInstallDefaults(boolean useInstallDefaults) {
			this.useInstallDefaults = useInstallDefaults;
		}

		public void setUseToolDefaults(boolean useToolDefaults) {
			this.useToolDefaults = useToolDefaults;
		}
	}

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

	public static ToolsConfig loadTool(IResourceManagerFactory factory,
			IMemento memento) {
		RemoteConfig remoteConfig = loadRemote(factory, memento);

		String launchCmd = memento.getString(TAG_LAUNCH_CMD);
		String debugCmd = memento.getString(TAG_DEBUG_CMD);
		String discoverCmd = memento.getString(TAG_DISCOVER_CMD);
		String periodicMonitorCmd = memento.getString(TAG_PERIODIC_MONITOR_CMD);
		Integer periodicMonitorTime = memento.getInteger(TAG_PERIODIC_MONITOR_TIME);
		String continuousMonitorCmd = memento.getString(TAG_CONTINUOUS_MONITOR_CMD);
		String remoteInstallPath = memento.getString(TAG_REMOTE_INSTALL_PATH);
		boolean useToolDefaults = memento.getBoolean(TAG_USE_TOOL_DEFAULTS).booleanValue();
		boolean useInstallDefaults = memento.getBoolean(TAG_USE_INSTALL_DEFAULTS).booleanValue();
		boolean commandsEnabled = memento.getBoolean(TAG_COMMANDS_ENABLED).booleanValue();

		ToolsConfig config = new ToolsConfig(remoteConfig, launchCmd, debugCmd,
				discoverCmd, periodicMonitorCmd, periodicMonitorTime.intValue(),
				continuousMonitorCmd, remoteInstallPath, useToolDefaults, 
				useInstallDefaults, commandsEnabled);
		return config;
	}
	private String launchCmd;
	private String debugCmd;
	private String discoverCmd;
	private String periodicMonitorCmd;
	private int periodicMonitorTime;
	private String continuousMonitorCmd;
	private String remoteInstallPath;
	private boolean useToolDefaults;
	private boolean useInstallDefaults;
	private boolean commandsEnabled;

	private int capabilities;

	public AbstractToolRMConfiguration(int capabilities,
			AbstractToolRMFactory factory, RemoteConfig config, String toolId,
			String launchCmd, String debugCmd, String discoverCmd, String periodicMonitorCmd,
			int periodicMonitorTime, String continuousMonitorCmd,
			String remoteInstallPath, boolean useToolDefaults,
			boolean useInstallDefaults, boolean commandsEnabled) {
		super(config, factory);
		this.capabilities = capabilities;
		setLaunchCmd(launchCmd);
		setDebugCmd(debugCmd);
		setDiscoverCmd(discoverCmd);
		setPeriodicMonitorCmd(periodicMonitorCmd);
		setPeriodicMonitorTime(periodicMonitorTime);
		setContinuousMonitorCmd(continuousMonitorCmd);
		setRemoteInstallPath(remoteInstallPath);
		setUseToolDefaults(useToolDefaults);
		setUseInstallDefaults(useInstallDefaults);
		setCommandsEnabled(commandsEnabled);
	}

	public AbstractToolRMConfiguration(int capabilities,
			ToolsConfig toolsConfig, IResourceManagerFactory factory) {
		super(toolsConfig.getRemoteConfig(), factory);
		this.capabilities = capabilities;
		this.launchCmd = toolsConfig.getLaunchCmd();
		this.debugCmd = toolsConfig.getDebugCmd();
		this.discoverCmd = toolsConfig.getDiscoverCmd();
		this.periodicMonitorCmd = toolsConfig.getPeriodicMonitorCmd();
		this.periodicMonitorTime = toolsConfig.getPeriodicMonitorTime();
		this.continuousMonitorCmd = toolsConfig.getContinuousMonitorCmd();
		this.remoteInstallPath = toolsConfig.getRemoteInstallPath();
		this.useToolDefaults = toolsConfig.getUseToolDefaults();
		this.useInstallDefaults = toolsConfig.getUseInstallDefaults();
		this.commandsEnabled = toolsConfig.getCommandsEnabled();
	}

	@Override
	public abstract Object clone();

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getCapabilities()
	 */
	public int getCapabilities() {
		return capabilities;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getCommandsEnabled()
	 */
	public boolean getCommandsEnabled() {
		return commandsEnabled;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getContinuousMonitorCmd()
	 */
	public String getContinuousMonitorCmd() {
		return continuousMonitorCmd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getDebugCmd()
	 */
	public String getDebugCmd() {
		return debugCmd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getDiscoverCmd()
	 */
	public String getDiscoverCmd() {
		return discoverCmd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getLaunchCmd()
	 */
	public String getLaunchCmd() {
		return launchCmd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getPeriodicMonitorCmd()
	 */
	public String getPeriodicMonitorCmd() {
		return periodicMonitorCmd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getPeriodicMonitorTime()
	 */
	public int getPeriodicMonitorTime() {
		return periodicMonitorTime;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getRemoteInstallPath()
	 */
	public String getRemoteInstallPath() {
		return remoteInstallPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getUseInstallDefaults()
	 */
	public boolean getUseInstallDefaults() {
		return useInstallDefaults;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#getUseToolDefaults()
	 */
	public boolean getUseToolDefaults() {
		return useToolDefaults;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#hasContinuousMonitorCmd()
	 */
	public boolean hasContinuousMonitorCmd() {
		return (capabilities & CAP_CONTINUOUS_MONITOR) != 0
		&& continuousMonitorCmd != null
		&& !continuousMonitorCmd.trim().equals(EMPTY_STRING);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#hasDebugCmd()
	 */
	public boolean hasDebugCmd() {
		return (capabilities & CAP_LAUNCH) != 0 && debugCmd != null
		&& !debugCmd.trim().equals(EMPTY_STRING);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#hasDiscoverCmd()
	 */
	public boolean hasDiscoverCmd() {
		return (capabilities & CAP_DISCOVER) != 0 && discoverCmd != null
		&& !discoverCmd.trim().equals(EMPTY_STRING);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#hasLaunchCmd()
	 */
	public boolean hasLaunchCmd() {
		return (capabilities & CAP_LAUNCH) != 0 && launchCmd != null
		&& !launchCmd.trim().equals(EMPTY_STRING);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#hasPeriodicMonitorCmd()
	 */
	public boolean hasPeriodicMonitorCmd() {
		return (capabilities & CAP_PERIODIC_MONITOR) != 0
		&& periodicMonitorCmd != null
		&& !periodicMonitorCmd.trim().equals(EMPTY_STRING);
	}

	@Override
	public void save(IMemento memento) {
		super.save(memento);
		memento.putString(TAG_LAUNCH_CMD, launchCmd);
		memento.putString(TAG_DEBUG_CMD, debugCmd);
		memento.putString(TAG_DISCOVER_CMD, discoverCmd);
		memento.putString(TAG_PERIODIC_MONITOR_CMD, periodicMonitorCmd);
		memento.putInteger(TAG_PERIODIC_MONITOR_TIME, periodicMonitorTime);
		memento.putString(TAG_CONTINUOUS_MONITOR_CMD, continuousMonitorCmd);
		memento.putString(TAG_REMOTE_INSTALL_PATH, remoteInstallPath);
		memento.putBoolean(TAG_USE_TOOL_DEFAULTS, useToolDefaults);
		memento.putBoolean(TAG_USE_INSTALL_DEFAULTS, useInstallDefaults);
		memento.putBoolean(TAG_COMMANDS_ENABLED, commandsEnabled);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#setCommandsEnabled(boolean)
	 */
	public void setCommandsEnabled(boolean commandsEnabled) {
		this.commandsEnabled = commandsEnabled;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#setContinuousMonitorCmd(java.lang.String)
	 */
	public void setContinuousMonitorCmd(String continuousMonitorCmd) {
		this.continuousMonitorCmd = continuousMonitorCmd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#setDebugCmd(java.lang.String)
	 */
	public void setDebugCmd(String debugCmd) {
		this.debugCmd = debugCmd;
	}

	abstract public void setDefaultNameAndDesc();

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#setDiscoverCmd(java.lang.String)
	 */
	public void setDiscoverCmd(String discoverCmd) {
		this.discoverCmd = discoverCmd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#setLaunchCmd(java.lang.String)
	 */
	public void setLaunchCmd(String launchCmd) {
		this.launchCmd = launchCmd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#setPeriodicMonitorCmd(java.lang.String)
	 */
	public void setPeriodicMonitorCmd(String periodicMonitorCmd) {
		this.periodicMonitorCmd = periodicMonitorCmd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#setPeriodicMonitorTime(int)
	 */
	public void setPeriodicMonitorTime(int periodicMonitorTime) {
		this.periodicMonitorTime = periodicMonitorTime;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#setRemoteInstallPath(java.lang.String)
	 */
	public void setRemoteInstallPath(String remoteInstallPath) {
		this.remoteInstallPath = remoteInstallPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#setUseInstallDefaults(boolean)
	 */
	public void setUseInstallDefaults(boolean useInstallDefaults) {
		this.useInstallDefaults = useInstallDefaults;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration#setUseToolDefaults(boolean)
	 */
	public void setUseToolDefaults(boolean useToolDefaults) {
		this.useToolDefaults = useToolDefaults;
	}
}
