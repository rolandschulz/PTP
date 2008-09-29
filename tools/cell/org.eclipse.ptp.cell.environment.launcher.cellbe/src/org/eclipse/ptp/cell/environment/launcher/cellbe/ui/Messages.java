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
package org.eclipse.ptp.cell.environment.launcher.cellbe.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.environment.launcher.cellbe.ui.messages"; //$NON-NLS-1$

	public static String TargetTab_OptionsFrame_ExportX11ButtonLabel;

	public static String TargetTab_OptionsFrame_ExportX11Note;

	public static String TargetTab_OptionsFrame_Title;

	public static String TargetTab_Tab_Message;

	public static String TargetTab_Tab_Title;

	public static String TargetTab_TargetFrame_TargetListLabel;

	public static String TargetTab_TargetFrame_Title;

	public static String TargetTab_WorkingDirectoryFrame_CleanUpButtonLabel;

	public static String TargetTab_WorkingDirectoryFrame_DirectoryFieldLabel;

	public static String TargetTab_WorkingDirectoryFrame_PreviewNotAvailable;

	public static String TargetTab_WorkingDirectoryFrame_Title;

	public static String TargetTab_WorkingDirectoryFrame_UseDefaultButtonLabel;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
