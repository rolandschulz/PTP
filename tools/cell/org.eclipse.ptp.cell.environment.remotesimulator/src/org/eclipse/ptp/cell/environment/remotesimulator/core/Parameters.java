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
package org.eclipse.ptp.cell.environment.remotesimulator.core;

import org.eclipse.osgi.util.NLS;

public class Parameters extends NLS {
	private static final String BUNDLE_ID = "org.eclipse.ptp.cell.environment.remotesimulator.core.parameters"; //$NON-NLS-1$
		
	public static String AUTOMATIC_IP_SIMULATOR;
	public static String AUTOMATIC_PORT_SIMULATOR;
	
	public static String MIN_TUNNEL_PORT;
	public static String MAX_TUNNEL_PORT;

	public static String AUTOMATIC_USERNAME;
	public static String AUTOMATIC_PASSWORD;
	public static String AUTOMATIC_TIMEOUT;
	
	/*public static int NUMERIC_TIMEOUT;
	public static int NUMERIC_PORT_SIMULATOR;
	
	public static int NUMERIC_MIN_TUNNEL_PORT;
	public static int NUMERIC_MAX_TUNNEL_PORT;*/
	
	/*public static int getNUMERIC_MAX_TUNNEL_PORT() {
		return Integer.parseInt(MAX_TUNNEL_PORT);
	}
	
	public static int getNUMERIC_MIN_TUNNEL_PORT() {
		return Integer.parseInt(MIN_TUNNEL_PORT);
	}*/
	
	public static int getNUMERIC_PORT_SIMULATOR() {
		return Integer.parseInt(AUTOMATIC_PORT_SIMULATOR);
	}
	
	public static int getNUMERIC_TIMEOUT() {
		return Integer.parseInt(AUTOMATIC_TIMEOUT);
	}
	
	static {
		NLS.initializeMessages(BUNDLE_ID, Parameters.class);
		
		/*NUMERIC_TIMEOUT = Integer.parseInt(AUTOMATIC_TIMEOUT);
		NUMERIC_PORT_SIMULATOR = Integer.parseInt(AUTOMATIC_PORT_SIMULATOR);
		
		NUMERIC_MIN_TUNNEL_PORT = Integer.parseInt(MIN_TUNNEL_PORT);
		NUMERIC_MAX_TUNNEL_PORT = Integer.parseInt(MAX_TUNNEL_PORT);*/
	}

	private Parameters() {
		// cannot create new instance
	}
}

