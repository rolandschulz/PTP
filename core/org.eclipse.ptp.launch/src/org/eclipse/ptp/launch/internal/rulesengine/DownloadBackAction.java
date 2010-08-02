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
import org.eclipse.core.runtime.SubMonitor;
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

	private final ILaunchProcessCallback fProcess;
	private final DownloadBackRule fRule;
	private final ILaunchConfiguration fConfiguration;
	private final IProgressMonitor fMonitor;

	public DownloadBackAction(ILaunchProcessCallback process, ILaunchConfiguration configuration,
			DownloadBackRule downloadBackRule, IProgressMonitor monitor) {
		super();
		fProcess = process;
		fRule = downloadBackRule;
		fConfiguration = configuration;
		fMonitor = monitor;

	}

	public void run() throws CoreException {
		Assert.isNotNull(fProcess);
		Assert.isNotNull(fRule);
		Assert.isNotNull(fConfiguration);

		SubMonitor progress = SubMonitor.convert(fMonitor, 10);

		try {
			// Get managers
			IRemoteFileManager remoteFileManager = fProcess.getRemoteFileManager(fConfiguration, progress.newChild(2));
			IRemoteFileManager localFileManager = fProcess.getLocalFileManager(fConfiguration);

			/*
			 * Process files in list.
			 */
			for (int i = 0; i < fRule.count(); i++) {
				IFileStore remoteFileStore = null;
				IFileStore localFileStore = null;

				remoteFileStore = remoteFileManager.getResource(fRule.getRemoteFile(i).toString());
				localFileStore = localFileManager.getResource(fRule.getLocalFile(i).getAbsolutePath());

				doDownload(remoteFileStore, localFileStore, progress.newChild(8));
			}
		} finally {
			progress.done();
		}
	}

	private void doDownload(IFileStore remoteFileStore, IFileStore localFileStore, IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 10);

		try {
			// Get remote and local file infos
			IFileInfo remoteFileInfo = remoteFileStore.fetchInfo(EFS.NONE, progress.newChild(5));

			if (!remoteFileInfo.exists()) {
				// Warn user that file doesn`t exist
				return;
			}

			remoteFileStore.copy(localFileStore, EFS.OVERWRITE, progress.newChild(5));
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}
}
