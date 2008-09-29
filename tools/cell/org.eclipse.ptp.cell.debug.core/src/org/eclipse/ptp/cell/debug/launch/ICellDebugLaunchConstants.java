/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.debug.launch;

import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;

/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.2
 */
public interface ICellDebugLaunchConstants {
	
	public static String TARGET_REMOTELAUNCH = EnvironmentPlugin.getUniqueIdentifier() + ".TARGET_RLAUNCH"; //$NON-NLS-1$
	public static String TARGET_REMOTELAUNCH_SELECTED_CFG = EnvironmentPlugin.getUniqueIdentifier() + ".TARGET_RLAUNCH_SELECTED_CFG"; //$NON-NLS-1$
	public static String TARGET_REMOTELAUNCH_SELECTED_DBGID = EnvironmentPlugin.getUniqueIdentifier() + ".TARGET_RLAUNCH_SELECTED_DBGID"; //$NON-NLS-1$
	public static String TARGET_REMOTELAUNCH_SELECTED_BINARY = EnvironmentPlugin.getUniqueIdentifier() + ".TARGET_RLAUNCH_SELECTED_BINARY"; //$NON-NLS-1$
	public static String TARGET_REMOTELAUNCH_SELECTED_PORT = EnvironmentPlugin.getUniqueIdentifier() + ".TARGET_RLAUNCH_SELECTED_PORT"; //$NON-NLS-1$
	public static String TARGET_REMOTELAUNCH_CAPABLE = EnvironmentPlugin.getUniqueIdentifier() + ".TARGET_RLAUNCH_CAPABLE"; //$NON-NLS-1$
	public static String TARGET_ENV_SELECTED = EnvironmentPlugin.getUniqueIdentifier() + ".TARGET_ENV"; //$NON-NLS-1$
	public static String TARGET_WDIR = EnvironmentPlugin.getUniqueIdentifier() + ".TARGET_WDIR"; //$NON-NLS-1$
}
