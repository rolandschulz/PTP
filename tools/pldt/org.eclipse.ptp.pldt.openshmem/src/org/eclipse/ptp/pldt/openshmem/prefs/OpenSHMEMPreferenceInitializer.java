/**********************************************************************
 * Copyright (c) 2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openshmem.prefs;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.pldt.openshmem.Activator;
import org.eclipse.ptp.pldt.openshmem.OpenSHMEMIDs;


/**
 * Class used to initialize default preference values.
 * 
 * @author Beth Tibbitts
 */
public class OpenSHMEMPreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * Initialize default  preferences
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(OpenSHMEMIDs.OpenSHMEM_RECOGNIZE_APIS_BY_PREFIX_ALONE, true);
	}

}
