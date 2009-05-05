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
package org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.rm.core.rmsystem.AbstractToolRMConfiguration;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIAutoPreferenceManager;
import org.eclipse.ptp.rm.mpi.openmpi.core.messages.Messages;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ui.IMemento;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class OpenMPIResourceManagerConfiguration extends
AbstractToolRMConfiguration implements Cloneable {

	/**
	 * Static class to hold openmpi configuration information
	 *
	 * @author dfferber
	 */
	static public class OpenMpiConfig {

		private ToolsConfig toolsConfig;
		private String versionId;

		public OpenMpiConfig() {
			this(new ToolsConfig(), null);
		}

		public OpenMpiConfig(ToolsConfig toolsConfig, String versionId) {
			super();
			this.toolsConfig = toolsConfig;
			this.versionId = versionId;
		}

		public ToolsConfig getToolsConfig() {
			return toolsConfig;
		}

		public String getVersionId() {
			return versionId;
		}

		public void setToolsConfig(ToolsConfig toolsConfig) {
			this.toolsConfig = toolsConfig;
		}

		public void setVersionId(String versionId) {
			this.versionId = versionId;
		}
	}

	public static int OPENMPI_CAPABILITIES = CAP_LAUNCH | CAP_DISCOVER | CAP_REMOTE_INSTALL_PATH;

	private static final String TAG_VERSION_ID = "versionId"; //$NON-NLS-1$

	public static final String VERSION_UNKNOWN = "0.0"; //$NON-NLS-1$
	public static final String VERSION_AUTO = "auto"; //$NON-NLS-1$
	public static final String VERSION_12 = "1.2"; //$NON-NLS-1$
	public static final String VERSION_13 = "1.3"; //$NON-NLS-1$

	public static OpenMPIResourceManagerConfiguration load(
			OpenMPIResourceManagerFactory factory, IMemento memento) {
		OpenMpiConfig openMpiConfig = loadOpenMpiConfig(factory, memento);
		OpenMPIResourceManagerConfiguration config = new OpenMPIResourceManagerConfiguration(factory, openMpiConfig);
		return config;
	}
	public static OpenMpiConfig loadOpenMpiConfig(IResourceManagerFactory factory,
			IMemento memento) {
		ToolsConfig toolsConfig = loadTool(factory, memento);

		String versionId = memento.getString(TAG_VERSION_ID);

		OpenMpiConfig config = new OpenMpiConfig(toolsConfig, versionId);
		return config;
	}
	/*
	 * Version that is selected when configuring the RM
	 */
	private String versionId;
	
	/*
	 * Actual version that is used to select correct commands. This version
	 * only persists while the RM is running.
	 */
	private int majorVersion = 0;
	private int minorVersion = 0;
	private int serviceVersion = 0;

	public OpenMPIResourceManagerConfiguration(OpenMPIResourceManagerFactory factory) {
		super(OPENMPI_CAPABILITIES, new ToolsConfig(), factory);

		/*
		 * By default, assume openmpi auto configuration.
		 */
		Preferences prefs = OpenMPIAutoPreferenceManager.getPreferences();
		setLaunchCmd(prefs.getString(OpenMPIAutoPreferenceManager.PREFIX + OpenMPIAutoPreferenceManager.PREFS_LAUNCH_CMD));
		setDebugCmd(prefs.getString(OpenMPIAutoPreferenceManager.PREFIX + OpenMPIAutoPreferenceManager.PREFS_DEBUG_CMD));
		setDiscoverCmd(prefs.getString(OpenMPIAutoPreferenceManager.PREFIX + OpenMPIAutoPreferenceManager.PREFS_DISCOVER_CMD));
		setRemoteInstallPath(prefs.getString(OpenMPIAutoPreferenceManager.PREFIX + OpenMPIAutoPreferenceManager.PREFS_REMOTE_INSTALL_PATH));
		setVersionId(VERSION_AUTO);
		setUseInstallDefaults(true);
		setUseToolDefaults(true);
		setCommandsEnabled(false);
	}

	public OpenMPIResourceManagerConfiguration(OpenMPIResourceManagerFactory factory,
			OpenMpiConfig config) {
		super(OPENMPI_CAPABILITIES, config.getToolsConfig(), factory);
		setVersionId(config.getVersionId());
	}

	@Override
	public Object clone() {
		CommonConfig commonConf = new CommonConfig(getName(),
				getDescription(), getUniqueName());
		RemoteConfig remoteConf = new RemoteConfig(commonConf,
				getRemoteServicesId(), getConnectionName(),
				getProxyServerPath(), getLocalAddress(),
				getInvocationOptionsStr(), getOptions());
		ToolsConfig toolsConfig = new ToolsConfig(
				remoteConf,
				getLaunchCmd(),
				getDebugCmd(),
				getDiscoverCmd(),
				getPeriodicMonitorCmd(),
				getPeriodicMonitorTime(),
				getContinuousMonitorCmd(),
				getRemoteInstallPath(),
				getUseToolDefaults(),
				getUseInstallDefaults(),
				getCommandsEnabled());
		OpenMpiConfig openMpiConfig = new OpenMpiConfig(toolsConfig, getVersionId());

		return new OpenMPIResourceManagerConfiguration(
				(OpenMPIResourceManagerFactory) getFactory(), openMpiConfig);
	}

	/**
	 * Get the detected Open MPI version. Only the major and minor version
	 * numbers are used. Any point or beta release information is discarded.
	 * 
	 * @return string representing the detected version 
	 *         or "unknown" if no version has been detected
	 */
	public String getDetectedVersion() {
		return majorVersion + "." + minorVersion; //$NON-NLS-1$
	}

	/**
	 * Get the detected Open MPI service version.
	 * 
	 * @return the detected point version (default 0)
	 */
	public int getServiceVersion() {
		return serviceVersion;
	}

	/**
	 * Get the version selected when configuring the RM
	 * 
	 * @return string representing the Open MPI version
	 */
	public String getVersionId() {
		return versionId;
	}

	@Override
	public void save(IMemento memento) {
		super.save(memento);
		memento.putString(TAG_VERSION_ID, versionId);
	}

	@Override
	public void setDefaultNameAndDesc() {
		String name = Messages.OpenMPIResourceManagerConfiguration_defaultName;
		String conn = getConnectionName();
		if (conn != null && !conn.equals(EMPTY_STRING)) {
			name += "@" + conn; //$NON-NLS-1$
		}
		setName(name);
		setDescription(Messages.OpenMPIResourceManagerConfiguration_defaultDescription);
	}
	
	/**
	 * Set the detected Open MPI version. Allowable version formats are:
	 * 
	 * 1.3		-> major=1, minor=3, point=0
	 * 1.2.8	-> major=1, minor=2, point=8
	 * 1.2b1	-> major=1, minor=2, point=0
	 * 
	 * Currently only 1.2 and 1.3 versions are valid.
	 * 
	 * If the versionId is not VERSION_AUTO, then the detected version
	 * must match the versionId.
	 * 
	 * @param version string representing the detected version
	 * @return true if version was correct
	 */
	public boolean setDetectedVersion(String version) {
		Pattern p = Pattern.compile("^(\\d+)\\.(\\d+)[^.]*(\\.(\\d+))?"); //$NON-NLS-1$
		Matcher m = p.matcher(version);
		if (m.matches()) {
			majorVersion = Integer.valueOf(m.group(1)).intValue();
			minorVersion = Integer.valueOf(m.group(2)).intValue();
			if (m.group(3) != null) {
				serviceVersion = Integer.valueOf(m.group(4)).intValue();
			}
			if (!validateVersion()) {
				return false;
			}
			if (!getVersionId().equals(OpenMPIResourceManagerConfiguration.VERSION_AUTO)) {
					return getVersionId().equals(getDetectedVersion());
			}
			return true;
		}
		return false;
	}

	/**
	 * Set the version that is selected when configuring the RM
	 * 
	 * @param versionId string representing the Open MPI version
	 */
	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}

	private boolean validateVersion() {
		return getDetectedVersion().equals(VERSION_12)
			|| getDetectedVersion().equals(VERSION_13);
	}
}
