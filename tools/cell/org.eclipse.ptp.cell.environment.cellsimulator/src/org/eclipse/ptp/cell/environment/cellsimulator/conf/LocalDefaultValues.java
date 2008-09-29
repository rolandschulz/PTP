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
package org.eclipse.ptp.cell.environment.cellsimulator.conf;

import org.eclipse.osgi.util.NLS;

/**
 * Default values that are used by Local Cell Simulator Target.
 * The Remote Cell Simulator Target may have the same attributes,
 * but they may have different default values.
 * This values are used to initialize preference pages. Also used as fallback
 * when the preferences or a target instance contains invalid data.
 * 
 * @author Richard Maciel, Daniel Ferber
 *
 * @since 1.2.1
 */
public class LocalDefaultValues extends NLS
{
private static final String BUNDLE_ID = "org.eclipse.ptp.cell.environment.cellsimulator.conf.localdefaults"; //$NON-NLS-1$
	public static String WORK_DIRECTORY;
	public static String AUTOMATIC_WORK_DIRECTORY;

	public static String SHOW_SIMULATOR_GUI;
	public static String CONSOLE_SHOW_LINUX;
	public static String CONSOLE_SHOW_SIMULATOR;

	public static String AUTOMATIC_NETWORK;
	public static String IP_HOST;
	public static String IP_SIMULATOR;
	public static String MAC_SIMULATOR;
	
	public static String AUTOMATIC_PORTCONFIG;
	public static String JAVA_API_SOCKET_PORT;
	public static String CONSOLE_SOCKET_PORT;
	
	public static String SYSTEM_WORKSPACE;
	
	public static String DefaultTargetName;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_ID, LocalDefaultValues.class);
	}

	private LocalDefaultValues() {
		// cannot create new instance
	}
	
	public static boolean doAutomaticPortConfig() {
		return Boolean.valueOf(AUTOMATIC_PORTCONFIG).booleanValue();
	}

	public static boolean doAutomaticNetworkConfig() {
		return Boolean.valueOf(AUTOMATIC_NETWORK).booleanValue();
	}
}

