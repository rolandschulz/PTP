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
package org.eclipse.ptp.cell.sputiming.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.sputiming.ui.messages"; //$NON-NLS-1$

	public static String SPUTimingMainTab_CompilerGroup_CompilerLabel;

	public static String SPUTimingMainTab_CompilerGroup_FlagsLabel;

	public static String SPUTimingMainTab_CompilerGroup_GroupLabel;

	public static String SPUTimingMainTab_GetCSources_SourceListError;

	public static String SPUTimingMainTab_IsValid_AssemblyFlagMessage;

	public static String SPUTimingMainTab_IsValid_InvalidFilenameError;

	public static String SPUTimingMainTab_IsValid_NoCompilerError;

	public static String SPUTimingMainTab_IsValid_NoProjectNameError;

	public static String SPUTimingMainTab_IsValid_NoSourceFileError;

	public static String SPUTimingMainTab_IsValid_ProjectMustBeOpened;

	public static String SPUTimingMainTab_IsValid_ProjectMustExistError;

	public static String SPUTimingMainTab_IsValid_SourceFileMustExistError;

	public static String SPUTimingMainTab_IsValid_ValidAssemblyFilenameError;

	public static String SPUTimingMainTab_IsValid_ValidSPUTimingPathError;

	public static String SPUTimingMainTab_MessageGroup_;

	public static String SPUTimingMainTab_ProjectGroup_GroupLabel;

	public static String SPUTimingMainTab_ProjectGroup_ProjectLabel;

	public static String SPUTimingMainTab_ProjectGroup_SearchProjectButton;

	public static String SPUTimingMainTab_ProjectGroup_SearchTargetButton;

	public static String SPUTimingMainTab_ProjectGroup_SourceLabel;

	public static String SPUTimingMainTab_SearchCCompilerButtonAction_CDTErrorMessage;

	public static String SPUTimingMainTab_SearchCCompilerButtonAction_CDTErrorTitle;

	public static String SPUTimingMainTab_SearchCCompilerButtonAction_FlagsFetchingMessage;

	public static String SPUTimingMainTab_SelectCSource_DialogMessage;

	public static String SPUTimingMainTab_SelectCSource_DialogTitle;

	public static String SPUTimingMainTab_SPUTimingGroup_AssemblyFileLabel;

	public static String SPUTimingMainTab_SPUTimingGroup_GroupLabel;

	public static String SPUTimingMainTab_SPUTimingGroup_SPUTimingLabel;

	public static String SPUTimingMainTab_SPUTimingGroup_ArchitectureComboLabel;

	public static String SPUTimingMainTab_SPUTimingGroup_ArchitectureCell;

	public static String SPUTimingMainTab_SPUTimingGroup_ArchitectureSoma;

	public static String SPUTimingMainTab_IsValid_ValidArchitectureError;

	public static String SPUTimingMainTab_Title;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
