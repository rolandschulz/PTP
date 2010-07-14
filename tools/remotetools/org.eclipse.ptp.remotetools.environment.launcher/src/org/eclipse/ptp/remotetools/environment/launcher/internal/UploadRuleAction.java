/******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *     Roland Schulz, University of Tennessee
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
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.core.IRemoteItem;
import org.eclipse.ptp.remotetools.core.IRemoteStatusTools;
import org.eclipse.ptp.remotetools.environment.launcher.core.LinuxPath;
import org.eclipse.ptp.remotetools.environment.launcher.data.ExecutionConfiguration;
import org.eclipse.ptp.remotetools.environment.launcher.data.OverwritePolicies;
import org.eclipse.ptp.remotetools.environment.launcher.data.UploadRule;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;
import org.eclipse.ptp.utils.file.core.FileRecursiveEnumeration;

public class UploadRuleAction implements IRuleAction {
	
	private ILaunchProcessCallback process;
	private UploadRule rule;
	private ExecutionConfiguration configuration;
	private PrintWriter outputWriter;
	private PrintWriter errorWriter;
	private IRemoteExecutionManager manager;
	private DownloadBackRule downloadBackRule;
	private long clockSkew;

	public UploadRuleAction(ILaunchProcessCallback process, UploadRule uploadRule) {
		super();
		this.process = process;
		this.rule = uploadRule;
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
		if (rule.getOverwritePolicy() == OverwritePolicies.NEWER) {
			IRemoteStatusTools statusTools = manager.getRemoteStatusTools();
			long localTime;
			long remoteTime;
			try {
				localTime = System.currentTimeMillis();
				remoteTime = statusTools.getTime();
				clockSkew = localTime -remoteTime;
			} catch (RemoteOperationException e) {
				errorWriter.println(NLS.bind(Messages.UploadRuleAction_0, e.getMessage()));			
				errorWriter.println(Messages.UploadRuleAction_1);
				clockSkew = 0;
			}
			if (clockSkew < -15000) {
				errorWriter.println(Messages.UploadRuleAction_2);
			} else if (clockSkew > 15000) {
				errorWriter.println(Messages.UploadRuleAction_3);				
			}
		}
		
		/*
		 * Determine remote path. Make it absolute.
		 */
		IPath defaultRemotePath = configuration.getRemoteDirectoryPath();
		IPath remotePath = null;
		if (rule.isDefaultRemoteDirectory()) {
			remotePath = defaultRemotePath;
		} else {
			remotePath = LinuxPath.fromString(rule.getRemoteDirectory());
			if (! remotePath.isAbsolute()) {
				remotePath = defaultRemotePath.append(remotePath);
			}
		}
		remotePath = remotePath.removeTrailingSeparator();
		Assert.isTrue(remotePath.isAbsolute(), "remotePath.isAbsolute()"); //$NON-NLS-1$
		
		/*
		 * Determine list of local paths. Make them absolute.
		 */
		IPath localPaths [] = rule.getLocalFilesAsPathArray();
		IPath workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		for (int i = 0; i < localPaths.length; i++) {
			IPath localPath = localPaths[i];
			if (!localPath.isAbsolute()) {
				localPath = workspace.append(localPath);
			}
			localPath = localPath.removeTrailingSeparator();
			Assert.isTrue(localPath.isAbsolute(), "localPath.isAbsolute()"); //$NON-NLS-1$
			localPaths[i] = localPath;
		}
		
		/*
		 * Process paths.
		 */
		for (int i = 0; i < localPaths.length; i++) {
			IPath localPath = localPaths[i];
			
			File localFile = localPath.toFile();
			if (! localFile.exists()) {
				errorWriter.println(NLS.bind(Messages.UploadRuleAction_4, localFile.toString()));
				continue;
			} else if (localFile.isDirectory()) {
				uploadDirectory(localFile, remotePath);
			} else if (localFile.isFile()) {
				uploadFile(localFile, remotePath);
			} else {
				errorWriter.println(NLS.bind(Messages.UploadRuleAction_5, localFile.toString()));
				continue;				
			}
		}
		
		/*
		 * If a download back rule was created during the upload,
		 * the add this rule the the list of synchronize rules.
		 */
		if (downloadBackRule != null) {
			process.addSynchronizationRule(downloadBackRule);
		}
	}

	private void uploadFile(File localFile, IPath remoteDirectoryPath) throws CoreException, CancelException, RemoteConnectionException {
		Assert.isTrue(localFile.isAbsolute(), "localFile.isAbsolute()"); //$NON-NLS-1$
		Assert.isTrue(remoteDirectoryPath.isAbsolute(), "remoteDirectoryPath.isAbsolute()"); //$NON-NLS-1$
		
		IPath remoteFile = remoteDirectoryPath.append(localFile.getName());
		outputWriter.println(NLS.bind(Messages.UploadRuleAction_6, localFile.toString()));
		outputWriter.println(NLS.bind(Messages.UploadRuleAction_7, LinuxPath.toString(remoteFile)));

		doFileUpload(localFile, remoteFile);
	}

	private void doFileUpload(File localFile, IPath remotePath) throws CoreException, CancelException, RemoteConnectionException {		
		if (! localFile.exists()) {
			errorWriter.println(NLS.bind(Messages.UploadRuleAction_8, localFile.toString()));
			return;
		}

		Assert.isTrue(localFile.isAbsolute(), "localFile.isAbsolute()"); //$NON-NLS-1$
		Assert.isTrue(localFile.isFile(), "localFile.isFile()"); //$NON-NLS-1$
		Assert.isTrue(remotePath.isAbsolute(), "remotePath.isAbsolute()"); //$NON-NLS-1$
		
		String remotePathAsString = LinuxPath.toString(remotePath);

		IRemoteCopyTools copyTools = manager.getRemoteCopyTools();
		IRemoteFileTools fileTools = manager.getRemoteFileTools();
				
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
			IRemoteItem remoteFile = null;
			try {
				remoteFile = fileTools.getFile(remotePathAsString, null);
			} catch (RemoteOperationException e) {
				errorWriter.println(NLS.bind(Messages.UploadRuleAction_9, e.getMessage()));
				return;
			}
			if (rule.getOverwritePolicy() == OverwritePolicies.SKIP) {
				if (remoteFile.exists()) {
					outputWriter.println(NLS.bind(Messages.UploadRuleAction_10, remotePath));
					return;
				}
			} else if (rule.getOverwritePolicy() == OverwritePolicies.NEWER) {
				long difference = localFile.lastModified() - remoteFile.getModificationTime();
				if (difference < 1000) {
					outputWriter.println(NLS.bind(Messages.UploadRuleAction_11, remotePath));
					return;					
				}
			}
		}  
		
		/*
		 * Upload the file.
		 */
		try {
			copyTools.uploadFileToFile(localFile, remotePathAsString);
		} catch (RemoteOperationException e) {
			errorWriter.println(NLS.bind(Messages.UploadRuleAction_12, e.getMessage()));
			return;
		}
	
		/*
		 * Add file to list of files to download back, if this feature was enabled.
		 */
		if (rule.isDownloadBack()) {
			if (downloadBackRule == null) {
				downloadBackRule = new DownloadBackRule();
			}
			downloadBackRule.add(localFile, remotePath);
		}

		/*
		 * Fetch properties of file that was just uploaded.
		 */
		IRemoteItem remoteFile = null;
		try {
			remoteFile = fileTools.getFile(remotePathAsString, null);
		} catch (RemoteOperationException e) {
			errorWriter.println(NLS.bind(Messages.UploadRuleAction_13, e.getMessage()));
			return;
		}
		
		/*
		 * Set file permissions. We need help of EFS, since the File class from
		 * Java does not providel all information.
		 */
		IFileSystem localFileSystem = EFS.getLocalFileSystem();
		IFileStore fileStore = localFileSystem.getStore(new Path(localFile.getPath()));
		IFileInfo fileInfo = fileStore.fetchInfo();

		boolean read = localFile.canRead();
		boolean write = localFile.canWrite();
		boolean execute = fileInfo.getAttribute(EFS.ATTRIBUTE_EXECUTABLE);
		
		if (rule.isAsReadOnly()) {
			write = false;
		}
		if (rule.isAsExecutable()) {
			execute = true;
		}
		
		remoteFile.setExecutable(execute);
		remoteFile.setWriteable(write);
		remoteFile.setReadable(read);
		
		/*
		 * Set date/time, if required
		 */
		if (rule.isPreserveTimeStamp()) {
			long timestamp = localFile.lastModified();
			remoteFile.setModificationTime(timestamp);
		}

		/*
		 * Commit changes
		 */
		try {
			remoteFile.commitAttributes(null);
		} catch (RemoteOperationException e) {
			errorWriter.println(NLS.bind(Messages.UploadRuleAction_14, e.getMessage()));
			return;
		}
	}
	
	private void doDirectoryUpload(File localDir, IPath remotePath) throws RemoteConnectionException, CancelException {
		if (! localDir.exists()) {
			errorWriter.println(NLS.bind(Messages.UploadRuleAction_15, localDir.toString()));
			return;
		}

		Assert.isTrue(localDir.isDirectory(), "localFile.isDirectory()"); //$NON-NLS-1$
		Assert.isTrue(localDir.isAbsolute(), "localFile.isAbsolute()"); //$NON-NLS-1$
		Assert.isTrue(remotePath.isAbsolute(), "remotePath.isAbsolute()"); //$NON-NLS-1$

		IRemoteFileTools fileTools = manager.getRemoteFileTools();
		
		/*
		 * Create remote directory if not already exists.
		 */
		try {
			fileTools.assureDirectory(LinuxPath.toString(remotePath), null);
		} catch (RemoteOperationException e) {
			errorWriter.println(NLS.bind(Messages.UploadRuleAction_16, e.getMessage()));
			return;
		}
	
		/*
		 * Fetch properties of directory that was just uploaded.
		 */
		IRemoteItem remoteDirectory = null;
		try {
			remoteDirectory = fileTools.getDirectory(LinuxPath.toString(remotePath), null);
		} catch (RemoteOperationException e) {
			errorWriter.println(NLS.bind(Messages.UploadRuleAction_17, e.getMessage()));
			return;
		}
		
		/*
		 * Set file permissions.
		 */
		boolean read = localDir.canRead();
		boolean write = localDir.canWrite();
		boolean execute = true;
		
		if (rule.isAsReadOnly()) {
			write = false;
		}
		
		remoteDirectory.setExecutable(execute);
		remoteDirectory.setWriteable(write);
		remoteDirectory.setReadable(read);
		
		/*
		 * Set date/time, if required
		 */
		if (rule.isPreserveTimeStamp()) {
			long timestamp = localDir.lastModified();
			remoteDirectory.setModificationTime(timestamp);
		}

		/*
		 * Commit changes
		 */
		try {
			remoteDirectory.commitAttributes(null);
		} catch (RemoteOperationException e) {
			errorWriter.println(NLS.bind(Messages.UploadRuleAction_18, e.getMessage()));
			return;
		}		
	}


	private void uploadDirectory(File localDir, IPath remotePath) throws CoreException, CancelException, RemoteConnectionException {
		Assert.isTrue(localDir.isAbsolute(), "localDir.isAbsolute()"); //$NON-NLS-1$
		Assert.isTrue(remotePath.isAbsolute(), "remotePath.isAbsolute()"); //$NON-NLS-1$

		outputWriter.println(NLS.bind(Messages.UploadRuleAction_19, localDir.toString()));
		outputWriter.println(NLS.bind(Messages.UploadRuleAction_20, LinuxPath.toString(remotePath)));

		FileRecursiveEnumeration enumeration = null;
		enumeration = new FileRecursiveEnumeration(localDir);
		
		IPath rootPath = new Path(localDir.getAbsolutePath());
		int rootPathLength = rootPath.segmentCount();
		while (enumeration.hasMoreElements()) {
			while (enumeration.hasMoreExceptions()) {
				errorWriter.println(NLS.bind(Messages.UploadRuleAction_21, enumeration.nextException()));	
			}
			File file = (File) enumeration.nextElement();
			IPath filePath = new Path(file.getAbsolutePath());
			IPath relativePath = filePath.removeFirstSegments(rootPathLength);
			IPath remoteFilePath = remotePath.append(relativePath);
			if (file.isDirectory()) {
				outputWriter.println(NLS.bind(Messages.UploadRuleAction_22, LinuxPath.toString(remoteFilePath)));
				doDirectoryUpload(file, remoteFilePath);
			} else {
				outputWriter.println(NLS.bind(Messages.UploadRuleAction_23, LinuxPath.toString(relativePath)));
				doFileUpload(file, remoteFilePath);
			}
		}
		while (enumeration.hasMoreExceptions()) {
			errorWriter.println(NLS.bind(Messages.UploadRuleAction_24, enumeration.nextException()));	
		}
	}
}
