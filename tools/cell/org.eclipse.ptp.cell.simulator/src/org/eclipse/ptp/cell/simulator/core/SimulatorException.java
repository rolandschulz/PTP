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

/**
 * A super class for all exceptions raised during simulator launch/shutdown.
 * @author Daniel Felix Ferber
 */
public class SimulatorException extends Exception {

	private static final long serialVersionUID = 5515903325708714993L;

	public SimulatorException() {
		super();
	}

	public SimulatorException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public SimulatorException(String arg0) {
		super(arg0);
	}

	public SimulatorException(Throwable arg0) {
		super(arg0);
	}

}
