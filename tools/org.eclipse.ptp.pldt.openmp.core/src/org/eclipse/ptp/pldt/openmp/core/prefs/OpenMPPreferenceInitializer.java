package org.eclipse.ptp.pldt.openmp.core.prefs;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.pldt.openmp.core.OpenMPIDs;
import org.eclipse.ptp.pldt.openmp.core.OpenMPPlugin;

/**
 * Class used to initialize default preference values.
 */
public class OpenMPPreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * Initialize default MPI preferences
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = OpenMPPlugin.getDefault()
				.getPreferenceStore();
    	store.setDefault(OpenMPIDs.OpenMP_BUILD_CMD, "gcc");
	}

}
