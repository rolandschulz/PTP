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

package org.eclipse.ptp.cell.simulator.core;

import org.eclipse.osgi.util.NLS;

/**
* Raised on a {@link ISimulatorControl} method that is waiting for simulator
* operation to complete, but the simulator terminates without any error condition.
* This may happen, for example, if the user closes the simulator GUI or 
* presses the "exit" button on the GUI.
* 
* @author Daniel Felix Ferber
* 
*
*/public class SimulatorTerminatedException extends SimulatorException {
	private static final long serialVersionUID = 6198433071628275734L;
	int exitValue = 0;
	
	public SimulatorTerminatedException() {
		super(Messages.SimulatorTerminatedException_DefaultMessage);
	}

	public SimulatorTerminatedException(int exitValue) {
		super(Messages.SimulatorTerminatedException_DefaultMessage);
		this.exitValue = exitValue;
	}
	
	public String getMessage() {
		if (exitValue == 0) {
			return Messages.SimulatorTerminatedException_DefaultMessage;
		} else {
			return NLS.bind(Messages.SimulatorTerminatedException_DefaultMessageWitError, Integer.toString(exitValue));
		}
	}
}
