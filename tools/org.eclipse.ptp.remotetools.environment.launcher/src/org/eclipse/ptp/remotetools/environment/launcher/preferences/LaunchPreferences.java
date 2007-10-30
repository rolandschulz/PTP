/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.environment.launcher.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.remotetools.environment.launcher.RemoteLauncherPlugin;

public class LaunchPreferences {
	public static final String PREFIX = "org.eclipse.ptp.remotetools.environment.launcher.preferences-"; //$NON-NLS-1$
	public static final String ATTR_WORKING_DIRECTORY = PREFIX + "working-directory"; //$NON-NLS-1$
	
	public static String DEFAULT_WORKING_DIRECTORY;
	
	static {
		NLS.initializeMessages("org.eclipse.ptp.remotetools.environment.launcher.preferences.launch_preferences", LaunchPreferences.class); //$NON-NLS-1$
	}

	public static IPreferenceStore getPreferenceStore() {
		return RemoteLauncherPlugin.getDefault().getPreferenceStore();
	}
}
