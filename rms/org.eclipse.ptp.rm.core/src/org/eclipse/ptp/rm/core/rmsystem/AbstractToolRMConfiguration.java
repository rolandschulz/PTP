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
public abstract class AbstractToolRMConfiguration extends
		AbstractRemoteResourceManagerConfiguration implements Cloneable {
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private static final String TAG_LAUNCH_CMD = "launchCmd"; //$NON-NLS-1$
	private static final String TAG_DISCOVER_CMD = "discoverCmd"; //$NON-NLS-1$
	private static final String TAG_PERIODIC_MONITOR_CMD = "periodicMonitorCmd"; //$NON-NLS-1$
	private static final String TAG_PERIODIC_MONITOR_TIME = "periodicMonitorTime"; //$NON-NLS-1$
	private static final String TAG_CONTINUOUS_MONITOR_CMD = "continuousMonitorCmd"; //$NON-NLS-1$
	private static final String TAG_REMOTE_INSTALL_PATH = "remoteInstallPath"; //$NON-NLS-1$
	private static final String TAG_USE_DEFAULTS = "useDefaults"; //$NON-NLS-1$

	public static final int CAP_LAUNCH = 1 << 0;
	public static final int CAP_DISCOVER = 1 << 1;
	public static final int CAP_PERIODIC_MONITOR = 1 << 2;
	public static final int CAP_CONTINUOUS_MONITOR = 1 << 3;
	public static final int CAP_REMOTE_INSTALL_PATH = 1 << 4;

	public static final int NO_CAP_SET = 0;

	/**
	 * Static class to hold tool configuration information
	 *
	 * @author dfferber
	 */
	static public class ToolsConfig {
		private RemoteConfig remoteConfig;
		private String launchCmd;
		private String discoverCmd;
		private String periodicMonitorCmd;
		private int periodicMonitorTime;
		private String continuousMonitorCmd;
		private String remoteInstallPath;
		private boolean useDefaults;

		public ToolsConfig() {
			this(new RemoteConfig(), null, null, null, 0, null, null, true);
		}

		public ToolsConfig(RemoteConfig remoteConfig, String launchCmd,
				String discoverCmd, String periodicMonitorCmd,
				int periodicMonitorTime, String continuousMonitorCmd,
				String remoteInstallPath, boolean useDefaults) {
			super();
			this.remoteConfig = remoteConfig;
			this.launchCmd = launchCmd;
			this.discoverCmd = discoverCmd;
			this.periodicMonitorCmd = periodicMonitorCmd;
			this.periodicMonitorTime = periodicMonitorTime;
			this.continuousMonitorCmd = continuousMonitorCmd;
			this.remoteInstallPath = remoteInstallPath;
			this.useDefaults = useDefaults;
		}

		public RemoteConfig getRemoteConfig() {
			return remoteConfig;
		}

		public void setRemoteConfig(RemoteConfig remoteConfig) {
			this.remoteConfig = remoteConfig;
		}

		public String getLaunchCmd() {
			return launchCmd;
		}

		public void setLaunchCmd(String launchCmd) {
			this.launchCmd = launchCmd;
		}

		public String getDiscoverCmd() {
			return discoverCmd;
		}

		public void setDiscoverCmd(String discoverCmd) {
			this.discoverCmd = discoverCmd;
		}

		public String getPeriodicMonitorCmd() {
			return periodicMonitorCmd;
		}

		public void setPeriodicMonitorCmd(String periodicMonitorCmd) {
			this.periodicMonitorCmd = periodicMonitorCmd;
		}

		public int getPeriodicMonitorTime() {
			return periodicMonitorTime;
		}

		public void setPeriodicMonitorTime(int periodicMonitorTime) {
			this.periodicMonitorTime = periodicMonitorTime;
		}

		public String getContinuousMonitorCmd() {
			return continuousMonitorCmd;
		}

		public void setContinuousMonitorCmd(String continuousMonitorCmd) {
			this.continuousMonitorCmd = continuousMonitorCmd;
		}

		public String getRemoteInstallPath() {
			return remoteInstallPath;
		}

		public void setRemoteInstallPath(String remoteInstallPath) {
			this.remoteInstallPath = remoteInstallPath;
		}

		public boolean useDefaults() {
			return useDefaults;
		}

		public void setUseDefaults(boolean useDefaults) {
			this.useDefaults = useDefaults;
		}
	}

	private String launchCmd;
	private String discoverCmd;
	private String periodicMonitorCmd;
	private int periodicMonitorTime;
	private String continuousMonitorCmd;
	private String remoteInstallPath;
	private boolean useDefaults;
	private int capabilities;

	public static ToolsConfig loadTool(IResourceManagerFactory factory,
			IMemento memento) {
		RemoteConfig remoteConfig = loadRemote(factory, memento);

		String launchCmd = memento.getString(TAG_LAUNCH_CMD);
		String discoverCmd = memento.getString(TAG_DISCOVER_CMD);
		String periodicMonitorCmd = memento.getString(TAG_PERIODIC_MONITOR_CMD);
		Integer periodicMonitorTime = memento
				.getInteger(TAG_PERIODIC_MONITOR_TIME);
		String continuousMonitorCmd = memento
				.getString(TAG_CONTINUOUS_MONITOR_CMD);
		String remoteInstallPath = memento.getString(TAG_REMOTE_INSTALL_PATH);
		boolean useDefaults = Boolean.parseBoolean(memento
				.getString(TAG_USE_DEFAULTS));

		ToolsConfig config = new ToolsConfig(remoteConfig, launchCmd,
				discoverCmd, periodicMonitorCmd, periodicMonitorTime,
				continuousMonitorCmd, remoteInstallPath, useDefaults);
		return config;
	}

	public AbstractToolRMConfiguration(int capabilities,
			ToolsConfig toolsConfig, IResourceManagerFactory factory) {
		super(toolsConfig.getRemoteConfig(), factory);
		this.capabilities = capabilities;
		this.launchCmd = toolsConfig.getLaunchCmd();
		this.discoverCmd = toolsConfig.getDiscoverCmd();
		this.periodicMonitorCmd = toolsConfig.getPeriodicMonitorCmd();
		this.periodicMonitorTime = toolsConfig.getPeriodicMonitorTime();
		this.continuousMonitorCmd = toolsConfig.getContinuousMonitorCmd();
		this.remoteInstallPath = toolsConfig.getRemoteInstallPath();
		this.useDefaults = toolsConfig.useDefaults();
	}

	public AbstractToolRMConfiguration(int capabilities,
			AbstractToolRMFactory factory, RemoteConfig config, String toolId,
			String launchCmd, String discoverCmd, String periodicMonitorCmd,
			int periodicMonitorTime, String continuousMonitorCmd,
			String remoteInstallPath, boolean useDefaults) {
		super(config, factory);
		this.capabilities = capabilities;
		setLaunchCmd(launchCmd);
		setDiscoverCmd(discoverCmd);
		setPeriodicMonitorCmd(periodicMonitorCmd);
		setPeriodicMonitorTime(periodicMonitorTime);
		setContinuousMonitorCmd(continuousMonitorCmd);
		setRemoteInstallPath(remoteInstallPath);
		setUseDefaults(useDefaults);
	}

	@Override
	public abstract Object clone();

	@Override
	public void save(IMemento memento) {
		super.save(memento);
		memento.putString(TAG_LAUNCH_CMD, launchCmd);
		memento.putString(TAG_DISCOVER_CMD, discoverCmd);
		memento.putString(TAG_PERIODIC_MONITOR_CMD, periodicMonitorCmd);
		memento.putInteger(TAG_PERIODIC_MONITOR_TIME, periodicMonitorTime);
		memento.putString(TAG_CONTINUOUS_MONITOR_CMD, continuousMonitorCmd);
		memento.putString(TAG_REMOTE_INSTALL_PATH, remoteInstallPath);
		memento.putString(TAG_USE_DEFAULTS, Boolean.toString(useDefaults));
	}

	abstract public void setDefaultNameAndDesc();

	public String getLaunchCmd() {
		return launchCmd;
	}

	public void setLaunchCmd(String launchCmd) {
		this.launchCmd = launchCmd;
	}

	public String getDiscoverCmd() {
		return discoverCmd;
	}

	public void setDiscoverCmd(String discoverCmd) {
		this.discoverCmd = discoverCmd;
	}

	public String getPeriodicMonitorCmd() {
		return periodicMonitorCmd;
	}

	public void setPeriodicMonitorCmd(String periodicMonitorCmd) {
		this.periodicMonitorCmd = periodicMonitorCmd;
	}

	public int getPeriodicMonitorTime() {
		return periodicMonitorTime;
	}

	public void setPeriodicMonitorTime(int periodicMonitorTime) {
		this.periodicMonitorTime = periodicMonitorTime;
	}

	public String getContinuousMonitorCmd() {
		return continuousMonitorCmd;
	}

	public void setContinuousMonitorCmd(String continuousMonitorCmd) {
		this.continuousMonitorCmd = continuousMonitorCmd;
	}

	public String getRemoteInstallPath() {
		return remoteInstallPath;
	}

	public void setRemoteInstallPath(String remoteInstallPath) {
		this.remoteInstallPath = remoteInstallPath;
	}

	public boolean useDefaults() {
		return useDefaults;
	}

	public void setUseDefaults(boolean useDefaults) {
		this.useDefaults = useDefaults;
	}

	public boolean hasDiscoverCmd() {
		return (capabilities & CAP_DISCOVER) != 0 && discoverCmd != null
				&& !discoverCmd.trim().equals(EMPTY_STRING);
	}

	public boolean hasLaunchCmd() {
		return (capabilities & CAP_LAUNCH) != 0 && launchCmd != null
				&& !launchCmd.trim().equals(EMPTY_STRING);
	}

	public boolean hasContinuousMonitorCmd() {
		return (capabilities & CAP_CONTINUOUS_MONITOR) != 0
				&& continuousMonitorCmd != null
				&& !continuousMonitorCmd.trim().equals(EMPTY_STRING);
	}

	public boolean hasPeriodicMonitorCmd() {
		return (capabilities & CAP_PERIODIC_MONITOR) != 0
				&& periodicMonitorCmd != null
				&& !periodicMonitorCmd.trim().equals(EMPTY_STRING);
	}

	protected int getCapabilities() {
		return capabilities;
	}
}
