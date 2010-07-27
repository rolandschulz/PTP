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
package org.eclipse.ptp.rm.generic.core.rmsystem;

import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.ptp.rm.core.rmsystem.AbstractEffectiveToolRMConfiguration;
import org.eclipse.ptp.rm.core.rmsystem.IToolRMConfiguration;
import org.eclipse.ptp.rm.generic.core.GenericRMCorePlugin;
import org.eclipse.ptp.rm.generic.core.GenericRMPreferenceManager;

/**
 * Represents a Generic Resource Manager configuration.
 */
public class EffectiveGenericRMConfiguration extends AbstractEffectiveToolRMConfiguration {

	private static final String EMPTY_STR = ""; //$NON-NLS-1$

	public EffectiveGenericRMConfiguration(IToolRMConfiguration configuration) {
		super(configuration);
		String launchCmd = null;
		String debugCmd = null;
		String remoteInstallPath = null;

		if (configuration.getUseToolDefaults()) {
			IPreferencesService preferences = GenericRMPreferenceManager.getPreferences();
			launchCmd = preferences.getString(GenericRMCorePlugin.getUniqueIdentifier(),
					GenericRMPreferenceManager.PREFS_LAUNCH_CMD, EMPTY_STR, null);
			debugCmd = preferences.getString(GenericRMCorePlugin.getUniqueIdentifier(), GenericRMPreferenceManager.PREFS_DEBUG_CMD,
					EMPTY_STR, null);
			remoteInstallPath = preferences.getString(GenericRMCorePlugin.getUniqueIdentifier(),
					GenericRMPreferenceManager.PREFS_REMOTE_INSTALL_PATH, EMPTY_STR, null);
		} else {
			launchCmd = configuration.getLaunchCmd();
			debugCmd = configuration.getDebugCmd();
			remoteInstallPath = configuration.getRemoteInstallPath();
		}

		applyValues(launchCmd, debugCmd, null, null, 0, null, remoteInstallPath);
	}
}
