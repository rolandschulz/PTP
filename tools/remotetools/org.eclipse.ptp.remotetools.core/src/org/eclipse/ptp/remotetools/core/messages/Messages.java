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
package org.eclipse.ptp.remotetools.core.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.remotetools.core.messages.messages"; //$NON-NLS-1$

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

	public static String Connection_0;
	public static String Connection_1;
	public static String Connection_2;
	public static String Connection_Connect_FailedCreateSFTPConnection;
	public static String Connection_CreateConnectionSlot_FailedConnectNewSession;
	public static String Connection_CreateConnectionSlot_FailedCreateNewSession;
	public static String Connection_CreateExecChannel_FailedCreateNewExecChannel;
	public static String Connection_CreateTunnel_FailedCreateTunnel;
	public static String Connection_CreateTunnel_TunnelPortAlreadyAlloced;
	public static String Connection_ReleaseTunnel_FailedRemoveTunnel;
	public static String Connection_ReleaseTunnel_PortNotAllocedForTunnel;
	public static String Connection_SetCipherType_CipherNotSupported;
	public static String ControlChannel_0;
	public static String ControlChannel_Debug_ControlConnectionReceived;
	public static String ControlChannel_Debug_ReceivedControlTerminalPath;
	public static String ControlChannel_Debug_StartedWaitingControlTerminalPath;
	public static String ControlChannel_Open_FailedCreateAuxiliaryShell;
	public static String ControlChannel_Open_FailedCreateIOStream;
	public static String ControlChannel_Open_FailedSendInitCommands;
	public static String ControlChannel_Open_WaitControlTerminalPathInterrupted;
	public static String CopyTools_0;
	public static String CopyTools_doDownloadFileToFile_CannotWriteFile;
	public static String CopyTools_doUploadFileFromFile_CannotReadFile;
	public static String DownloadExecution_DownloadExecution_FailedCreateDownload;
	public static String ExecutionManager_CreateTunnel_AllLocalPortsBusy;
	public static String ExecutionObserver_ExecutionObserver_RemoteCommandObserver;
	public static String ExecutionTools_ExecuteBashCommand_FailedRunBashCommand;
	public static String FileEnumeration_0;
	public static String FileTools_10;
	public static String FileTools_12;
	public static String FileTools_13;
	public static String FileTools_14;
	public static String FileTools_15;
	public static String FileTools_2;
	public static String FileTools_3;
	public static String FileTools_4;
	public static String FileTools_5;
	public static String FileTools_6;
	public static String FileTools_7;
	public static String FileTools_8;
	public static String FileTools_9;
	public static String KillableExecution_Debug_1;
	public static String KillableExecution_FinishStatus_AlarmClock;
	public static String KillableExecution_FinishStatus_BackgroundProcessReadTTY;
	public static String KillableExecution_FinishStatus_BackgroundWriteTTY;
	public static String KillableExecution_FinishStatus_BrokenPipe;
	public static String KillableExecution_FinishStatus_BUSError;
	public static String KillableExecution_FinishStatus_ChildProcessStoppedOrExited;
	public static String KillableExecution_FinishStatus_CommandNotFound;
	public static String KillableExecution_FinishStatus_ContinueExecuting;
	public static String KillableExecution_FinishStatus_CPULimitExceeded;
	public static String KillableExecution_FinishStatus_Error;
	public static String KillableExecution_FinishStatus_FileSizeLimitExceeded;
	public static String KillableExecution_FinishStatus_FloatingPointException;
	public static String KillableExecution_FinishStatus_Hangup;
	public static String KillableExecution_FinishStatus_IllegalInstruction;
	public static String KillableExecution_FinishStatus_InvalidExitCode;
	public static String KillableExecution_FinishStatus_InvalidMemorySegmentAccess;
	public static String KillableExecution_FinishStatus_IOPossible;
	public static String KillableExecution_FinishStatus_IOTTrap;
	public static String KillableExecution_FinishStatus_Kill;
	public static String KillableExecution_FinishStatus_NotExecutable;
	public static String KillableExecution_FinishStatus_Ok;
	public static String KillableExecution_FinishStatus_PowerFailureRestart;
	public static String KillableExecution_FinishStatus_ProfilingAlarmClock;
	public static String KillableExecution_FinishStatus_StackFault;
	public static String KillableExecution_FinishStatus_StopExecuting;
	public static String KillableExecution_FinishStatus_TerminalInterrupt;
	public static String KillableExecution_FinishStatus_TerminalQuit;
	public static String KillableExecution_FinishStatus_TerminalStopSignal;
	public static String KillableExecution_FinishStatus_Termination;
	public static String KillableExecution_FinishStatus_TraceTrap;
	public static String KillableExecution_FinishStatus_Unknown;
	public static String KillableExecution_FinishStatus_UrgentConditionSocket;
	public static String KillableExecution_FinishStatus_UserDefinedSignal1;
	public static String KillableExecution_FinishStatus_UserDefinedSignal2;
	public static String KillableExecution_FinishStatus_VirtualAlarmClock;
	public static String KillableExecution_FinishStatus_WindowSizeChange;
	public static String PathTools_47;
	public static String PortForwardingException_0;
	public static String PortForwardingException_1;
	public static String PortForwardingException_2;
	public static String PortForwardingException_3;
	public static String RemoteExecutionException_0;
	public static String RemoteExecutionException_1;
	public static String RemoteFileTools_ValidateRemotePath_NotValid;
	public static String RemoteStatusTools_GetPasswdFields_NoUsernameInPasswdFile;
	public static String RemotePortForwarding_0;
	public static String ScriptExecution_StartExecution_FailedInitStreams;
	public static String UploadExecution_StartExecution_FailedCreateUpload;
}
