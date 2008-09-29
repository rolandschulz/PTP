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
 * Class responsible for keeping the information about console and api ports.
 * 
 * @author Richard Maciel
 *
 */
public class PortsKeeper {

	int consolePort;
	int apiPort;
	
	public PortsKeeper(int consolePort, int apiPort) {
		this.consolePort = consolePort;
		this.apiPort = apiPort;
	}

	public int getApiPort() {
		return apiPort;
	}

	public void setApiPort(int apiPort) {
		this.apiPort = apiPort;
	}

	public int getConsolePort() {
		return consolePort;
	}

	public void setConsolePort(int consolePort) {
		this.consolePort = consolePort;
	}
}
