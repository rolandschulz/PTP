package org.eclipse.ptp.pldt.mpi.core.prefs;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.pldt.mpi.core.MpiIDs;
import org.eclipse.ptp.pldt.mpi.core.MpiPlugin;

/**
 * Class used to initialize default preference values.
 */
public class MPIPreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * Initialize default MPI preferences
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = MpiPlugin.getDefault()
				.getPreferenceStore();
    	store.setDefault(MpiIDs.MPI_BUILD_CMD, "mpicc");
	}

}
