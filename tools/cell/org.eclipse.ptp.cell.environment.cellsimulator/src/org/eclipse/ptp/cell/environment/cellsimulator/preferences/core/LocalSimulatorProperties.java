/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.environment.cellsimulator.preferences.core;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author laggarcia
 * @since 3.0.0
 */
public class LocalSimulatorProperties extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.environment.cellsimulator.preferences.core.localSimulator"; //$NON-NLS-1$

	public static String simulatorSearchRootDirectory;

	public static String systemsim_cell_executable;

	public static String systemsim_cell_package;

	public static String SYSTEMSIM_TOP_variableName;

	public static String snifExecutable;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, LocalSimulatorProperties.class);
	}

	private LocalSimulatorProperties() {
	}
}
