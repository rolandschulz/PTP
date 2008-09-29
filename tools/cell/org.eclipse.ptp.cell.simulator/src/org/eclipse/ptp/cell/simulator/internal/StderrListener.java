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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ptp.cell.utils.stream.ILineStreamListener;


/**
 * Receives text lines from the simulator stderr that runs the TCL command
 * console. Notifies the Controller about important events, mainly most common
 * errors. Lines that match events are filtered out. Other lines that do not
 * match are forwarded to the controller.
 * 
 * @author Daniel Felix Ferber
 * @since 1.0
 */
class StderrListener implements ILineStreamListener {
	/*
	 * TODO: Handle unrecognized errors
	 * - invalid option -- a (when an invalid command line switch is used)
	 * 
	 * TODO: Handle special messages
	 * - Timing information: hide/give color
	 * 320000000: [0:0]: (PC:0xC0000000000ECFE8) :   6645.3 Kilo-Inst/Sec :   6666.7 Kilo-Cycles/Sec
	 * 320000000: [0:1]: (PC:0xC000000000032590) :      3.6 Kilo-Inst/Sec
	 * 
	 * - Errors: hide/give color/show as error
	 * WARNING: 23739901: [0:0]: RTAS freeze-time-base
	 * WARNING: 23740785: [0:0]: RTAS thaw-time-base
	 * 
	 * - Special errors:
	 * WARNING: 696346594194: Caught TERM Signal. Stopping Simulation
	 * WARNING: 0: Unable to bind socket for console connection
	 * 
	 * - Simulated stdout or messages:
	 * 56075859: (55968631): io scheduler cfq registered
	 * 59186994: (58986190): wdrtas: couldn't get token for get-sensor-state. Trying to continue without temperature support.
	 */
	
	SimulatorControl control;

	Pattern patternStdout = Pattern.compile("\\s*\\d+\\s*:\\s*\\(\\s*\\d+\\s*\\)\\s*:.*"); //$NON-NLS-1$
//	Pattern patternTimeInfo = Pattern.compile("\d+\s*:\s*\[\s*\d+:\s*\d+\s*\]);
	
	/**
	 * Constructor. 
	 * <p>
	 * Do not forget to call run() to observer the simulator.
	 * 
	 * @param control
	 *            controller class that receives events
	 */
	StderrListener(SimulatorControl control) {
		this.control = control;
	}

	public void newLine(String line) {
		if (line.startsWith("WARNING:")) { //$NON-NLS-1$
			// Parse common error messages.
			if (line.endsWith("no console connection made")) { //$NON-NLS-1$
				control.notifyError("Could not connect console to simulator."); //$NON-NLS-1$
			} else if (line.endsWith("unable to bind socket")) { //$NON-NLS-1$
				control.notifyError("Could not connect console to simulator."); //$NON-NLS-1$
			} else if (line.endsWith("init: couldn't allocated tun")) { //$NON-NLS-1$
				control.notifyError("Could not allocate tun/tap network."); //$NON-NLS-1$
			} else {
				forwardUnhandledError(line);
			}
		} else {
			// Parse common patterns
			Matcher matcher = patternStdout.matcher(line);
			if (matcher.matches()) {
				// Ignore the line
			} else if (line.indexOf("Execution stopped: Mambo Error") > 0) { //$NON-NLS-1$
				// Ignore the line				
			} else if ((line.indexOf("finished running") > 0) && (line.indexOf("instructions") > 0)) { //$NON-NLS-1$ //$NON-NLS-2$
				// Ignore the line
			} else {
				forwardLine(line);
			}
		}
	}
	
	private void forwardLine(String line) {
		control.receiveProcessErrorLine(line);
	}

	private void forwardUnhandledError(String line) {
		control.receiveProcessErrorLine(line);
	}

	public void streamClosed() {
		control.notifyEvent(SimulatorControl.TCL_ERROR_CLOSED);
	}

	public void streamError(Exception e) {
		control.notifyEvent(SimulatorControl.TCL_ERROR_CLOSED);
	}
}
