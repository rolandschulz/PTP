/**
 * Copyright (c) 2014 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, FZ Juelich
 */
package org.eclipse.ptp.rm.lml.monitor.core;

import org.eclipse.ptp.internal.rm.lml.monitor.core.LMLMonitorCorePlugin;

/**
 * Constants used for this plug-in.
 * E.g. contains default values for specified preferences.
 */
public interface IMonitorCoreConstants {

	public static final String PREF_UPDATE_INTERVAL = "updateInterval"; //$NON-NLS-1$ key name for storing the update interval in the preferences
	public static final int PREF_UPDATE_INTERVAL_DEFAULT = 60;// Default value for PREF_UPDATE_INTERVAL
	public static final String PREF_FORCE_UPDATE = "forceUpdate"; //$NON-NLS-1$
	public static final boolean PREF_FORCE_UPDATE_DEFAULT = false;// Default value for PREF_FORCE_UPDATE
	public static final String PLUGIN_ID = LMLMonitorCorePlugin.PLUGIN_ID; // Used to forward the plug-in id to the monitor.ui
																			// plug-in

}
