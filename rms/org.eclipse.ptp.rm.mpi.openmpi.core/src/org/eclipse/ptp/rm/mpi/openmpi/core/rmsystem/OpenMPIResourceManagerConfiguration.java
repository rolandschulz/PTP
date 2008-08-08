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

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.rm.core.rmsystem.AbstractToolRMConfiguration;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPI13PreferenceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ui.IMemento;

public class OpenMPIResourceManagerConfiguration extends
		AbstractToolRMConfiguration implements Cloneable {

	public static int OPENMPI_CAPABILITIES = CAP_LAUNCH | CAP_DISCOVER | CAP_REMOTE_INSTALL_PATH;

	private static final String TAG_VERSION_ID = "versionId"; //$NON-NLS-1$

	public static final String VERSION_12 = "openmpi-1.2";
	public static final String VERSION_13 = "openmpi-1.3";


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

		public void setToolsConfig(ToolsConfig toolsConfig) {
			this.toolsConfig = toolsConfig;
		}

		public String getVersionId() {
			return versionId;
		}

		public void setVersionId(String versionId) {
			this.versionId = versionId;
		}
	}

	private String versionId;

	public static OpenMpiConfig loadOpenMpiConfig(IResourceManagerFactory factory,
			IMemento memento) {
		ToolsConfig toolsConfig = loadTool(factory, memento);

		String versionId = memento.getString(TAG_VERSION_ID);

		OpenMpiConfig config = new OpenMpiConfig(toolsConfig, versionId);
		return config;
	}

	public static OpenMPIResourceManagerConfiguration load(
			OpenMPIResourceManagerFactory factory, IMemento memento) {
		OpenMpiConfig openMpiConfig = loadOpenMpiConfig(factory, memento);
		OpenMPIResourceManagerConfiguration config = new OpenMPIResourceManagerConfiguration(factory, openMpiConfig);
		return config;
	}

	public OpenMPIResourceManagerConfiguration(OpenMPIResourceManagerFactory factory) {
		super(OPENMPI_CAPABILITIES, new ToolsConfig(), factory);

		/*
		 * By default, assume openmpi 1.3 configuration.
		 */
		Preferences prefs = OpenMPI13PreferenceManager.getPreferences();
		setLaunchCmd(prefs.getString(OpenMPI13PreferenceManager.PREFIX + OpenMPI13PreferenceManager.PREFS_LAUNCH_CMD));
		setDebugCmd(prefs.getString(OpenMPI13PreferenceManager.PREFIX + OpenMPI13PreferenceManager.PREFS_DEBUG_CMD));
		setDiscoverCmd(prefs.getString(OpenMPI13PreferenceManager.PREFIX + OpenMPI13PreferenceManager.PREFS_DISCOVER_CMD));
		setRemoteInstallPath(prefs.getString(OpenMPI13PreferenceManager.PREFIX + OpenMPI13PreferenceManager.PREFS_REMOTE_INSTALL_PATH));
		setUseDefaults(prefs.getBoolean(OpenMPI13PreferenceManager.PREFIX + OpenMPI13PreferenceManager.PREFS_USE_DEFAULTS));
		setVersionId(VERSION_13);
	}

	public OpenMPIResourceManagerConfiguration(OpenMPIResourceManagerFactory factory,
			OpenMpiConfig config) {
		/*
		 * By default, assume openmpi 1.3 configuration.
		 */
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
				useDefaults());
		OpenMpiConfig openMpiConfig = new OpenMpiConfig(toolsConfig, getVersionId());

		return new OpenMPIResourceManagerConfiguration(
				(OpenMPIResourceManagerFactory) getFactory(), openMpiConfig);
	}

	@Override
	public void save(IMemento memento) {
		super.save(memento);
		memento.putString(TAG_VERSION_ID, versionId);
	}

	@Override
	public void setDefaultNameAndDesc() {
		String name = "Open_MPI";
		String conn = getConnectionName();
		if (conn != null && !conn.equals(EMPTY_STRING)) {
			name += "@" + conn; //$NON-NLS-1$
		}
		setName(name);
		setDescription("Open MPI Resource Manager");
	}

	public String getVersionId() {
		return versionId;
	}

	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}
}
