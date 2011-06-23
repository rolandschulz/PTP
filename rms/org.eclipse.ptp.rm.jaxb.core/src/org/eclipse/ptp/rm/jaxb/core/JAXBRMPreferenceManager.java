/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.ptp.core.Preferences;

/**
 * @author arossi
 * 
 */
public class JAXBRMPreferenceManager extends AbstractPreferenceInitializer {

	public static void savePreferences() {
		Preferences.savePreferences(JAXBCorePlugin.getUniqueIdentifier());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		Preferences.setDefaultBoolean(JAXBCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.FORCE_XML_RELOAD, false);
		Preferences.setDefaultBoolean(JAXBCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.SEGMENT_PATTERN, false);
		Preferences.setDefaultBoolean(JAXBCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.MATCH_STATUS, false);
		Preferences.setDefaultBoolean(JAXBCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.ACTIONS, false);
		Preferences.setDefaultBoolean(JAXBCorePlugin.getUniqueIdentifier(), JAXBRMPreferenceConstants.CREATED_PROPERTIES, false);
	}

}
