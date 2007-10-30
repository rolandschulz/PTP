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

import java.io.File;
import java.io.PrintWriter;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.remotetools.core.IRemoteCopyTools;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteFile;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.environment.launcher.core.LinuxPath;
import org.eclipse.ptp.remotetools.environment.launcher.data.ExecutionConfiguration;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;


public class DownloadBackAction implements IRuleAction {
	
	private ILaunchProcessCallback process;
	private DownloadBackRule rule;
	private ExecutionConfiguration configuration;
	private PrintWriter outputWriter;
	private PrintWriter errorWriter;
	private IRemoteExecutionManager manager;

	public DownloadBackAction(ILaunchProcessCallback process, DownloadBackRule downloadBackRule) {
		super();
		this.process = process;
		this.rule = downloadBackRule;
		configuration = process.getConfiguration();
		outputWriter = process.getOutputWriter();
		errorWriter = process.getErrorWriter();	
		manager = process.getExecutionManager();
	}

	public void run() throws CoreException, CancelException, RemoteConnectionException {
		Assert.isNotNull(process);
		Assert.isNotNull(rule);
		Assert.isNotNull(configuration);
		Assert.isNotNull(outputWriter);
		Assert.isNotNull(errorWriter);
		Assert.isNotNull(manager);
		
		/*
		 * Process files in list.
		 */
		for (int i = 0; i < rule.count(); i++) {
			File localFile = rule.getLocalFile(i);
			IPath remotePath = rule.getRemoteFile(i);
			downloadFile(localFile, remotePath);
		}
	}

	private void downloadFile(File localFile, IPath remotePath) throws CoreException, CancelException, RemoteConnectionException {
		String remotePathAsString = LinuxPath.toString(remotePath);
		
		IRemoteCopyTools copyTools = manager.getRemoteCopyTools();
		IRemoteFileTools fileTools = manager.getRemoteFileTools();
				
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
		
		/*
		 * Test if file has been changed.
		 */
		long difference = remoteFile.getModificationTime() - localFile.lastModified();
		if (difference < 1000 && remoteFile.getSize() == localFile.length()) {
			outputWriter.println(NLS.bind(Messages.DownloadBackAction_NotifyFileNotChanged, remotePath));
			return;					
		}
		
		/*
		 * Download file
		 */
		try {
			outputWriter.println(NLS.bind(Messages.DownloadBackAction_NotifyDonwloadBack, remotePathAsString));
			copyTools.downloadFileToFile(remotePathAsString, localFile);
		} catch (RemoteOperationException e) {
			errorWriter.println(NLS.bind(Messages.DownloadBackAction_FailedDownloadBack, e.getMessage()));
			return;
		}
	}
}
