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
package org.eclipse.ptp.remotetools.environment.launcher.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.remotetools.environment.launcher.internal.messages"; //$NON-NLS-1$

	public static String DownloadBackAction_FailedDownloadBack;
	public static String DownloadBackAction_FailedFetchRemoteProperties;
	public static String DownloadBackAction_FailedFiledDoesNotExist;
	public static String DownloadBackAction_NotifyDonwloadBack;
	public static String DownloadBackAction_NotifyFileNotChanged;

	public static String DownloadBackRule_0;
	public static String DownloadRuleAction_FailedCalculateClockDifference;
	public static String DownloadRuleAction_FailedCalculateClockDifference2;
	public static String DownloadRuleAction_FailedCreateLocalDirectory;
	public static String DownloadRuleAction_FailedFetchAttributes;
	public static String DownloadRuleAction_FailedFetchRemoteAttributes;
	public static String DownloadRuleAction_FailedLocalDiretoryIsNotDirectory;
	public static String DownloadRuleAction_FailedRemotePathDoesNotExit;
	public static String DownloadRuleAction_FailedSetDownloadFile;
	public static String DownloadRuleAction_FailedSetLocalDirectoryAttributes;
	public static String DownloadRuleAction_FailedSetLocalFileAttributes;
	public static String DownloadRuleAction_NotiftFileExistsLocally;
	public static String DownloadRuleAction_NotiftNewerFileExistsLocally;
	public static String DownloadRuleAction_NotifyDirectoryDownload1;
	public static String DownloadRuleAction_NotifyDirectoryDownload2;
	public static String DownloadRuleAction_NotifyFileDownload1;
	public static String DownloadRuleAction_NotifyFileDownload2;
	public static String DownloadRuleAction_WarningClockBackward;
	public static String DownloadRuleAction_WarningClockForward;

	public static String RemoteLaunchProcess_0;
	public static String RemoteLaunchProcess_1;
	public static String RemoteLaunchProcess_10;
	public static String RemoteLaunchProcess_11;
	public static String RemoteLaunchProcess_12;
	public static String RemoteLaunchProcess_13;
	public static String RemoteLaunchProcess_14;
	public static String RemoteLaunchProcess_15;
	public static String RemoteLaunchProcess_16;
	public static String RemoteLaunchProcess_17;
	public static String RemoteLaunchProcess_18;
	public static String RemoteLaunchProcess_19;
	public static String RemoteLaunchProcess_2;
	public static String RemoteLaunchProcess_20;
	public static String RemoteLaunchProcess_21;
	public static String RemoteLaunchProcess_22;
	public static String RemoteLaunchProcess_23;
	public static String RemoteLaunchProcess_24;
	public static String RemoteLaunchProcess_25;
	public static String RemoteLaunchProcess_26;
	public static String RemoteLaunchProcess_27;
	public static String RemoteLaunchProcess_28;
	public static String RemoteLaunchProcess_3;
	public static String RemoteLaunchProcess_4;
	public static String RemoteLaunchProcess_5;
	public static String RemoteLaunchProcess_6;
	public static String RemoteLaunchProcess_7;
	public static String RemoteLaunchProcess_8;
	public static String RemoteLaunchProcess_9;

	public static String RemoteLaunchProcess_All_FailedConnection;
	public static String RemoteLaunchProcess_Failed;
	public static String RemoteLaunchProcess_FailedPermissions;
	public static String RemoteLaunchProcess_PrepareWorkingDir_Failed;
	public static String RemoteLaunchProcess_PrepareWorkingDir_FailedCreate;
	public static String RemoteLaunchProcess_PrepareWorkingDir_FailedCreateHint;
	public static String RemoteLaunchProcess_PrepareWorkingDir_FailedPermissions;
	public static String RemoteLaunchProcess_PrepareWorkingDir_NotRequired;
	public static String RemoteLaunchProcess_PrepareWorkingDir_Title;
	public static String RemoteLaunchProcess_RequestToCancelLaunch;
	public static String RemoteLaunchProcess_UploadApplication_CompletedUpload;
	public static String RemoteLaunchProcess_UploadApplication_FailedUpload;
	public static String RemoteLaunchProcess_UploadApplication_Title;
	public static String RemoteLaunchProcess_UploadApplication_TitleNoUpload;
	public static String RemoteLaunchProcess_UploadApplication_UploadMessage;
	public static String RemoteLaunchProcess_UploadWorkingDirectory_FailedRule;
	public static String RemoteLaunchProcess_UploadWorkingDirectory_IgnoreInactive;
	public static String RemoteLaunchProcess_UploadWorkingDirectory_IgnoreInvalid;
	public static String RemoteLaunchProcess_UploadWorkingDirectory_NoRules;
	public static String RemoteLaunchProcess_UploadWorkingDirectory_Title;
	public static String RemoteLaunchProcess_UploadWorkingDirectory_TitleUploadDisabled;
	public static String RemoteLaunchProcess_DownloadWorkingDirectory_TitleDownloadDisabled;
	public static String RemoteLaunchProcess_DownloadWorkingDirectory_Title;
	public static String RemoteLaunchProcess_DownloadWorkingDirectory_IgnoreInvalid;
	public static String RemoteLaunchProcess_DownloadWorkingDirectory_FailedRule;

	public static String UploadRuleAction_0;
	public static String UploadRuleAction_1;
	public static String UploadRuleAction_10;
	public static String UploadRuleAction_11;
	public static String UploadRuleAction_12;
	public static String UploadRuleAction_13;
	public static String UploadRuleAction_14;
	public static String UploadRuleAction_15;
	public static String UploadRuleAction_16;
	public static String UploadRuleAction_17;
	public static String UploadRuleAction_18;
	public static String UploadRuleAction_19;
	public static String UploadRuleAction_2;
	public static String UploadRuleAction_20;
	public static String UploadRuleAction_21;
	public static String UploadRuleAction_22;
	public static String UploadRuleAction_23;
	public static String UploadRuleAction_24;
	public static String UploadRuleAction_3;
	public static String UploadRuleAction_4;
	public static String UploadRuleAction_5;
	public static String UploadRuleAction_6;
	public static String UploadRuleAction_7;
	public static String UploadRuleAction_8;
	public static String UploadRuleAction_9;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
