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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.core.RMPreferenceConstants;
import org.eclipse.ptp.rm.core.proxy.IRemoteProxyOptions;

public class SLURMPreferenceManager extends AbstractPreferenceInitializer {
	// private static final String PROXY_EXECUTABLE_NAME = "ptp_slurm_proxy.py";
	private static final String PROXY_EXECUTABLE_PATH = null;
	private static final int OPTIONS = IRemoteProxyOptions.PORT_FORWARDING;

	public static int getDefaultOptions() {
		return OPTIONS;
	}

	public static String getDefaultProxyExecutablePath() {
		return PROXY_EXECUTABLE_PATH;
	}

	public static void savePreferences() {
		Preferences.savePreferences(SLURMCorePlugin.getUniqueIdentifier());
	}

	@Override
	public void initializeDefaultPreferences() {
		Preferences.setDefaultInt(SLURMCorePlugin.getUniqueIdentifier(), RMPreferenceConstants.OPTIONS, OPTIONS);
	}
}