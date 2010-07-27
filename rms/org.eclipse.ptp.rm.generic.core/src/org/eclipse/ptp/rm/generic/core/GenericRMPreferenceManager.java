/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.generic.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.ptp.rm.core.AbstractToolsPreferenceManager;
import org.osgi.service.prefs.Preferences;

public class GenericRMPreferenceManager extends AbstractToolsPreferenceManager {

	public static IPreferencesService getPreferences() {
		return Platform.getPreferencesService();
	}

	public static void savePreferences() {
		// do nothing
	}

	@Override
	public void initializeDefaultPreferences() {
		Preferences preferences = new DefaultScope().getNode(GenericRMCorePlugin.getUniqueIdentifier());
		try {
			GenericRMDefaults.loadDefaults();
		} catch (CoreException e) {
			GenericRMCorePlugin.log(e);
		}
		preferences.put(PREFS_LAUNCH_CMD, GenericRMDefaults.LAUNCH_CMD);
		preferences.put(PREFS_DEBUG_CMD, GenericRMDefaults.DEBUG_CMD);
		preferences.put(PREFS_REMOTE_INSTALL_PATH, GenericRMDefaults.PATH);
	}
}
