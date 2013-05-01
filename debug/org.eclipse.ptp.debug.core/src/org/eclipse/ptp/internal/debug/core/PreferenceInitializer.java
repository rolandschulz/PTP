/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.debug.core;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.pdi.IPDIFormat;

public class PreferenceInitializer extends AbstractPreferenceInitializer {
	public PreferenceInitializer() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		Preferences.setDefaultInt(PTPDebugCorePlugin.getUniqueIdentifier(), IPDebugConstants.PREF_DEFAULT_VARIABLE_FORMAT,
				IPDIFormat.NATURAL);
		Preferences.setDefaultInt(PTPDebugCorePlugin.getUniqueIdentifier(), IPDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT,
				IPDIFormat.NATURAL);
		Preferences.setDefaultInt(PTPDebugCorePlugin.getUniqueIdentifier(), IPDebugConstants.PREF_DEFAULT_REGISTER_FORMAT,
				IPDIFormat.NATURAL);
		Preferences.setDefaultBoolean(PTPDebugCorePlugin.getUniqueIdentifier(), IPDebugConstants.PREF_SHOW_FULL_PATHS,
				IPDebugConstants.DEFAULT_SHOW_FULL_PATHS);
		Preferences.setDefaultBoolean(PTPDebugCorePlugin.getUniqueIdentifier(), IPDebugConstants.PREF_DEBUG_REGISTER_PROC_0,
				IPDebugConstants.DEFAULT_DEBUG_REGISTER_PROC_0);
		Preferences.setDefaultInt(PTPDebugCorePlugin.getUniqueIdentifier(), IPDebugConstants.PREF_DEBUG_COMM_TIMEOUT,
				IPDebugConstants.DEFAULT_DEBUG_COMM_TIMEOUT);
		Preferences.setDefaultBoolean(PTPDebugCorePlugin.getUniqueIdentifier(), IPDebugConstants.PREF_UPDATE_VARIABLES_ON_SUSPEND,
				IPDebugConstants.DEFAULT_UPDATE_VARIABLES_ON_SUSPEND);
		Preferences.setDefaultBoolean(PTPDebugCorePlugin.getUniqueIdentifier(), IPDebugConstants.PREF_UPDATE_VARIABLES_ON_CHANGE,
				IPDebugConstants.DEFAULT_UPDATE_VARIABLES_ON_CHANGE);
		Preferences.setDefaultBoolean(PTPDebugCorePlugin.getUniqueIdentifier(), IPDebugConstants.PREF_INSTRUCTION_STEP_MODE_ON,
				IPDebugConstants.DEFAULT_INSTRUCTION_STEP_MODE);
		Preferences.setDefaultString(PTPDebugCorePlugin.getUniqueIdentifier(), IPDebugConstants.PREF_COMMON_SOURCE_CONTAINERS, ""); //$NON-NLS-1$
		Preferences.setDefaultString(PTPDebugCorePlugin.getUniqueIdentifier(), IPDebugConstants.PREF_SOURCE_LOCATIONS, ""); //$NON-NLS-1$
	}
}
