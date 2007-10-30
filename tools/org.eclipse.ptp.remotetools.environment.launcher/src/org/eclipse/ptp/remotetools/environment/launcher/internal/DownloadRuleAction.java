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

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.remotetools.core.IRemoteCopyTools;
import org.eclipse.ptp.remotetools.core.IRemoteDirectory;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteFile;
import org.eclipse.ptp.remotetools.core.IRemoteFileEnumeration;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.core.IRemoteItem;
import org.eclipse.ptp.remotetools.core.IRemoteStatusTools;
import org.eclipse.ptp.remotetools.environment.launcher.core.LinuxPath;
import org.eclipse.ptp.remotetools.environment.launcher.data.DownloadRule;
import org.eclipse.ptp.remotetools.environment.launcher.data.ExecutionConfiguration;
import org.eclipse.ptp.remotetools.environment.launcher.data.OverwritePolicies;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;


public class DownloadRuleAction implements IRuleAction {

	private ILaunchProcessCallback process;
	private DownloadRule rule;
	private ExecutionConfiguration configuration;
	private PrintWriter outputWriter;
	private PrintWriter errorWriter;
	private IRemoteExecutionManager manager;

	public DownloadRuleAction(ILaunchProcessCallback process, DownloadRule rule) {
		super();
		this.process = process;
		this.rule = rule;
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
		 * If overwrite policy depends on time, then get remote clock skew.
		 */
		long clockSkew = 0;
		if (rule.getOverwritePolicy() == OverwritePolicies.NEWER) {
			IRemoteStatusTools statusTools = manager.getRemoteStatusTools();
			long localTime;
			long remoteTime;
			try {
				localTime = System.currentTimeMillis();
				remoteTime = statusTools.getTime();
				clockSkew = localTime -remoteTime;
			} catch (RemoteOperationException e) {
				errorWriter.println(NLS.bind(Messages.DownloadRuleAction_FailedCalculateClockDifference, e.getMessage()));			
				errorWriter.println(Messages.DownloadRuleAction_FailedCalculateClockDifference2);
				clockSkew = 0;
			}
			if (clockSkew < -15000) {
				errorWriter.println(Messages.DownloadRuleAction_WarningClockForward);
			} else if (clockSkew > 15000) {
				errorWriter.println(Messages.DownloadRuleAction_WarningClockBackward);				
			}
		}
		
		/*
		 * Determine local path. Make it absolute. Create if necessary
		 */
		IPath localPath = new Path(rule.getLocalDirectory());
		if (! localPath.isAbsolute()) {
			IPath workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation();
			localPath = workspace.append(localPath);
		}
		Assert.isTrue(localPath.isAbsolute(), "localPath.isAbsolute()"); //$NON-NLS-1$
		File localDir = localPath.toFile();
		if (! localDir.exists()) {
			if (! localDir.mkdirs()) {
				errorWriter.println(NLS.bind(Messages.DownloadRuleAction_FailedCreateLocalDirectory, localPath.toString()));
				return;								
			}
		} else if (! localDir.isDirectory()) {
			errorWriter.println(NLS.bind(Messages.DownloadRuleAction_FailedLocalDiretoryIsNotDirectory, localPath.toString()));
			return;											
		}
		
		/*
		 * Determine list of remote paths. Make them absolute.
		 */
		IPath remotePaths [] = rule.getRemoteFilesAsPathArray();
		IPath remoteWorkingPath = configuration.getRemoteDirectoryPath();
		for (int i = 0; i < remotePaths.length; i++) {
			IPath remotePath = remotePaths[i];
			if (! remotePath.isAbsolute()) {
				remotePath = remoteWorkingPath.append(remotePath);
			}
			remotePath = remotePath.removeTrailingSeparator();
			Assert.isTrue(remotePath.isAbsolute(), "remotePath.isAbsolute()"); //$NON-NLS-1$
			remotePaths[i] = remotePath;
		}

		/*
		 * Process paths.
		 */
		IRemoteFileTools fileTools = manager.getRemoteFileTools();
		for (int i = 0; i < remotePaths.length; i++) {
			IPath remotePath = remotePaths[i];
			String remotePathAsString = LinuxPath.toString(remotePath);
			IRemoteItem item = null;
			try {
				item = fileTools.getItem(remotePathAsString);
			} catch (RemoteOperationException e) {
				errorWriter.println(NLS.bind(Messages.DownloadRuleAction_FailedFetchAttributes, remotePathAsString));
				continue;
			}
			
			if (! item.exists()) {
				errorWriter.println(NLS.bind(Messages.DownloadRuleAction_FailedRemotePathDoesNotExit, remotePathAsString));
				continue;
			} else if (item instanceof IRemoteDirectory) {
				IRemoteDirectory remoteDirectory = (IRemoteDirectory) item;
				downloadDirectory(remoteDirectory, localDir);
			} else if (item instanceof IRemoteFile) {
				IRemoteFile remoteFile = (IRemoteFile) item;
				downloadFile(remoteFile, localDir);
			} else {
				errorWriter.println(NLS.bind(Messages.DownloadRuleAction_FailedRemotePathNoDirectoryNorFile, remotePathAsString));
				continue;				
			}
		}

	}

