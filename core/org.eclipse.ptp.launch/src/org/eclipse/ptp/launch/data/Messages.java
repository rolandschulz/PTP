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
package org.eclipse.ptp.launch.data;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.remotetools.environment.launcher.data.messages"; //$NON-NLS-1$

	public static String DownloadRule_Validation_MissingOverwritePolicy;

	public static String DownloadRule_Validation_MissingRemoteDirectory;

	public static String ExecutionConfiguration_Error_MissingExecutable;

	public static String ExecutionConfiguration_Error_MissingLocalRemoteDirectory;

	public static String ExecutionConfiguration_Error_MissingProject;

	public static String ExecutionConfiguration_Error_MissingRemoteWorkingDirectory;

	public static String UploadRule_Validate_MissingOverwritePolicy;

	public static String UploadRule_Validate_MissingRemotedirectory;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
