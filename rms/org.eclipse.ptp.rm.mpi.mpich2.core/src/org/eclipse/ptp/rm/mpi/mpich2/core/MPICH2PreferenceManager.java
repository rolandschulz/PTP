/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.mpich2.core;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.rm.core.AbstractToolsPreferenceManager;


/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class MPICH2PreferenceManager extends AbstractToolsPreferenceManager {
	public static final String PREFIX = "mpich2-"; //$NON-NLS-1$

	public static Preferences getPreferences() {
		return MPICH2Plugin.getDefault().getPluginPreferences();
	}

	public static void savePreferences() {
		MPICH2Plugin.getDefault().savePluginPreferences();
	}

	public static void initializePreferences() {
		Preferences preferences = MPICH2Plugin.getDefault().getPluginPreferences();
		preferences.setDefault(PREFIX + PREFS_LAUNCH_CMD, MPICH2Defaults.LAUNCH_CMD);
		preferences.setDefault(PREFIX + PREFS_DEBUG_CMD, MPICH2Defaults.DEBUG_CMD);
		preferences.setDefault(PREFIX + PREFS_DISCOVER_CMD, MPICH2Defaults.DISCOVER_CMD);
		preferences.setDefault(PREFIX + PREFS_PERIODIC_MONITOR_CMD, MPICH2Defaults.PERIODIC_CMD);
		preferences.setDefault(PREFIX + PREFS_PERIODIC_MONITOR_TIME, MPICH2Defaults.PERIODIC_TIME);
		preferences.setDefault(PREFIX + PREFS_REMOTE_INSTALL_PATH, MPICH2Defaults.PATH);
	}
}
