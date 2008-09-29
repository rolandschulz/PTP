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
package org.eclipse.ptp.remotetools.internal.ssh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.remotetools.RemotetoolsPlugin;
import org.eclipse.ptp.remotetools.core.IRemoteCopyTools;
import org.eclipse.ptp.remotetools.core.IRemoteDownloadExecution;
import org.eclipse.ptp.remotetools.core.IRemoteUploadExecution;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;
import org.eclipse.ptp.remotetools.internal.common.Debug;
import org.eclipse.ptp.utils.core.file.FileEnumeration;
import org.eclipse.ptp.utils.core.file.FileRecursiveEnumeration;

public class CopyTools implements IRemoteCopyTools {
	
	ExecutionManager manager;
	FileTools remoteFileTools;
	
	public CopyTools(ExecutionManager manager) {
		this.manager = manager;
		remoteFileTools = new FileTools(manager);
	}

	public IRemoteDownloadExecution executeDownload(String remoteFile, OutputStream sink) throws RemoteConnectionException {
		DownloadExecution execution = new DownloadExecution(manager, remoteFile, sink);
		execution.startExecution();
		return execution;
	}

	public IRemoteDownloadExecution executeDownload(String remoteFile) throws RemoteConnectionException {
		return executeDownload(remoteFile, null);
	}

	public IRemoteUploadExecution executeUpload(String remoteFile, InputStream source) throws RemoteConnectionException {
		UploadExecution execution = new UploadExecution(manager, remoteFile, source);
		execution.startExecution();
		return execution;
	}

	public IRemoteUploadExecution executeUpload(String remoteFile) throws RemoteConnectionException {
		return executeUpload(remoteFile, null);
	}

	public void downloadFileToDir(String remotePath, File localDir) throws RemoteConnectionException, CancelException, RemoteOperationException {
		remoteFileTools.validateRemotePath(remotePath);
		File localFile = new File(localDir, remoteFileTools.suffixOfRemotePath(remotePath));
		if (!localDir.exists()) {
			if (! localDir.mkdirs()) {
				throw new RemoteOperationException(NLS.bind("Failed to create local directory {0}", localDir.getAbsolutePath()));
			}
		}
		doDownloadFileToFile(remotePath, localFile);
	}

	public void downloadFileToDir(String remotePath, String localPath) throws RemoteConnectionException, CancelException, RemoteOperationException {
		File localDir = new File(localPath);
		downloadFileToDir(remotePath, localDir);
	}

	public void downloadFileToFile(String remotePath, File localFile) throws RemoteConnectionException, CancelException, RemoteOperationException {
		remoteFileTools.validateRemotePath(remotePath);
		File localDir = localFile.getParentFile();
		if (!localDir.exists()) {
			if (! localDir.mkdirs()) {
				throw new RemoteOperationException(NLS.bind("Failed to create local directory {0}", localDir.getAbsolutePath()));
			}
		}
		doDownloadFileToFile(remotePath, localFile);
	}

	public void downloadFileToFile(String remotePath, String localPath) throws RemoteConnectionException, CancelException, RemoteOperationException {
		File localFile = new File(localPath);
		downloadFileToFile(remotePath, localFile);
	}

	public void uploadFileToDir(File localFile, String remotePath) throws RemoteConnectionException, CancelException, RemoteOperationException {
//		remoteFileTools.validateRemotePath(localFile);
		remoteFileTools.validateRemotePath(remotePath);
		remoteFileTools.assureDirectory(remotePath);
		remotePath = remoteFileTools.concatenateRemotePath(remotePath, localFile.getName());
		doUploadFileToFile(localFile, remotePath);
	}

	public void uploadFileToDir(String localPath, String remotePath) throws RemoteConnectionException, CancelException, RemoteOperationException {
		File localFile = new File(localPath);
		uploadFileToDir(localFile, remotePath);
	}

	public void uploadFileToFile(File localFile, String remotePath) throws RemoteConnectionException, CancelException, RemoteOperationException {
		remoteFileTools.validateRemotePath(remotePath);
		remoteFileTools.assureDirectory(remoteFileTools.parentOfRemotePath(remotePath));
		doUploadFileToFile(localFile, remotePath);
	}

	public void uploadFileToFile(String localPath, String remotePath) throws RemoteConnectionException, CancelException, RemoteOperationException {
		File localFile = new File(localPath);
		uploadFileToFile(localFile, remotePath);
	}

	private void doDownloadFileToFile(String remotePath, File localFile) throws RemoteConnectionException, CancelException, RemoteOperationException {
		FileOutputStream sink;
		try {
			sink = new FileOutputStream(localFile);
		} catch (FileNotFoundException e) {
			throw new RemoteOperationException(NLS.bind(Messages.CopyTools_doDownloadFileToFile_CannotWriteFile, e.getMessage()), e);
		}
		IRemoteDownloadExecution execution = executeDownload(remotePath, sink);
		try {
			execution.waitForEndOfExecution();
		} catch (RemoteExecutionException e) {
			throw new RemoteOperationException(NLS.bind(Messages.CopyTools_doDownloadFileToFile_ExecutionFailed, e.getMessage()), e);
		}
		if (execution.getReturnCode() != 0) {
			throw new RemoteOperationException(NLS.bind(Messages.CopyTools_doDownloadFileToFile_CommandFailes, Integer.toString(execution.getReturnCode())));
		}
		execution.close();
	}

