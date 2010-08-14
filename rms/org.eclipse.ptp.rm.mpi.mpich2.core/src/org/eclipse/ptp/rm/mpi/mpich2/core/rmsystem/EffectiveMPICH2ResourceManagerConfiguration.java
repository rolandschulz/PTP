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
package org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem;

import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.core.rmsystem.AbstractEffectiveToolRMConfiguration;
import org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2Plugin;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2PreferenceManager;

public class EffectiveMPICH2ResourceManagerConfiguration extends AbstractEffectiveToolRMConfiguration {
	public EffectiveMPICH2ResourceManagerConfiguration(IToolRMConfiguration configuration) {
		super(configuration);
		IMPICH2ResourceManagerConfiguration MPICH2configuration = (IMPICH2ResourceManagerConfiguration) configuration;
		String launchCmd = null;
		String debugCmd = null;
		String discoverCmd = null;
		String periodicCmd = null;
		int periodicTime = 0;
		String remoteInstallPath = null;
		if (MPICH2configuration.getUseToolDefaults()) {
			launchCmd = Preferences.getString(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
					+ MPICH2PreferenceManager.PREFS_LAUNCH_CMD);
			debugCmd = Preferences.getString(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
					+ MPICH2PreferenceManager.PREFS_DEBUG_CMD);
			discoverCmd = Preferences.getString(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
					+ MPICH2PreferenceManager.PREFS_DISCOVER_CMD);
			periodicCmd = Preferences.getString(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
					+ MPICH2PreferenceManager.PREFS_PERIODIC_MONITOR_CMD);
			periodicTime = Preferences.getInt(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
					+ MPICH2PreferenceManager.PREFS_PERIODIC_MONITOR_TIME);
		} else {
			launchCmd = MPICH2configuration.getLaunchCmd();
			debugCmd = MPICH2configuration.getDebugCmd();
			discoverCmd = MPICH2configuration.getDiscoverCmd();
			periodicCmd = MPICH2configuration.getPeriodicMonitorCmd();
			periodicTime = MPICH2configuration.getPeriodicMonitorTime();
		}
		remoteInstallPath = MPICH2configuration.getRemoteInstallPath();
		applyValues(launchCmd, debugCmd, discoverCmd, periodicCmd, periodicTime, null, remoteInstallPath);
	}

}
