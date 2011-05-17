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
package org.eclipse.ptp.remotetools.environment.generichost.messages;

import org.eclipse.osgi.util.NLS;

/**
 * @author Daniel Felix Ferber
 * 
 * @since 3.0
 */
public class Messages extends NLS {
	private static final String BUNDLE_ID = "org.eclipse.ptp.remotetools.environment.generichost.messages.messages"; //$NON-NLS-1$

	public static String TargetControl_Connection_is_not_open;

	public static String TargetControl_create_MonitorConnecting;
	public static String TargetControl_resume_CannotResume;
	public static String TargetControl_stop_CannotPause;
	public static String PreferencePage_HeaderConnection;
	public static String PreferencePage_LabelConnectionPort;
	public static String PreferencePage_LabelConnectionAddress;
	public static String PreferencePage_LabelLoginUserName;
	public static String PreferencePage_HeaderLaunch;
	public static String PreferencePage_Description;
	public static String PreferencePage_Title;

	public static String ConfigurationPage_LabelLocalhost;
	public static String ConfigurationPage_LabelRemoteHost;
	public static String ConfigurationPage_LabelHideAdvancedOptions;
	public static String ConfigurationPage_LabelHostAddress;
	public static String ConfigurationPage_LabelHostPort;
	public static String ConfigurationPage_LabelIsPasswordBased;
	public static String ConfigurationPage_LabelIsPublicKeyBased;
	public static String ConfigurationPage_LabelPassphrase;
	public static String ConfigurationPage_LabelPassword;
	public static String ConfigurationPage_LabelPublicKeyPath;
	public static String ConfigurationPage_LabelPublicKeyPathButton;
	public static String ConfigurationPage_LabelUserName;
	public static String ConfigurationPage_LabelTimeout;
	public static String ConfigurationPage_LabelShowAdvancedOptions;
	public static String ConfigurationPage_LabelPublicKeyPathTitle;
	public static String ConfigurationPage_ConnectionFrameTitle;
	public static String ConfigurationPage_DefaultTargetName;
	public static String ConfigurationPage_DialogDescription;
	public static String ConfigurationPage_DialogTitle;
	public static String ConfigurationPage_LabelTargetName;
	public static String ConfigurationPage_CipherType;
	public static String Environment_Info;

	public static String Environment_Warning;

	public static String KeyboardInteractiveDialog_User_authentication;
	public static String UserValidationDialog_3;

	public static String UserValidationDialog_Password;

	public static String UserValidationDialog_Password_required;

	public static String UserValidationDialog_Save_password;
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_ID, Messages.class);
	}

	private Messages() {
		// cannot create new instance
	}
}
