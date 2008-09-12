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

import java.io.IOException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.data.DownloadBackRule;
import org.eclipse.ptp.launch.data.OverwritePolicies;
import org.eclipse.ptp.launch.data.UploadRule;
import org.eclipse.ptp.launch.internal.LinuxPath;
import org.eclipse.ptp.remote.core.IRemoteFileManager;


public class UploadRuleAction implements IRuleAction {
	
	private final ILaunchProcessCallback process;
	private final UploadRule rule;
	//private ExecutionConfiguration configuration;
	//private PrintWriter outputWriter;
	//private PrintWriter errorWriter;
	//private IRemoteExecutionManager manager;
	private final ILaunchConfiguration configuration;
	//private IRemoteFileManager remoteFileManager;
	//private IRemoteFileManager localFileManager;
	private DownloadBackRule downloadBackRule;
	private final IProgressMonitor monitor;
	
	public UploadRuleAction(ILaunchProcessCallback process, ILaunchConfiguration configuration, 
			UploadRule uploadRule, IProgressMonitor monitor) {
		super();
		this.process = process;
		this.rule = uploadRule;
		/*configuration = process.getConfiguration();
		outputWriter = process.getOutputWriter();
		errorWriter = process.getErrorWriter();*/	
		//manager = process.getExecutionManager();
		this.configuration = configuration;
		
		//remoteFileManager = process.getRemoteFileManager(configuration);
		//localFileManager = process.getLocalFileManager(configuration);
		this.monitor = monitor;
	}

	public void run() throws CoreException {
		Assert.isNotNull(process);
		Assert.isNotNull(rule);
		Assert.isNotNull(configuration);

		// The actual interface doesn't provide a way to check the remote time,
		// se we assume the clocks are synchronized.
		/*
		 * If overwrite policy depends on time, then get remote clock skew.
		 */
		/*if (rule.getOverwritePolicy() == OverwritePolicies.NEWER) {
			IRemoteStatusTools statusTools = manager.getRemoteStatusTools();
			long localTime;
			long remoteTime;
			try {
				localTime = System.currentTimeMillis();
				remoteTime = statusTools.getTime();
				clockSkew = localTime -remoteTime;
			} catch (RemoteOperationException e) {
				errorWriter.println(NLS.bind("   Could not calculate clock difference with remote host: {0}", e.getMessage()));			
				errorWriter.println("   Assuming same a remote clock synchronized with local clock.");
				clockSkew = 0;
			}
			if (clockSkew < -15000) {
				errorWriter.println("   Warning! Clock at remote target is more recent than local clock. File synchronization may not be correct.");
			} else if (clockSkew > 15000) {
				errorWriter.println("   Warning! Clock at remote target is older than local clock. File synchronization may not be correct.");				
			}
		}*/
		
		/*
		 * Determine the first part of the remote path. Make it absolute.
		 */
		String execPath = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, (String)null);
		
		
		IPath defaultRemotePath = LinuxPath.fromString(execPath).removeLastSegments(1);//configuration.getRemoteDirectoryPath();
		IPath remotePathParent = null;
		if (rule.isDefaultRemoteDirectory()) {
			remotePathParent = defaultRemotePath;
		} else {
			remotePathParent = LinuxPath.fromString(rule.getRemoteDirectory());
			if (! remotePathParent.isAbsolute()) {
				remotePathParent = defaultRemotePath.append(remotePathParent);
			}
		}
		remotePathParent = remotePathParent.removeTrailingSeparator();
		Assert.isTrue(remotePathParent.isAbsolute(), "remotePathWoLastSegment.isAbsolute()");
		
		/*// Generate the FileStore for the remote path
		IFileStore remoteFileStore = null;
		try {
			remoteFileStore = remoteFileManager.getResource(remotePath, monitor);
		} catch (IOException e1) {
			throw new CoreException(new Status(Status.ERROR, PTPLaunchPlugin.PLUGIN_ID, "Error retrieving remote resource", e1));
		}*/
		
		// Retrieve the local working dir (workspace path)
		IPath workingDir = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		//IPath workingDir = new Path(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_WORK_DIRECTORY, ""));		
		
