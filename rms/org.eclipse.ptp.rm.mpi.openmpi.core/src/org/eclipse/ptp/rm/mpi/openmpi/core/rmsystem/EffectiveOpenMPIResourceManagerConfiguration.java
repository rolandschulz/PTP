package org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.rm.core.rmsystem.AbstractEffectiveTollRMConfiguration;
import org.eclipse.ptp.rm.core.rmsystem.AbstractToolRMConfiguration;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPI12PreferenceManager;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPI13PreferenceManager;

public class EffectiveOpenMPIResourceManagerConfiguration extends
AbstractEffectiveTollRMConfiguration {

	public EffectiveOpenMPIResourceManagerConfiguration(
			AbstractToolRMConfiguration configuration) {
		super(configuration);
		OpenMPIResourceManagerConfiguration openMPIconfiguration = (OpenMPIResourceManagerConfiguration)configuration;
		String launchCmd = null;
		String debugCmd = null;
		String discoverCmd = null;
		String remoteInstallPath = null;
		if (openMPIconfiguration.useToolDefaults()) {
			if (openMPIconfiguration.getVersionId().equals(OpenMPIResourceManagerConfiguration.VERSION_12)) {
				Preferences preferences = OpenMPI12PreferenceManager.getPreferences();
				launchCmd = preferences.getString(OpenMPI12PreferenceManager.PREFIX + OpenMPI12PreferenceManager.PREFS_LAUNCH_CMD);
				debugCmd = preferences.getString(OpenMPI12PreferenceManager.PREFIX + OpenMPI12PreferenceManager.PREFS_DEBUG_CMD);
				discoverCmd = preferences.getString(OpenMPI12PreferenceManager.PREFIX + OpenMPI12PreferenceManager.PREFS_DISCOVER_CMD);
				remoteInstallPath = preferences.getString(OpenMPI12PreferenceManager.PREFIX + OpenMPI12PreferenceManager.PREFS_REMOTE_INSTALL_PATH);
			} else if (openMPIconfiguration.getVersionId().equals(OpenMPIResourceManagerConfiguration.VERSION_13)) {
				Preferences preferences = OpenMPI13PreferenceManager.getPreferences();
				launchCmd = preferences.getString(OpenMPI13PreferenceManager.PREFIX + OpenMPI13PreferenceManager.PREFS_LAUNCH_CMD);
				debugCmd = preferences.getString(OpenMPI13PreferenceManager.PREFIX + OpenMPI13PreferenceManager.PREFS_DEBUG_CMD);
				discoverCmd = preferences.getString(OpenMPI13PreferenceManager.PREFIX + OpenMPI13PreferenceManager.PREFS_DISCOVER_CMD);
				remoteInstallPath = preferences.getString(OpenMPI13PreferenceManager.PREFIX + OpenMPI12PreferenceManager.PREFS_REMOTE_INSTALL_PATH);
			} else {
				assert false;
			}
		} else {
			launchCmd = openMPIconfiguration.getLaunchCmd();
			debugCmd = openMPIconfiguration.getDebugCmd();
			discoverCmd = openMPIconfiguration.getDiscoverCmd();
			remoteInstallPath = openMPIconfiguration.getRemoteInstallPath();
		}
		applyValues(launchCmd, debugCmd, discoverCmd, null, 0, null, remoteInstallPath);
	}

}
