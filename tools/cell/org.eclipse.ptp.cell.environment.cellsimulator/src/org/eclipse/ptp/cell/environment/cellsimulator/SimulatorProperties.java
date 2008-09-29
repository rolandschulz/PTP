/**
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.environment.cellsimulator;

import org.eclipse.osgi.util.NLS;

public class SimulatorProperties extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.environment.cellsimulator.simulator"; //$NON-NLS-1$

	public static String simulatorPackage; 
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, SimulatorProperties.class);
	}

	private SimulatorProperties() {
	}
}
