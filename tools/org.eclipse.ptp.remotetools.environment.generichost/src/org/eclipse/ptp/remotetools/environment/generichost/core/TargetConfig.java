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
package org.eclipse.ptp.remotetools.environment.generichost.core;

import org.eclipse.ptp.remotetools.environment.control.ITargetConfig;
import org.eclipse.ptp.remotetools.utils.verification.ControlAttributes;

/**
 * Describes a target created from the environment.
 * 
 * @author Daniel Felix Ferber
 * @since 1.4
 */
public class TargetConfig implements ITargetConfig {
	private final ControlAttributes fAttrs;

	public TargetConfig(ControlAttributes attrs) {
		fAttrs = attrs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.environment.control.ITargetConfig#getAttributes
	 * ()
	 */
	public ControlAttributes getAttributes() {
		return fAttrs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.environment.control.ITargetConfig#getCipherType
	 * ()
	 */
	public String getCipherType() {
		return fAttrs.getString(ConfigFactory.ATTR_CIPHER_TYPE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.environment.control.ITargetConfig#
	 * getConnectionAddress()
	 */
	public String getConnectionAddress() {
		return fAttrs.getString(ConfigFactory.ATTR_CONNECTION_ADDRESS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.environment.control.ITargetConfig#
	 * getConnectionPort()
	 */
	public int getConnectionPort() {
		return fAttrs.getInt(ConfigFactory.ATTR_CONNECTION_PORT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.environment.control.ITargetConfig#
	 * getConnectionTimeout()
	 */
	public int getConnectionTimeout() {
		return fAttrs.getInt(ConfigFactory.ATTR_CONNECTION_TIMEOUT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.environment.control.ITargetConfig#
	 * getKeyPassphrase()
	 */
	public String getKeyPassphrase() {
		return fAttrs.getString(ConfigFactory.ATTR_KEY_PASSPHRASE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.environment.control.ITargetConfig#getKeyPath
	 * ()
	 */
	public String getKeyPath() {
		return fAttrs.getString(ConfigFactory.ATTR_KEY_PATH);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.environment.control.ITargetConfig#
	 * getLoginPassword()
	 */
	public String getLoginPassword() {
		return fAttrs.getString(ConfigFactory.ATTR_LOGIN_PASSWORD);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.environment.control.ITargetConfig#
	 * getLoginUsername()
	 */
	public String getLoginUsername() {
		return fAttrs.getString(ConfigFactory.ATTR_LOGIN_USERNAME);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.environment.control.ITargetConfig#isPasswordAuth
	 * ()
	 */
	public boolean isPasswordAuth() {
		return fAttrs.getBoolean(ConfigFactory.ATTR_IS_PASSWORD_AUTH);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.environment.control.ITargetConfig#setAttribute
	 * (java.lang.String, java.lang.String)
	 */
	public void setAttribute(String key, String value) {
		fAttrs.setString(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.environment.control.ITargetConfig#setCipherType
	 * (java.lang.String)
	 */
	public void setCipherType(String cipherType) {
		fAttrs.setString(ConfigFactory.ATTR_CIPHER_TYPE, cipherType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.environment.control.ITargetConfig#
	 * setConnectionAddress(java.lang.String)
	 */
	public void setConnectionAddress(String connectionAddress) {
		fAttrs.setString(ConfigFactory.ATTR_CONNECTION_ADDRESS, connectionAddress);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.environment.control.ITargetConfig#
	 * setConnectionPort(int)
	 */
	public void setConnectionPort(int connectionPort) {
		fAttrs.setInt(ConfigFactory.ATTR_CONNECTION_PORT, connectionPort);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.environment.control.ITargetConfig#
	 * setConnectionTimeout(int)
	 */
	public void setConnectionTimeout(int connectionTimeout) {
		fAttrs.setInt(ConfigFactory.ATTR_CONNECTION_TIMEOUT, connectionTimeout);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.environment.control.ITargetConfig#
	 * setKeyPassphrase(java.lang.String)
	 */
	public void setKeyPassphrase(String keyPassphrase) {
		fAttrs.setString(ConfigFactory.ATTR_KEY_PASSPHRASE, keyPassphrase);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.environment.control.ITargetConfig#setKeyPath
	 * (java.lang.String)
	 */
	public void setKeyPath(String keyPath) {
		fAttrs.setString(ConfigFactory.ATTR_KEY_PATH, keyPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.environment.control.ITargetConfig#
	 * setLoginPassword(java.lang.String)
	 */
	public void setLoginPassword(String password) {
		fAttrs.setString(ConfigFactory.ATTR_LOGIN_PASSWORD, password);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.environment.control.ITargetConfig#
	 * setLoginUsername(java.lang.String)
	 */
	public void setLoginUsername(String username) {
		fAttrs.setString(ConfigFactory.ATTR_LOGIN_USERNAME, username);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.environment.control.ITargetConfig#setPasswordAuth
	 * (boolean)
	 */
	public void setPasswordAuth(boolean isPasswordAuth) {
		fAttrs.setBoolean(ConfigFactory.ATTR_IS_PASSWORD_AUTH, isPasswordAuth);
	}
}
