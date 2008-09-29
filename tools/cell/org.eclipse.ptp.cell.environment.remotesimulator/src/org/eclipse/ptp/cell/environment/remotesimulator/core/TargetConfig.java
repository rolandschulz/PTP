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
package org.eclipse.ptp.cell.environment.remotesimulator.core;

 /**
  * Describes a target created from the environment. This class contains only attributes that apply how the environment
  * interacts with Eclipse. Cell Simulator specific attributes are stored in the proper configuration class from the Cell
  * Simulator Control.
  * 
  * @author Daniel Felix Ferber
  * @since 1.2.0
  */
public class TargetConfig {

	private boolean remoteIsPasswordAuth;
	private String remoteLoginUserName;
	private String remoteLoginPassword;
	private String remoteKeyPath;
	private String remoteKeyPassphrase;
	
	private int remoteConnectionPort;
	private String remoteConnectionAddress;
	private int remoteConnectionTimeout;
	private String remoteCipherType;

	private boolean simulatorIsPasswordAuth;
	private String simulatorLoginUserName;
	private String simulatorLoginPassword;
	private String simulatorKeyPath;
	private String simulatorPassphrase;
	
	private int simulatorConnectionPort;
	private int simulatorConnectionTimeout;
	private String simulatorCipherType;
	private String simulatorConnectionAddress;
	
	private String systemWorkspace;
	
	/*int tunnelPortMin;
	int tunnelPortMax;*/
	
	
	//private boolean simulatorIsAutomaticConfig;
	
	public String getRemoteConnectionAddress() {
		return remoteConnectionAddress;
	}
	public void setRemoteConnectionAddress(String remoteConnectionAddress) {
		this.remoteConnectionAddress = remoteConnectionAddress;
	}
	public int getRemoteConnectionPort() {
		return remoteConnectionPort;
	}
	public void setRemoteConnectionPort(int remoteConnectionPort) {
		this.remoteConnectionPort = remoteConnectionPort;
	}
	public String getRemoteLoginPassword() {
		return remoteLoginPassword;
	}
	public void setRemoteLoginPassword(String remoteLoginPassword) {
		this.remoteLoginPassword = remoteLoginPassword;
	}
	public String getRemoteLoginUserName() {
		return remoteLoginUserName;
	}
	public void setRemoteLoginUserName(String remoteLoginUserName) {
		this.remoteLoginUserName = remoteLoginUserName;
	}
	public String getSimulatorConnectionAddress() {
		return simulatorConnectionAddress;
	}
	public void setSimulatorConnectionAddress(String simulatorConnectionAddress) {
		this.simulatorConnectionAddress = simulatorConnectionAddress;
	}
	public int getSimulatorConnectionPort() {
		return simulatorConnectionPort;
	}
	public void setSimulatorConnectionPort(int simulatorConnectionPort) {
		this.simulatorConnectionPort = simulatorConnectionPort;
	}
	public String getSimulatorLoginPassword() {
		return simulatorLoginPassword;
	}
	public void setSimulatorLoginPassword(String simulatorLoginPassword) {
		this.simulatorLoginPassword = simulatorLoginPassword;
	}
	public String getSimulatorLoginUserName() {
		return simulatorLoginUserName;
	}
	public void setSimulatorLoginUserName(String simulatorLoginUserName) {
		this.simulatorLoginUserName = simulatorLoginUserName;
	}
	
	/*public int getTunnelPortMax() {
		return tunnelPortMax;
	}
	public void setTunnelPortMax(int tunnelPortMax) {
		this.tunnelPortMax = tunnelPortMax;
	}
	public int getTunnelPortMin() {
		return tunnelPortMin;
	}
	public void setTunnelPortMin(int tunnelPortMin) {
		this.tunnelPortMin = tunnelPortMin;
	}*/
	public int getSimulatorConnectionTimeout() {
		return simulatorConnectionTimeout;
	}
	public void setSimulatorConnectionTimeout(int simulatorConnectionTimeout) {
		this.simulatorConnectionTimeout = simulatorConnectionTimeout;
	}
	public void setRemoteKeyPath(String remoteKeyPath) {
		this.remoteKeyPath = remoteKeyPath;
		
	}
	public void setRemoteKeyPassphrase(String remoteKeyPassphrase) {
		this.remoteKeyPassphrase = remoteKeyPassphrase;
		
	}
	public void setRemoteIsPasswordAuth(boolean remoteIsPasswordAuth) {
		this.remoteIsPasswordAuth = remoteIsPasswordAuth;
		
	}
	
	/**
	 * @return the simulatorIsPasswordAuth
	 */
	public boolean isSimulatorIsPasswordAuth() {
		return simulatorIsPasswordAuth;
	}
	/**
	 * @param simulatorIsPasswordAuth the simulatorIsPasswordAuth to set
	 */
	public void setSimulatorIsPasswordAuth(boolean simulatorIsPasswordAuth) {
		this.simulatorIsPasswordAuth = simulatorIsPasswordAuth;
	}
	/**
	 * @return the simulatorKeyPath
	 */
	public String getSimulatorKeyPath() {
		return simulatorKeyPath;
	}
	/**
	 * @param simulatorKeyPath the simulatorKeyPath to set
	 */
	public void setSimulatorKeyPath(String simulatorKeyPath) {
		this.simulatorKeyPath = simulatorKeyPath;
	}
	/**
	 * @return the simulatorPassphrase
	 */
	public String getSimulatorPassphrase() {
		return simulatorPassphrase;
	}
	/**
	 * @param simulatorPassphrase the simulatorPassphrase to set
	 */
	public void setSimulatorPassphrase(String simulatorPassphrase) {
		this.simulatorPassphrase = simulatorPassphrase;
	}
	/**
	 * @return the remoteIsPasswordAuth
	 */
	public boolean isRemoteIsPasswordAuth() {
		return remoteIsPasswordAuth;
	}
	/**
	 * @return the remoteKeyPassphrase
	 */
	public String getRemoteKeyPassphrase() {
		return remoteKeyPassphrase;
	}
	/**
	 * @return the remoteKeyPath
	 */
	public String getRemoteKeyPath() {
		return remoteKeyPath;
	}
	/**
	 * @return the remoteConnectionTimeout
	 */
	public int getRemoteConnectionTimeout() {
		return remoteConnectionTimeout;
	}
	/**
	 * @param remoteConnectionTimeout the remoteConnectionTimeout to set
	 */
	public void setRemoteConnectionTimeout(int remoteConnectionTimeout) {
		this.remoteConnectionTimeout = remoteConnectionTimeout;
	}
	
	public String getSystemWorkspace() {
		return systemWorkspace;
	}
	public void setSystemWorkspace(String systemWorkspace) {
		this.systemWorkspace = systemWorkspace;
	}
	public void setRemoteCipherType(String remoteCipherType) {
		this.remoteCipherType = remoteCipherType;
	}
	public void setSimulatorCipherType(String simulatorCipherType) {
		this.simulatorCipherType = simulatorCipherType;
	}
	public String getRemoteCipherType() {
		return remoteCipherType;
	}
	public String getSimulatorCipherType() {
		return simulatorCipherType;
	}



}
