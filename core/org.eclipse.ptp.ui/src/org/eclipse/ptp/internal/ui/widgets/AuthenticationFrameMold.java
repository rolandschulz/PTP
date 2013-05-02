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
package org.eclipse.ptp.internal.ui.widgets;

import org.eclipse.ptp.internal.ui.messages.Messages;

/**
 * @author Richard Maciel
 * 
 */
public final class AuthenticationFrameMold {

	public static final int HAS_DESCRIPTION = 1 << 0;

	public static final int SHOW_HOST_TYPE_RADIO_BUTTON = 1 << 1;

	int bitmask;

	String description;
	String title;

	String labelLocalhost = Messages.AuthenticationFrameMold_Option_localhost;
	String labelRemoteHost = Messages.AuthenticationFrameMold_Option_Remotehost;
	String labelShowAdvancedOptions = Messages.AuthenticationFrameMold_Button_ShowAdvanced;
	String labelHideAdvancedOptions = Messages.AuthenticationFrameMold_Button_HideAdvanced;
	String labelPassword = Messages.AuthenticationFrameMold_Field_Password;
	String labelUserName = Messages.AuthenticationFrameMold_Field_User;
	String labelIsPasswordBased = Messages.AuthenticationFrameMold_Option_PasswordAuthentication;
	String labelHostPort = Messages.AuthenticationFrameMold_Field_Port;
	String labelHostAddress = Messages.AuthenticationFrameMold_Field_Host;
	String labelTimeout = Messages.AuthenticationFrameMold_Field_Timeout;
	String labelPassphrase = Messages.AuthenticationFrameMold_Field_Passphrase;
	String labelPublicKeyPathButton = Messages.AuthenticationFrameMold_Button_PrivateKeyPath;
	String labelPrivateKeyPath = Messages.AuthenticationFrameMold_SelectionWindow_Label_PrivateKeyPath;
	String labelPrivateKeyPathTitle = Messages.AuthenticationFrameMold_SelectionWindow_Title_PrivateKeyPath;
	String labelIsPublicKeyBased = Messages.AuthenticationFrameMold_Option_PublicKeyAuth;
	String labelCipherType = Messages.AuthenticationFrameMold_Combo_CipherType;

	public AuthenticationFrameMold(String title) {
		this.bitmask = 0;
		setTitle(title);
	}

	public AuthenticationFrameMold(String title, String description) {
		this.bitmask = 0;
		setTitle(title);
		setDescription(description);
	}

	public int getBitmask() {
		return bitmask;
	}

	public void setBitmask(int bitmask) {
		this.bitmask = bitmask;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String label) {
		this.title = label;
	}

	public void setLabelLocalhost(String labelLocalhost) {
		this.labelLocalhost = labelLocalhost;
	}

	public void setLabelRemoteHost(String labelRemoteHost) {
		this.labelRemoteHost = labelRemoteHost;
	}

	public void setLabelHideAdvancedOptions(String labelHideAdvancedOptions) {
		this.labelHideAdvancedOptions = labelHideAdvancedOptions;
	}

	public void setLabelHostAddress(String labelHostAddress) {
		this.labelHostAddress = labelHostAddress;
	}

	public void setLabelHostPort(String labelHostPort) {
		this.labelHostPort = labelHostPort;
	}

	public void setLabelIsPasswordBased(String labelIsPasswordBased) {
		this.labelIsPasswordBased = labelIsPasswordBased;
	}

	public void setLabelIsPublicKeyBased(String labelIsPublicKeyBased) {
		this.labelIsPublicKeyBased = labelIsPublicKeyBased;
	}

	public void setLabelPassphrase(String labelPassphrase) {
		this.labelPassphrase = labelPassphrase;
	}

	public void setLabelPassword(String labelPassword) {
		this.labelPassword = labelPassword;
	}

	public void setLabelPublicKeyPath(String labelPublicKeyPath) {
		this.labelPrivateKeyPath = labelPublicKeyPath;
	}

	public void setLabelPublicKeyPathButton(String labelPublicKeyPathButton) {
		this.labelPublicKeyPathButton = labelPublicKeyPathButton;
	}

	public void setLabelPublicKeyPathTitle(String labelPublicKeyPathTitle) {
		this.labelPrivateKeyPathTitle = labelPublicKeyPathTitle;
	}

	public void setLabelShowAdvancedOptions(String labelShowAdvancedOptions) {
		this.labelShowAdvancedOptions = labelShowAdvancedOptions;
	}

	public void setLabelTimeout(String labelTimeout) {
		this.labelTimeout = labelTimeout;
	}

	public void setLabelUserName(String labelUserName) {
		this.labelUserName = labelUserName;
	}

	public void setLabelCipherType(String labelCipherType) {
		this.labelCipherType = labelCipherType;
	}
}
