/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.core;
/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;


/**
 * Node attributes
 */
public class SDMAttributes {

	private static final String SDM_EXECUTABLE = "sdmExec";
	private static final String SDM_HOST = "sdmHost";
	private static final String SDM_PORT = "sdmPort";
	private static final String SDM_DEBUGGER = "sdmDebugger";

	private final static StringAttributeDefinition sdmExecutabledDefinition =
		new StringAttributeDefinition(SDM_EXECUTABLE, "Debugger executable for SDM",
				"Debugger executable for SDM", true, "gdm-mi");

	private final static StringAttributeDefinition sdmHostDefinition =
		new StringAttributeDefinition(SDM_HOST, "Host for SDM",
				"Host for SDM", true, "localhost.localdomain");

	private final static IntegerAttributeDefinition sdmPortDefinition =
		new IntegerAttributeDefinition(SDM_PORT, "Port number for SDM",
				"Port number for SDM", true, 40876);
	
	private final static StringAttributeDefinition sdmDebuggerDefinition =
		new StringAttributeDefinition(SDM_DEBUGGER, "Debugger executable for SDM",
				"Debugger executable for SDM", true, "gdm-mi");

	/**
	 * Remote path to SDM debugger.
	 * <p>
	 * Note: openmpi 1.2 and 1.3
	 */
	public static StringAttributeDefinition getSdmExecutabledDefinition() {
		return sdmExecutabledDefinition;
	}
	
	/**
	 * Host name passed to SDM.
	 * <p>
	 * Note: openmpi 1.2 and 1.3
	 */
	public static StringAttributeDefinition getSDMHostDefinition() {
		return sdmHostDefinition;
	}

	/**
	 * Port number passed to SDM.
	 * <p>
	 * Note: openmpi 1.2 and 1.3
	 */
	public static IntegerAttributeDefinition getSDMPortDefinition() {
		return sdmPortDefinition;
	}

	/**
	 * Debugger name passed to SDM.
	 * <p>
	 * Note: openmpi 1.2 and 1.3
	 */
	public static StringAttributeDefinition getSDMDebuggerDefinition() {
		return sdmDebuggerDefinition;
	}

	public static IAttributeDefinition<?,?,?>[] getDefaultAttributeDefinitions() {
		return new IAttributeDefinition[] {sdmExecutabledDefinition, sdmHostDefinition, sdmPortDefinition, sdmDebuggerDefinition};
	}

}
