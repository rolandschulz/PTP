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
package org.eclipse.ptp.cell.sputiming.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.sputiming.core.messages"; //$NON-NLS-1$

	public static String ExternalTools_FailedExecuteExtensionPoint;

	public static String LaunchConfigurationDelegate_Task_Name;
	public static String LaunchConfigurationDelegate_Console_Name;
	public static String CommonOperations_DisplayFile_NoFileFound;
	public static String PopupActionDelegate_GetCompilerFlags_CannotExtractInformation;
	public static String PopupActionDelegate_GetCompilerCommand_CannotResolveName;
	public static String PopupActionDelegate_GetCompilerCommand_CannotExtractBuildCommand;
	public static String PopupActionDelegate_CheckBuildInfo_CannotExtractArchitectureInfo;
	public static String PopupActionDelegate_GetCompilerTool_ProblemFetchingCompilerTool;
	public static String PopupActionDelegate_CheckSPEProjectType_NotSPEProject;
	public static String PopupActionDelegate_CheckBuildInfo_CannotExtractBuildInfo;
	public static String PopupActionDelegate_CheckBuildInfo_ProjectTypeNotSupported;
	public static String SPUTimingPopupAction_Popup_Run_JobLabel;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
