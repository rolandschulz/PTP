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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.data.DownloadBackRule;
import org.eclipse.ptp.launch.internal.LinuxPath;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.exception.RemoteConnectionException;


public class DownloadBackAction implements IRuleAction {
	
	private ILaunchProcessCallback process;
	private DownloadBackRule rule;
	private ILaunchConfiguration configuration;
	private IProgressMonitor monitor;

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
			
			try {
			remoteFileStore = remoteFileManager.getResource(rule.getRemoteFile(i), 
					monitor);
			IPath localPath = new Path(rule.getLocalFile(i).getAbsolutePath());
			localFileStore = localFileManager.getResource(localPath, monitor);
			} catch (IOException e) {
				throw new CoreException(new Status(Status.ERROR, PTPLaunchPlugin.PLUGIN_ID, 
						"Error retrieving resource", e));
			}
			
			doDownload(remoteFileStore, localFileStore);
			//downloadFile(localFile, remotePath);
		}
	}

	private void doDownload(IFileStore remoteFileStore,
			IFileStore localFileStore) throws CoreException {
		
		// Get remote and local file infos
		IFileInfo remoteFileInfo = remoteFileStore.fetchInfo(EFS.NONE, monitor);
		//IFileInfo localFileInfo = localFileStore.fetchInfo();
		
		if(!remoteFileInfo.exists()) {
			// Warn user that file doesn`t exist
			return;
		}
		
		remoteFileStore.copy(localFileStore, EFS.OVERWRITE, monitor);
	}

	/*private void downloadFile(File localFile, IPath remotePath) throws CoreException, CancelException, RemoteConnectionException {
		String remotePathAsString = LinuxPath.toString(remotePath);
		
				
		IRemoteFile remoteFile = null;
		try {
			remoteFile = fileTools.getFile(remotePathAsString);
		} catch (RemoteOperationException e) {
			errorWriter.println(NLS.bind(Messages.DownloadBackAction_FailedFetchRemoteProperties, e.getMessage()));
			return;
		}
		
		if (! remoteFile.exists()) {
			errorWriter.println(NLS.bind(Messages.DownloadBackAction_FailedFiledDoesNotExist, remotePathAsString));
			return;			
		}
		
		
		 * Test if file has been changed.
		 
		long difference = remoteFile.getModificationTime() - localFile.lastModified();
		if (difference < 1000 && remoteFile.getSize() == localFile.length()) {
			outputWriter.println(NLS.bind(Messages.DownloadBackAction_NotifyFileNotChanged, remotePath));
			return;					
		}
		
		
		 * Download file
		 
		try {
			outputWriter.println(NLS.bind(Messages.DownloadBackAction_NotifyDonwloadBack, remotePathAsString));
			copyTools.downloadFileToFile(remotePathAsString, localFile);
		} catch (RemoteOperationException e) {
			errorWriter.println(NLS.bind(Messages.DownloadBackAction_FailedDownloadBack, e.getMessage()));
			return;
		}
	}*/
}
