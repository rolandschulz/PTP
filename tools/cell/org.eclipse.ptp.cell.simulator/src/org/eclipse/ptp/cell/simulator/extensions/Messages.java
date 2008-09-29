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
package org.eclipse.ptp.cell.simulator.extensions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.simulator.extensions.messages"; //$NON-NLS-1$

	public static String ArchitectureManager_ExensionPointError;

	public static String ArchitectureManager_MissingArchitectureID;

	public static String ArchitectureManager_MissingArchitectureName;

	public static String ArchitectureManager_MissingArchitectureTCLScript;

	public static String ArchitectureManager_TCLScriptFileNotFound;

	public static String ArchitectureManager_TCLScriptURLNotResolved;

	public static String LaunchProfileManager_DeployFileNotFound;

	public static String LaunchProfileManager_DeployFileNotResolved;

	public static String LaunchProfileManager_EmptyFileNameEntry;

	public static String LaunchProfileManager_ExactlyOneTCLScript;

	public static String LaunchProfileManager_ExtensionPointError;

	public static String LaunchProfileManager_MissingProfileID;

	public static String LaunchProfileManager_MissingProfileName;

	public static String LaunchProfileManager_TCLScriptFileNotFound;

	public static String LaunchProfileManager_TCLScriptFileNotResolved;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
