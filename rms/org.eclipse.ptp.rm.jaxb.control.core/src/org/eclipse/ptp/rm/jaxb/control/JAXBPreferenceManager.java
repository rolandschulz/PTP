/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.core.AbstractToolsPreferenceManager;

public class JAXBPreferenceManager extends AbstractToolsPreferenceManager {

	@Override
	public void initializeDefaultPreferences() {
		try {
			JAXBDefaults.loadDefaults();
		} catch (CoreException e) {
			JAXBControlCorePlugin.log(e);
		}
		Preferences.setDefaultString(JAXBControlCorePlugin.getUniqueIdentifier(), PREFS_LAUNCH_CMD, JAXBDefaults.LAUNCH_CMD);
		Preferences.setDefaultString(JAXBControlCorePlugin.getUniqueIdentifier(), PREFS_DEBUG_CMD, JAXBDefaults.DEBUG_CMD);
		Preferences.setDefaultString(JAXBControlCorePlugin.getUniqueIdentifier(), PREFS_REMOTE_INSTALL_PATH, JAXBDefaults.PATH);
	}

	public static void savePreferences() {
		Preferences.savePreferences(JAXBControlCorePlugin.getUniqueIdentifier());
	}

}
