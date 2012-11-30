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
import org.eclipse.ptp.remote.core.IRemoteFileManager;

/**
 * TODO NEEDS TO BE DOCUMENTED
 * 
 * @since 4.1
 */
public class DownloadRuleAction implements IRuleAction {

	private final DownloadRule fRule;
	private final ILaunchConfiguration fConfiguration;
	private final IProgressMonitor fMonitor;

	public DownloadRuleAction(ILaunchConfiguration configuration, DownloadRule rule, IProgressMonitor monitor) {
		super();
		fRule = rule;
		fConfiguration = configuration;
		fMonitor = monitor;
	}

	public void run() throws CoreException {
		Assert.isNotNull(fRule);
		Assert.isNotNull(fConfiguration);

		SubMonitor progress = SubMonitor.convert(fMonitor, 40);

		/*
		 * Determine local path. Make it absolute.
		 */
		IPath localParentPath = new Path(fRule.getLocalDirectory());
		if (!localParentPath.isAbsolute()) {
			IPath defaultPath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
			localParentPath = defaultPath.append(localParentPath);
			/*
			 * IPath workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation(); localPath = workspace.append(localPath);
			 */
		}
		localParentPath.removeTrailingSeparator();
		Assert.isTrue(localParentPath.isAbsolute(), "localPath.isAbsolute()"); //$NON-NLS-1$

		// Get the file store of the parent dir.
		IRemoteFileManager localFileManager = RMLaunchUtils.getLocalFileManager(fConfiguration);
		IFileStore localFileParentResource = localFileManager.getResource(localParentPath.toString());
		IFileInfo localFileParentInfo = localFileParentResource.fetchInfo(EFS.NONE, progress.newChild(5));

		// Create the localpath if necessary
		if (!localFileParentInfo.exists()) {
			localFileParentResource.mkdir(EFS.NONE, progress.newChild(5));
		}

		// Download all remote paths
		IPath remotePaths[] = fRule.getRemoteFilesAsPathArray();
		IPath remoteWorkingPath = new Path(fConfiguration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, "")).removeLastSegments(1); //$NON-NLS-1$
		for (int i = 0; i < remotePaths.length; i++) {
			progress.setWorkRemaining(100);

			IPath remotePath = remotePaths[i];

			// Make paths absolute
			if (!remotePath.isAbsolute()) {
				remotePath = remoteWorkingPath.append(remotePath);
			}

			IRemoteFileManager remoteFileManager = RMLaunchUtils.getRemoteFileManager(fConfiguration, progress.newChild(5));
			IFileStore remoteFileStore = remoteFileManager.getResource(remotePath.toString());

			// Check if the remote resource exists
			IFileInfo remoteFileInfo = remoteFileStore.fetchInfo(EFS.NONE, progress.newChild(5));

			if (!remoteFileInfo.exists()) {
				// Warn the user and continue processing the next file
				// FIXME Warn the user that the resource doesn't exist
				continue;
			}

			// Generate the entire path from the combination of the
			// localPathParent
			// and the name of the file or directory which will be copied.
			IPath localPath = localParentPath.append(remotePath.lastSegment());

			// Generate the file store for the local path
			IFileStore localFileStore = localFileManager.getResource(localPath.toString());

			doDownload(remoteFileStore, remotePath, localFileStore, localPath, progress.newChild(20));
		}
	}

	private void doDownload(IFileStore remoteFileStore, IPath remotePath, IFileStore localFileStore, IPath localPath,
			IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 20);

		try {
			// Fetch file infos
			IFileInfo localFileInfo = localFileStore.fetchInfo();
			IFileInfo remoteFileInfo = remoteFileStore.fetchInfo(EFS.NONE, progress.newChild(5));

			// Find if file already exists on the local machine
			if (localFileInfo.exists()) {
				// Follows the selected policy
				switch (fRule.getOverwritePolicy()) {
				case OverwritePolicies.ALWAYS:
					// Always copy anyway...
					break;
				case OverwritePolicies.NEWER:
					long dupFileModTime,
					localFileModTime;
					dupFileModTime = localFileInfo.getLastModified();
					localFileModTime = remoteFileInfo.getLastModified();

					if (dupFileModTime >= localFileModTime) {
						// Remote file is newer. Skip
						return;
					}
					break;
				default:
					return;
				}
			}

			// Download resource
			remoteFileStore.copy(localFileStore, EFS.OVERWRITE, progress.newChild(5));

			/* Set local file permissions from the remote resource */
			boolean changedAttr = false;
			if (fRule.isAsReadOnly()) {
				localFileInfo.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
				changedAttr = true;
			}
			if (fRule.isAsExecutable()) {
				localFileInfo.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, true);
				changedAttr = true;
			}
			if (changedAttr) {
				localFileStore.putInfo(localFileInfo, EFS.SET_ATTRIBUTES, progress.newChild(5));
			}

			// Set date/time, if required
			if (fRule.isPreserveTimeStamp()) {
				localFileInfo.setLastModified(remoteFileInfo.getLastModified());
				localFileStore.putInfo(localFileInfo, EFS.SET_LAST_MODIFIED, progress.newChild(5));
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}
}
