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
package org.eclipse.ptp.mpich2.core;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.remote.IRemoteProxyOptions;
import org.eclipse.ptp.rm.remote.ui.preferences.PreferenceConstants;

public class MPICH2PreferenceManager {
	private static final String PROXY_EXECUTABLE_NAME = "ptp_mpich2_proxy.py";
	private static final String PROXY_EXECUTABLE_PATH = null; // use local fragment directory
	private static final int OPTIONS = IRemoteProxyOptions.PORT_FORWARDING;
	
	public static int getDefaultOptions() {
		return OPTIONS;
	}

	public static String getDefaultProxyExecutableName() {
		return PROXY_EXECUTABLE_NAME;
	}

	public static String getDefaultProxyExecutablePath() {
		return PROXY_EXECUTABLE_PATH;
	}

	public static Preferences getPreferences() {
		return Activator.getDefault().getPluginPreferences();
	}
	
	public static void savePreferences() {
		Activator.getDefault().savePluginPreferences();
	}
	
	public static void initializePreferences() {
		Preferences preferences = Activator.getDefault().getPluginPreferences();
		
		String server = "";
			
		if (PROXY_EXECUTABLE_PATH != null) {
			server = new Path(PROXY_EXECUTABLE_PATH).append(PROXY_EXECUTABLE_NAME).toOSString();
		} else {
			server = PTPCorePlugin.getDefault().locateFragmentFile("org.eclipse.ptp", PROXY_EXECUTABLE_NAME);
			if (server == null) {
				server = "";
			}
		}
		
		preferences.setDefault(PreferenceConstants.PROXY_PATH, server);
		preferences.setDefault(PreferenceConstants.OPTIONS, OPTIONS);
	}
}