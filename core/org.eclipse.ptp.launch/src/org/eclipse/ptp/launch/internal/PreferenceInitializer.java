/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.launch.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.PreferenceConstants;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		Preferences.setDefaultBoolean(PTPLaunchPlugin.getUniqueIdentifier(), PreferenceConstants.PREFS_AUTO_START,
				PreferenceConstants.DEFAULT_AUTO_START);
		Preferences.setDefaultString(PTPLaunchPlugin.getUniqueIdentifier(),
				PreferenceConstants.PREF_SWITCH_TO_MONITORING_PERSPECTIVE, MessageDialogWithToggle.PROMPT);
		Preferences.setDefaultString(PTPLaunchPlugin.getUniqueIdentifier(), PreferenceConstants.PREF_SWITCH_TO_DEBUG_PERSPECTIVE,
				MessageDialogWithToggle.PROMPT);
	}
}
