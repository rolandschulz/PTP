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
package org.eclipse.ptp.remotetools.utils.ui.swt;

import org.eclipse.osgi.util.NLS;

public class AuthenticationFrameMoldMessages extends NLS {
	private static final String BUNDLE_NAME = "com.ibm.celldt.ui.swt.auth_frame_messages"; //$NON-NLS-1$

	public static String AuthenticationFrameMold_Button_HideAdvanced;

	public static String AuthenticationFrameMold_Button_PrivateKeyPath;

	public static String AuthenticationFrameMold_Button_ShowAdvanced;

	public static String AuthenticationFrameMold_Combo_CipherType;

	public static String AuthenticationFrameMold_Field_Host;

	public static String AuthenticationFrameMold_Field_Passphrase;

	public static String AuthenticationFrameMold_Field_Password;

	public static String AuthenticationFrameMold_Field_Port;

	public static String AuthenticationFrameMold_Field_Timeout;

	public static String AuthenticationFrameMold_Field_User;

	public static String AuthenticationFrameMold_Option_localhost;

	public static String AuthenticationFrameMold_Option_PasswordAuthentication;

	public static String AuthenticationFrameMold_Option_PublicKeyAuth;

	public static String AuthenticationFrameMold_Option_Remotehost;

	public static String AuthenticationFrameMold_SelectionWindow_Label_PrivateKeyPath;

	public static String AuthenticationFrameMold_SelectionWindow_Title_PrivateKeyPath;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, AuthenticationFrameMoldMessages.class);
	}

	private AuthenticationFrameMoldMessages() {
	}
}
