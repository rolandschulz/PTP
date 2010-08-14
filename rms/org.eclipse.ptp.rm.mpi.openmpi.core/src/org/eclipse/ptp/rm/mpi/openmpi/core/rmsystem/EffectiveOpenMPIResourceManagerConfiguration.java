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
 * Open MPI requires different commands for different versions. The installed
 * version is automatically discovered and the correct commands are selected
 * based on the version. Since the discover command is used to determine the
 * version, it must work for any version of Open MPI. Querying commands prior to
 * running the discover command will always return the version 1.3 commands.
 * 
 */
public class EffectiveOpenMPIResourceManagerConfiguration extends AbstractEffectiveToolRMConfiguration {

	private static final String BINDIR = "bin"; //$NON-NLS-1$
	private static final String EMPTY_STR = ""; //$NON-NLS-1$

	public EffectiveOpenMPIResourceManagerConfiguration(IToolRMConfiguration configuration) {
		super(configuration);
		IOpenMPIResourceManagerConfiguration conf = (IOpenMPIResourceManagerConfiguration) configuration;
		String launchCmd = null;
		String debugCmd = null;
		String discoverCmd = null;
		String remoteInstallPath = null;

		if (conf.getUseToolDefaults()) {
			String version = conf.getDetectedVersion();
			if (version.equals(IOpenMPIResourceManagerConfiguration.VERSION_UNKNOWN)
					&& conf.getVersionId().equals(IOpenMPIResourceManagerConfiguration.VERSION_AUTO)) {
				discoverCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_AUTO
						+ OpenMPIPreferenceManager.PREFS_DISCOVER_CMD);
			} else if (version.equals(IOpenMPIResourceManagerConfiguration.VERSION_12)) {
				launchCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_12
						+ OpenMPIPreferenceManager.PREFS_LAUNCH_CMD);
				debugCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_12
						+ OpenMPIPreferenceManager.PREFS_DEBUG_CMD);
				discoverCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_12
						+ OpenMPIPreferenceManager.PREFS_DISCOVER_CMD);
			} else if (version.equals(IOpenMPIResourceManagerConfiguration.VERSION_13)) {
				launchCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_13
						+ OpenMPIPreferenceManager.PREFS_LAUNCH_CMD);
				debugCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_13
						+ OpenMPIPreferenceManager.PREFS_DEBUG_CMD);
				discoverCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_13
						+ OpenMPIPreferenceManager.PREFS_DISCOVER_CMD);
			} else if (version.equals(IOpenMPIResourceManagerConfiguration.VERSION_14)) {
				launchCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_14
						+ OpenMPIPreferenceManager.PREFS_LAUNCH_CMD);
				debugCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_14
						+ OpenMPIPreferenceManager.PREFS_DEBUG_CMD);
				discoverCmd = Preferences.getString(OpenMPIPlugin.getUniqueIdentifier(), OpenMPIPreferenceManager.PREFIX_14
						+ OpenMPIPreferenceManager.PREFS_DISCOVER_CMD);
			} else {
				assert false;
			}
		} else {
			launchCmd = conf.getLaunchCmd();
			debugCmd = conf.getDebugCmd();
			discoverCmd = conf.getDiscoverCmd();
		}

		remoteInstallPath = conf.getRemoteInstallPath();
		applyValues(launchCmd, debugCmd, discoverCmd, null, 0, null, remoteInstallPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.core.rmsystem.AbstractEffectiveToolRMConfiguration
	 * #completeCommand(java.lang.String)
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
