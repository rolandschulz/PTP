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

import org.eclipse.ptp.cell.utils.stream.IStreamListener;

/**
 * Receives text from the Linux console running inside the Cell Simulator and
 * forwards this text to the controller class.
 * <p>
 * Although this observer could be used to parse events from input stream, this
 * task should be delegated to the Mambo Simulator, which already has features
 * (called triggers) to parse Linux console output. When Mambo Simulator
 * recognizes an event, the trigger should print the event name on the stdout,
 * so it can be received by the TCLConsoleObserver, which is transparent to the
 * user.
 * 
 * @author Daniel Felix Ferber
 * @since 1.0
 */
class ConsoleListener implements IStreamListener {
	SimulatorControl control;
	
	ConsoleListener(SimulatorControl control) {
		this.control = control;
	}

	public void newBytes(byte[] bytes, int length) {
		control.receiveLinuxConsoleBytes(bytes, length);
	}

	public void streamClosed() {
		control.notifyEvent(SimulatorControl.LINUX_STREAM_CLOSED);
	}

	public void streamError(Exception e) {
		control.notifyEvent(SimulatorControl.LINUX_STREAM_CLOSED);
	}
}
