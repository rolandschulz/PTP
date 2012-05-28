/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.launch;

/**
 * @since 6.0
 */
public interface PreferenceConstants {
	public static final String PREFS_AUTO_START = "autoStart"; //$NON-NLS-1$
	public static final boolean DEFAULT_AUTO_START = false;
	public static final String PREF_SWITCH_TO_MONITORING_PERSPECTIVE = PTPLaunchPlugin.PLUGIN_ID
			+ ".switch_to_monitoring_perspective"; //$NON-NLS-1$
	public static final String PREF_SWITCH_TO_DEBUG_PERSPECTIVE = PTPLaunchPlugin.PLUGIN_ID + ".switch_to_debug_perspective"; //$NON-NLS-1$
}
