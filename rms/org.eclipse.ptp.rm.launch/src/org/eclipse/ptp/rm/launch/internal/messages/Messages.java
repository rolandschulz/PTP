/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.rm.launch.internal.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.launch.internal.messages.messages"; //$NON-NLS-1$

	public static String Launch_common_DebuggerColon;
	public static String Launch_common_Error;

	public static String ResourcesTab_No_Resource_Manager_Available;
	public static String ResourcesTab_Resources;
	public static String ResourcesTab_No_Resource_Manager;
	public static String ResourcesTab_No_Launch_Configuration;
	public static String ResourcesTab_pleaseSelectRM;
	public static String ResourcesTab_Resource_Manager_Not_Started;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
