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
package org.eclipse.ptp.cell.simulator.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.simulator.internal.messages"; //$NON-NLS-1$

	public static String SimulatorControl_BogusnetCleanUpError;

	public static String SimulatorControl_CouldNotConnectConsole;

	public static String SimulatorControl_CouldNotConnectJavaAPI;

	public static String SimulatorControl_CouldNotConnectProcess;

	public static String SimulatorControl_EmptyArrayElement;

	public static String SimulatorControl_ErrorNotifiedBySimulator;

	public static String SimulatorControl_FailedToDeployTCLFile;
	
	public static String SimulatorControl_InvalidEvent1;

	public static String SimulatorControl_InvalidEvent10;

	public static String SimulatorControl_InvalidEvent2;

	public static String SimulatorControl_InvalidEvent3;

	public static String SimulatorControl_InvalidEvent4;

	public static String SimulatorControl_InvalidEvent5;

	public static String SimulatorControl_InvalidEvent6;

	public static String SimulatorControl_InvalidEvent7;

	public static String SimulatorControl_InvalidEvent8;

	public static String SimulatorControl_InvalidEvent9;

	public static String SimulatorControl_MaxConsoleTries;

	public static String SimulatorControl_MaxJavaAPITries;

	public static String SimulatorControl_MissingArrayElement;

	public static String SimulatorControl_SameSizeArray;

	public static String SimulatorControl_SimulatorAlreadyRunningInDirectory;

	public static String SimulatorControl_SimulatorAlreadyStarted;

	public static String SimulatorControl_SimulatorNotOperatioal;

	public static String SimulatorControl_SimulatorNotRunningAnymore;

	public static String SimulatorControl_SimulatorNowPaused;

	public static String SimulatorControl_SimulatorNowResumed;

	public static String SimulatorControl_SimulatorTerminated;

	public static String SimulatorControl_UnexpectedError;

	public static String SimulatorControl_UseThisLinuxConsole;

	public static String SimulatorControl_UseThisTCLConsole;

	public static String SimulatorControl_WorkDirCleanUpError;

	public static String StdoutListener_UnknownEvent;

	public static String ConfigurationControl_MemorySize;
	
	public static String SimulatorControl_JavaAPINotAllowed;
	
	public static String SimulatorControl_CannotShowSimulatorGUI;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
