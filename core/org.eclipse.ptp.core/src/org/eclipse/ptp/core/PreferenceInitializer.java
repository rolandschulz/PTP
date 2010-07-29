/*******************************************************************************
 * Copyright (c) 2009,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.core;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.osgi.service.prefs.Preferences;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		Preferences preferences = new DefaultScope().getNode(PTPCorePlugin.getUniqueIdentifier());
		preferences.put(PreferenceConstants.PREFS_OUTPUT_DIR, PreferenceConstants.DEFAULT_OUTPUT_DIR_NAME);
		preferences.putInt(PreferenceConstants.PREFS_STORE_LINES, PreferenceConstants.DEFAULT_STORE_LINES);
		preferences.putBoolean(PreferenceConstants.PREFS_AUTO_START_RMS, PreferenceConstants.DEFAULT_AUTO_START);
	}

}
