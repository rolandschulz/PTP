/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 */
package org.eclipse.ptp.rm.core.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.core.messages.messages"; //$NON-NLS-1$
	public static String AbstractRemoteCommandJob_Exception_CommandFailed;
	public static String AbstractRemoteCommandJob_Exception_InternalError;
	public static String AbstractToolRuntimeSystem_Exception_NoConnection;
	public static String AbstractToolRuntimeSystem_Exception_NoRemoteServices;
	public static String AbstractToolRuntimeSystem_Exception_ResourceManagerNotInitialized;
	public static String AbstractToolRuntimeSystem_JobQueueManagerThreadTitle;
	public static String AbstractToolRuntimeSystemJob_Exception_BeforeExecution;
	public static String AbstractToolRuntimeSystemJob_Exception_CreateCommand;
	public static String AbstractToolRuntimeSystemJob_Exception_DefaultAttributeValue;
	public static String AbstractToolRuntimeSystemJob_Exception_ExecuteCommand;
	public static String AbstractToolRuntimeSystemJob_Exception_ExecutionFinished;
	public static String AbstractToolRuntimeSystemJob_Exception_ExecutionStarted;
	public static String AbstractToolRuntimeSystemJob_Exception_LaunchApplication;
	public static String AbstractToolRuntimeSystemJob_Exception_PrepareExecution;
	public static String AbstractToolRuntimeSystemJob_Exception_WaitExecution;
	public static String AbstractToolRuntimeSystemJob_Success;
	
	public static String ToolsRMPlugin_Exception_InternalError;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}
