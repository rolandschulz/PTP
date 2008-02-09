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
package org.eclipse.ptp.debug.sdm.core;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.ptp.core.PTPCorePlugin;

/**
 * @author Clement chu
 * 
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		Preferences store = SDMDebugCorePlugin.getDefault().getPluginPreferences();

		String debuggerFile = PTPCorePlugin.getDefault().locateFragmentFile("org.eclipse.ptp", "sdm");
		if (debuggerFile != null)
			store.setDefault(SDMPreferenceConstants.SDM_DEBUGGER_FILE, debuggerFile);
 
		store.setDefault(SDMPreferenceConstants.SDM_DEBUGGER_HOST, SDMPreferenceConstants.SDM_DEFAULT_DEUBGGER_HOST);
		store.setDefault(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_TYPE, 
				SDMPreferenceConstants.SDM_DEBUGGER_BACKENDS[SDMPreferenceConstants.SDM_DEFAULT_DEDUGGER_BACKEND_INDEX]);
		store.setDefault(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_PATH, SDMPreferenceConstants.SDM_DEFAULT_DEDUGGER_BACKEND_PATH);
	}
}
