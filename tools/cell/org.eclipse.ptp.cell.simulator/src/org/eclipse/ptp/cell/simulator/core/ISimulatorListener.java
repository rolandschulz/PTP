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
 * Listener that is called when the simulator status changes.
 * This listener can be registered via {@link SimulatorControl#addListener(ISimulatorListener)}.
 * @author Daniel Felix Ferber
 */
public interface ISimulatorListener {
	// Event with no meaning. Should be ignored.
	static final int UNKNOWN = 0;
	
	// Simulator status
	static final int PAUSED = 1;
	static final int RESUMED = 2;
	
	// Process state
	static final int DEPLOYING = 14;
	static final int LAUNCHING = 10;
	static final int OPERATIONAL = 11;
	static final int SHUTTING_DOWN = 12;
	static final int TERMINATED = 13;
		
	// events during SHUTTING_DOWN
	static final int SHUTDOWN_PREPARED = 20;
	static final int SHUTDOWN_START = 22;
	static final int SHUTDOWN_COMPLETE = 23;
	
	// events during LAUNCHING
	static final int LAUNCH = 200;
	
	static final int INIT_PARSE = 300;
	static final int INIT_CHECK = 301;
	static final int INIT_CONFIGURE = 302;
	static final int INIT_BOGUSNET = 303;
	static final int INIT_CONSOLE = 304;
	static final int INIT_CONFIGURED = 305;
	
	static final int BOOT_BIOS = 400;
	static final int BOOT_LINUX = 401;
	static final int BOOT_SYSTEM = 402;
	static final int BOOT_CONFIGURE = 403;
	static final int BOOT_COMPLETE = 404;
	
	// TERMINATED and OPERATIONAL have not events
		
	/**
	 * The simulator was paused or resumed.
	 * @param newStatus May be PAUSED or RESUMED.
	 */
	void simulationStatus(int status);
	
	/**
	 * The simulator changed lifecycle state.
	 * @param event
	 * @param operation May be LAUNCHING, OPERATIONAL, SHUTTING_DOWN, TERMINATED
	 */
	void lifecycleStateChanged(int state);
	
	/**
	 * The simulator advanced to next step during a transition from lifecycle states.
	 * @param progress
	 */
	void progressChanged(int progress);
}
