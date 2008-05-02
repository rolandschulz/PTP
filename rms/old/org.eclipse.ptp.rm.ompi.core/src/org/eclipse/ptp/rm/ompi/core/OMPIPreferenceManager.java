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
package org.eclipse.ptp.rm.ompi.core;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.remote.IRemoteProxyOptions;

public class OMPIPreferenceManager {
	public static final String PREFS_LAUNCH_CMD = "launchCmd";
	public static final String PREFS_DISCOVER_CMD = "discoverCmd";
	public static final String PREFS_MONITOR_CMD = "monitorCmd";
	public static final String PREFS_PATH = "path";
	public static final String PREFS_OPTIONS = "options";
	
	private static final String LAUNCH_CMD = "mpirun";
	private static final String DISCOVER_CMD = "ompi_info -a --parseable";
	private static final String MONITOR_CMD = "";
	private static final String PATH = "";
	private static final int OPTIONS = IRemoteProxyOptions.PORT_FORWARDING;
	
	public static Preferences getPreferences() {
		return Activator.getDefault().getPluginPreferences();
	}
	
	public static void savePreferences() {
		Activator.getDefault().savePluginPreferences();
	}
	
	public static void initializePreferences() {
		Preferences preferences = Activator.getDefault().getPluginPreferences();
		preferences.setDefault(PREFS_PATH, PATH);
		preferences.setDefault(PREFS_LAUNCH_CMD, LAUNCH_CMD);
		preferences.setDefault(PREFS_DISCOVER_CMD, DISCOVER_CMD);
		preferences.setDefault(PREFS_MONITOR_CMD, MONITOR_CMD);
		preferences.setDefault(PREFS_OPTIONS, OPTIONS);
	}
}