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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.simulator.core.messages"; //$NON-NLS-1$

	public static String IllegalParameterException_DefaultMessage;

	public static String IllegalParameterException_EmptyValue;

	public static String IllegalParameterException_NullValue;

	public static String MissingParameterException_DefaultMessage;

	public static String SimulatorIllegalConfigurationException_DefaultMessage;

	public static String SimulatorKilledException_DefaultMessage;

	public static String SimulatorOperationException_CreateWorkingDirFailed;

	public static String SimulatorOperationException_DefaultMessage;

	public static String SimulatorOperationException_DeployFileFailed;

	public static String SimulatorOperationException_PathDoesNotExist;

	public static String SimulatorOperationException_PathNotAbsolute;

	public static String SimulatorOperationException_PathNotDir;

	public static String SimulatorOperationException_PathNotFile;

	public static String SimulatorOperationException_PathNotReadable;

	public static String SimulatorOperationException_PathNotWriteable;

	public static String SimulatorOperationException_UnexpectedException;

	public static String SimulatorOperationException_Unknown;

	public static String SimulatorOperationException_WorkingDirInuse;

	public static String SimulatorOperationException_WorkingDirNotWriteable;

	public static String SimulatorTerminatedException_DefaultMessage;
	
	public static String SimulatorTerminatedException_DefaultMessageWitError;
	
	public static String AbstractSimulatorConfiguration_SimulatorParameters;
	
	public static String AbstractSimulatorConfiguration_Network;
	
	public static String AbstractSimulatorConfiguration_FileSystem;
	
	public static String AbstractSimulatorConfiguration_JavaAPI;
	
	public static String AbstractSimulatorConfiguration_JavaAPIQuestion;
	
	public static String AbstractSimulatorConfiguration_JavaAPIGUIQuestion;
	
	public static String AbstractSimulatorConfiguration_LinuxConsole;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