	private void downloadFile(IRemoteFile remoteFile, File localDir) throws RemoteConnectionException, CancelException {
		Assert.isTrue(localDir.isAbsolute(), "localDir.isAbsolute()"); //$NON-NLS-1$
		
		IPath remoteFilePath = LinuxPath.fromString(remoteFile.getPath());
		File localFile = new File(localDir, remoteFilePath.lastSegment());
		outputWriter.println(NLS.bind(Messages.DownloadRuleAction_NotifyFileDownload1, remoteFile.getPath()));
		outputWriter.println(NLS.bind(Messages.DownloadRuleAction_NotifyFileDownload2, localFile.getAbsolutePath()));

		doFileDownload(remoteFile, localFile);	
	}

	private void downloadDirectory(IRemoteDirectory remoteDirectory, File localDir) throws RemoteConnectionException, CancelException {
		Assert.isTrue(localDir.isAbsolute(), "localDir.isAbsolute()"); //$NON-NLS-1$

		outputWriter.println(NLS.bind(Messages.DownloadRuleAction_NotifyDirectoryDownload1, remoteDirectory.getPath()));
		outputWriter.println(NLS.bind(Messages.DownloadRuleAction_NotifyDirectoryDownload2, localDir.getAbsolutePath()));

		IRemoteFileTools fileTools = manager.getRemoteFileTools();
		
		IRemoteFileEnumeration enumeration = null;
		try {
			enumeration = fileTools.createRecursiveFileEnumeration(remoteDirectory.getPath());
		} catch (RemoteOperationException e) {
			errorWriter.println(NLS.bind(Messages.DownloadRuleAction_FailedFetchRemoteAttributes, e.getMessage()));	
			return;
		}
		
		IPath remoteRootPath = LinuxPath.fromString(remoteDirectory.getPath());
		IPath localRootPath = new Path(localDir.getAbsolutePath());
		int remoteRootPathLength = remoteRootPath.segmentCount();
		
		while (enumeration.hasMoreElements()) {
			while (enumeration.hasMoreExceptions()) {
				errorWriter.println(NLS.bind(Messages.DownloadRuleAction_FailedFetchRemoteAttributes, enumeration.nextException()));
			}
			/*
			 * Calculate path relative to the remote root.
			 * Generate path relative to the local root.
			 */ 
			IRemoteItem remoteItem = enumeration.nextElementAsItem();
			IPath remotePath = LinuxPath.fromString(remoteItem.getPath());
			IPath relativePath = remotePath.removeFirstSegments(remoteRootPathLength);
			IPath localFilePath = localRootPath.append(relativePath);
			File localFile = localFilePath.toFile();
			if (remoteItem instanceof IRemoteDirectory) {
				IRemoteDirectory directoryItem = (IRemoteDirectory) remoteItem;
				doDirectoryDownload(directoryItem, localFile);
			} else if (remoteItem instanceof IRemoteFile) {
				IRemoteFile fileItem = (IRemoteFile) remoteItem;
				doFileDownload(fileItem, localFile);
			}
		}
		while (enumeration.hasMoreExceptions()) {
			errorWriter.println(NLS.bind(Messages.DownloadRuleAction_FailedFetchRemoteAttributes, enumeration.nextException()));	
		}
	}
	
