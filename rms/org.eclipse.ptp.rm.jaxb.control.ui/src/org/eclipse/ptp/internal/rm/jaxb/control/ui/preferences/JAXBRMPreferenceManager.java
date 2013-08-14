/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlCorePlugin;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBRMPreferenceConstants;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;

/**
 * @author arossi
 * 
 */
public class JAXBRMPreferenceManager extends AbstractPreferenceInitializer {

	public static void savePreferences() {
		Preferences.savePreferences(JAXBControlCorePlugin.getUniqueIdentifier());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer# initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		Preferences.setDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.FORCE_XML_RELOAD,
				false);
		Preferences
				.setDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.SEGMENT_PATTERN, false);
		Preferences.setDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.MATCH_STATUS, false);
		Preferences.setDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.ACTIONS, false);
		Preferences.setDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.CREATED_PROPERTIES,
				false);
		Preferences.setDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.SHOW_COMMAND, false);
		Preferences.setDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.SHOW_COMMAND_OUTPUT,
				false);
		Preferences.setDefaultBoolean(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.KEEP_MANAGED_FILES,
				false);
		Preferences.setDefaultString(JAXBControlCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.LOG_FILE,
				JAXBCoreConstants.ZEROSTR);
	}

}
