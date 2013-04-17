/*******************************************************************************
 * Copyright (c) 2013 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 *     Chris Navarro (Illinois/NCSA) - Design and implementation
 ******************************************************************************/
package org.eclipse.ptp.etfw.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.ptp.etfw.Activator;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.PreferenceConstants;
import org.eclipse.ptp.etfw.Preferences;

/**
 * Initializes default preferences for the external tools framework
 * 
 * @author "Chris Navarro"
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		Preferences.setDefaultString(Activator.PLUGIN_ID, PreferenceConstants.ETFW_VERSION,
				IToolLaunchConfigurationConstants.USE_JAXB_PARSER);
	}
}
