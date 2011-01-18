/**********************************************************************
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.lapi.prefs;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.pldt.lapi.LapiIDs;
import org.eclipse.ptp.pldt.lapi.LapiPlugin;

/**
 * Class used to initialize default preference values.
 * 
 * @author Beth Tibbitts
 */
public class LAPIPreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * Initialize default LAPI preferences
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = LapiPlugin.getDefault().getPreferenceStore();
		store.setDefault(LapiIDs.LAPI_RECOGNIZE_APIS_BY_PREFIX_ALONE, true);
	}

}
