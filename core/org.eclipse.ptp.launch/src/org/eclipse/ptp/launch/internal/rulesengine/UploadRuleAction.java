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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.launch.rulesengine.ILaunchProcessCallback;
import org.eclipse.ptp.launch.rulesengine.IRuleAction;
import org.eclipse.ptp.launch.rulesengine.OverwritePolicies;
import org.eclipse.ptp.remote.core.IRemoteFileManager;

/**
 * TODO NEEDS TO BE DOCUMENTED
 */
public class UploadRuleAction implements IRuleAction {

	private final ILaunchProcessCallback process;
	private final UploadRule rule;
	private final ILaunchConfiguration configuration;
	private DownloadBackRule downloadBackRule;
	private final IProgressMonitor monitor;

	public UploadRuleAction(ILaunchProcessCallback process, ILaunchConfiguration configuration, UploadRule uploadRule,
			IProgressMonitor monitor) {
		super();
		this.process = process;
		this.rule = uploadRule;
		this.configuration = configuration;
		this.monitor = monitor;
	}

	public void run() throws CoreException {
		Assert.isNotNull(process);
		Assert.isNotNull(rule);
		Assert.isNotNull(configuration);

		SubMonitor progress = SubMonitor.convert(monitor, 40);

		try {
			/*
			 * Determine the first part of the remote path. Make it absolute.
			 */
			String execPath = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, (String) null);

			IPath defaultRemotePath = new Path(execPath).removeLastSegments(1);// configuration.getRemoteDirectoryPath();
			IPath remotePathParent = null;
			if (rule.isDefaultRemoteDirectory()) {
				remotePathParent = defaultRemotePath;
			} else {
				remotePathParent = new Path(rule.getRemoteDirectory());
				if (!remotePathParent.isAbsolute()) {
					remotePathParent = defaultRemotePath.append(remotePathParent);
				}
			}
			remotePathParent = remotePathParent.removeTrailingSeparator();
			Assert.isTrue(remotePathParent.isAbsolute(), "remotePathWoLastSegment.isAbsolute()"); //$NON-NLS-1$

			// Retrieve the local working dir (workspace path)
			IPath workingDir = ResourcesPlugin.getWorkspace().getRoot().getLocation();

			/*
			 * Determine list of local paths. Make them absolute.
			 */
			IPath localPaths[] = rule.getLocalFilesAsPathArray();

			for (int i = 0; i < localPaths.length; i++) {
				IPath localPath = localPaths[i];
				if (!localPath.isAbsolute()) {
					localPath = workingDir.append(localPath);
				}
				localPath = localPath.removeTrailingSeparator();
				Assert.isTrue(localPath.isAbsolute(), "localPath.isAbsolute()"); //$NON-NLS-1$
				localPaths[i] = localPath;
			}

			/*
			 * Process paths.
			 */
			for (int i = 0; i < localPaths.length; i++) {
				progress.setWorkRemaining(100);

				IPath localPath = localPaths[i];

				IRemoteFileManager localFileManager = process.getLocalFileManager(configuration);
				IFileStore localFileStore = localFileManager.getResource(localPath.toString());
				IFileInfo localFileInfo = localFileStore.fetchInfo(EFS.NONE, progress.newChild(5));

				if (!localFileInfo.exists()) {
					// Warn user and go to the next file
					// TODO Warn users that file doesn't exist
					continue;
				}

				// Generate the entire path from the combination of the
				// remotePathParent
				// and the name of the file or directory which will be copied.
				IPath remotePath = remotePathParent.append(localPath.lastSegment());

				// Generate the FileStore for the remote path
				IRemoteFileManager remoteFileManager = process.getRemoteFileManager(configuration, progress.newChild(5));
				IFileStore remoteFileStore = remoteFileManager.getResource(remotePath.toString());

				/*
				 * Assure the remote path exists.
				 */
				IFileStore parentFileStore = remoteFileManager.getResource(remotePathParent.toString());
				parentFileStore.mkdir(EFS.NONE, progress.newChild(5));

				doUpload(localFileStore, localPath, remoteFileStore, remotePath, progress.newChild(25));
			}

			/*
			 * If a download back rule was created during the upload, the add
			 * this rule the the list of synchronize rules.
			 */
			if (downloadBackRule != null) {
				process.addSynchronizationRule(downloadBackRule);
			}
		} finally {
			progress.done();
		}
	}

	private void doUpload(IFileStore localFileStore, IPath localPath, IFileStore remoteFileStore, IPath remotePath,
			IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 20);
		try {
			// Fetch remoteFileStore info
			IFileInfo remoteFileInfo = remoteFileStore.fetchInfo(EFS.NONE, progress.newChild(5));

			// Fetch localFileStore info
			IFileInfo localFileInfo = localFileStore.fetchInfo();

			// Find if file already exists on the remote machine
			if (remoteFileInfo.exists()) {
				switch (rule.getOverwritePolicy()) {
				case OverwritePolicies.ALWAYS:
					// Always copy anyway...
					break;
				case OverwritePolicies.NEWER:
					long dupFileModTime,
					localFileModTime;
					dupFileModTime = remoteFileInfo.getLastModified();
					localFileModTime = localFileInfo.getLastModified();

					if (dupFileModTime >= localFileModTime) {
						// Remote file is newer. Skip
						return;
					}
					break;
				default:
					// SKIP
					return;
				}
			}

			// Upload file or directory
			localFileStore.copy(remoteFileStore, EFS.OVERWRITE, progress.newChild(5));

			// Add remote path to list of files to download back,
			// if this feature was enabled.
			if (rule.isDownloadBack()) {
				if (downloadBackRule == null) {
					downloadBackRule = new DownloadBackRule();
				}
				downloadBackRule.add(localPath.toFile(), remotePath);
			}

			/* Set remote file permissions from the local */
			boolean changedAttr = false;
			if (rule.isAsReadOnly()) {
				remoteFileInfo.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
				changedAttr = true;
			}
			if (rule.isAsExecutable()) {
				remoteFileInfo.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, true);
				changedAttr = true;
			}
			if (changedAttr) {
				remoteFileStore.putInfo(remoteFileInfo, EFS.SET_ATTRIBUTES, progress.newChild(5));
			}

			// Set date/time, if required
			if (rule.isPreserveTimeStamp()) {
				remoteFileInfo.setLastModified(localFileInfo.getLastModified());
				remoteFileStore.putInfo(remoteFileInfo, EFS.SET_LAST_MODIFIED, progress.newChild(5));
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}
}
