/*******************************************************************************
 * Copyright (c) 2009 School of Computer Science, 
 * National University of Defense Technology, P.R.China
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Peichang Shi <pcmutates@163.com>/<pcshi@nudt.edu.cn>
 *******************************************************************************/
package org.eclipse.ptp.rm.slurm.core;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.remote.core.IRemoteProxyOptions;
import org.eclipse.ptp.rm.core.RMPreferenceConstants;

public class SLURMPreferenceManager {
	//private static final String PROXY_EXECUTABLE_NAME = "ptp_slurm_proxy.py";
	private static final String PROXY_EXECUTABLE_PATH = null; // use local fragment directory
	private static final int OPTIONS = IRemoteProxyOptions.PORT_FORWARDING;
	
	public static int getDefaultOptions() {
		return OPTIONS;
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
			//server = new Path(PROXY_EXECUTABLE_PATH).append(PROXY_EXECUTABLE_NAME).toOSString();
		} else {
			//server = PTPCorePlugin.getDefault().locateFragmentFile("org.eclipse.ptp", PROXY_EXECUTABLE_NAME);
			if (server == null) {
				server = "";
			}
       }
		
		preferences.setDefault(RMPreferenceConstants.PROXY_PATH, server);
		preferences.setDefault(RMPreferenceConstants.OPTIONS, OPTIONS);
	}
}