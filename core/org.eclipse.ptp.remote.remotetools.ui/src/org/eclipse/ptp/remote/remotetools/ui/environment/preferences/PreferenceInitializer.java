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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.remote.remotetools.core.RemoteToolsAdapterCorePlugin;
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
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		Preferences.setDefaultString(RemoteToolsAdapterCorePlugin.getUniqueIdentifier(), ConfigFactory.ATTR_LOGIN_USERNAME,
				DefaultValues.LOGIN_USERNAME);
		Preferences.setDefaultString(RemoteToolsAdapterCorePlugin.getUniqueIdentifier(), ConfigFactory.ATTR_CONNECTION_ADDRESS,
				DefaultValues.CONNECTION_ADDRESS);
		Preferences.setDefaultString(RemoteToolsAdapterCorePlugin.getUniqueIdentifier(), ConfigFactory.ATTR_CONNECTION_PORT,
				DefaultValues.CONNECTION_PORT);
	}

}