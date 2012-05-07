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
package org.eclipse.ptp.rm.ibm.ll.core;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.core.RMPreferenceConstants;
import org.eclipse.ptp.rm.core.proxy.IRemoteProxyOptions;

public class IBMLLPreferenceManager extends AbstractPreferenceInitializer {
	private static final String PROXY_EXECUTABLE_NAME = "ptp_ibmll_proxy"; //$NON-NLS-1$
	private static final String PROXY_EXECUTABLE_PATH = null; // use local
																// fragment
																// directory
	private static final boolean LAUNCH_MANUALLY = false; // use local fragment
															// directory
	private static final int OPTIONS = IRemoteProxyOptions.PORT_FORWARDING;

	public static int getDefaultOptions() {
		return OPTIONS;
	}

	public static boolean getDefaultLaunchManualFlag() {
		return LAUNCH_MANUALLY;
	}

	public static String getDefaultProxyExecutablePath() {
		return PROXY_EXECUTABLE_PATH;
	}

	public static void savePreferences() {
		Preferences.savePreferences(IBMLLCorePlugin.getUniqueIdentifier());
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

		Preferences.setDefaultInt(IBMLLCorePlugin.getUniqueIdentifier(), RMPreferenceConstants.OPTIONS, OPTIONS);
		Preferences.setDefaultString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_INFO_MESSAGE,
				IBMLLPreferenceConstants.LL_NO);
		Preferences.setDefaultString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_TRACE_MESSAGE,
				IBMLLPreferenceConstants.LL_NO);
		Preferences.setDefaultString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_WARNING_MESSAGE,
				IBMLLPreferenceConstants.LL_NO);
		Preferences.setDefaultString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_ERROR_MESSAGE,
				IBMLLPreferenceConstants.LL_NO);
		Preferences.setDefaultString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_FATAL_MESSAGE,
				IBMLLPreferenceConstants.LL_NO);
		Preferences.setDefaultString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_ARGS_MESSAGE,
				IBMLLPreferenceConstants.LL_NO);
		Preferences.setDefaultString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_DEBUG_LOOP,
				IBMLLPreferenceConstants.LL_NO);
		Preferences.setDefaultString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_DEFAULT_MULTICLUSTER,
				IBMLLPreferenceConstants.LL_NO);
		Preferences.setDefaultString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_FORCE_LOCAL,
				IBMLLPreferenceConstants.LL_NO);
		Preferences.setDefaultString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_FORCE_MULTICLUSTER,
				IBMLLPreferenceConstants.LL_NO);
		Preferences.setDefaultString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_WRITE_TEMPLATE_ALWAYS,
				IBMLLPreferenceConstants.LL_NO);
		Preferences.setDefaultString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_WRITE_TEMPLATE_NEVER,
				IBMLLPreferenceConstants.LL_NO);
		Preferences.setDefaultInt(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_MIN_NODE_POLLING, 0);
		Preferences.setDefaultInt(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_MIN_NODE_POLLING, 0);
		Preferences.setDefaultInt(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.PROXY_JOB_POLLING, 0);
		Preferences.setDefaultString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_INFO_MESSAGE,
				IBMLLPreferenceConstants.LL_NO);
		Preferences.setDefaultString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_TRACE_MESSAGE,
				IBMLLPreferenceConstants.LL_NO);
		Preferences.setDefaultString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_WARNING_MESSAGE,
				IBMLLPreferenceConstants.LL_NO);
		Preferences.setDefaultString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_ERROR_MESSAGE,
				IBMLLPreferenceConstants.LL_NO);
		Preferences.setDefaultString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_FATAL_MESSAGE,
				IBMLLPreferenceConstants.LL_NO);
		Preferences.setDefaultString(IBMLLCorePlugin.getUniqueIdentifier(), IBMLLPreferenceConstants.GUI_ARGS_MESSAGE,
				IBMLLPreferenceConstants.LL_NO);
	}
}
