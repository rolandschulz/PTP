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
package org.eclipse.ptp.cell.environment.cellsimulator.preferences.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.cell.environment.cellsimulator.CellSimulatorTargetPlugin;
import org.eclipse.ptp.cell.environment.cellsimulator.conf.CommonDefaultValues;
import org.eclipse.ptp.cell.environment.cellsimulator.conf.RemoteDefaultValues;
import org.eclipse.ptp.cell.environment.cellsimulator.core.remote.RemoteConfigurationBean;


/**
 * Initializes preference values
 * 
 * @author Richard Maciel, Daniel Felix Ferber
 * @since 1.2.1
 */
public class RemoteSimulatorPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = CellSimulatorTargetPlugin.getDefault().getPreferenceStore();
		store.setDefault(RemoteConfigurationBean.ATTR_PREFERENCES_PREFIX + RemoteConfigurationBean.ATTR_SIMULATOR_BASE_DIRECTORY, CommonDefaultValues.SIMULATOR_BASE_DIRECTORY);
		store.setDefault(RemoteConfigurationBean.ATTR_PREFERENCES_PREFIX + RemoteConfigurationBean.ATTR_WORK_DIRECTORY, RemoteDefaultValues.WORK_DIRECTORY);
		store.setDefault(RemoteConfigurationBean.ATTR_PREFERENCES_PREFIX + RemoteConfigurationBean.ATTR_KERNEL_IMAGE_PATH, CommonDefaultValues.KERNEL_IMAGE_PATH);
		store.setDefault(RemoteConfigurationBean.ATTR_PREFERENCES_PREFIX + RemoteConfigurationBean.ATTR_ROOT_IMAGE_PATH, CommonDefaultValues.ROOT_IMAGE_PATH);

		store.setDefault(RemoteConfigurationBean.ATTR_PREFERENCES_PREFIX + RemoteConfigurationBean.ATTR_SYSTEM_WORKSPACE, RemoteDefaultValues.SYSTEM_WORKSPACE);
		store.setDefault(RemoteConfigurationBean.ATTR_PREFERENCES_PREFIX + RemoteConfigurationBean.ATTR_CONSOLE_SHOW_LINUX, RemoteDefaultValues.CONSOLE_SHOW_LINUX);
		store.setDefault(RemoteConfigurationBean.ATTR_PREFERENCES_PREFIX + RemoteConfigurationBean.ATTR_CONSOLE_SHOW_SIMULATOR, RemoteDefaultValues.CONSOLE_SHOW_SIMULATOR);
		store.setDefault(RemoteConfigurationBean.ATTR_PREFERENCES_PREFIX + RemoteConfigurationBean.ATTR_SHOW_SIMULATOR_GUI, RemoteDefaultValues.SHOW_SIMULATOR_GUI);
		
	}

}