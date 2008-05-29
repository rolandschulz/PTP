/**********************************************************************
 * Copyright (c) 2005,2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
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
