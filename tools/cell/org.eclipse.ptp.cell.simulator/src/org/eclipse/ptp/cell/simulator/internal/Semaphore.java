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

import org.eclipse.ptp.cell.simulator.core.SimulatorException;
import org.eclipse.ptp.cell.simulator.core.SimulatorTerminatedException;

public class Semaphore {
	boolean isOpen = false;
	SimulatorException error = null;
	
	public synchronized void waitToOpen(int time) throws SimulatorException {
		if (isOpen) {
			return;
		}
		if (error != null) {
			throw error;
		}
		try {
			this.wait(time);
		} catch (InterruptedException e) {
			throw new SimulatorTerminatedException();
		}
		if (error != null) {
			throw error;
		}		
	}
	
	public synchronized void waitToOpen() throws SimulatorException {
		try {
			while (! isOpen) {
				if (error != null) {
					throw error;
				}
				this.wait();
			} 
		} catch (InterruptedException e) {
			throw new SimulatorTerminatedException();
		}
	}
	
	public synchronized void open() {
		isOpen = true;
		this.notifyAll();
	}
	
	public synchronized void open(SimulatorException e) {
		error = e;
		this.notifyAll();
	}
	
	public synchronized boolean isOpen() {
		return isOpen;
	}
	
	public synchronized void reset() {
		isOpen = false;
		error = null;
	}
}
