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
package org.eclipse.ptp.remotetools.core;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;


/**
 * Groups all operations that do data transference between remote and local
 * host.
 * 
 * @author Daniel Felix Ferber, Richard Maciel
 * 
 */
public interface IRemoteCopyTools {
	public IRemoteUploadExecution executeUpload(String remoteFile, InputStream source) throws RemoteConnectionException;

	public IRemoteDownloadExecution executeDownload(String remoteFile, OutputStream sink) throws RemoteConnectionException;

	public IRemoteUploadExecution executeUpload(String remoteFile) throws RemoteConnectionException;

	public IRemoteDownloadExecution executeDownload(String remoteFile) throws RemoteConnectionException;

	public void downloadFileToDir(String remotePath, File localDir) throws RemoteConnectionException, CancelException, RemoteOperationException;

	public void downloadFileToDir(String remotePath, String localPah) throws RemoteConnectionException, CancelException, RemoteOperationException;

	public void downloadFileToFile(String remotePath, File localFile) throws RemoteConnectionException, CancelException, RemoteOperationException;

	public void downloadFileToFile(String remotePath, String localPath) throws RemoteConnectionException, CancelException, RemoteOperationException;

	public void uploadFileToDir(File localFile, String remotePath) throws RemoteConnectionException, CancelException, RemoteOperationException;

	public void uploadFileToDir(String localPath, String remotePath) throws RemoteConnectionException, CancelException, RemoteOperationException;

	public void uploadFileToFile(File localFile, String remotePath) throws RemoteConnectionException, CancelException, RemoteOperationException;

	public void uploadFileToFile(String localPath, String remotePath) throws RemoteConnectionException, CancelException, RemoteOperationException;

	/**
	 * @deprecated This method is not supported anymore. It will not be tested in future versions of RemoteTools.
	 * It will be replaced by better mechanisms to walk and copy recursively through the file system.
	 */
	public void downloadDirToDir(String remotePath, File localDir, boolean recursive) throws RemoteConnectionException, RemoteOperationException, CancelException;

	/**
	 * @deprecated This method is not supported anymore. It will not be tested in future versions of RemoteTools.
	 * It will be replaced by better mechanisms to walk and copy recursively through the file system.
	 */
	public void downloadDirToDir(String remotePath, String localPath, boolean recursive) throws RemoteConnectionException, RemoteOperationException, CancelException;

	/**
	 * @throws RemoteOperationException 
	 * @deprecated This method is not supported anymore. It will not be tested in future versions of RemoteTools.
	 * It will be replaced by better mechanisms to walk and copy recursively through the file system.
	 */
	public void uploadDirToDir(File localDir, String remotePath, boolean recursive) throws RemoteConnectionException, CancelException, RemoteOperationException;

	/**
	 * @deprecated This method is not supported anymore. It will not be tested in future versions of RemoteTools.
	 * It will be replaced by better mechanisms to walk and copy recursively through the file system.
	 */
	public void uploadDirToDir(String localPath, String remotePath, boolean recursive) throws RemoteConnectionException, RemoteOperationException, CancelException;

}
