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
package org.eclipse.ptp.debug.internal.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;

/**
 * @author Clement chu
 * 
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = PTPDebugUIPlugin.getDefault().getPreferenceStore();

		String debuggerFile = PTPCorePlugin.getDefault().locateFragmentFile("org.eclipse.ptp", "sdm");
		if (debuggerFile != null)
			store.setDefault(IPDebugConstants.PREF_PTP_DEBUGGER_FILE, debuggerFile);
 
		store.setDefault(IPDebugConstants.PREF_PTP_DEBUGGER_HOST, IPDebugConstants.PREF_DEFAULT_DEUBGGER_HOST);
		store.setDefault(IPDebugConstants.PREF_PTP_DEBUGGER_BACKEND, IPDebugConstants.DEBUGGER_BACKENDS[IPDebugConstants.PREF_DEFAULT_DEDUGGER_BACKEND_INDEX]);
		store.setDefault(IPDebugConstants.PREF_PTP_DEBUGGER_BACKEND_PATH, IPDebugConstants.PREF_DEFAULT_DEDUGGER_BACKEND_PATH);
		
		store.setDefault(IPDebugConstants.PREF_SHOW_FULL_PATHS, false);
		store.setDefault(IPDebugConstants.PREF_PTP_DEBUG_REGISTER_PROC_0, true);

		store.setDefault(IPDebugConstants.PREF_PTP_DEBUG_COMM_TIMEOUT, IPDebugConstants.DEFAULT_DEBUG_COMM_TIMEOUT);

		store.setDefault(IPDebugConstants.PREF_UPDATE_VARIABLES_ON_SUSPEND, true);
		store.setDefault(IPDebugConstants.PREF_UPDATE_VARIABLES_ON_CHANGE, false);
	}
}
