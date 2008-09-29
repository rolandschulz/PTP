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

import org.eclipse.ptp.cell.simulator.core.ISimulatorStatus;


/**
 * A set of facility getters to query simulator status.
 * 
 * @author Daniel Felix Ferber
 * @since 1.1
 */
public class SimulatorStatus implements ISimulatorStatus {
	private SimulatorControl control;

	/**
	 * Default constructor.
	 */
	SimulatorStatus(SimulatorControl control) {
		super();
		this.control = control;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorStatus#isNotRunning()
	 */
	public boolean isNotRunning() {
		return control.processState == SimulatorControl.NOT_STARTED;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorStatus#isLaunching()
	 */
	public boolean isLaunching() {
		return (control.processState == SimulatorControl.LAUNCHING)
		|| control.processState == SimulatorControl.DEPLOYING;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorStatus#isOperational()
	 */
	public boolean isOperational() {
		return control.processState == SimulatorControl.OPERATIONAL;
	}

	public boolean isShuttingDown() {
		return (control.processState == SimulatorControl.SHUTTING_DOWN)
				|| (control.processState == SimulatorControl.KILLING);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorStatus#isPaused()
	 */
	public boolean isPaused() {
		return control.simulatorState == SimulatorControl.STOPPED;
	}

	public boolean isResumed() {
		return control.simulatorState == SimulatorControl.STARTED;
	}
}
