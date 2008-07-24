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
package org.eclipse.ptp.remote.remotetools.ui.environment.preferences;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.ptp.remote.remotetools.core.Activator;
import org.eclipse.ptp.remote.remotetools.core.environment.ConfigFactory;
import org.eclipse.ptp.remote.remotetools.core.environment.conf.DefaultValues;


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
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		Preferences store = Activator.getDefault().getPluginPreferences();
		store.setDefault(ConfigFactory.ATTR_LOGIN_USERNAME, DefaultValues.LOGIN_USERNAME);
		store.setDefault(ConfigFactory.ATTR_CONNECTION_ADDRESS, DefaultValues.CONNECTION_ADDRESS);
		store.setDefault(ConfigFactory.ATTR_CONNECTION_PORT, DefaultValues.CONNECTION_PORT);
	}

}