/*******************************************************************************
 * Copyright (c) 2011 University of Tennessee and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University of Tennessee (Roland Schulz) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.git.core.messages;

import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rdt.sync.git.core.messages.messages"; //$NON-NLS-1$
	public static String CommandRunner_0;
	public static String CR_CreateInstanceError;
	public static String GRSC_CommitMessage;
	public static String GRSC_GitAddFailure;
	public static String GRSC_GitCommitFailure;
	public static String GRSC_GitInitFailure;
	public static String GRSC_GitLsFilesFailure;
	public static String GRSC_GitMergeFailure;
	public static String GRSC_GitRmFailure;
	public static String GSP_ChangeConnectionError;
	public static String GSP_ChangeLocationError;
	public static String GSP_ChangeProjectError;
	public static String GSP_SyncTaskName;
	public static String GitRemoteSyncConnection_0;
	public static String GitRemoteSyncConnection_1;
	public static String GitRemoteSyncConnection_2;
	public static String GitRemoteSyncConnection_3;
	public static String GitRemoteSyncConnection_adding_files;
	public static String GitRemoteSyncConnection_building_repo;
	public static String GitRemoteSyncConnection_committing_remote;
	public static String GitRemoteSyncConnection_getting_remote_file_status;
	public static String GitRemoteSyncConnection_sync_local_to_remote;
	public static String GitRemoteSyncConnection_sync_remote_to_local;
	public static String GitServiceProvider_0;
	public static String GitServiceProvider_1;
	public static String GitServiceProvider_2;
	public static String GitServiceProvider_3;
	public static String GitServiceProvider_4;
	public static String GitServiceProvider_5;
	public static String GitServiceProvider_6;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
