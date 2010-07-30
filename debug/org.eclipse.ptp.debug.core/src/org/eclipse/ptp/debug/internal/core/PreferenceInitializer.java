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
package org.eclipse.ptp.debug.internal.core;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.pdi.IPDIFormat;
import org.osgi.service.prefs.Preferences;

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
		Preferences preferences = new DefaultScope().getNode(PTPCorePlugin.getUniqueIdentifier());
		preferences.putInt(IPDebugConstants.PREF_DEFAULT_VARIABLE_FORMAT, IPDIFormat.NATURAL);
		preferences.putInt(IPDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT, IPDIFormat.NATURAL);
		preferences.putBoolean(IPDebugConstants.PREF_SHOW_FULL_PATHS, IPDebugConstants.DEFAULT_SHOW_FULL_PATHS);
		preferences.putBoolean(IPDebugConstants.PREF_PTP_DEBUG_REGISTER_PROC_0, IPDebugConstants.DEFAULT_DEBUG_REGISTER_PROC_0);
		preferences.putInt(IPDebugConstants.PREF_PTP_DEBUG_COMM_TIMEOUT, IPDebugConstants.DEFAULT_DEBUG_COMM_TIMEOUT);
		preferences.putBoolean(IPDebugConstants.PREF_UPDATE_VARIABLES_ON_SUSPEND,
				IPDebugConstants.DEFAULT_UPDATE_VARIABLES_ON_SUSPEND);
		preferences.putBoolean(IPDebugConstants.PREF_UPDATE_VARIABLES_ON_CHANGE,
				IPDebugConstants.DEFAULT_UPDATE_VARIABLES_ON_CHANGE);
	}
}
