/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.preferences.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.remotetools.preferences.PreferencesPlugin;

import org.eclipse.ptp.remotetools.preferences.ui.PreferenceConstants;
import org.eclipse.ptp.remotetools.preferences.ui.PreferenceConstantsFromFile;

/**
 * Class used to initialize default preference values.
 * 
 * @author Ricardo M. Matinata, Richard Maciel
 * @since 1.0
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = PreferencesPlugin.getDefault()
				.getPreferenceStore();
		PreferenceConstants prefs = PreferenceConstants.getInstance();
		store.setDefault(PreferenceConstants.TIMING_SPUBIN,PreferenceConstantsFromFile.TIMING_SPUBIN_VALUE);
	}

}
