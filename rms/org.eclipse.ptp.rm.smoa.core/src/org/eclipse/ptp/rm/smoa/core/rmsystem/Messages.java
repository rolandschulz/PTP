/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.core.rmsystem;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.smoa.core.rmsystem.messages"; //$NON-NLS-1$
	public static String JobThread_ErrorDeletingTempFiles_text;
	public static String JobThread_ErrorDeletingTempFiles_title;
	public static String JobThread_ErrorErr;
	public static String JobThread_ErrorOpeningRemote;
	public static String JobThread_ErrorOut;
	public static String JobThread_ErrorReadingRemote;
	public static String JobThread_ExceptionByMonitoring;
	public static String JobThread_JobStateCancelled;
	public static String JobThread_JobStateFailed;
	public static String JobThread_JobStateFinisedWithStatus;
	public static String JobThread_JobStateUnknown;
	public static String JobThread_UnknownNode_text_1;
	public static String JobThread_UnknownNode_text_2;
	public static String JobThread_UnknownNode_title;
	public static String PoolingIntervalsAndStatic_JobSubmissionFailed;
	public static String SMOAResourceManager_7;
	public static String SMOAResourceManager_8;
	public static String SMOAResourceManager_JobSubmissionFailed;
	public static String SMOAResourceManager_PortForwardingFailed_text;
	public static String SMOAResourceManager_PortForwardingFailed_title;
	public static String SMOAResourceManagerConfiguration_AdditionalConnectionNotAvailable_text;
	public static String SMOAResourceManagerConfiguration_AdditionalConnectionNotAvailable_title;
	public static String SMOAResourceManagerConfiguration_CouldNotRetreivePassword;
	public static String SMOAResourceManagerConfiguration_CouldNotStorePassword;
	public static String SMOAResourceManagerConfiguration_PrefixAnonymous;
	public static String SMOAResourceManagerConfiguration_PrefixGsi;
	public static String SMOAResourceManagerConfiguration_ReusingConnection;
	public static String SMOAResourceManagerConfiguration_SmoaRmDescription;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
