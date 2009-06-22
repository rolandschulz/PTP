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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.rm.core.AbstractToolsPreferenceManager;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class OpenMPIPreferenceManager extends AbstractToolsPreferenceManager {
	public static final String PREFIX_AUTO = "openmpi-auto-"; //$NON-NLS-1$
	public static final String PREFIX_12 = "openmpi-1.2-"; //$NON-NLS-1$
	public static final String PREFIX_13 = "openmpi-1.3-"; //$NON-NLS-1$
	public static final String PREFIX_14 = "openmpi-1.4-"; //$NON-NLS-1$

	public static Preferences getPreferences() {
		return OpenMPIPlugin.getDefault().getPluginPreferences();
	}

	public static void savePreferences() {
		OpenMPIPlugin.getDefault().savePluginPreferences();
	}

	public static void initializePreferences() throws CoreException {
		Preferences preferences = OpenMPIPlugin.getDefault().getPluginPreferences();
		// Initialize only preferences as in OpenMpiResourceManagerConfiguration.OPENMPI_CAPABILITIES
		OpenMPIAutoDefaults.loadDefaults();
		preferences.setDefault(PREFIX_AUTO + PREFS_LAUNCH_CMD, OpenMPIAutoDefaults.LAUNCH_CMD);
		preferences.setDefault(PREFIX_AUTO + PREFS_DEBUG_CMD, OpenMPIAutoDefaults.DEBUG_CMD);
		preferences.setDefault(PREFIX_AUTO + PREFS_DISCOVER_CMD, OpenMPIAutoDefaults.DISCOVER_CMD);
		preferences.setDefault(PREFIX_AUTO + PREFS_REMOTE_INSTALL_PATH, OpenMPIAutoDefaults.PATH);
		
		OpenMPI12Defaults.loadDefaults();
		preferences.setDefault(PREFIX_12 + PREFS_LAUNCH_CMD, OpenMPI12Defaults.LAUNCH_CMD);
		preferences.setDefault(PREFIX_12 + PREFS_DEBUG_CMD, OpenMPI12Defaults.DEBUG_CMD);
		preferences.setDefault(PREFIX_12 + PREFS_DISCOVER_CMD, OpenMPI12Defaults.DISCOVER_CMD);
		preferences.setDefault(PREFIX_12 + PREFS_REMOTE_INSTALL_PATH, OpenMPI12Defaults.PATH);

		OpenMPI13Defaults.loadDefaults();
		preferences.setDefault(PREFIX_13 + PREFS_LAUNCH_CMD, OpenMPI13Defaults.LAUNCH_CMD);
		preferences.setDefault(PREFIX_13 + PREFS_DEBUG_CMD, OpenMPI13Defaults.DEBUG_CMD);
		preferences.setDefault(PREFIX_13 + PREFS_DISCOVER_CMD, OpenMPI13Defaults.DISCOVER_CMD);
		preferences.setDefault(PREFIX_13 + PREFS_REMOTE_INSTALL_PATH, OpenMPI13Defaults.PATH);

		OpenMPI14Defaults.loadDefaults();
		preferences.setDefault(PREFIX_14 + PREFS_LAUNCH_CMD, OpenMPI14Defaults.LAUNCH_CMD);
		preferences.setDefault(PREFIX_14 + PREFS_DEBUG_CMD, OpenMPI14Defaults.DEBUG_CMD);
		preferences.setDefault(PREFIX_14 + PREFS_DISCOVER_CMD, OpenMPI14Defaults.DISCOVER_CMD);
		preferences.setDefault(PREFIX_14 + PREFS_REMOTE_INSTALL_PATH, OpenMPI14Defaults.PATH);
	}
}
