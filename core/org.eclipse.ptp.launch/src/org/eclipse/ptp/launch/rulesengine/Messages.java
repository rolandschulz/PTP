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
package org.eclipse.ptp.launch.rulesengine;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.remotetools.environment.launcher.internal.messages"; //$NON-NLS-1$

	public static String DownloadBackAction_FailedDownloadBack;

	public static String DownloadBackAction_FailedFetchRemoteProperties;

	public static String DownloadBackAction_FailedFiledDoesNotExist;

	public static String DownloadBackAction_NotifyDonwloadBack;

	public static String DownloadBackAction_NotifyFileNotChanged;

	public static String DownloadRuleAction_FailedCalculateClockDifference;

	public static String DownloadRuleAction_FailedCalculateClockDifference2;

	public static String DownloadRuleAction_FailedCreateLocalDirectory;

	public static String DownloadRuleAction_FailedFetchAttributes;

	public static String DownloadRuleAction_FailedFetchRemoteAttributes;

	public static String DownloadRuleAction_FailedLocalDiretoryIsNotDirectory;

	public static String DownloadRuleAction_FailedRemotePathDoesNotExit;

	public static String DownloadRuleAction_FailedRemotePathNoDirectoryNorFile;

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
	public static String RemoteLaunchProcess_DownloadWorkingDirectory_IgnoreInactive;
	public static String RemoteLaunchProcess_DownloadWorkingDirectory_IgnoreInvalid;
	public static String RemoteLaunchProcess_DownloadWorkingDirectory_FailedRule;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
