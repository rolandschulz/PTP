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
package org.eclipse.ptp.cell.simulator.internal;

import java.io.IOException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ptp.cell.utils.terminal.AbstractTerminalProvider;


public class LinuxTerminalProvider extends AbstractTerminalProvider {

	SimulatorControl control;

	LinuxTerminalProvider(SimulatorControl control) {
		this.control = control;
	}

	public void writeDataToTerminal(byte[] bytes, int length) {
		Assert.isNotNull(control.terminalSocket);
		if (! control.terminalSocket.isConnected()) return;
		
		try {
			control.terminalSocket.getOutputStream().write(bytes, 0, length);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