		/*
		 * Determine list of local paths. Make them absolute.
		 */
		IPath localPaths [] = rule.getLocalFilesAsPathArray();
		//IPath workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		
		for (int i = 0; i < localPaths.length; i++) {
			IPath localPath = localPaths[i];
			if (!localPath.isAbsolute()) {
				localPath = workingDir.append(localPath);
			}
			localPath = localPath.removeTrailingSeparator();
			Assert.isTrue(localPath.isAbsolute(), "localPath.isAbsolute()");
			localPaths[i] = localPath;
		}
		
		/*
		 * Process paths.
		 */
		for (int i = 0; i < localPaths.length; i++) {
			IPath localPath = localPaths[i];
			
			IFileStore localFileStore = null;
			
			try {
				IRemoteFileManager localFileManager = process.getLocalFileManager(configuration);
				localFileStore = localFileManager.getResource(localPath, monitor);
			} catch (IOException e) {
				throw new CoreException(new Status(Status.ERROR, PTPLaunchPlugin.PLUGIN_ID, "Error retrieving local resource", e));
			}
			IFileInfo localFileInfo = localFileStore.fetchInfo();
			
			if(!localFileInfo.exists()) {
				// Warn user and go to the next file
				// TODO Warn users that file doesn't exist
				continue;
			}
			
			// Generate the entire path from the combination of the remotePathParent
			// and the name of the file or directory which will be copied.
			IPath remotePath = remotePathParent.append(localPath.lastSegment());
			
			// Generate the FileStore for the remote path
			IFileStore remoteFileStore = null;
			try {
				IRemoteFileManager remoteFileManager = process.getRemoteFileManager(configuration);
				remoteFileStore = remoteFileManager.getResource(remotePath, monitor);
			} catch (IOException e1) {
				throw new CoreException(new Status(Status.ERROR, PTPLaunchPlugin.PLUGIN_ID, "Error retrieving remote resource", e1));
			}
			
			doUpload(localFileStore, localPath, remoteFileStore, remotePath);
			
			//if(localFileStore.fetchInfo().isDirectory())
			
			//File localFile = localPath.toFile();
			/*if (! localFile.exists()) {
				// TODO Warn that file doesn't exists
				//errorWriter.println(NLS.bind("   Ignoring {0}: Path does not exist locally", localFile.toString()));
				continue;
			} else if (localFile.isDirectory()) {
				uploadDirectory(localFile, remotePath);
			} else if (localFile.isFile()) {
				uploadFile(localFile, remotePath);
			} else {
				//errorWriter.println(NLS.bind("   Ignoring {0}: Path is not a file nor directory", localFile.toString()));
				continue;				
			}*/
		}
		
