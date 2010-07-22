package org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem;

import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.ptp.rm.core.rmsystem.AbstractEffectiveToolRMConfiguration;
import org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2Plugin;
import org.eclipse.ptp.rm.mpi.mpich2.core.MPICH2PreferenceManager;

public class EffectiveMPICH2ResourceManagerConfiguration extends AbstractEffectiveToolRMConfiguration {
	private static final String EMPTY_STR = ""; //$NON-NLS-1$

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
			IPreferencesService preferences = MPICH2PreferenceManager.getPreferences();
			launchCmd = preferences.getString(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
					+ MPICH2PreferenceManager.PREFS_LAUNCH_CMD, EMPTY_STR, null);
			debugCmd = preferences.getString(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
					+ MPICH2PreferenceManager.PREFS_DEBUG_CMD, EMPTY_STR, null);
			discoverCmd = preferences.getString(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
					+ MPICH2PreferenceManager.PREFS_DISCOVER_CMD, EMPTY_STR, null);
			periodicCmd = preferences.getString(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
					+ MPICH2PreferenceManager.PREFS_PERIODIC_MONITOR_CMD, EMPTY_STR, null);
			periodicTime = preferences.getInt(MPICH2Plugin.getUniqueIdentifier(), MPICH2PreferenceManager.PREFIX
					+ MPICH2PreferenceManager.PREFS_PERIODIC_MONITOR_TIME, 0, null);
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
