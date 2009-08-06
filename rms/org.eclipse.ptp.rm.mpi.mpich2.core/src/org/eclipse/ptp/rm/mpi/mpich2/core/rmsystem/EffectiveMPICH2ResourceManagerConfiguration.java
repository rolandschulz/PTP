package org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.rm.core.rmsystem.AbstractEffectiveToolRMConfiguration;
import org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2PreferenceManager;

public class EffectiveMPICH2ResourceManagerConfiguration extends
AbstractEffectiveToolRMConfiguration {

	public EffectiveMPICH2ResourceManagerConfiguration(
			IToolRMConfiguration configuration) {
		super(configuration);
		MPICH2ResourceManagerConfiguration MPICH2configuration = (MPICH2ResourceManagerConfiguration)configuration;
		String launchCmd = null;
		String debugCmd = null;
		String discoverCmd = null;
		String periodicCmd = null;
		int periodicTime = 0;
		String remoteInstallPath = null;
		if (MPICH2configuration.getUseToolDefaults()) {
			Preferences preferences = MPICH2PreferenceManager.getPreferences();
			launchCmd = preferences.getString(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_LAUNCH_CMD);
			debugCmd = preferences.getString(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_DEBUG_CMD);
			discoverCmd = preferences.getString(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_DISCOVER_CMD);
			periodicCmd = preferences.getString(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_PERIODIC_MONITOR_CMD);
			periodicTime = preferences.getInt(MPICH2PreferenceManager.PREFIX + MPICH2PreferenceManager.PREFS_PERIODIC_MONITOR_TIME);
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
