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
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIPreferenceManager;
import org.eclipse.ptp.rm.mpi.openmpi.core.messages.Messages;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ui.IMemento;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class OpenMPIResourceManagerConfiguration extends
AbstractToolRMConfiguration implements Cloneable, IOpenMPIResourceManagerConfiguration {

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

	private static final String TAG_VERSION_ID = "versionId"; //$NON-NLS-1$

	public static IOpenMPIResourceManagerConfiguration load(
			OpenMPIResourceManagerFactory factory, IMemento memento) {
		OpenMpiConfig openMpiConfig = loadOpenMpiConfig(factory, memento);
		IOpenMPIResourceManagerConfiguration config = new OpenMPIResourceManagerConfiguration(factory, openMpiConfig);
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
		Preferences prefs = OpenMPIPreferenceManager.getPreferences();
		setLaunchCmd(prefs.getString(OpenMPIPreferenceManager.PREFIX_AUTO + OpenMPIPreferenceManager.PREFS_LAUNCH_CMD));
		setDebugCmd(prefs.getString(OpenMPIPreferenceManager.PREFIX_AUTO + OpenMPIPreferenceManager.PREFS_DEBUG_CMD));
		setDiscoverCmd(prefs.getString(OpenMPIPreferenceManager.PREFIX_AUTO + OpenMPIPreferenceManager.PREFS_DISCOVER_CMD));
		setRemoteInstallPath(prefs.getString(OpenMPIPreferenceManager.PREFIX_AUTO + OpenMPIPreferenceManager.PREFS_REMOTE_INSTALL_PATH));
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
				getDescription(), getUniqueName(),
				getRemoteServicesId(), getConnectionName());
		RemoteConfig remoteConf = new RemoteConfig(commonConf,
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

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.IOpenMPIResourceManagerConfiguration#getDetectedVersion()
	 */
	public String getDetectedVersion() {
		return majorVersion + "." + minorVersion; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.IOpenMPIResourceManagerConfiguration#getServiceVersion()
	 */
	public int getServiceVersion() {
		return serviceVersion;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.IOpenMPIResourceManagerConfiguration#getVersionId()
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.IOpenMPIResourceManagerConfiguration#setDetectedVersion(java.lang.String)
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
			if (!getVersionId().equals(IOpenMPIResourceManagerConfiguration.VERSION_AUTO)) {
				return getVersionId().equals(getDetectedVersion());
			}
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.IOpenMPIResourceManagerConfiguration#setVersionId(java.lang.String)
	 */
	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}

	private boolean validateVersion() {
		return getDetectedVersion().equals(VERSION_12)
			|| getDetectedVersion().equals(VERSION_13)
			|| getDetectedVersion().equals(VERSION_14);
	}
}
