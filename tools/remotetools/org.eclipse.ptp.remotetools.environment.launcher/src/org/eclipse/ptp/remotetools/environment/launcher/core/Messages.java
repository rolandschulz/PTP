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
package org.eclipse.ptp.remotetools.environment.launcher.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.remotetools.environment.launcher.core.messages"; //$NON-NLS-1$

	public static String RemoteLaunchDelegate_LocalDirectory_DoesNotExist;

	public static String RemoteLaunchDelegate_LocalDirectory_IsNotADirectory;

	public static String RemoteLaunchDelegate_LocalDirectory_Missing;

	public static String RemoteLaunchDelegate_LocalDirectory_MustBeAbsolute;

	public static String RemoteLaunchDelegate_RemoteDirectory_MacroFailed;

	public static String RemoteLaunchDelegate_RemoteDirectory_MacroFailed_Unknown;

	public static String RemoteLaunchDelegate_RemoteDirectory_Missing;

	public static String RemoteLaunchDelegate_RemoteDirectory_MissingInPreferences;

	public static String RemoteLaunchDelegate_RemoteDirectory_MustBeAbsolute;

	public static String RemoteLaunchDelegate_SynchronizationRules_InternalError;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
