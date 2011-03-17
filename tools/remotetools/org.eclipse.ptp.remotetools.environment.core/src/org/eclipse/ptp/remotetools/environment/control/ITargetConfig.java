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
package org.eclipse.ptp.remotetools.environment.control;

import org.eclipse.ptp.remotetools.utils.verification.ControlAttributes;

/**
 * Describes a target created from the environment.
 * 
 * @since 2.0
 */
public interface ITargetConfig {
	/**
	 * Get all the attributes for this configuration
	 * 
	 * @return control attributes
	 */
	public ControlAttributes getAttributes();

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
	 * Get the passphrase for the connection.
	 * 
	 * Only valid if {@link #isPasswordAuth()} returns false.
	 * 
	 * @return connection passphrase
	 */
	public String getKeyPassphrase();

	/**
	 * Get path to key file. Note that this path is on the local machine.
	 * 
	 * @return path
	 */
	public String getKeyPath();

	/**
	 * Get the password for the connection.
	 * 
	 * Only valid if {@link #isPasswordAuth()} returns true.
	 * 
	 * @return connection password
	 */
	public String getLoginPassword();

	/**
	 * Get the username for the connection
	 * 
	 * @return connection username
	 */
	public String getLoginUsername();

	/**
	 * Check if this connection should use password authentication.
	 * 
	 * @return true if password authentication is used
	 */
	public boolean isPasswordAuth();

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
