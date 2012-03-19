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
package org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem;

import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.core.rmsystem.AbstractEffectiveToolRMConfiguration;
import org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIPlugin;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIPreferenceManager;

/**
 * Represents an Open MPI Resource Manager configuration.
 * 
 * Open MPI requires different commands for different versions. The installed version is automatically discovered and the correct
 * commands are selected based on the version. Since the discover command is used to determine the version, it must work for any
 * version of Open MPI.
 * 
 */
public class EffectiveOpenMPIResourceManagerConfiguration extends AbstractEffectiveToolRMConfiguration {

	private static final String BINDIR = "bin"; //$NON-NLS-1$

	public EffectiveOpenMPIResourceManagerConfiguration(IToolRMConfiguration configuration) {
		super(configuration);
		IOpenMPIResourceManagerConfiguration conf = (IOpenMPIResourceManagerConfiguration) configuration;
		String launchCmd = null;
		String debugCmd = null;
		String discoverCmd = null;

		/*
		 * If the selected version is AUTO, then use the detected version to determine which commands should be used.
		 */
		if (conf.getVersionId().equals(IOpenMPIResourceManagerConfiguration.VERSION_AUTO)) {
			String version = conf.getDetectedVersion();
			String prefix = OpenMPIPreferenceManager.PREFIX_AUTO;
			if (version.equals(IOpenMPIResourceManagerConfiguration.VERSION_12)) {
				prefix = OpenMPIPreferenceManager.PREFIX_12;
			} else if (version.equals(IOpenMPIResourceManagerConfiguration.VERSION_13)) {
				prefix = OpenMPIPreferenceManager.PREFIX_13;
			} else if (version.equals(IOpenMPIResourceManagerConfiguration.VERSION_14)) {
				prefix = OpenMPIPreferenceManager.PREFIX_14;
			}
			launchCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), prefix
					+ OpenMPIPreferenceManager.PREFS_LAUNCH_CMD);
			debugCmd = Preferences
					.getString(OpenMPIPlugin.getUniqueIdentifier(), prefix + OpenMPIPreferenceManager.PREFS_DEBUG_CMD);
			discoverCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), prefix
					+ OpenMPIPreferenceManager.PREFS_DISCOVER_CMD);
		} else {
			launchCmd = conf.getLaunchCmd();
			debugCmd = conf.getDebugCmd();
			discoverCmd = conf.getDiscoverCmd();
		}

		applyValues(launchCmd, debugCmd, discoverCmd, null, 0, null, conf.getRemoteInstallPath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.core.rmsystem.AbstractEffectiveToolRMConfiguration #completeCommand(java.lang.String)
	 */
	@Override
	protected String completeCommand(String command) {
		String prefix = getConfiguration().getRemoteInstallPath();
		if (prefix == null || prefix.length() == 0) {
			return command;
		}
		return new Path(prefix).append(BINDIR).append(command.trim()).toString();
	}

}
