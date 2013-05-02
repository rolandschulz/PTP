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
package org.eclipse.ptp.remotetools.internal.ssh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.remotetools.core.IRemoteCopyTools;
import org.eclipse.ptp.remotetools.core.IRemoteFileEnumeration;
import org.eclipse.ptp.remotetools.core.messages.Messages;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;
import org.eclipse.ptp.remotetools.internal.common.Debug;
import org.eclipse.ptp.remotetools.internal.common.FileEnumeration;
import org.eclipse.ptp.remotetools.internal.common.FileRecursiveEnumeration;


public class CopyTools implements IRemoteCopyTools {
	
	private FileTools remoteFileTools;
	
	public CopyTools(ExecutionManager manager) throws RemoteConnectionException {
		remoteFileTools = (FileTools)manager.getRemoteFileTools(); //TODO update interface
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteCopyTools#downloadFileToDir(java.lang.String, java.io.File)
	 */
	public void downloadFileToDir(String remotePath, File localDir) throws RemoteConnectionException, CancelException, RemoteOperationException {
		remoteFileTools.validateRemotePath(remotePath);
		File localFile = new File(localDir, remoteFileTools.suffixOfRemotePath(remotePath));
		if (!localDir.exists()) {
			if (! localDir.mkdirs()) {
				throw new RemoteOperationException(NLS.bind(Messages.CopyTools_0, localDir.getAbsolutePath()));
			}
		}
		doDownloadFileToFile(remotePath, localFile);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteCopyTools#downloadFileToDir(java.lang.String, java.lang.String)
	 */
	public void downloadFileToDir(String remotePath, String localPath) throws RemoteConnectionException, CancelException, RemoteOperationException {
		File localDir = new File(localPath);
		downloadFileToDir(remotePath, localDir);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteCopyTools#downloadFileToFile(java.lang.String, java.io.File)
	 */
	public void downloadFileToFile(String remotePath, File localFile) throws RemoteConnectionException, CancelException, RemoteOperationException {
		remoteFileTools.validateRemotePath(remotePath);
		File localDir = localFile.getParentFile();
		if (!localDir.exists()) {
			if (! localDir.mkdirs()) {
				throw new RemoteOperationException(NLS.bind(Messages.CopyTools_0, localDir.getAbsolutePath()));
			}
		}
		doDownloadFileToFile(remotePath, localFile);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteCopyTools#downloadFileToFile(java.lang.String, java.lang.String)
	 */
	public void downloadFileToFile(String remotePath, String localPath) throws RemoteConnectionException, CancelException, RemoteOperationException {
		File localFile = new File(localPath);
		downloadFileToFile(remotePath, localFile);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteCopyTools#uploadFileToDir(java.io.File, java.lang.String)
	 */
	public void uploadFileToDir(File localFile, String remotePath) throws RemoteConnectionException, CancelException, RemoteOperationException {
//		remoteFileTools.validateRemotePath(localFile);
		remoteFileTools.validateRemotePath(remotePath);
		remoteFileTools.assureDirectory(remotePath, null);
		remotePath = remoteFileTools.concatenateRemotePath(remotePath, localFile.getName());
		doUploadFileToFile(localFile, remotePath);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteCopyTools#uploadFileToDir(java.lang.String, java.lang.String)
	 */
	public void uploadFileToDir(String localPath, String remotePath) throws RemoteConnectionException, CancelException, RemoteOperationException {
		File localFile = new File(localPath);
		uploadFileToDir(localFile, remotePath);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteCopyTools#uploadFileToFile(java.io.File, java.lang.String)
	 */
	public void uploadFileToFile(File localFile, String remotePath) throws RemoteConnectionException, CancelException, RemoteOperationException {
		remoteFileTools.validateRemotePath(remotePath);
		remoteFileTools.assureDirectory(remoteFileTools.parentOfRemotePath(remotePath), null);
		doUploadFileToFile(localFile, remotePath);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteCopyTools#uploadFileToFile(java.lang.String, java.lang.String)
	 */
	public void uploadFileToFile(String localPath, String remotePath) throws RemoteConnectionException, CancelException, RemoteOperationException {
		File localFile = new File(localPath);
		uploadFileToFile(localFile, remotePath);
	}

	private void doDownloadFileToFile(String remotePath, File localFile) throws RemoteConnectionException, CancelException, RemoteOperationException {
		FileOutputStream sink = null;
		try {
			sink = new FileOutputStream(localFile);
			remoteFileTools.downloadIntoOutputStream(remotePath, sink, null);
		} catch (FileNotFoundException e) {
			throw new RemoteOperationException(NLS.bind(Messages.CopyTools_doDownloadFileToFile_CannotWriteFile, e.getMessage()), e);
		} finally {
			try {
				if (sink!=null) sink.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void doUploadFileToFile(File localFile, String remotePath) throws RemoteConnectionException, CancelException, RemoteOperationException {
		FileInputStream source = null;
		try {
			source = new FileInputStream(localFile); 
			remoteFileTools.uploadFromInputStream(source, remotePath, null);
		} catch (FileNotFoundException e) {
			throw new RemoteOperationException(NLS.bind(Messages.CopyTools_doUploadFileFromFile_CannotReadFile, e.getMessage()), e);
		} finally {
			try {
				if (source!=null) source.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteCopyTools#downloadDirToDir(java.lang.String, java.io.File, boolean)
	 */
	public void downloadDirToDir(String remotePath, File localDir, boolean recursive) throws RemoteConnectionException, RemoteOperationException, CancelException {
		remoteFileTools.validateRemotePath(remotePath);
//		remoteFileTools.validateRemoteDir(remotePath);
		remotePath = remoteFileTools.addTrailingSlash(remotePath);
		if (!localDir.exists()) {
			if (! localDir.mkdirs()) {
				throw new RemoteOperationException(NLS.bind(Messages.CopyTools_0, localDir.getAbsolutePath()));
			}
		}
	
		IRemoteFileEnumeration enumeration;
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
			String remoteFilePath = enumeration.nextElement().getPath();
			String relativePath = remoteFilePath.substring(remotePath.length());
			File localFile = new File(localDir, relativePath);
		
			if (remoteFileTools.hasDirectory(remoteFilePath, null)) {
				// Create directory on the remote host
				Debug.println("Create: " + remoteFilePath); //$NON-NLS-1$
				if (!localFile.exists()) {
					if (! localDir.mkdirs()) {
						throw new RemoteOperationException(NLS.bind(Messages.CopyTools_0, localFile.getAbsolutePath()));
					}
				}
			} else {
				// Copy to the remote host
				Debug.println(relativePath + " -> " + remoteFilePath); //$NON-NLS-1$
				doDownloadFileToFile(remoteFilePath, localFile);
			}
		}
	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteCopyTools#downloadDirToDir(java.lang.String, java.lang.String, boolean)
	 */
	public void downloadDirToDir(String remotePath, String localPath, boolean recursive) throws RemoteConnectionException, RemoteOperationException, CancelException {
		File localDir = new File(localPath);
		downloadDirToDir(remotePath, localDir, recursive);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteCopyTools#uploadDirToDir(java.io.File, java.lang.String, boolean)
	 */
	public void uploadDirToDir(File localDir, String remotePath, boolean recursive) throws RemoteConnectionException, RemoteOperationException, CancelException {
//		remoteFileTools.validateLocalDir(localDir);
		remoteFileTools.validateRemotePath(remotePath);
		remoteFileTools.assureDirectory(remotePath, null);
		
		Enumeration<File> enumeration;
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
				Debug.println("Create: " + remoteFilePath); //$NON-NLS-1$
				remoteFileTools.assureDirectory(remoteFilePath, null);
				remoteFileTools.uploadPermissions(file.getAbsoluteFile(), remoteFilePath);
			} else {
				// Copy to the remote host
				Debug.println(relativePath + " -> " + remoteFilePath); //$NON-NLS-1$
				doUploadFileToFile(file, remoteFilePath);
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteCopyTools#uploadDirToDir(java.lang.String, java.lang.String, boolean)
	 */
	public void uploadDirToDir(String localPath, String remotePath, boolean recursive) throws RemoteConnectionException, RemoteOperationException, CancelException {
		File localFile = new File(localPath);
		uploadDirToDir(localFile, remotePath, recursive);
	}


}
