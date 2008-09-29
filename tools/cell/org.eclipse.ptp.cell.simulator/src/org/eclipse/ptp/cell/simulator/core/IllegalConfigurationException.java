/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.simulator.core;

/**
 * Thrown when trying to launch the simulator with a launch configuration
 * that contains invalid attributes or that make the configuration invalid as a whole.
 * @author Daniel Felix Ferber
 *
 */
public class IllegalConfigurationException extends SimulatorException {

	private static final long serialVersionUID = -8063672172919678022L;

	public IllegalConfigurationException() {
		super(Messages.SimulatorIllegalConfigurationException_DefaultMessage);
	}

	public IllegalConfigurationException(String arg0) {
		super(arg0);
	}

}
