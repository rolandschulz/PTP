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
 * Default values that are used by Remote Cell Simulator Target.
 * The Local Cell Simulator Target may have the same attributes,
 * but they may have different default values.
 * This values are used to initialize preference pages. Also used as fallback
 * when the preferences or a target instance contains invalid data.
 * 
 * @author Richard Maciel, Daniel Ferber
 *
 * @since 1.2.1
 */
public class RemoteDefaultValues extends NLS {
	private static final String BUNDLE_ID = "org.eclipse.ptp.cell.environment.cellsimulator.conf.remotedefaults"; //$NON-NLS-1$

	public static String WORK_DIRECTORY;

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
	
	public static String REMOTE_LOGIN_USERNAME;
	public static String REMOTE_LOGIN_PASSWORD;
	public static String REMOTE_CONNECTION_ADDRESS;
	public static String REMOTE_CONNECTION_PORT;
	public static String REMOTE_KEY_PATH;
	public static String REMOTE_KEY_PASSPHRASE;
	public static String REMOTE_IS_PASSWORD_AUTH;
	public static String REMOTE_TIMEOUT;

	public static String DefaultTargetName;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_ID, RemoteDefaultValues.class);
	}

	private RemoteDefaultValues() {
		// cannot create new instance
	}
}

