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
 *  
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.pe.core;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.core.RMPreferenceConstants;
import org.eclipse.ptp.rm.core.proxy.IRemoteProxyOptions;

public class PEPreferenceManager extends AbstractPreferenceInitializer {
	private static final String PROXY_EXECUTABLE_NAME = "ptp_ibmpe_proxy"; //$NON-NLS-1$
	private static final String PROXY_EXECUTABLE_PATH = null; // use local
																// fragment
																// directory
	private static final int OPTIONS = IRemoteProxyOptions.PORT_FORWARDING;

	public static int getDefaultOptions() {
		return OPTIONS;
	}

	public static String getDefaultProxyExecutablePath() {
		return PROXY_EXECUTABLE_PATH;
	}

	public static void savePreferences() {
		Preferences.savePreferences(PECorePlugin.getUniqueIdentifier());
	}

	@Override
	public void initializeDefaultPreferences() {
		String server = ""; //$NON-NLS-1$

		if (PROXY_EXECUTABLE_PATH != null) {
			server = new Path(PROXY_EXECUTABLE_PATH).append(PROXY_EXECUTABLE_NAME).toOSString();
		} else {
			server = PTPCorePlugin.getDefault().locateFragmentFile("org.eclipse.ptp", PROXY_EXECUTABLE_NAME); //$NON-NLS-1$
			if (server == null) {
				server = ""; //$NON-NLS-1$
			}
		}

		Preferences.setDefaultInt(PECorePlugin.getUniqueIdentifier(), RMPreferenceConstants.OPTIONS, OPTIONS);
		Preferences.setDefaultString(PECorePlugin.getUniqueIdentifier(), PEPreferenceConstants.LOAD_LEVELER_OPTION,
				PEPreferenceConstants.OPTION_NO);
		Preferences.setDefaultString(PECorePlugin.getUniqueIdentifier(), PEPreferenceConstants.LOAD_LEVELER_MODE, "d"); //$NON-NLS-1$
		Preferences.setDefaultString(PECorePlugin.getUniqueIdentifier(), PEPreferenceConstants.JOB_POLL_INTERVAL, "30"); //$NON-NLS-1$
		Preferences.setDefaultString(PECorePlugin.getUniqueIdentifier(), PEPreferenceConstants.NODE_MIN_POLL_INTERVAL, "30"); //$NON-NLS-1$
		Preferences.setDefaultString(PECorePlugin.getUniqueIdentifier(), PEPreferenceConstants.NODE_MAX_POLL_INTERVAL, "120"); //$NON-NLS-1$
		Preferences.setDefaultString(PECorePlugin.getUniqueIdentifier(), PEPreferenceConstants.LIBRARY_OVERRIDE, ""); //$NON-NLS-1$
		Preferences.setDefaultString(PECorePlugin.getUniqueIdentifier(), PEPreferenceConstants.TRACE_LEVEL,
				PEPreferenceConstants.TRACE_NOTHING);
	}
}