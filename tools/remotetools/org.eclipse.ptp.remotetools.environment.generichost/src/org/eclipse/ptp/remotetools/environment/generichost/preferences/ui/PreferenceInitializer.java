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
package org.eclipse.ptp.remotetools.environment.generichost.preferences.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.remotetools.environment.generichost.Activator;
import org.eclipse.ptp.remotetools.environment.generichost.conf.DefaultValues;
import org.eclipse.ptp.remotetools.environment.generichost.core.ConfigFactory;

/**
 * Initializes preference values
 * 
 * @author Richard Maciel, Daniel Felix Ferber
 * @since 1.2.1
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault()// PreferencesPlugin.getDefault()
				.getPreferenceStore();
		store.setDefault(ConfigFactory.ATTR_LOGIN_USERNAME, DefaultValues.LOGIN_USERNAME);
		store.setDefault(ConfigFactory.ATTR_CONNECTION_ADDRESS, DefaultValues.CONNECTION_ADDRESS);
		store.setDefault(ConfigFactory.ATTR_CONNECTION_PORT, DefaultValues.CONNECTION_PORT);
		store.setDefault("logging", false);
	}

}