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


public class ProcessTerminalProvider extends AbstractTerminalProvider {

	SimulatorControl control;

	ProcessTerminalProvider(SimulatorControl control) {
		this.control = control;
	}

	public void writeDataToTerminal(byte[] bytes, int length) {
		Assert.isNotNull(control.processOutputStream);
		try {
			control.processOutputStream.write(bytes, 0, length);
			control.processOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
