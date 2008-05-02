/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
/**
 * 
 */
package org.eclipse.ptp.rm.ompi.core.rmsystem;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.rm.ompi.core.OMPIPreferenceManager;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ui.IMemento;

final public class OMPIResourceManagerConfiguration extends
		AbstractRemoteResourceManagerConfiguration implements Cloneable {
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private static final String TAG_OMPI_LAUNCH_CMD = "launchCmd"; //$NON-NLS-1$
	private static final String TAG_OMPI_DISCOVER_CMD = "discoverCmd"; //$NON-NLS-1$
	private static final String TAG_OMPI_MONITOR_CMD = "monitorCmd"; //$NON-NLS-1$
	private static final String TAG_OMPI_PATH = "path"; //$NON-NLS-1$
	private static final String TAG_OMPI_DEFAULTS = "defaults"; //$NON-NLS-1$

	/**
	 * @param factory
	 * @param memento
	 * @return
	 */
	public static IResourceManagerConfiguration load(
			OMPIResourceManagerFactory factory, IMemento memento) {

		RemoteConfig remoteConfig = loadRemote(factory, memento);

		String launchCmd = memento.getString(TAG_OMPI_LAUNCH_CMD);
		String discoverCmd = memento.getString(TAG_OMPI_DISCOVER_CMD);
		String monitorCmd = memento.getString(TAG_OMPI_MONITOR_CMD);
		String path = memento.getString(TAG_OMPI_PATH);
		boolean useDefaults = Boolean.parseBoolean(memento
				.getString(TAG_OMPI_DEFAULTS));

		OMPIResourceManagerConfiguration config = new OMPIResourceManagerConfiguration(
				factory, remoteConfig, launchCmd, discoverCmd, monitorCmd, path, useDefaults);

		return config;
	}

	private String launchCmd;
	private String discoverCmd;
	private String monitorCmd;
	private String path;
	private boolean useDefaults;

	public OMPIResourceManagerConfiguration(OMPIResourceManagerFactory factory) {
		super(new RemoteConfig(), factory);
		Preferences prefs = OMPIPreferenceManager.getPreferences();
		setLaunchCmd(prefs.getDefaultString(OMPIPreferenceManager.PREFS_LAUNCH_CMD));
		setDiscoverCmd(prefs.getDefaultString(OMPIPreferenceManager.PREFS_DISCOVER_CMD));
		setMonitorCmd(prefs.getDefaultString(OMPIPreferenceManager.PREFS_MONITOR_CMD));
		setPath(prefs.getDefaultString(OMPIPreferenceManager.PREFS_PATH));
		setUseDefaults(true);
	}
	
	public OMPIResourceManagerConfiguration(OMPIResourceManagerFactory factory,
			RemoteConfig config, String launchCmd, String discoverCmd, String monitorCmd,
			String path, boolean useDefaults) {
		super(config, factory);
		setLaunchCmd(launchCmd);
		setDiscoverCmd(discoverCmd);
		setMonitorCmd(monitorCmd);
		setPath(path);
		setUseDefaults(useDefaults);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		CommonConfig commonConf = new CommonConfig(getName(),
				getDescription(), getUniqueName());
		RemoteConfig remoteConf = new RemoteConfig(commonConf,
				getRemoteServicesId(), getConnectionName(),
				getProxyServerPath(), getLocalAddress(),
				getInvocationOptionsStr(), getOptions());
		return new OMPIResourceManagerConfiguration(
				(OMPIResourceManagerFactory) getFactory(), remoteConf,
				getLaunchCmd(), getDiscoverCmd(), getMonitorCmd(), getPath(), useDefaults());
	}

	/**
	 * @return the discoverCmd
	 */
	public String getDiscoverCmd() {
		return discoverCmd;
	}

	/**
	 * @return the launchCmd
	 */
	public String getLaunchCmd() {
		return launchCmd;
	}

	/**
	 * @return the monitorCmd
	 */
	public String getMonitorCmd() {
		return monitorCmd;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration#save(org.eclipse.ui.IMemento)
	 */
	public void save(IMemento memento) {
		super.save(memento);
		memento.putString(TAG_OMPI_LAUNCH_CMD, launchCmd);
		memento.putString(TAG_OMPI_DISCOVER_CMD, discoverCmd);
		memento.putString(TAG_OMPI_MONITOR_CMD, monitorCmd);
		memento.putString(TAG_OMPI_PATH, path);
		memento.putString(TAG_OMPI_DEFAULTS, Boolean.toString(useDefaults));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setDefaultNameAndDesc()
	 */
	public void setDefaultNameAndDesc() {
		String name = "OMPI"; //$NON-NLS-1$
		String conn = getConnectionName();
		if (conn != null && !conn.equals(EMPTY_STRING)) {
			name += "@" + conn; //$NON-NLS-1$
		}
		setName(name);
		setDescription("OMPI Resource Manager"); //$NON-NLS-1$
	}

	/**
	 * @param discoverCmd the discoverCmd to set
	 */
	public void setDiscoverCmd(String discoverCmd) {
		this.discoverCmd = discoverCmd;
	}

	/**
	 * @param launchCmd
	 *            the launchCmd to set
	 */
	public void setLaunchCmd(String ompiCmd) {
		this.launchCmd = ompiCmd;
	}
	

	/**
	 * @param monitorCmd the monitorCmd to set
	 */
	public void setMonitorCmd(String monitorCmd) {
		this.monitorCmd = monitorCmd;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @param useDefaults
	 *            the useDefaults to set
	 */
	public void setUseDefaults(boolean useDefaults) {
		this.useDefaults = useDefaults;
	}

	/**
	 * @return the useDefaults
	 */
	public boolean useDefaults() {
		return useDefaults;
	}
}