	private void doUploadFileToFile(File localFile, String remotePath) throws RemoteConnectionException, CancelException, RemoteOperationException {
		FileInputStream source;
		try {
			source = new FileInputStream(localFile); 
		} catch (FileNotFoundException e) {
			throw new RemoteOperationException(NLS.bind(Messages.CopyTools_doUploadFileFromFile_CannotReadFile, e.getMessage()), e);
		}
		IRemoteUploadExecution execution = executeUpload(remotePath, source);
		try {
			execution.waitForEndOfExecution();
		} catch (RemoteExecutionException e) {
			throw new RemoteOperationException(NLS.bind(Messages.CopyTools_doUploadFileFromFile_ExecutionFailed, e.getMessage()), e);
		}
		if (execution.getReturnCode() != 0) {
			throw new RemoteOperationException(NLS.bind(Messages.CopyTools_doUploadFileFromFile_CommandFailed, Integer.toString(execution.getReturnCode())));
		}
		execution.close();
	}
	
	public void downloadDirToDir(String remotePath, File localDir, boolean recursive) throws RemoteConnectionException, RemoteOperationException, CancelException {
		remoteFileTools.validateRemotePath(remotePath);
//		remoteFileTools.validateRemoteDir(remotePath);
		remotePath = remoteFileTools.addTrailingSlash(remotePath);
		if (!localDir.exists()) {
			if (! localDir.mkdirs()) {
				throw new RemoteOperationException(NLS.bind("Failed to create local directory {0}", localDir.getAbsolutePath()));
			}
		}
	
		Enumeration enumeration;
		try {
			if (recursive) {
				enumeration = new RemoteFileRecursiveEnumeration(remoteFileTools, remoteFileTools.removeTrailingSlash(remotePath));
			} else {
				enumeration = new RemoteFileEnumeration(remoteFileTools, remoteFileTools.removeTrailingSlash(remotePath));
			}
		} catch (IllegalArgumentException e) {
			throw new RemoteOperationException(e.getMessage());
		}
		
		while (enumeration.hasMoreElements()) {
			String remoteFilePath = (String) enumeration.nextElement();
			String relativePath = remoteFilePath.substring(remotePath.length());
			File localFile = new File(localDir, relativePath);
		
			if (remoteFileTools.hasDirectory(remoteFilePath)) {
				// Create directory on the remote host
				Debug.println("Create: " + remoteFilePath);
				if (!localFile.exists()) {
					if (! localDir.mkdirs()) {
						throw new RemoteOperationException(NLS.bind("Failed to create local directory {0}", localFile.getAbsolutePath()));
					}
				}
			} else {
				// Copy to the remote host
				Debug.println(relativePath + " -> " + remoteFilePath);
				doDownloadFileToFile(remoteFilePath, localFile);
			}
		}
	
	}

	public void downloadDirToDir(String remotePath, String localPath, boolean recursive) throws RemoteConnectionException, RemoteOperationException, CancelException {
		File localDir = new File(localPath);
		downloadDirToDir(remotePath, localDir, recursive);
	}
	
	public void uploadDirToDir(File localDir, String remotePath, boolean recursive) throws RemoteConnectionException, RemoteOperationException, CancelException {
//		remoteFileTools.validateLocalDir(localDir);
		remoteFileTools.validateRemotePath(remotePath);
		remoteFileTools.assureDirectory(remotePath);
		
		Enumeration enumeration;
		try {
			if (recursive) {
				enumeration = new FileRecursiveEnumeration(localDir);
			} else {
				enumeration = new FileEnumeration(localDir);
			}
		} catch (IOException e) {
			throw new RemoteOperationException(e.getMessage());
		}		
	
		String rootPath;
		try {
			rootPath = localDir.getCanonicalPath();
		} catch (IOException e) {
			throw new RemoteOperationException(e);			
		}
		
		while (enumeration.hasMoreElements()) {
			File file = (File) enumeration.nextElement();
			String relativePath;
			try {
				relativePath = file.getCanonicalPath().substring(rootPath.length()+1).replace('\\', '/');
			} catch (IOException e) {
				throw new RemoteOperationException(e);
			}
	
			String remoteFilePath = remoteFileTools.concatenateRemotePath(remotePath, relativePath);
			
			if (file.isDirectory()) {
				// Create directory on the remote host
				Debug.println("Create: " + remoteFilePath);
				remoteFileTools.assureDirectory(remoteFilePath);
				remoteFileTools.uploadPermissions(file.getAbsoluteFile(), remoteFilePath);
			} else {
				// Copy to the remote host
				Debug.println(relativePath + " -> " + remoteFilePath);
				doUploadFileToFile(file, remoteFilePath);
			}
		}
		
	}

	public void uploadDirToDir(String localPath, String remotePath, boolean recursive) throws RemoteConnectionException, RemoteOperationException, CancelException {
		File localFile = new File(localPath);
		uploadDirToDir(localFile, remotePath, recursive);
	}


}
