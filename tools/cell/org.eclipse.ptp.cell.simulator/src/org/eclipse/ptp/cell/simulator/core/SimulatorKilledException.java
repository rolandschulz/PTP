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

import org.eclipse.ptp.cell.simulator.internal.SimulatorControl;

/**
 * Raised on a {@link ISimulatorControl} method that is waiting for simulator
 * operation to complete, but is forced to terminate by having
 * {@link SimulatorControl#kill()} called by another thread. The simulator is
 * not be running after this exception was raised.
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class SimulatorKilledException extends SimulatorException {
	private static final long serialVersionUID = 821161416593670808L;

	public SimulatorKilledException() {
		super(Messages.SimulatorKilledException_DefaultMessage);
	}
}
