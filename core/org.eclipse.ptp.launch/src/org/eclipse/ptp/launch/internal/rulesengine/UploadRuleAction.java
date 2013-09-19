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
import org.eclipse.ptp.launch.RMLaunchUtils;
import org.eclipse.ptp.launch.rulesengine.IRuleAction;
import org.eclipse.ptp.launch.rulesengine.OverwritePolicies;
import org.eclipse.remote.core.IRemoteFileManager;

/**
 * TODO NEEDS TO BE DOCUMENTED
 */
public class UploadRuleAction implements IRuleAction {

	private final UploadRule fRule;
	private final ILaunchConfiguration fConfiguration;
	private DownloadBackRule fDownloadBackRule;
	private final IProgressMonitor fMonitor;

	public UploadRuleAction(ILaunchConfiguration configuration, UploadRule uploadRule, IProgressMonitor monitor) {
		super();
		fRule = uploadRule;
		fConfiguration = configuration;
		fMonitor = monitor;
	}

	public void run() throws CoreException {
		Assert.isNotNull(fRule);
		Assert.isNotNull(fConfiguration);

		SubMonitor progress = SubMonitor.convert(fMonitor, 40);

		/*
		 * Determine the first part of the remote path. Make it absolute.
		 */
		String execPath = fConfiguration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, (String) null);

		IPath defaultRemotePath = new Path(execPath).removeLastSegments(1);// fConfiguration.getRemoteDirectoryPath();
		IPath remotePathParent = null;
		if (fRule.isDefaultRemoteDirectory()) {
			remotePathParent = defaultRemotePath;
		} else {
			remotePathParent = new Path(fRule.getRemoteDirectory());
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
		IPath localPaths[] = fRule.getLocalFilesAsPathArray();

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
		for (IPath localPath : localPaths) {
			progress.setWorkRemaining(100);

			IRemoteFileManager localFileManager = RMLaunchUtils.getLocalFileManager(fConfiguration);
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
			IRemoteFileManager remoteFileManager = RMLaunchUtils.getRemoteFileManager(fConfiguration, progress.newChild(5));
			IFileStore remoteFileStore = remoteFileManager.getResource(remotePath.toString());

			/*
			 * Assure the remote path exists.
			 */
			IFileStore parentFileStore = remoteFileManager.getResource(remotePathParent.toString());
			parentFileStore.mkdir(EFS.NONE, progress.newChild(5));

			doUpload(localFileStore, localPath, remoteFileStore, remotePath, progress.newChild(25));
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
				switch (fRule.getOverwritePolicy()) {
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
			if (fRule.isDownloadBack()) {
				if (fDownloadBackRule == null) {
					fDownloadBackRule = new DownloadBackRule();
				}
				fDownloadBackRule.add(localPath.toFile(), remotePath);
			}

			/* Set remote file permissions from the local */
			boolean changedAttr = false;
			if (fRule.isAsReadOnly()) {
				remoteFileInfo.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
				changedAttr = true;
			}
			if (fRule.isAsExecutable()) {
				remoteFileInfo.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, true);
				changedAttr = true;
			}
			if (changedAttr) {
				remoteFileStore.putInfo(remoteFileInfo, EFS.SET_ATTRIBUTES, progress.newChild(5));
			}

			// Set date/time, if required
			if (fRule.isPreserveTimeStamp()) {
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
