package org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.rm.core.rmsystem.AbstractEffectiveTollRMConfiguration;
import org.eclipse.ptp.rm.core.rmsystem.AbstractToolRMConfiguration;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2PreferenceManager;

public class EffectiveMPICH2ResourceManagerConfiguration extends
AbstractEffectiveTollRMConfiguration {

	public EffectiveMPICH2ResourceManagerConfiguration(
			AbstractToolRMConfiguration configuration) {
		super(configuration);
		MPICH2ResourceManagerConfiguration openMPIconfiguration = (MPICH2ResourceManagerConfiguration)configuration;
		String launchCmd = null;
		String debugCmd = null;
		String discoverCmd = null;
		String remoteInstallPath = null;
		if (openMPIconfiguration.useToolDefaults()) {
			Preferences preferences = MPICH2PreferenceManager.getPreferences();
			launchCmd = preferences.getString(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_LAUNCH_CMD);
			debugCmd = preferences.getString(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_DEBUG_CMD);
			discoverCmd = preferences.getString(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_DISCOVER_CMD);
			// remoteInstallPath = preferences.getString(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_REMOTE_INSTALL_PATH);
		} else {
			launchCmd = openMPIconfiguration.getLaunchCmd();
			debugCmd = openMPIconfiguration.getDebugCmd();
			discoverCmd = openMPIconfiguration.getDiscoverCmd();
			// remoteInstallPath = openMPIconfiguration.getRemoteInstallPath();
		}
		remoteInstallPath = openMPIconfiguration.getRemoteInstallPath();
		applyValues(launchCmd, debugCmd, discoverCmd, null, 0, null, remoteInstallPath);
	}

}
