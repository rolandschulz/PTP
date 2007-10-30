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
package org.eclipse.ptp.remotetools.core;

import java.io.File;

/**
 * @author Richard Maciel
 *
 */
public class KeyAuthToken extends AuthToken {
	private String passphrase;
	private File keyPath;

	public KeyAuthToken(String username, File keyPath, String passphrase) {
		super(username);
		this.passphrase = passphrase;
		this.keyPath = keyPath;
	}
	
	public String getPassphrase() {
		return passphrase;
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}

	public File getKeyPath() {
		return keyPath;
	}

	public void setKeyPath(File keyPath) {
		this.keyPath = keyPath;
	}
	
	
	
}
