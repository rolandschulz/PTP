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
package org.eclipse.ptp.remotetools.environment.generichost.preferences.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author Daniel Felix Ferber
 */
public class Messages {
	public static String PreferencePage_HeaderConnection;
	public static String PreferencePage_LabelConnectionPort;
	public static String PreferencePage_LabelConnectionAddress;
	public static String PreferencePage_LabelLoginUserName;
	public static String PreferencePage_HeaderLaunch;
	public static String PreferencePage_LabelSystemWorkspace;
	public static String PreferencePage_Description;
	public static String PreferencePage_Title;

	static {
		NLS.initializeMessages("org.eclipse.ptp.remotetools.environment.generichost.preferences.ui.messages", Messages.class);
	}
}
