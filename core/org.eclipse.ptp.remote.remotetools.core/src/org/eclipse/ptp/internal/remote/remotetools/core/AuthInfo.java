/**
 * Copyright (c) 2013 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.internal.remote.remotetools.core;

import java.net.PasswordAuthentication;

import org.eclipse.ptp.internal.remote.remotetools.core.messages.Messages;
import org.eclipse.ptp.remote.core.IUserAuthenticator;
import org.eclipse.ptp.remotetools.core.IAuthInfo;

/**
 * Provides feedback to user for connection authentication
 */
public class AuthInfo implements IAuthInfo {

	private final IUserAuthenticator fAuthenticator;
	private String fPassword;
	private String fPassphrase;
	private boolean fIsPassword;
	private String fKeyPath;
	private String fUsername;

	public AuthInfo(IUserAuthenticator authenticator) {
		fAuthenticator = authenticator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IAuthInfo#getKeyPath()
	 */
	public String getKeyPath() {
		return fKeyPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IAuthInfo#getPassphrase()
	 */
	public String getPassphrase() {
		return fPassphrase;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IAuthInfo#getPassword()
	 */
	public String getPassword() {
		return fPassword;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IAuthInfo#getUsername()
	 */
	public String getUsername() {
		return fUsername;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IAuthInfo#isPasswordAuth()
	 */
	public boolean isPasswordAuth() {
		return fIsPassword;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IAuthInfo#promptKeyboardInteractive(java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String[], boolean[])
	 */
	public String[] promptKeyboardInteractive(final String destination, final String name, final String instruction,
			final String[] prompt, final boolean[] echo) {
		if (prompt.length == 0) {
			// No need to prompt, just return an empty String array
			return new String[0];
		}
		return fAuthenticator.prompt(destination, name, instruction, prompt, echo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IAuthInfo#promptPassphrase(java.lang.String)
	 */
	public boolean promptPassphrase(String message) {
		PasswordAuthentication auth = fAuthenticator.prompt(null, message);
		if (auth == null) {
			return false;
		}
		fUsername = auth.getUserName();
		fPassphrase = new String(auth.getPassword());
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IAuthInfo#promptPassword(java.lang.String)
	 */
	public boolean promptPassword(String message) {
		PasswordAuthentication auth = fAuthenticator.prompt(null, message);
		if (auth == null) {
			return false;
		}
		fUsername = auth.getUserName();
		fPassword = new String(auth.getPassword());
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IAuthInfo#promptYesNo(java.lang.String)
	 */
	public boolean promptYesNo(final String str) {
		int prompt = fAuthenticator.prompt(IUserAuthenticator.QUESTION, Messages.AuthInfo_Authentication_message, str, new int[] {
				IUserAuthenticator.YES, IUserAuthenticator.NO }, IUserAuthenticator.YES);
		return prompt == IUserAuthenticator.YES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IAuthInfo#setKeyPath(java.lang.String)
	 */
	public void setKeyPath(String keyPath) {
		fKeyPath = keyPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IAuthInfo#setPassphrase(java.lang.String)
	 */
	public void setPassphrase(String passphrase) {
		fPassphrase = passphrase;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IAuthInfo#setPassword(java.lang.String)
	 */
	public void setPassword(String password) {
		fPassword = password;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IAuthInfo#setUsePassword(boolean)
	 */
	public void setUsePassword(boolean usePassword) {
		fIsPassword = usePassword;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IAuthInfo#setUsername(java.lang.String)
	 */
	public void setUsername(String username) {
		fUsername = username;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IAuthInfo#showMessage(java.lang.String)
	 */
	public void showMessage(final String message) {
		fAuthenticator.prompt(IUserAuthenticator.INFORMATION, Messages.AuthInfo_Authentication_message, message,
				new int[] { IUserAuthenticator.OK }, IUserAuthenticator.OK);
	}
}
