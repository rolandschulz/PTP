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

package org.eclipse.ptp.rm.smoa.ui.actions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.smoa.ui.actions.messages"; //$NON-NLS-1$
	public static String SelectConnetionAndDestDir_Browse;
	public static String SelectConnetionAndDestDir_Cancel;
	public static String SelectConnetionAndDestDir_CreateIfNotExists;
	public static String SelectConnetionAndDestDir_DefaultRules;
	public static String SelectConnetionAndDestDir_DefaultWindowTitle;
	public static String SelectConnetionAndDestDir_DirBrowserTitle;
	public static String SelectConnetionAndDestDir_ErrorByFetchInfoTitle;
	public static String SelectConnetionAndDestDir_ErrorNoActiveConnection;
	public static String SelectConnetionAndDestDir_ErrorNoActiveConnectionTitle;
	public static String SelectConnetionAndDestDir_ExcludeRules;
	public static String SelectConnetionAndDestDir_Ok;
	public static String SelectConnetionAndDestDir_RemoteRootPath;
	public static String SelectConnetionAndDestDir_ResourceManager;
	public static String SMOACustomSyncAction_CurrentFile;
	public static String SMOACustomSyncAction_ErrorAccessingRemoteFS;
	public static String SMOACustomSyncAction_ErrorDialogTitle;
	public static String SMOACustomSyncAction_ErrorOverwritePolicyTitle;
	public static String SMOACustomSyncAction_ExceptionByReadingSettings;
	public static String SMOACustomSyncAction_ExceptionBySynchro;
	public static String SMOACustomSyncAction_ExceptionByWritingSettings;
	public static String SMOACustomSyncAction_FileOverwritePolicy;
	public static String SMOACustomSyncAction_FilesCopied;
	public static String SMOACustomSyncAction_FilesTotal;
	public static String SMOACustomSyncAction_FromLocal;
	public static String SMOACustomSyncAction_FromRemote;
	public static String SMOACustomSyncAction_IncorretOverwritePolicy;
	public static String SMOACustomSyncAction_Local;
	public static String SMOACustomSyncAction_PleaswWait;
	public static String SMOACustomSyncAction_ProgressWindowTitle;
	public static String SMOACustomSyncAction_RegeneratingTree;
	public static String SMOACustomSyncAction_RegeneratingTree_FoundFiles;
	public static String SMOACustomSyncAction_Remote;
	public static String SMOACustomSyncAction_WindowTitle;
	public static String SMOAToLocalSyncAction_WindowTitle;
	public static String SMOAToRemoteSyncAction_WindowTitle;
	public static String SMOASyncAction_ExceptionBySynchro;
	public static String SMOASyncAction_ErrorByRsync_desc;
	public static String SMOASyncAction_ErrorByRsync_title;
	public static String SMOASyncAction_RsyncInProgress;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
