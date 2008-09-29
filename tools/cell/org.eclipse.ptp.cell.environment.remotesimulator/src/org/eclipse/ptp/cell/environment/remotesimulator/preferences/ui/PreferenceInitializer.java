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
package org.eclipse.ptp.cell.environment.remotesimulator.preferences.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.cell.environment.remotesimulator.Activator;
import org.eclipse.ptp.cell.environment.remotesimulator.core.ConfigFactory;
import org.eclipse.ptp.cell.environment.remotesimulator.core.DefaultValues;





/**
 * Initializes preference values
 * 
 * @author Richard Maciel
 * @since 1.2.1
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault()//PreferencesPlugin.getDefault()
				.getPreferenceStore();
		store.setDefault(ConfigFactory.ATTR_REMOTE_LOGIN_USERNAME, ConfigFactory.DEFAULT_REMOTE_LOGIN_USERNAME);
		store.setDefault(ConfigFactory.ATTR_REMOTE_CONNECTION_ADDRESS, ConfigFactory.DEFAULT_REMOTE_CONNECTION_ADDRESS);
		store.setDefault(ConfigFactory.ATTR_REMOTE_CONNECTION_PORT, ConfigFactory.DEFAULT_REMOTE_CONNECTION_PORT);
		store.setDefault(ConfigFactory.ATTR_SIMULATOR_LOGIN_USERNAME, ConfigFactory.DEFAULT_SIMULATOR_LOGIN_USERNAME);
		store.setDefault(ConfigFactory.ATTR_SIMULATOR_CONNECTION_ADDRESS, ConfigFactory.DEFAULT_SIMULATOR_CONNECTION_ADDRESS);
		store.setDefault(ConfigFactory.ATTR_SIMULATOR_CONNECTION_PORT, ConfigFactory.DEFAULT_SIMULATOR_CONNECTION_PORT);
		store.setDefault(ConfigFactory.ATTR_SIMULATOR_CONNECTION_TIMEOUT, ConfigFactory.DEFAULT_SIMULATOR_CONNECTION_TIMEOUT);
		store.setDefault(ConfigFactory.ATTR_SYSTEM_WORKSPACE, DefaultValues.DEFAULT_SYSTEM_WORKSPACE);
		/*store.setDefault(ConfigFactory.ATTR_TUNNEL_PORT_MIN, ConfigFactory.DEFAULT_TUNNEL_PORT_MIN);
		store.setDefault(ConfigFactory.ATTR_TUNNEL_PORT_MAX, ConfigFactory.DEFAULT_TUNNEL_PORT_MAX);*/
	}

}
