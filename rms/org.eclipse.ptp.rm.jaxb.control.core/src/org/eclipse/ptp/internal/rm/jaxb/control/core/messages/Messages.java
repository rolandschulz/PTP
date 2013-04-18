/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.core.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.internal.rm.jaxb.control.core.messages.messages"; //$NON-NLS-1$
	public static String JAXBControlCorePlugin_Exception_InternalError;
	public static String RMVariableResolver_derefError;
	public static String StreamParserNoSuchVariableError;
	public static String StreamParserInconsistentMapValues;
	public static String ManagedFilesJob;
	public static String ManagedFilesJobError;
	public static String UninitializedRemoteServices;
	public static String OperationWasCancelled;

	public static String ScriptHandlerJob;
	public static String RMNoSuchCommandError;
	public static String MissingRunCommandsError;

	public static String MissingArglistFromCommandError;
	public static String CouldNotLaunch;
	public static String StdoutParserError;
	public static String StderrParserError;
	public static String ProcessExitValueError;
	public static String CannotCompleteSubmitFailedStaging;
	public static String ReadSegmentError;
	public static String StreamTokenizerInstantiationError;
	public static String MalformedExpressionError;
	public static String UnsupportedWriteException;
	public static String ProcessRunError;
	public static String CommandJobStreamMonitor_label;
	public static String CommandJobNullMonitorStreamError;
	public static String RemoteConnectionError;
	public static String StreamParserInconsistentPropertyWarning;
	public static String TargetImpl_2;
	public static String TargetImpl_3;
	public static String TargetImpl_6;

	public static String AbstractAssign_0;
	public static String AbstractAssign_1;
	public static String AbstractAssign_2;
	public static String AbstractAssign_3;
	public static String AbstractAssign_4;
	public static String AbstractAssign_5;
	public static String AbstractAssign_6;
	public static String AbstractAssign_7;
	public static String AbstractAssign_8;
	public static String AbstractAssign_9;
	public static String AbstractAssign_Invalid_arguments;
	public static String AbstractAssign_No_such_method;
	public static String AbstractAssign_Unable_to_get_attribute_value;
	public static String AbstractAssign_Unable_to_set_attribute_value;
	public static String MatchImpl_0;
	public static String MatchImpl_1;
	public static String MatchImpl_2;
	public static String MatchImpl_3;
	public static String MatchImpl_4;
	public static String MatchImpl_5;
	public static String BadEntryIndex;

	public static String LaunchController_missingServicesOrConnectionName;
	public static String LaunchController_notStarted;

	public static String ForceXmlReload;
	public static String ShowSegmentPattern;
	public static String ShowMatchStatus;
	public static String ShowActionsForMatch;
	public static String ShowCreatedProperties;
	public static String ShowCommand;
	public static String ShowCommandOutput;
	public static String LogFile;

	public static String RemoteServicesDelegate_Copy_Operation_NullSourceFileManager;
	public static String RemoteServicesDelegate_Copy_Operation_NullTargetFileManager;
	public static String RemoteServicesDelegate_Copy_Operation_NullSource;
	public static String RemoteServicesDelegate_Copy_Operation_NullTarget;
	public static String RemoteServicesDelegate_Copy_Operation_Local_resource_does_not_exist;
	public static String RemoteServicesDelegate_Write_Operation_NullSourceFileManager;
	public static String RemoteServicesDelegate_Write_Operation_NullPath;
	public static String RemoteServicesDelegate_Write_OperationFailed;
	public static String RemoteServicesDelegate_Read_Operation_NullSourceFileManager;
	public static String RemoteServicesDelegate_Read_Operation_NullPath;
	public static String RemoteServicesDelegate_Read_Operation_resource_does_not_exist;
	public static String RemoteServicesDelegate_Read_OperationFailed;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}
