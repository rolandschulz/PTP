/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.environment.cellsimulator.core.common;

public class ConnectionConfig {
	
	private boolean isPasswordAuth;
	private String loginUserName;
	private String loginPassword;
	private String keyPath;
	private String keyPassphrase;
	
	private int connectionPort;
	private String connectionAddress;
	private int connectionTimeout;
	private String cipherType;
	
	public String getConnectionAddress() {
		return connectionAddress;
	}
	public void setConnectionAddress(String ConnectionAddress) {
		this.connectionAddress = ConnectionAddress;
	}
	public int getConnectionPort() {
		return connectionPort;
	}
	public void setConnectionPort(int ConnectionPort) {
		this.connectionPort = ConnectionPort;
	}
	public int getConnectionTimeout() {
		return connectionTimeout;
	}
	public void setConnectionTimeout(int ConnectionTimeout) {
		this.connectionTimeout = ConnectionTimeout;
	}
	public boolean isPasswordAuth() {
		return isPasswordAuth;
	}
	public void setIsPasswordAuth(boolean IsPasswordAuth) {
		this.isPasswordAuth = IsPasswordAuth;
	}
	public String getKeyPassphrase() {
		return keyPassphrase;
	}
	public void setKeyPassphrase(String KeyPassphrase) {
		this.keyPassphrase = KeyPassphrase;
	}
	public String getKeyPath() {
		return keyPath;
	}
	public void setKeyPath(String KeyPath) {
		this.keyPath = KeyPath;
	}
	public String getLoginPassword() {
		return loginPassword;
	}
	public void setLoginPassword(String LoginPassword) {
		this.loginPassword = LoginPassword;
	}
	public String getLoginUserName() {
		return loginUserName;
	}
	public void setLoginUserName(String LoginUserName) {
		this.loginUserName = LoginUserName;
	}
	public String getCipherType() {
		return cipherType;
	}
	public void setCipherType(String cipherType) {
		this.cipherType = cipherType;
	}
	
	
}
