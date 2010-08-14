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
import org.eclipse.ptp.core.Preferences;
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

	public static void savePreferences() {
		Preferences.savePreferences(OpenMPIPlugin.getUniqueIdentifier());
	}

	@Override
	public void initializeDefaultPreferences() {
		// Initialize only preferences as in
		// OpenMpiResourceManagerConfiguration.OPENMPI_CAPABILITIES
		try {
			OpenMPIAutoDefaults.loadDefaults();
		} catch (CoreException e) {
			OpenMPIPlugin.log(e);
		}
		Preferences.setDefaultString(OpenMPIPlugin.getUniqueIdentifier(), PREFIX_AUTO + PREFS_LAUNCH_CMD,
				OpenMPIAutoDefaults.LAUNCH_CMD);
		Preferences.setDefaultString(OpenMPIPlugin.getUniqueIdentifier(), PREFIX_AUTO + PREFS_DEBUG_CMD,
				OpenMPIAutoDefaults.DEBUG_CMD);
		Preferences.setDefaultString(OpenMPIPlugin.getUniqueIdentifier(), PREFIX_AUTO + PREFS_DISCOVER_CMD,
				OpenMPIAutoDefaults.DISCOVER_CMD);
		Preferences.setDefaultString(OpenMPIPlugin.getUniqueIdentifier(), PREFIX_AUTO + PREFS_REMOTE_INSTALL_PATH,
				OpenMPIAutoDefaults.PATH);

		try {
			OpenMPI12Defaults.loadDefaults();
		} catch (CoreException e) {
			OpenMPIPlugin.log(e);
		}
		Preferences.setDefaultString(OpenMPIPlugin.getUniqueIdentifier(), PREFIX_12 + PREFS_LAUNCH_CMD,
				OpenMPI12Defaults.LAUNCH_CMD);
		Preferences.setDefaultString(OpenMPIPlugin.getUniqueIdentifier(), PREFIX_12 + PREFS_DEBUG_CMD, OpenMPI12Defaults.DEBUG_CMD);
		Preferences.setDefaultString(OpenMPIPlugin.getUniqueIdentifier(), PREFIX_12 + PREFS_DISCOVER_CMD,
				OpenMPI12Defaults.DISCOVER_CMD);
		Preferences.setDefaultString(OpenMPIPlugin.getUniqueIdentifier(), PREFIX_12 + PREFS_REMOTE_INSTALL_PATH,
				OpenMPI12Defaults.PATH);

		try {
			OpenMPI13Defaults.loadDefaults();
		} catch (CoreException e) {
			OpenMPIPlugin.log(e);
		}
		Preferences.setDefaultString(OpenMPIPlugin.getUniqueIdentifier(), PREFIX_13 + PREFS_LAUNCH_CMD,
				OpenMPI13Defaults.LAUNCH_CMD);
		Preferences.setDefaultString(OpenMPIPlugin.getUniqueIdentifier(), PREFIX_13 + PREFS_DEBUG_CMD, OpenMPI13Defaults.DEBUG_CMD);
		Preferences.setDefaultString(OpenMPIPlugin.getUniqueIdentifier(), PREFIX_13 + PREFS_DISCOVER_CMD,
				OpenMPI13Defaults.DISCOVER_CMD);
		Preferences.setDefaultString(OpenMPIPlugin.getUniqueIdentifier(), PREFIX_13 + PREFS_REMOTE_INSTALL_PATH,
				OpenMPI13Defaults.PATH);

		try {
			OpenMPI14Defaults.loadDefaults();
		} catch (CoreException e) {
			OpenMPIPlugin.log(e);
		}
		Preferences.setDefaultString(OpenMPIPlugin.getUniqueIdentifier(), PREFIX_14 + PREFS_LAUNCH_CMD,
				OpenMPI14Defaults.LAUNCH_CMD);
		Preferences.setDefaultString(OpenMPIPlugin.getUniqueIdentifier(), PREFIX_14 + PREFS_DEBUG_CMD, OpenMPI14Defaults.DEBUG_CMD);
		Preferences.setDefaultString(OpenMPIPlugin.getUniqueIdentifier(), PREFIX_14 + PREFS_DISCOVER_CMD,
				OpenMPI14Defaults.DISCOVER_CMD);
		Preferences.setDefaultString(OpenMPIPlugin.getUniqueIdentifier(), PREFIX_14 + PREFS_REMOTE_INSTALL_PATH,
				OpenMPI14Defaults.PATH);
	}
}
