package org.eclipse.ptp.rm.mpi.openmpi.core;

import org.eclipse.core.runtime.Preferences;

public class SDMPreferenceManager {
	public static final String PREF_SDM_CMD = "sdmCmd";
	public static final String PREF_SDM_PORT = "sdmPort";
	public static final String PREF_SDM_HOST = "sdmHost";
	public static final String PREF_SDM_EXEC = "sdmExec";
	public static final String PREF_SDM_DEBUGGER = "sdmDebugger";
	
	private static final String SDM_CMD = "/home/richardm/workspaces/workspace-ptp/org.eclipse.ptp.linux.x86/bin/sdm--port=${sdmPort}, --host=${sdmHost}, --debugger=${sdmDebugger} --jobid=${jobId}";
	private static final int SDM_PORT = 40876;
	private static final String SDM_HOST = "localhost.localdomain";
	private static final String SDM_EXEC = "/opt/sdm/bin/sdm";
	private static final String SDM_DEBUGGER = "gdb-mi";

	public static final String PREFIX = "sdm-";

	public static Preferences getPreferences() {
		return Activator.getDefault().getPluginPreferences();
	}

	public static void savePreferences() {
		Activator.getDefault().savePluginPreferences();
	}

	public static void initializePreferences() {
		Preferences preferences = Activator.getDefault().getPluginPreferences();

		preferences.setDefault(PREFIX + PREF_SDM_CMD, SDM_CMD);
		preferences.setDefault(PREFIX + PREF_SDM_HOST, SDM_HOST);
		preferences.setDefault(PREFIX + PREF_SDM_PORT, SDM_PORT);
		preferences.setDefault(PREFIX + PREF_SDM_EXEC, SDM_EXEC);
		preferences.setDefault(PREFIX + PREF_SDM_DEBUGGER, SDM_DEBUGGER);
	}

}
