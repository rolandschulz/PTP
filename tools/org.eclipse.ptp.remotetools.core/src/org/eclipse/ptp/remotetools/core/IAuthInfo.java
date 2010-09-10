/******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.core;

/**
 * Abstraction of the Jsch UserInfo and UIKeyboardInteractive interfaces.
 * 
 * @since 4.0
 */
public interface IAuthInfo {
	public String getKeyPath();

	public String getPassphrase();

	public String getPassword();

	public String getUsername();

	public boolean isPasswordAuth();

	public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo);

	public boolean promptPassphrase(String message);

	public boolean promptPassword(String message);

	public boolean promptYesNo(String message);

	public void setKeyPath(String keyPath);

	public void setPassphrase(String passphrase);

	public void setPassword(String password);

	public void setUsePassword(boolean usePassword);

	public void setUsername(String username);

	public void showMessage(String message);
}