	private void doFileDownload(IRemoteFile remoteFile, File localFile) throws RemoteConnectionException, CancelException {
		Assert.isTrue(localFile.isAbsolute(), "localFile.isAbsolute()"); //$NON-NLS-1$
		
		String remoteFileAsString = remoteFile.getPath();

		IRemoteCopyTools copyTools = manager.getRemoteCopyTools();
				
		/*
		 * Check if file exists and if file is newer, depending on overwrite policy.
		 */
		if (rule.getOverwritePolicy() == OverwritePolicies.ALWAYS) {
			/*
			 * File is always copied, no policy.
			 */
		} else {
			/*
			 * Policy needs to check remote file.
			 */
			if (rule.getOverwritePolicy() == OverwritePolicies.SKIP) {
				if (localFile.exists()) {
					outputWriter.println(NLS.bind(Messages.DownloadRuleAction_NotiftFileExistsLocally, localFile.getAbsolutePath()));
					return;
				}
			} else if (rule.getOverwritePolicy() == OverwritePolicies.NEWER) {
				long difference = remoteFile.getModificationTime() - localFile.lastModified();
				if (difference < 1000) {
					outputWriter.println(NLS.bind(Messages.DownloadRuleAction_NotiftNewerFileExistsLocally, localFile.getAbsolutePath()));
					return;					
				}
			}
		}  
		
		/*
		 * Upload the file.
		 */
		try {
			copyTools.downloadFileToFile(remoteFileAsString, localFile);
		} catch (RemoteOperationException e) {
			errorWriter.println(NLS.bind(Messages.DownloadRuleAction_FailedSetDownloadFile, e.getMessage()));
			return;
		}
	
		/*
		 * Set file permissions. We need help of EFS, since the File class from
		 * Java does not providel all information.
		 */
//		boolean read = remoteFile.isReadable();
		boolean write = remoteFile.isWritable();
		boolean execute = remoteFile.isExecutable();
		
		if (rule.isAsReadOnly()) {
			write = false;
		}
		if (rule.isAsExecutable()) {
			execute = true;
		}
		
		IFileSystem localFileSystem = EFS.getLocalFileSystem();
		IFileStore fileStore = localFileSystem.getStore(new Path(localFile.getPath()));
		IFileInfo fileInfo = fileStore.fetchInfo();

		fileInfo.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, execute);
		fileInfo.setAttribute(EFS.ATTRIBUTE_READ_ONLY, ! write);
		
		/*
		 * Set date/time, if required
		 */
		if (rule.isPreserveTimeStamp()) {
			long timestamp = remoteFile.getModificationTime();
			fileInfo.setLastModified(timestamp);
		}
		
		/*
		 * Commit changes
		 */
		try {
			fileStore.putInfo(fileInfo, EFS.SET_LAST_MODIFIED | EFS.SET_ATTRIBUTES, null);
		} catch (CoreException e) {
			errorWriter.println(NLS.bind(Messages.DownloadRuleAction_FailedSetLocalFileAttributes, localFile.getAbsolutePath()));
			return;
		}
	}

	private void doDirectoryDownload(IRemoteDirectory remoteDir, File localDir) throws RemoteConnectionException, CancelException {
		Assert.isTrue(localDir.isAbsolute(), "localFile.isAbsolute()"); //$NON-NLS-1$
		
		/*
		 * Create local directory, if not already exists.
		 */
		if (! localDir.exists()) {
			if (! localDir.mkdirs()) {
				errorWriter.println(NLS.bind(Messages.DownloadRuleAction_FailedCreateLocalDirectory, localDir.getAbsolutePath()));
				return;
			}
		}
	
		/*
		 * Set file permissions. We need help of EFS, since the File class from
		 * Java does not providel all information.
		 */
//		boolean read = remoteDir.isReadable();
		boolean write = remoteDir.isWritable();
		boolean execute = true;
		
		if (rule.isAsReadOnly()) {
			write = false;
		}
		
		IFileSystem localFileSystem = EFS.getLocalFileSystem();
		IFileStore fileStore = localFileSystem.getStore(new Path(localDir.getPath()));
		IFileInfo fileInfo = fileStore.fetchInfo();

		fileInfo.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, execute);
		fileInfo.setAttribute(EFS.ATTRIBUTE_READ_ONLY, ! write);
		
		/*
		 * Set date/time, if required
		 */
		if (rule.isPreserveTimeStamp()) {
			long timestamp = remoteDir.getModificationTime();
			fileInfo.setLastModified(timestamp);
		}
		
		/*
		 * Commit changes
		 */
		try {
			fileStore.putInfo(fileInfo, EFS.SET_LAST_MODIFIED | EFS.SET_ATTRIBUTES, null);
		} catch (CoreException e) {
			errorWriter.println(NLS.bind(Messages.DownloadRuleAction_FailedSetLocalDirectoryAttributes, localDir.getAbsolutePath()));
			return;
		}
	}

}
