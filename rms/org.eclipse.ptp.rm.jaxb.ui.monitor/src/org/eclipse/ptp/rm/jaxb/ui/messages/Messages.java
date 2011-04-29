/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.jaxb.ui.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.jaxb.ui.messages.messages"; //$NON-NLS-1$

	public static String JOB_ID;
	public static String STATE;
	public static String STATE_DETAIL;
	public static String STDOUT_PATH;
	public static String STDERR_PATH;
	public static String STDOUT_READY;
	public static String STDERR_READY;
	public static String JobListUpdate;

	public static String OperationFailed;
	public static String DoControlError;
	public static String ConsoleWriteError;

	public static String RemoveJobWarning;
	public static String CannotUndoOperation;

	public static String JAXBMonitorPlugin_Exception_InternalError;
	public static String RefreshJobStatus;
	public static String RefreshJobStatusError;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}
