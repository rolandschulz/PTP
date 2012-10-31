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
package org.eclipse.ptp.remotetools.environment.generichost.conf;

import org.eclipse.osgi.util.NLS;

/**
 * @author Richard Maciel
 * 
 * @since 1.2.1
 */
public class DefaultValues extends NLS {
	private static final String BUNDLE_ID = "org.eclipse.ptp.remotetools.environment.generichost.conf.defaults"; //$NON-NLS-1$

	public static String LOCALHOST_SELECTION;
	public static String LOGIN_USERNAME;
	public static String LOGIN_PASSWORD;
	public static String CONNECTION_ADDRESS;
	public static String CONNECTION_PORT;
	public static String CONNECTION_TIMEOUT;
	public static String KEY_PATH;
	public static String KEY_PASSPHRASE;
	public static String IS_PASSWORD_AUTH;
	public static String USE_LOGIN_SHELL;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_ID, DefaultValues.class);
	}

	private DefaultValues() {
		// cannot create new instance
	}
}
