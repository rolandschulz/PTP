/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.ui.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.ui.preferences.messages"; //$NON-NLS-1$
	public static String AbstractToolsPreferencePage_Label_ContinuosMonitorCommand;
	public static String AbstractToolsPreferencePage_Label_DebugCommand;
	public static String AbstractToolsPreferencePage_Label_DiscoverCommand;
	public static String AbstractToolsPreferencePage_Label_InstallationPath;
	public static String AbstractToolsPreferencePage_Label_LaunchCommand;
	public static String AbstractToolsPreferencePage_Label_PeriodicMonitorCommand;
	public static String AbstractToolsPreferencePage_Label_PeriodicMonitorCommandPeriod;
	public static String AbstractToolsPreferencePage_Validation_InvalidPeriodicMonitorCommandTimeRange;
	public static String AbstractToolsPreferencePage_Validation_MissingDiscoverCommand;
	public static String AbstractToolsPreferencePage_Validation_MissingLaunchCommand;
	public static String AbstractToolsPreferencePage_Validation_MissingMissingDebugCommand;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}
