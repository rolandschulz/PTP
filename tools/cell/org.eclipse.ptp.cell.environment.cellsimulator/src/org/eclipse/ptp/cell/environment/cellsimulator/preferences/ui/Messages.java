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
package org.eclipse.ptp.cell.environment.cellsimulator.preferences.ui;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Leonardo Garcia, Daniel Felix Ferber
 * @since 3.0.0
 */
public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.environment.cellsimulator.preferences.ui.messages"; //$NON-NLS-1$

	public static String LocalSimulatorPreferencePage_LabelKernelImagePath;

	public static String LocalSimulatorPreferencePage_LabelRootImagePath;

	public static String LocalSimulatorPreferencePage_LabelShowLinuxconsole;

	public static String LocalSimulatorPreferencePage_LabelShowSimulatorConsole;

	public static String LocalSimulatorPreferencePage_LabelShowSimulatorGUI;

	public static String LocalSimulatorPreferencePage_LabelSimulatorBaseDirectory;

	public static String LocalSimulatorPreferencePage_LabelSystemWorkspace;

	public static String LocalSimulatorPreferencePage_LabelWorkDirectory;

	public static String RemoteSimulatorPreferencePage_LabelKernelImagePath;

	public static String RemoteSimulatorPreferencePage_LabelRootImagePath;

	public static String RemoteSimulatorPreferencePage_LabelShowLinuxConsole;

	public static String RemoteSimulatorPreferencePage_LabelShowSimulatorConsole;

	public static String RemoteSimulatorPreferencePage_LabelShowSimulatorGUI;

	public static String RemoteSimulatorPreferencePage_LabelSimulatorBaseDirectory;

	public static String RemoteSimulatorPreferencePage_LabelSystemWorkspace;

	public static String RemoteSimulatorPreferencePage_LabelWorkDirectory;

	public static String searchButtonText;
	public static String LocalSimulatorPreferencePage_NetworkConfigHeader1;
	public static String LocalSimulatorPreferencePage_PortConfigHeader1;
	public static String LocalSimulatorPreferencePage_PortConfigHeader2;

	public static String LocalSimulatorPreferencePage_Title;
	public static String LocalSimulatorPreferencePage_Description;
	public static String LocalSimulatorPreferencePage_Description_NoLocalSimulator;
	public static String LocalSimulatorPreferencePage_LabelBaseNetwork;
	public static String LocalSimulatorPreferencePage_LabelBaseMacaddress;
	public static String LocalSimulatorPreferencePage_LabelMinPort;
	public static String LocalSimulatorPreferencePage_LabelMaxPort;

	public static String RemoteSimulatorPreferencePage_Title;
	public static String RemoteSimulatorPreferencePage_Description;

	public static String RemoteSimulatorPreferencePage_HeaderLaunch;
	public static String LocalSimulatorPreferencePage_HeaderLaunch;


	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME,
				Messages.class);
	}

	private Messages() {
	}
}
