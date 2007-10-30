/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.environment.launcher.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.remotetools.environment.launcher.RemoteLauncherPlugin;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public PreferenceInitializer() {
		super();
	}

	public void initializeDefaultPreferences() {
		IPreferenceStore store = RemoteLauncherPlugin.getDefault().getPreferenceStore();
		store.setDefault(LaunchPreferences.ATTR_WORKING_DIRECTORY, LaunchPreferences.DEFAULT_WORKING_DIRECTORY);
	}

}
