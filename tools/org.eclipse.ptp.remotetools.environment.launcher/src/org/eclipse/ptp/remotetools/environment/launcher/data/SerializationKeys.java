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
package org.eclipse.ptp.remotetools.environment.launcher.data;


interface SerializationKeys {
	public static String TYPE_UPLOAD = "upload"; //$NON-NLS-1$
	public static String TYPE_DOWNLOAD = "download"; //$NON-NLS-1$
	
	public static String KEY_REMOTE_PATH = "remote-path"; //$NON-NLS-1$

	public static String KEY_OVERWRITE_POLICY = "overwrite-policy"; //$NON-NLS-1$

	public static String KEY_OVERWRITE_POLICY_SKIP = "skip"; //$NON-NLS-1$

	public static String KEY_OVERWRITE_POLICY_ALWAYS = "always"; //$NON-NLS-1$

	public static String KEY_OVERWRITE_POLICY_NEWER = "newer"; //$NON-NLS-1$

	public static String KEY_OVERWRITE_POLICY_ASK = "ask"; //$NON-NLS-1$

	public static String KEY_PERMISSIONS = "permissions"; //$NON-NLS-1$

	public static String KEY_PERMISSIONS_READONLY = "readonly"; //$NON-NLS-1$

	public static String KEY_PERMISSIONS_EXECUTABLE = "executable"; //$NON-NLS-1$

	public static String KEY_FLAGS = "flags"; //$NON-NLS-1$

	public static String KEY_FLAGS_TIMESTAMP = "timestamp"; //$NON-NLS-1$

	public static String KEY_FLAGS_DOWNLOAD_BACK = "download-back"; //$NON-NLS-1$

	public static String KEY_FLAGS_DEFAULT_REMOTE_DIRECTORY = "default-remote-directory"; //$NON-NLS-1$

	public static String KEY_LOCAL_PATH = "local-path"; //$NON-NLS-1$
}
