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

public class Parameters extends NLS {
	private static final String BUNDLE_ID = "org.eclipse.ptp.cell.environment.cellsimulator.conf.parameters"; //$NON-NLS-1$
	
	public static String JAVA_API_SOCKET_PORT_MAX_TRIES;
	public static String JAVA_API_SOCKET_PORT_TRY_WAIT;

	public static String CONSOLE_SOCKET_PORT_MAX_TRIES;
	public static String CONSOLE_SOCKET_PORT_TRY_WAIT;
	
	public static String LOGIN_PORT;

	public static String AUTOMATIC_USERNAME;
	public static String AUTOMATIC_PASSWORD;

	public static String PATH_SIMULATOR;
	public static String PATH_SNIF;
	public static String PATH_RUNINFO;
	public static String PATH_PID;
	public static String PATH_TAPDEVICE;
	
	public static String SIMULATOR_NETMASK;
	public static String BASE_NETWORK;
	public static String BASE_MACADDRESS;
	public static String MIN_PORTVALUE;
	public static String MAX_PORTVALUE;
	
	static {
		NLS.initializeMessages(BUNDLE_ID, Parameters.class);
	}

	private Parameters() {
		// cannot create new instance
	}
}

