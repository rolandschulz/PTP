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

/**
 * @author Richard Maciel
 *
 * @since 1.2.1
 */
public class DefaultValues extends NLS
{
	private static final String BUNDLE_ID = "org.eclipse.ptp.cell.environment.remotesimulator.core.PluginResources"; //$NON-NLS-1$


	public static String DEFAULT_REMOTE_KEY_PATH;
	public static String DEFAULT_REMOTE_KEY_PASSPHRASE;
	public static String DEFAULT_REMOTE_IS_PASSWORD_AUTH;
	public static String DEFAULT_REMOTE_TIMEOUT;
	
	public static String DEFAULT_REMOTE_LOGIN_USERNAME;
	public static String DEFAULT_REMOTE_LOGIN_PASSWORD;
	public static String DEFAULT_REMOTE_CONNECTION_ADDRESS;
	public static String DEFAULT_REMOTE_CONNECTION_PORT;
	public static String DEFAULT_SIMULATOR_IS_AUTOMATIC_CONFIG;
	public static String DEFAULT_SIMULATOR_IS_PASSWORD_AUTH;
	public static String DEFAULT_SIMULATOR_LOGIN_USERNAME;
	public static String DEFAULT_SIMULATOR_LOGIN_PASSWORD;
	public static String DEFAULT_SIMULATOR_KEY_PATH;
	public static String DEFAULT_SIMULATOR_KEY_PASSPHRASE;
	public static String DEFAULT_SIMULATOR_CONNECTION_ADDRESS;
	public static String DEFAULT_SIMULATOR_CONNECTION_PORT;
	public static String DEFAULT_SIMULATOR_CONNECTION_TIMEOUT;
	
	public static String DEFAULT_SYSTEM_WORKSPACE;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_ID, DefaultValues.class);
	}

	private DefaultValues() {
		// cannot create new instance
	}
}

	
	
