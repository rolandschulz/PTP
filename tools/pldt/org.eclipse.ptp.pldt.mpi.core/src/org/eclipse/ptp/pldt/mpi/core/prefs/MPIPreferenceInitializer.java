/**********************************************************************
 * Copyright (c) 2008,2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.mpi.core.prefs;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.pldt.mpi.core.MpiPlugin;
import org.eclipse.ptp.pldt.mpi.internal.core.MpiIDs;

/**
 * Class used to initialize default preference values.
 * 
 * @author Beth Tibbitts
 */
public class MPIPreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * Initialize default MPI preferences
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = MpiPlugin.getDefault()
				.getPreferenceStore();
		store.setDefault(MpiIDs.MPI_BUILD_CMD, "mpicc");//$NON-NLS-1$
		store.setDefault(MpiIDs.MPI_CPP_BUILD_CMD, "mpic++");//$NON-NLS-1$
		store.setDefault(MpiIDs.MPI_PROMPT_FOR_OTHER_INCLUDES, true);
		store.setDefault(MpiIDs.MPI_RECOGNIZE_APIS_BY_PREFIX_ALONE, true);
	}

}
