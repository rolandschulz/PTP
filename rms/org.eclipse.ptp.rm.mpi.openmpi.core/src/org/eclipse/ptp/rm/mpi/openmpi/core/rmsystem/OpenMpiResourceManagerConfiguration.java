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
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMpiPreferenceManager;
import org.eclipse.ui.IMemento;

public class OpenMpiResourceManagerConfiguration extends
		AbstractToolRMConfiguration implements Cloneable {

	public static int OPENMPI_CAPABILITIES = CAP_LAUNCH | CAP_DISCOVER | CAP_REMOTE_INSTALL_PATH;

	public static OpenMpiResourceManagerConfiguration load(
			OpenMpiResourceManagerFactory factory, IMemento memento) {
		ToolsConfig toolsConfig = loadTool(factory, memento);

		OpenMpiResourceManagerConfiguration config = new OpenMpiResourceManagerConfiguration(factory, toolsConfig);

		return config;
	}

	public OpenMpiResourceManagerConfiguration(OpenMpiResourceManagerFactory factory) {
		super(OPENMPI_CAPABILITIES, new ToolsConfig(), factory);

		Preferences prefs = OpenMpiPreferenceManager.getPreferences();
		setLaunchCmd(prefs.getDefaultString(OpenMpiPreferenceManager.PREFS_LAUNCH_CMD));
		setDiscoverCmd(prefs.getDefaultString(OpenMpiPreferenceManager.PREFS_DISCOVER_CMD));
		setRemoteInstallPath(prefs.getDefaultString(OpenMpiPreferenceManager.PREFS_REMOTE_INSTALL_PATH));
		setUseDefaults(prefs.getDefaultBoolean(OpenMpiPreferenceManager.PREFS_USE_DEFAULTS));
	}

	public OpenMpiResourceManagerConfiguration(OpenMpiResourceManagerFactory factory,
			ToolsConfig config) {
		super(OPENMPI_CAPABILITIES, config, factory);
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
				getDiscoverCmd(),
				getPeriodicMonitorCmd(),
				getPeriodicMonitorTime(),
				getContinuousMonitorCmd(),
				getRemoteInstallPath(),
				useDefaults());

		return new OpenMpiResourceManagerConfiguration(
				(OpenMpiResourceManagerFactory) getFactory(), toolsConfig);
	}

	@Override
	public void save(IMemento memento) {
		super.save(memento);
		// Nothing else to save.
	}

	@Override
	public void setDefaultNameAndDesc() {
		// QUESTION Ask greg, why were they not NLSed?
		String name = "Open MPI";
		String conn = getConnectionName();
		if (conn != null && !conn.equals(EMPTY_STRING)) {
			name += "@" + conn; //$NON-NLS-1$
		}
		setName(name);
		setDescription("Open MPI Resource Manager");
	}

}
