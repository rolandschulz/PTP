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
package org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.rm.core.rmsystem.AbstractToolRMConfiguration;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2PreferenceManager;
import org.eclipse.ptp.rm.mpi.mpich2.core.messages.Messages;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ui.IMemento;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class MPICH2ResourceManagerConfiguration extends
AbstractToolRMConfiguration implements Cloneable {

	public static int MPICH2_CAPABILITIES = CAP_LAUNCH | CAP_DISCOVER | CAP_PERIODIC_MONITOR | CAP_REMOTE_INSTALL_PATH;

	private static final String TAG_VERSION_ID = "versionId"; //$NON-NLS-1$

	/**
	 * Static class to hold openmpi configuration information
	 *
	 * @author dfferber
	 */
	static public class MPICH2Config {

		private ToolsConfig toolsConfig;
		private String versionId;

		public MPICH2Config() {
			this(new ToolsConfig(), null);
		}

		public MPICH2Config(ToolsConfig toolsConfig, String versionId) {
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

	public static MPICH2Config loadOpenMpiConfig(IResourceManagerFactory factory,
			IMemento memento) {
		ToolsConfig toolsConfig = loadTool(factory, memento);

		String versionId = memento.getString(TAG_VERSION_ID);

		MPICH2Config config = new MPICH2Config(toolsConfig, versionId);
		return config;
	}

	public static MPICH2ResourceManagerConfiguration load(
			MPICH2ResourceManagerFactory factory, IMemento memento) {
		MPICH2Config mpich2Config = loadOpenMpiConfig(factory, memento);
		MPICH2ResourceManagerConfiguration config = new MPICH2ResourceManagerConfiguration(factory, mpich2Config);
		return config;
	}

	public MPICH2ResourceManagerConfiguration(MPICH2ResourceManagerFactory factory) {
		super(MPICH2_CAPABILITIES, new ToolsConfig(), factory);

		Preferences prefs = MPICH2PreferenceManager.getPreferences();
		setLaunchCmd(prefs.getString(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_LAUNCH_CMD));
		setDebugCmd(prefs.getString(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_DEBUG_CMD));
		setDiscoverCmd(prefs.getString(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_DISCOVER_CMD));
		setPeriodicMonitorCmd(prefs.getString(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_PERIODIC_MONITOR_CMD));
		setPeriodicMonitorTime(prefs.getInt(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_PERIODIC_MONITOR_TIME));
		setRemoteInstallPath(prefs.getString(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_REMOTE_INSTALL_PATH));
		//		setUseToolDefaults(prefs.getBoolean(MPICH213PreferenceManager.PREFIX + MPICH213PreferenceManager.PREFS_USE_DEFAULTS));
	}

	public MPICH2ResourceManagerConfiguration(MPICH2ResourceManagerFactory factory,
			MPICH2Config config) {
		super(MPICH2_CAPABILITIES, config.getToolsConfig(), factory);
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
		MPICH2Config openMpiConfig = new MPICH2Config(toolsConfig, getVersionId());

		return new MPICH2ResourceManagerConfiguration(
				(MPICH2ResourceManagerFactory) getFactory(), openMpiConfig);
	}

	@Override
	public void save(IMemento memento) {
		super.save(memento);
		memento.putString(TAG_VERSION_ID, versionId);
	}

	@Override
	public void setDefaultNameAndDesc() {
		String name = Messages.MPICH2ResourceManagerConfiguration_defaultName;
		String conn = getConnectionName();
		if (conn != null && !conn.equals(EMPTY_STRING)) {
			name += "@" + conn; //$NON-NLS-1$
		}
		setName(name);
		setDescription(Messages.MPICH2ResourceManagerConfiguration_defaultDescription);
	}

	public String getVersionId() {
		return versionId;
	}

	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}
}
