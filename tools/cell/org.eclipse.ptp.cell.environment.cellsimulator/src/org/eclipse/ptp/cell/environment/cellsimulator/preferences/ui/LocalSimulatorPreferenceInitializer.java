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
import org.eclipse.ptp.cell.environment.cellsimulator.conf.LocalDefaultValues;
import org.eclipse.ptp.cell.environment.cellsimulator.conf.Parameters;
import org.eclipse.ptp.cell.environment.cellsimulator.core.local.LocalConfigurationBean;
import org.eclipse.ptp.cell.environment.cellsimulator.core.local.LocalLaunchAutomaticAttributeGenerator;


/**
 * Initializes preference values
 * 
 * @author Richard Maciel, Daniel Felix Ferber
 * @since 1.2.1
 */
public class LocalSimulatorPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		
		IPreferenceStore store = CellSimulatorTargetPlugin.getDefault().getPreferenceStore();
		store.setDefault(LocalConfigurationBean.ATTR_PREFERENCES_PREFIX + LocalConfigurationBean.ATTR_SIMULATOR_BASE_DIRECTORY, CommonDefaultValues.SIMULATOR_BASE_DIRECTORY);
		store.setDefault(LocalConfigurationBean.ATTR_PREFERENCES_PREFIX + LocalConfigurationBean.ATTR_WORK_DIRECTORY, LocalDefaultValues.WORK_DIRECTORY);
		store.setDefault(LocalConfigurationBean.ATTR_PREFERENCES_PREFIX + LocalConfigurationBean.ATTR_KERNEL_IMAGE_PATH, CommonDefaultValues.KERNEL_IMAGE_PATH);
		store.setDefault(LocalConfigurationBean.ATTR_PREFERENCES_PREFIX + LocalConfigurationBean.ATTR_ROOT_IMAGE_PATH, CommonDefaultValues.ROOT_IMAGE_PATH);
		store.setDefault(LocalConfigurationBean.ATTR_PREFERENCES_PREFIX + LocalConfigurationBean.ATTR_CONSOLE_SHOW_LINUX, LocalDefaultValues.CONSOLE_SHOW_LINUX);
		store.setDefault(LocalConfigurationBean.ATTR_PREFERENCES_PREFIX + LocalConfigurationBean.ATTR_CONSOLE_SHOW_SIMULATOR, LocalDefaultValues.CONSOLE_SHOW_SIMULATOR);
		store.setDefault(LocalConfigurationBean.ATTR_PREFERENCES_PREFIX + LocalConfigurationBean.ATTR_SHOW_SIMULATOR_GUI, LocalDefaultValues.SHOW_SIMULATOR_GUI);

		store.setDefault(LocalConfigurationBean.ATTR_PREFERENCES_PREFIX + LocalConfigurationBean.ATTR_SYSTEM_WORKSPACE, LocalDefaultValues.SYSTEM_WORKSPACE);
		
		// Base values for automatic attributes 
		store.setDefault(LocalLaunchAutomaticAttributeGenerator.ATTR_BASE_NETWORK, Parameters.BASE_NETWORK);
		store.setDefault(LocalLaunchAutomaticAttributeGenerator.ATTR_BASE_MACADDRESS, Parameters.BASE_MACADDRESS);
		store.setDefault(LocalLaunchAutomaticAttributeGenerator.ATTR_MIN_PORTVALUE, Parameters.MIN_PORTVALUE);
		store.setDefault(LocalLaunchAutomaticAttributeGenerator.ATTR_MAX_PORTVALUE, Parameters.MAX_PORTVALUE);
	}

}