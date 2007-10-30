/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.remotetools.internal.core;

import java.util.ResourceBundle;

/**
 * @author laggarcia
 * @since 1.3.2
 */
public class ConnectionProperties {

	private static final String ATTR_BUNDLE_ID = "org.eclipse.ptp.remotetools.internal.core.Connection"; //$NON-NLS-1$
	private static final String ATTR_MULTIPLE_CONNECTION_PROPERTY = "multipleConnection"; //$NON-NLS-1$
	private static final String ATTR_MAX_CHANNELS_PER_CONNECTION_PROPERTY = "maxChannelsPerConnection"; //$NON-NLS-1$
	private static final String ATTR_LONG_DUTY_CYCLE = "longDutyCycle"; //$NON-NLS-1$
	private static final String ATTR_FAST_DUTY_CYCLE = "fastDutyCycle"; //$NON-NLS-1$
	private static final String ATTR_INACTIVITY_THREASHOLD = "inactivityThreashold"; //$NON-NLS-1$
	private static final String ATTR_DEFAULT_PORT = "defaultPort";
	private static final String ATTR_DEFAULT_TIMEOUT = "defaultTimeout";
	private static final String ATTR_INITIAL_DEFAULT_SESSION_LOAD = "initialDefaultSessionLoad";
	
	public static boolean multipleConnection;
	public static int maxChannelsPerConnection;
	public static int initialDefaultSessionLoad;
	
	public static int longDutyCycle = 1000;
	public static int fastDutyCycle = 100;
	public static int inactivityThreashold = 5;
	
	public static int defaultTimeout = 10000;
	public static int defaultPort = 22;
	
	static {
		// load message values from bundle file
		ResourceBundle bundle = ResourceBundle.getBundle(ATTR_BUNDLE_ID);
		multipleConnection = new Boolean(bundle
				.getString(ATTR_MULTIPLE_CONNECTION_PROPERTY)).booleanValue();
		maxChannelsPerConnection = new Integer(bundle
				.getString(ATTR_MAX_CHANNELS_PER_CONNECTION_PROPERTY)).intValue();
		initialDefaultSessionLoad = new Integer(bundle.getString(ATTR_INITIAL_DEFAULT_SESSION_LOAD)).intValue();
		longDutyCycle = new Integer(bundle.getString(ATTR_LONG_DUTY_CYCLE)).intValue();
		fastDutyCycle = new Integer(bundle.getString(ATTR_FAST_DUTY_CYCLE)).intValue();
		inactivityThreashold = new Integer(bundle.getString(ATTR_INACTIVITY_THREASHOLD)).intValue();
		defaultPort = new Integer(bundle.getString(ATTR_DEFAULT_PORT)).intValue();
		defaultTimeout = new Integer(bundle.getString(ATTR_DEFAULT_TIMEOUT)).intValue();		
	}

	private ConnectionProperties() {
		// cannot create new instance
	}

}
