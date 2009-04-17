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
package org.eclipse.ptp.rm.mpi.openmpi.core;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.rm.core.AbstractToolsPreferenceManager;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class OpenMPI13PreferenceManager extends AbstractToolsPreferenceManager {
	public static final String PREFIX = "openmpi-1.3-"; //$NON-NLS-1$

	public static Preferences getPreferences() {
		return OpenMPIPlugin.getDefault().getPluginPreferences();
	}

	public static void savePreferences() {
		OpenMPIPlugin.getDefault().savePluginPreferences();
	}

	public static void initializePreferences() {
		Preferences preferences = OpenMPIPlugin.getDefault().getPluginPreferences();
		// Initialize only preferences as in OpenMpiResourceManagerConfiguration.OPENMPI_CAPABILITIES
		preferences.setDefault(PREFIX + PREFS_LAUNCH_CMD, OpenMPI13Defaults.LAUNCH_CMD);
		preferences.setDefault(PREFIX + PREFS_DEBUG_CMD, OpenMPI13Defaults.DEBUG_CMD);
		preferences.setDefault(PREFIX + PREFS_DISCOVER_CMD, OpenMPI13Defaults.DISCOVER_CMD);
		preferences.setDefault(PREFIX + PREFS_REMOTE_INSTALL_PATH, OpenMPI13Defaults.PATH);
	}
}
