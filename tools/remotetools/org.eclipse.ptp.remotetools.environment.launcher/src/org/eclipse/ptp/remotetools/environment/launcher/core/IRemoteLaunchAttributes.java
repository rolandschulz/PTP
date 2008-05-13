/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.environment.launcher.core;

/**
 * Definition of key names and default values that are used by the launcher.
 * @author Daniel Felix Ferber
 */
public interface IRemoteLaunchAttributes {
	public static final String LAUNCH_ID = "org.eclipse.ptp.remotetools.environment.launcher.core"; //$NON-NLS-1$
	
	public static final String ATTR_AUTOMATIC_WORKING_DIRECTORY = LAUNCH_ID + ".AUTO_REMOTE_DIRECTORY"; //$NON-NLS-1$
	public static final String ATTR_REMOTE_DIRECTORY = LAUNCH_ID + ".REMOTE_DIRECTORY"; //$NON-NLS-1$
	/** @deprecated */
	public static final String ATTR_LOCAL_DIRECTORY = LAUNCH_ID + ".LOCAL_DIRECTORY"; //$NON-NLS-1$
	public static final String ATTR_SYNC_BEFORE = LAUNCH_ID + ".SYNC_BEFORE"; //$NON-NLS-1$
	public static final String ATTR_SYNC_AFTER = LAUNCH_ID + ".SYNC_AFTER"; //$NON-NLS-1$
	public static final String ATTR_SYNC_CLEANUP = LAUNCH_ID + ".SYNC_CLEANUP"; //$NON-NLS-1$
	public static final String ATTR_USE_FORWARDED_X11 = LAUNCH_ID + ".FORWARD_X11"; //$NON-NLS-1$
	public static final String ATTR_OUTPUT_OBSERVER = LAUNCH_ID + ".OUTPUT_OBSERVER";  //$NON-NLS-1$
	public static final String ATTR_BEFORE_COMMAND = LAUNCH_ID + ".BEFORE_COMMAND";  //$NON-NLS-1$
	public static final String ATTR_AFTER_COMMAND = LAUNCH_ID + ".AFTER_COMMAND"; //$NON-NLS-1$
	public static final String ATTR_SYNC_RULES = LAUNCH_ID + ".SYNC_RULES"; //$NON-NLS-1$
	
	public static final boolean DEFAULT_AUTOMATIC_WORKING_DIRECTORY = true;
	public static final String DEFAULT_LOCAL_DIRECTORY = ""; //$NON-NLS-1$
	public static final boolean DEFAULT_SYNC_BEFORE = true;
	public static final boolean DEFAULT_SYNC_AFTER = true;
	public static final boolean DEFAULT_SYNC_CLEANUP = true;
	public static final boolean DEFAULT_USE_FORWARDED_X11 = false;
	public static final String DEFAULT_AFTER_COMMAND = null;
	public static final String DEFAULT_BEFORE_COMMAND = null;
	public static final String DEFAULT_OUTPUT_OBSERVER = null;
	public static final String DEFAULT_BEFORE_FILES = ""; //$NON-NLS-1$
	public static final String DEFAULT_AFTER_FILES = ""; //$NON-NLS-1$

}
