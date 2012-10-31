/**
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.core;

/**
 * Configuration information for a connection
 * 
 * @since 6.0
 * 
 */
public interface IConnectionInfo {

	/**
	 * Get cipher type for connection
	 * 
	 * @return cipher type
	 */
	public String getCipherType();

	/**
	 * Get the connection address (hostname)
	 * 
	 * @return connection address
	 */
	public String getConnectionAddress();

	/**
	 * Get the connection port number
	 * 
	 * @return connection port number
	 */
	public int getConnectionPort();

	/**
	 * Get the timeout for the connection (seconds)
	 * 
	 * @return connection timeout
	 */
	public int getConnectionTimeout();

	/**
	 * Get the login shell flag. If true, the connection will attempt to start a login shell so that the user's environment is
	 * properly configured.
	 * 
	 * @return login shell flag
	 */
	public boolean getUseLoginShell();

	/**
	 * Set the value for the give attribute
	 * 
	 * @param key
	 *            attribute key
	 * @param value
	 *            attribute value
	 */
	public void setAttribute(String key, String value);

	/**
	 * Set the cipher type
	 * 
	 * @param cipherType
	 */
	public void setCipherType(String cipherType);

	/**
	 * Set the connection address
	 * 
	 * @param connectionAddress
	 */
	public void setConnectionAddress(String connectionAddress);

	/**
	 * Set the connection port
	 * 
	 * @param connectionPort
	 */
	public void setConnectionPort(int connectionPort);

	/**
	 * Set the connection timeout (seconds)
	 * 
	 * @param connectionTimeout
	 */
	public void setConnectionTimeout(int connectionTimeout);

	/**
	 * Set the passphrase.
	 * 
	 * @param keyPassphrase
	 */
	public void setKeyPassphrase(String keyPassphrase);

	/**
	 * Set the key file path
	 * 
	 * @param keyPath
	 */
	public void setKeyPath(String keyPath);

	/**
	 * Set the password
	 * 
	 * @param password
	 */
	public void setLoginPassword(String password);

	/**
	 * Set the connection username
	 * 
	 * @param username
	 */
	public void setLoginUsername(String username);

	/**
	 * Set authentication type
	 * 
	 * @param isPasswordAuth
	 *            true if password authentication is used
	 */
	public void setPasswordAuth(boolean isPasswordAuth);
}
