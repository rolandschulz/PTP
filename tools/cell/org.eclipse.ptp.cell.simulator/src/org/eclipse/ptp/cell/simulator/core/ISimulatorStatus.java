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
 * Queries different properties of the simulator status. This interface is
 * returned by {@link SimulatorControl#getStatus()}.
 * 
 * @author Daniel Felix Ferber
 */
public interface ISimulatorStatus {

	/**
	 * True if the simulator is not running. There is no running process for the
	 * simulator.
	 * 
	 * @return True/False.
	 */
	public abstract boolean isNotRunning();

	/**
	 * True if the simulator is deploying or starting. There is a running
	 * process for the simulator, but the simulated operating system is not yet
	 * ready to receive SSH connections.
	 * 
	 * @return True/False.
	 */
	public abstract boolean isLaunching();

	/**
	 * True if the simulator is running and ready to receive SSH connections,
	 * but it may be paused. There is a running process for the simulator.
	 * 
	 * @return True/False.
	 */
	public abstract boolean isOperational();

	/**
	 * True if the simulator is operational, but paused. Although the simulated
	 * operating system is able to receive SSH connections, it will not respond
	 * since the simulated machine is paused.
	 * 
	 * @return True/False.
	 */

	public abstract boolean isPaused();

	/**
	 * True if the simulator is operational and simulating (resumed). The simulated
	 * operating system is able to receive SSH connections.
	 * 
	 * @return True/False.
	 */
	public abstract boolean isResumed();

	/**
	 * True if the simulator is shutting down. There is still a running process
	 * for the simulator, but the simulated operating system is not ready to
	 * receive SSH connections anymore.
	 * 
	 * @return True/False.
	 */
	public abstract boolean isShuttingDown();

}