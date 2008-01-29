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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.ptp.remotetools.core.IRemoteCopyTools;
import org.eclipse.ptp.remotetools.core.IRemoteDirectory;
import org.eclipse.ptp.remotetools.core.IRemoteFile;
import org.eclipse.ptp.remotetools.core.IRemoteFileEnumeration;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.core.IRemoteItem;
import org.eclipse.ptp.remotetools.core.IRemotePathTools;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.ChannelSftp.LsEntry;

/**
 * @author Richard Maciel, Daniel Ferber
 * 
 */
public class FileTools implements IRemoteFileTools {
	protected ExecutionManager manager;
	public int cachedUserID;
	public Set cachedGroupIDSet;

	protected FileTools(ExecutionManager manager) {
		this.manager = manager;
	}

	public void moveFile(String from, String to) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(from);
		validateRemotePath(to);
		IRemotePathTools pathTool = manager.getRemotePathTools();
		try {
			executeCommand("mv -f " + pathTool.quote(from, true) + " " + pathTool.quote(to, true)); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (RemoteExecutionException e) {
			throw new RemoteOperationException(e);
		}
	}

	public void copyFile(String from, String to) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(from);
		validateRemotePath(to);
		
		IRemotePathTools pathTool = manager.getRemotePathTools();
		try {
			executeCommand("cp -f " + pathTool.quote(from, true) + " " + pathTool.quote(to, true));//$NON-NLS-1$ //$NON-NLS-2$
		} catch (RemoteExecutionException e) {
			throw new RemoteOperationException(e);
		} 
	}
	
	public void removeFile(String file) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(file);
		IRemotePathTools pathTool = manager.getRemotePathTools();
		try {
			executeCommand("rm -rf " + pathTool.quote(file, true)); //$NON-NLS-1$
		} catch (RemoteExecutionException e) {
			throw new RemoteOperationException(e);
		} 
	}

	public void createDirectory(String file) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(file);
		IRemotePathTools pathTool = manager.getRemotePathTools();
		try {
			executeCommand("mkdir -p " + pathTool.quote(file, true)); //$NON-NLS-1$
		} catch (RemoteExecutionException e) {
			throw new RemoteOperationException(e);
		} 
	}
	
	public void assureDirectory(String directory) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(directory);
		if (! hasDirectory(directory)) {
			createDirectory(directory);
		}
	}
	
	protected void test() throws RemoteConnectionException, CancelException {
		manager.test();
		manager.testCancel();
	}
	
	/**
	 * Read attributes of the remote file.
	 * @param path
	 * @return A Jsch data structure with attributes or null if path does not exist.
	 * @throws RemoteExecutionException
	 */
	protected SftpATTRS fetchRemoteAttr(String path) throws RemoteOperationException {
		SftpATTRS attrs;
		try {
			attrs = manager.getConnection().getDefaultSFTPChannel().stat(path);
			return attrs;
		} catch (SftpException e) {
			if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				return null;
			}
			throw new RemoteOperationException(Messages.RemoteFileTools_FetchRemoteAttr_FailedFetchAttr);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.internal.ssh.teste#hasDirectory(java.lang.String)
	 */
	public boolean hasDirectory(String directory) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(directory);
		RemoteItem item = (RemoteItem) getItem(directory);
		if (item == null) {
			return false;
		}
		if (! item.exists()) {
			return false;
		}
		if (! item.isDirectory()) {
			return false;
		}		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.internal.ssh.teste#hasFile(java.lang.String)
	 */
	public boolean hasFile(String file) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(file);
		RemoteItem item = (RemoteItem) getItem(file);
		if (item == null) {
			return false;
		}
		if (! item.exists()) {
			return false;
		}
		if (! item.isFile()) {
			return false;
		}		
		return true;
	}
	
	public boolean hasItem(String path) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(path);
		RemoteItem item = (RemoteItem) getItem(path);
		if (item == null) {
			return false;
		}
		if (! item.exists()) {
			return false;
		}
		return true;
	}
	
	public boolean canExecute(String remotePath) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(remotePath);
		RemoteItem item = (RemoteItem) getItem(remotePath);
		return item.isExecutable();
	}
	
	public boolean canRead(String remotePath) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(remotePath);
		RemoteItem item = (RemoteItem) getItem(remotePath);
		return item.isReadable();
	}

	public boolean canWrite(String remotePath) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(remotePath);
		RemoteItem item = (RemoteItem) getItem(remotePath);
		return item.isWritable();
	}

	protected void executeCommand(String command) throws RemoteConnectionException, RemoteExecutionException, CancelException {
		manager.executionTools.executeBashCommand(command);
	}

	void validateRemotePath(String path) throws RemoteOperationException {
		if (! path.startsWith("/")) {
			throw new RemoteOperationException(path + Messages.RemoteFileTools_ValidateRemotePath_NotValid);
		}
	}

	String concatenateRemotePath(String p1, String p2) {
		if (p1.endsWith("/")) { //$NON-NLS-1$
			return p1 + p2;
		} else {
			return p1 + "/" + p2; //$NON-NLS-1$
		}
	}
	
	String parentOfRemotePath(String path) {
		path = removeTrailingSlash(path);
		int index = path.lastIndexOf('/');
		if (index == -1) return null;
		return removeTrailingSlash(path.substring(0, index));
	}
	
	String suffixOfRemotePath(String path) {
		path = removeTrailingSlash(path);
		int index = path.lastIndexOf('/');
		if (index == -1) return null;
		return removeTrailingSlash(path.substring(index+1));
	}
	
	String addTrailingSlash(String path) {
		if (path.endsWith("/")) { //$NON-NLS-1$
			return path;
		} else {
			return path + "/"; //$NON-NLS-1$
		}
	}

	String removeTrailingSlash(String path) {
		if (!path.equals("/") && path.endsWith("/")) { //$NON-NLS-1$ //$NON-NLS-2$
			return path.substring(0, path.length() - 1);
		} else {
			return path;
		}
	}


	public IRemoteItem [] listItems(String root) throws RemoteOperationException {
		validateRemotePath(root);
		Vector files;
		try { 
			files = manager.getConnection().getDefaultSFTPChannel().ls(root);
		} catch (SftpException e) {
			throw new RemoteOperationException(Messages.RemoteFileTools_ListFiles_FailedListRemote, e);
		}
		
		List result = new ArrayList();
		Enumeration enumeration = files.elements();
		while (enumeration.hasMoreElements()) {
			LsEntry entry = (LsEntry) enumeration.nextElement();
			String fileName = entry.getFilename();
			String pathName = concatenateRemotePath(root, fileName);
			if (fileName.equals(".") || fileName.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
				// Ignore parent and current dir entry.
				continue;
			}
			if (entry.getAttrs().isDir()) {
				result.add(new RemoteDirectory(this, pathName, entry.getAttrs()));
			} else {
				result.add(new RemoteFile(this, pathName, entry.getAttrs()));
			}
		}
		
		IRemoteItem [] resultArray = new IRemoteItem[result.size()];
		result.toArray(resultArray);
		return resultArray;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#getFile(java.lang.String)
	 */
	public IRemoteFile getFile(String filePath) throws RemoteOperationException, RemoteConnectionException, CancelException, RemoteOperationException {
		test();
		validateRemotePath(filePath);
		filePath = removeTrailingSlash(filePath);
		cacheUserData();
				
		RemoteFile remfile = new RemoteFile(this, filePath);
		remfile.refreshAttributes();
		if (! remfile.isFile()) {
			throw new RemoteOperationException("Not a file");
		}
		return remfile;
	}

	private void cacheUserData() throws RemoteConnectionException, RemoteOperationException, CancelException {
		if (cachedGroupIDSet == null) {
			cachedGroupIDSet = manager.getRemoteStatusTools().getGroupIDSet();
			cachedUserID = manager.getRemoteStatusTools().getUserID();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#getDirectory(java.lang.String)
	 */
	public IRemoteDirectory getDirectory(String directoryPath) throws RemoteOperationException, RemoteConnectionException, CancelException, RemoteOperationException {
		test();
		validateRemotePath(directoryPath);
		directoryPath = removeTrailingSlash(directoryPath);
		cacheUserData();
				
		RemoteDirectory remfile = new RemoteDirectory(this, directoryPath);
		remfile.refreshAttributes();
		if (! remfile.isDirectory()) {
			throw new RemoteOperationException("Not a directory");
		}
		return remfile;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#getItem(java.lang.String)
	 */
	public IRemoteItem getItem(String path) throws RemoteConnectionException, RemoteOperationException, CancelException, RemoteOperationException {
		test();
		validateRemotePath(path);
		path = removeTrailingSlash(path);
		cacheUserData();

		RemoteItem newitem = new RemoteItem(this, path);
		newitem.refreshAttributes();
		if (newitem.isDirectory()) {
			newitem = new RemoteDirectory(newitem);
		} else {
			newitem = new RemoteFile(newitem);
		}
		return newitem;
	}

	public IRemoteCopyTools getRemoteCopyTools() throws RemoteConnectionException {
		return manager.getRemoteCopyTools();
	}

	public IRemoteFileEnumeration createFileEnumeration(String path) throws RemoteOperationException, RemoteConnectionException, CancelException {
		return new RemoteFileEnumeration(this, path);
	}

	public IRemoteFileEnumeration createRecursiveFileEnumeration(String path) throws RemoteOperationException, RemoteConnectionException, CancelException {
		return new RemoteFileRecursiveEnumeration(this, path);
	}

	/**
	 * @throws CancelException 
	 * @throws RemoteOperationException 
	 * @throws RemoteConnectionException 
	 * @deprecated
	 */
	void uploadPermissions(File file, String remoteFilePath) throws RemoteConnectionException, RemoteOperationException, CancelException {
		IRemoteItem item = getItem(remoteFilePath);
		item.setReadable(file.canRead());
		item.setWriteable(file.canWrite());
		item.commitAttributes();
	}
}