		/*
		 * If a download back rule was created during the upload,
		 * the add this rule the the list of synchronize rules.
		 */
		if (downloadBackRule != null) {
			process.addSynchronizationRule(downloadBackRule);
		}
	}

	private void doUpload(IFileStore localFileStore, IPath localPath, 
			IFileStore remoteFileStore, IPath remotePath) throws CoreException {
		// Fetch remoteFileStore info
		IFileInfo remoteFileInfo = remoteFileStore.fetchInfo(EFS.NONE, monitor);
		
		// Fetch localFileStore info
		IFileInfo localFileInfo = localFileStore.fetchInfo();
		
		// Find if file already exists on the remote machine
		if(remoteFileInfo.exists()) {
			switch(rule.getOverwritePolicy()) {
			case OverwritePolicies.ALWAYS:
				// Always copy anyway...
				break;
			case OverwritePolicies.NEWER:
				long dupFileModTime, localFileModTime;
				dupFileModTime = remoteFileInfo.getLastModified();
				localFileModTime = localFileInfo.getLastModified();
				
				if(dupFileModTime >= localFileModTime) {
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
		localFileStore.copy(remoteFileStore, EFS.OVERWRITE, monitor);
		
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
		if(changedAttr) {
			remoteFileStore.putInfo(remoteFileInfo, EFS.SET_ATTRIBUTES, monitor);
		}
		
		 // Set date/time, if required
		if (rule.isPreserveTimeStamp()) {
			remoteFileInfo.setLastModified(localFileInfo.getLastModified());
			remoteFileStore.putInfo(remoteFileInfo, EFS.SET_LAST_MODIFIED, monitor);
		}
		
	}
	/*private void uploadFile(File localFile, IPath remoteDirectoryPath) throws CoreException, CancelException, RemoteConnectionException {
		Assert.isTrue(localFile.isAbsolute(), "localFile.isAbsolute()");
		Assert.isTrue(remoteDirectoryPath.isAbsolute(), "remoteDirectoryPath.isAbsolute()");
		
		IPath remoteFile = remoteDirectoryPath.append(localFile.getName());
		outputWriter.println(NLS.bind("   Copying file {0} (local)", localFile.toString()));
		outputWriter.println(NLS.bind("   to file {0} (remote)", LinuxPath.toString(remoteFile)));

		doFileUpload(localFile, remoteFile);
	}

	private void doFileUpload(File localFile, IPath remotePath) throws CoreException {		
		if (! localFile.exists()) {
			errorWriter.println(NLS.bind("   Ignoring {0}: File does not exist locally", localFile.toString()));
			return;
		}

		Assert.isTrue(localFile.isAbsolute(), "localFile.isAbsolute()");
		Assert.isTrue(localFile.isFile(), "localFile.isFile()");
		Assert.isTrue(remotePath.isAbsolute(), "remotePath.isAbsolute()");
		
		String remotePathAsString = LinuxPath.toString(remotePath);

		IRemoteCopyTools copyTools = manager.getRemoteCopyTools();
		IRemoteFileTools fileTools = manager.getRemoteFileTools();
				
		
		 * Check if file exists and if file is newer, depending on overwrite policy.
		 
		if (rule.getOverwritePolicy() == OverwritePolicies.ALWAYS) {
			
			 * File is always copied, no policy.
			 
		} else {
			
			 * Policy needs to check remote file.
			 
			IRemoteFile remoteFile = null;
			try {
				remoteFile = fileTools.getFile(remotePathAsString);
			} catch (RemoteOperationException e) {
				errorWriter.println(NLS.bind("   Could not fetch properties for remote file, ignoring it: {0}", e.getMessage()));
				return;
			}
			if (rule.getOverwritePolicy() == OverwritePolicies.SKIP) {
				if (remoteFile.exists()) {
					outputWriter.println(NLS.bind("   File alredy exists on remote target, ignoring it: {0}", remotePath));
					return;
				}
			} else if (rule.getOverwritePolicy() == OverwritePolicies.NEWER) {
				long difference = localFile.lastModified() - remoteFile.getModificationTime();
				if (difference < 1000) {
					outputWriter.println(NLS.bind("   A newer file alredy exists on remote target, ignoring it: {0}", remotePath));
					return;					
				}
			}
		}  
		
		
		 * Upload the file.
		 
		try {
			copyTools.uploadFileToFile(localFile, remotePathAsString);
		} catch (RemoteOperationException e) {
			errorWriter.println(NLS.bind("   Could not upload file: {0}", e.getMessage()));
			return;
		}
	
		
		 * Add file to list of files to download back, if this feature was enabled.
		 
		if (rule.isDownloadBack()) {
			if (downloadBackRule == null) {
				downloadBackRule = new DownloadBackRule();
			}
			downloadBackRule.add(localFile, remotePath);
		}

		
		 * Fetch properties of file that was just uploaded.
		 
		IRemoteFile remoteFile = null;
		try {
			remoteFile = fileTools.getFile(remotePathAsString);
		} catch (RemoteOperationException e) {
			errorWriter.println(NLS.bind("   Could not fetch properties for uploaded file: {0}", e.getMessage()));
			return;
		}
		
		
		 * Set file permissions. We need help of EFS, since the File class from
		 * Java does not providel all information.
		 
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
		
		
		 * Set date/time, if required
		 
		if (rule.isPreserveTimeStamp()) {
			long timestamp = localFile.lastModified();
			remoteFile.setModificationTime(timestamp);
		}

		
		 * Commit changes
		 
		try {
			remoteFile.commitAttributes();
		} catch (RemoteOperationException e) {
			errorWriter.println(NLS.bind("   Could not write properties for uploaded file: {0}", e.getMessage()));
			return;
		}
	}
	
	private void doDirectoryUpload(File localDir, IPath remotePath) throws RemoteConnectionException, CancelException {
		if (! localDir.exists()) {
			errorWriter.println(NLS.bind("   Ignoring {0}: Directory does not exist locally", localDir.toString()));
			return;
		}

		Assert.isTrue(localDir.isDirectory(), "localFile.isDirectory()");
		Assert.isTrue(localDir.isAbsolute(), "localFile.isAbsolute()");
		Assert.isTrue(remotePath.isAbsolute(), "remotePath.isAbsolute()");

		IRemoteFileTools fileTools = manager.getRemoteFileTools();
		
		
		 * Create remote directory if not already exists.
		 
		try {
			fileTools.assureDirectory(LinuxPath.toString(remotePath));
		} catch (RemoteOperationException e) {
			errorWriter.println(NLS.bind("   Could not create remote directory: {0}", e.getMessage()));
			return;
		}
	
		
		 * Fetch properties of directory that was just uploaded.
		 
		IRemoteDirectory remoteDirectory = null;
		try {
			remoteDirectory = fileTools.getDirectory(LinuxPath.toString(remotePath));
		} catch (RemoteOperationException e) {
			errorWriter.println(NLS.bind("   Could not fetch properties for created remote directory: {0}", e.getMessage()));
			return;
		}
		
		
		 * Set file permissions.
		 
		boolean read = localDir.canRead();
		boolean write = localDir.canWrite();
		boolean execute = true;
		
		if (rule.isAsReadOnly()) {
			write = false;
		}
		
		remoteDirectory.setAccessible(execute);
		remoteDirectory.setWriteable(write);
		remoteDirectory.setReadable(read);
		
		
		 * Set date/time, if required
		 
		if (rule.isPreserveTimeStamp()) {
			long timestamp = localDir.lastModified();
			remoteDirectory.setModificationTime(timestamp);
		}

		
		 * Commit changes
		 
		try {
			remoteDirectory.commitAttributes();
		} catch (RemoteOperationException e) {
			errorWriter.println(NLS.bind("   Could not write properties for uploaded directory: {0}", e.getMessage()));
			return;
		}		
	}


	private void uploadDirectory(File localDir, IPath remotePath) throws CoreException, CancelException, RemoteConnectionException {
		Assert.isTrue(localDir.isAbsolute(), "localDir.isAbsolute()");
		Assert.isTrue(remotePath.isAbsolute(), "remotePath.isAbsolute()");

		outputWriter.println(NLS.bind("   Copying contents of directory {0} (local)", localDir.toString()));
		outputWriter.println(NLS.bind("   into directory {0} (remote)", LinuxPath.toString(remotePath)));

		FileRecursiveEnumeration enumeration = null;
		enumeration = new FileRecursiveEnumeration(localDir);
		
		IPath rootPath = new Path(localDir.getAbsolutePath());
		int rootPathLength = rootPath.segmentCount();
		while (enumeration.hasMoreElements()) {
			while (enumeration.hasMoreExceptions()) {
				errorWriter.println(NLS.bind("   Could not read file/directory: {0}", enumeration.nextException()));	
			}
			File file = (File) enumeration.nextElement();
			IPath filePath = new Path(file.getAbsolutePath());
			IPath relativePath = filePath.removeFirstSegments(rootPathLength);
			IPath remoteFilePath = remotePath.append(relativePath);
			if (file.isDirectory()) {
				outputWriter.println(NLS.bind("   Create remote directory {0}", LinuxPath.toString(remoteFilePath)));
				doDirectoryUpload(file, remoteFilePath);
			} else {
				outputWriter.println(NLS.bind("   Upload {0}", LinuxPath.toString(relativePath)));
				doFileUpload(file, remoteFilePath);
			}
		}
		while (enumeration.hasMoreExceptions()) {
			errorWriter.println(NLS.bind("   Could not read file/directory: {0}", enumeration.nextException()));	
		}
	}*/
}
