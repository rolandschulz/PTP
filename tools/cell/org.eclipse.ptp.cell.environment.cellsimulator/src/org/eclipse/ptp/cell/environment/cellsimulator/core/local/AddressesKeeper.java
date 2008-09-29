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
package org.eclipse.ptp.cell.environment.cellsimulator.core.local;

/**
 * @author Richard Maciel
 *
 */
public class AddressesKeeper {
	
	String hostAddress;
	String simulatorAddress;
	String macAddress;
	
	public AddressesKeeper(String hostAddress, String simulatorAddress, String macAddress) {
		this.hostAddress = hostAddress;
		this.simulatorAddress = simulatorAddress;
		this.macAddress = macAddress;
	}

	public String getHostAddress() {
		return hostAddress;
	}

	public void setHostAddress(String hostAddress) {
		this.hostAddress = hostAddress;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getSimulatorAddress() {
		return simulatorAddress;
	}

	public void setSimulatorAddress(String simulatorAddress) {
		this.simulatorAddress = simulatorAddress;
	}
	
	

}
