/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.ptp.debug.internal.core; 

import org.eclipse.cdt.debug.core.cdi.ICDIFormat;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
 
/**
 * Default preference value initializer for <code>CDebugCorePlugin</code>.
 */
public class PTPDebugCorePreferenceInitializer extends AbstractPreferenceInitializer {

	/** 
	 * Constructor for CDebugCorePreferenceInitializer. 
	 */
	public PTPDebugCorePreferenceInitializer() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		PTPDebugCorePlugin.getDefault().getPluginPreferences().setDefault( IPDebugConstants.PREF_MAX_NUMBER_OF_INSTRUCTIONS, IPDebugConstants.DEF_NUMBER_OF_INSTRUCTIONS );
		PTPDebugCorePlugin.getDefault().getPluginPreferences().setDefault( IPDebugConstants.PREF_DEFAULT_VARIABLE_FORMAT, ICDIFormat.NATURAL );
		PTPDebugCorePlugin.getDefault().getPluginPreferences().setDefault( IPDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT, ICDIFormat.NATURAL );
		PTPDebugCorePlugin.getDefault().getPluginPreferences().setDefault( IPDebugConstants.PREF_DEFAULT_REGISTER_FORMAT, ICDIFormat.NATURAL );
		PTPDebugCorePlugin.getDefault().getPluginPreferences().setDefault( IPDebugConstants.PREF_INSTRUCTION_STEP_MODE_ON, false );
	}
}
