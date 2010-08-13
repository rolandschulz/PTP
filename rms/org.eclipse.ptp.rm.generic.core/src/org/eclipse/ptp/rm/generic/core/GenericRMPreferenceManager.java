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
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.core.AbstractToolsPreferenceManager;

public class GenericRMPreferenceManager extends AbstractToolsPreferenceManager {

	public static void savePreferences() {
		Preferences.savePreferences(GenericRMCorePlugin.getUniqueIdentifier());
	}

	@Override
	public void initializeDefaultPreferences() {
		try {
			GenericRMDefaults.loadDefaults();
		} catch (CoreException e) {
			GenericRMCorePlugin.log(e);
		}
		Preferences.setDefaultString(GenericRMCorePlugin.getUniqueIdentifier(), PREFS_LAUNCH_CMD, GenericRMDefaults.LAUNCH_CMD);
		Preferences.setDefaultString(GenericRMCorePlugin.getUniqueIdentifier(), PREFS_DEBUG_CMD, GenericRMDefaults.DEBUG_CMD);
		Preferences.setDefaultString(GenericRMCorePlugin.getUniqueIdentifier(), PREFS_REMOTE_INSTALL_PATH, GenericRMDefaults.PATH);
	}
}
