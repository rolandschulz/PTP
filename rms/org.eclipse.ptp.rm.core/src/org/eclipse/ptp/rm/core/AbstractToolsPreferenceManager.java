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
package org.eclipse.ptp.rm.core;

/**
 * Defines constants used to store preferences for a tool. This preference
 * manager only defines constants. It does not provide static methods to
 * read/write/initialize preferences, as used by the PTP plugins. The
 * read/write/initialize operations need to be implemented as static methods in
 * classes extending this resource manager, since only the extending class has
 * knowledge how to read/write/initialize a subset of the preferences that are
 * applicable to the tool.
 * 
 * @author Daniel Felix Ferber
 */
public abstract class AbstractToolsPreferenceManager {
	public static final String PREFS_LAUNCH_CMD = "launchCmd"; //$NON-NLS-1$
	public static final String PREFS_DEBUG_CMD = "debugCmd"; //$NON-NLS-1$
	public static final String PREFS_DISCOVER_CMD = "discoverCmd"; //$NON-NLS-1$
	public static final String PREFS_PERIODIC_MONITOR_CMD = "periodicMonitorCmd"; //$NON-NLS-1$
	public static final String PREFS_PERIODIC_MONITOR_TIME = "periodicMonitorTime"; //$NON-NLS-1$
	public static final String PREFS_CONTINUOUS_MONITOR_CMD = "continuousMonitorCmd"; //$NON-NLS-1$
	public static final String PREFS_REMOTE_INSTALL_PATH = "remoteInstallPath"; //$NON-NLS-1$
}
