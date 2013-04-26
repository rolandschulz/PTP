/******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.internal.remote.rse.core.messages;

import org.eclipse.osgi.util.NLS;

/**
 * @since 4.0
 */
public class Messages extends NLS {
	private static final String BUNDLE_ID = "org.eclipse.ptp.internal.remote.rse.core.messages.messages"; //$NON-NLS-1$

	public static String Activator_0;
	public static String RSEConnection_noPortFwd;
	public static String RSEConnection_noShellService;

	public static String RSEConnection_Operation_not_supported;
	public static String RSEProcessBuilder_0;
	public static String RSEServices_0;

	public static String RSEServices_Initializing_RSE_services;
	public static String RSEProcess_0;
	public static String RSEProcess_1;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_ID, Messages.class);
	}

	private Messages() {
		// cannot create new instance
	}
}
