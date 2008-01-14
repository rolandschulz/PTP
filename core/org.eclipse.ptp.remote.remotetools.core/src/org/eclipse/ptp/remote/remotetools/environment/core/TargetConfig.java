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
package org.eclipse.ptp.remote.remotetools.environment.core;

/**
 * Describes a target created from the environment. This class contains only attributes that apply how the environment
 * interacts with Eclipse. 
 * 
 * @author Daniel Felix Ferber
 * @since 1.2
 */
public class TargetConfig {
	private String loginUserName;
	private String loginPassword;
	private int connectionPort;
	private String connectionAddress;
	private int connectionTimeout;
	private String keyPath;
	private String keyPassphrase;
	private boolean isPasswordAuth;
	private String systemWorkspace;
	private String cipherType;
	
	public String getConnectionAddress() {
		return connectionAddress;
	}
	public void setConnectionAddress(String connectionAddress) {
		this.connectionAddress = connectionAddress;
	}
	public int getConnectionPort() {
		return connectionPort;
	}
	public void setConnectionPort(int connectionPort) {
		this.connectionPort = connectionPort;
	}
	public String getLoginPassword() {
		return loginPassword;
	}
	public void setLoginPassword(String loginPassword) {
		this.loginPassword = loginPassword;
	}
	public String getLoginUserName() {
		return loginUserName;
	}
	public void setLoginUserName(String loginUserName) {
		this.loginUserName = loginUserName;
	}
	public void setKeyPath(String keyPath) {
		this.keyPath = keyPath;
	}
	public void setKeyPassphrase(String keyPassphrase) {
		this.keyPassphrase = keyPassphrase;
	}
	public void setIsPasswordAuth(boolean isPasswordAuth) {
		this.isPasswordAuth = isPasswordAuth; 
		
	}
	public int getConnectionTimeout() {
		return connectionTimeout;
	}
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}
	public boolean isPasswordAuth() {
		return isPasswordAuth;
	}
	public void setPasswordAuth(boolean isPasswordAuth) {
		this.isPasswordAuth = isPasswordAuth;
	}
	public String getKeyPassphrase() {
		return keyPassphrase;
	}
	public String getKeyPath() {
		return keyPath;
	}
	public void setSystemWorkspace(String systemWorkspace) {
		this.systemWorkspace = systemWorkspace;
	}
	public String getSystemWorkspace() {
		return systemWorkspace;
	}
	public void setCipherType(String cipherType) {
		this.cipherType = cipherType;
	}
	public String getCipherType() {
		return cipherType;
	}
}
