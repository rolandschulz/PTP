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
package org.eclipse.ptp.launch.internal.rulesengine;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.launch.rulesengine.ILaunchProcessCallback;
import org.eclipse.ptp.launch.rulesengine.IRuleAction;
import org.eclipse.ptp.remote.core.IRemoteFileManager;

/**
 * TODO NEEDS TO BE DOCUMENTED
 * 
 * @since 4.1
 */
public class DownloadBackAction implements IRuleAction {

	private final ILaunchProcessCallback process;
	private final DownloadBackRule rule;
	private final ILaunchConfiguration configuration;
	private final IProgressMonitor monitor;

	public DownloadBackAction(ILaunchProcessCallback process, ILaunchConfiguration configuration,
			DownloadBackRule downloadBackRule, IProgressMonitor monitor) {
		super();
		this.process = process;
		this.rule = downloadBackRule;
		this.configuration = configuration;
		this.monitor = monitor;

	}

	public void run() throws CoreException {
		Assert.isNotNull(process);
		Assert.isNotNull(rule);
		Assert.isNotNull(configuration);

		// Get managers
		IRemoteFileManager remoteFileManager = process.getRemoteFileManager(configuration);
		IRemoteFileManager localFileManager = process.getLocalFileManager(configuration);

		/*
		 * Process files in list.
		 */
		for (int i = 0; i < rule.count(); i++) {
			IFileStore remoteFileStore = null;
			IFileStore localFileStore = null;

			remoteFileStore = remoteFileManager.getResource(rule.getRemoteFile(i).toString());
			localFileStore = localFileManager.getResource(rule.getLocalFile(i).getAbsolutePath());

			doDownload(remoteFileStore, localFileStore);
		}
	}

	private void doDownload(IFileStore remoteFileStore, IFileStore localFileStore) throws CoreException {

		// Get remote and local file infos
		IFileInfo remoteFileInfo = remoteFileStore.fetchInfo(EFS.NONE, monitor);
		// IFileInfo localFileInfo = localFileStore.fetchInfo();

		if (!remoteFileInfo.exists()) {
			// Warn user that file doesn`t exist
			return;
		}

		remoteFileStore.copy(localFileStore, EFS.OVERWRITE, monitor);
	}

	/*
	 * private void downloadFile(File localFile, IPath remotePath) throws
	 * CoreException, CancelException, RemoteConnectionException { String
	 * remotePathAsString = LinuxPath.toString(remotePath);
	 * 
	 * 
	 * IRemoteFile remoteFile = null; try { remoteFile =
	 * fileTools.getFile(remotePathAsString); } catch (RemoteOperationException
	 * e) { errorWriter.println(NLS.bind(Messages.
	 * DownloadBackAction_FailedFetchRemoteProperties, e.getMessage())); return;
	 * }
	 * 
	 * if (! remoteFile.exists()) { errorWriter.println(NLS.bind(Messages.
	 * DownloadBackAction_FailedFiledDoesNotExist, remotePathAsString)); return;
	 * }
	 * 
	 * 
	 * Test if file has been changed.
	 * 
	 * long difference = remoteFile.getModificationTime() -
	 * localFile.lastModified(); if (difference < 1000 && remoteFile.getSize()
	 * == localFile.length()) { outputWriter.println(NLS.bind(Messages.
	 * DownloadBackAction_NotifyFileNotChanged, remotePath)); return; }
	 * 
	 * 
	 * Download file
	 * 
	 * try {
	 * outputWriter.println(NLS.bind(Messages.DownloadBackAction_NotifyDonwloadBack
	 * , remotePathAsString)); copyTools.downloadFileToFile(remotePathAsString,
	 * localFile); } catch (RemoteOperationException e) {
	 * errorWriter.println(NLS
	 * .bind(Messages.DownloadBackAction_FailedDownloadBack, e.getMessage()));
	 * return; } }
	 */
}
