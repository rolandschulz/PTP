package org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.rm.core.rmsystem.AbstractEffectiveToolRMConfiguration;
import org.eclipse.ptp.rm.core.rmsystem.AbstractToolRMConfiguration;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPIPreferenceManager;

/**
 * Represents an Open MPI Resource Manager configuration. 
 * 
 * Open MPI requires different commands for different versions. The installed version is automatically discovered and the
 * correct commands are selected based on the version. Since the discover command is used to determine the version, it must
 * work for any version of Open MPI. Querying commands prior to running the discover command will always return the version
 * 1.3 commands.
 *
 */
public class EffectiveOpenMPIResourceManagerConfiguration extends AbstractEffectiveToolRMConfiguration {

	private static final String BINDIR = "bin"; //$NON-NLS-1$
	
	public EffectiveOpenMPIResourceManagerConfiguration(
			AbstractToolRMConfiguration configuration) {
		super(configuration);
		OpenMPIResourceManagerConfiguration conf = (OpenMPIResourceManagerConfiguration)configuration;
		String launchCmd = null;
		String debugCmd = null;
		String discoverCmd = null;
		String remoteInstallPath = null;

		if (conf.getUseToolDefaults()) {
			String version = conf.getDetectedVersion();
			if (version.equals(OpenMPIResourceManagerConfiguration.VERSION_UNKNOWN) &&
					conf.getVersionId().equals(OpenMPIResourceManagerConfiguration.VERSION_AUTO)) {
				Preferences preferences = OpenMPIPreferenceManager.getPreferences();
				discoverCmd = preferences.getString(OpenMPIPreferenceManager.PREFIX_AUTO + OpenMPIPreferenceManager.PREFS_DISCOVER_CMD);
			} else if (version.equals(OpenMPIResourceManagerConfiguration.VERSION_12)) {
				Preferences preferences = OpenMPIPreferenceManager.getPreferences();
				launchCmd = preferences.getString(OpenMPIPreferenceManager.PREFIX_12 + OpenMPIPreferenceManager.PREFS_LAUNCH_CMD);
				debugCmd = preferences.getString(OpenMPIPreferenceManager.PREFIX_12 + OpenMPIPreferenceManager.PREFS_DEBUG_CMD);
				discoverCmd = preferences.getString(OpenMPIPreferenceManager.PREFIX_12 + OpenMPIPreferenceManager.PREFS_DISCOVER_CMD);
			} else if (version.equals(OpenMPIResourceManagerConfiguration.VERSION_13)) {
				Preferences preferences = OpenMPIPreferenceManager.getPreferences();
				launchCmd = preferences.getString(OpenMPIPreferenceManager.PREFIX_13 + OpenMPIPreferenceManager.PREFS_LAUNCH_CMD);
				debugCmd = preferences.getString(OpenMPIPreferenceManager.PREFIX_13 + OpenMPIPreferenceManager.PREFS_DEBUG_CMD);
				discoverCmd = preferences.getString(OpenMPIPreferenceManager.PREFIX_13 + OpenMPIPreferenceManager.PREFS_DISCOVER_CMD);
			} else if (version.equals(OpenMPIResourceManagerConfiguration.VERSION_14)) {
				Preferences preferences = OpenMPIPreferenceManager.getPreferences();
				launchCmd = preferences.getString(OpenMPIPreferenceManager.PREFIX_14 + OpenMPIPreferenceManager.PREFS_LAUNCH_CMD);
				debugCmd = preferences.getString(OpenMPIPreferenceManager.PREFIX_14 + OpenMPIPreferenceManager.PREFS_DEBUG_CMD);
				discoverCmd = preferences.getString(OpenMPIPreferenceManager.PREFIX_14 + OpenMPIPreferenceManager.PREFS_DISCOVER_CMD);
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

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rmsystem.AbstractEffectiveToolRMConfiguration#completeCommand(java.lang.String)
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
