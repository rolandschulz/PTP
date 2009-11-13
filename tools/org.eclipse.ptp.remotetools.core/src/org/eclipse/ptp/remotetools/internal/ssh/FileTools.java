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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.remotetools.core.IRemoteCopyTools;
import org.eclipse.ptp.remotetools.core.IRemoteFileEnumeration;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.core.IRemoteItem;
import org.eclipse.ptp.remotetools.core.IRemotePathTools;
import org.eclipse.ptp.remotetools.core.IRemoteScript;
import org.eclipse.ptp.remotetools.core.RemoteProcess;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.jcraft.jsch.ChannelSftp.LsEntry;

/**
 * @author Richard Maciel, Daniel Ferber
 * 
 */
public class FileTools implements IRemoteFileTools {
	public static class FileToolsProgressMonitor implements SftpProgressMonitor {
		private IProgressMonitor fMonitor;
		private long fWorkToDate;

		public FileToolsProgressMonitor(IProgressMonitor monitor) {
			fMonitor = monitor;
		}
		public boolean count(long count){
			fWorkToDate += count;
			//fMonitor.worked((int)count);
			return !(fMonitor.isCanceled());
		}
		public void end(){
			//fMonitor.done();
		}
		public void init(int op, String src, String dest, long max){
			fWorkToDate = 0;
			String srcFile = new Path(src).lastSegment();
			String desc = srcFile;
			//TODO avoid cast from long to int
			fMonitor.beginTask(desc, (int)max);

		}
	}

//	private static class SftpBufferedInputStream extends BufferedInputStream {
//		private ChannelSftp channel;
//
//		public SftpBufferedInputStream(InputStream in, ChannelSftp channel) {
//			super(in);
//			this.channel = channel;
//		}
//
//		public void close() throws IOException {
//			super.close();
//			if (channel.isConnected()) {
//				channel.disconnect();
//			}
//		}
//	}
//
//	private static class SftpBufferedOutputStream extends BufferedOutputStream {
//
//		private ChannelSftp channel;
//
//		public SftpBufferedOutputStream(OutputStream out, ChannelSftp channel) {
//			super(out);
//			this.channel = channel;
//		}
//
//		public void close() throws IOException {
//			super.close();
//			if (channel.isConnected()) {
//				channel.disconnect();
//			}
//		}
//	}

	protected ExecutionManager manager;
	private int cachedUserID;
	private Set<Integer> cachedGroupIDSet;
	private String fOSName = null;

	protected FileTools(ExecutionManager manager) {
		this.manager = manager;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#assureDirectory(java.lang.String)
	 */
	public void assureDirectory(String directory) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(directory);
		if (! hasDirectory(directory)) {
			createDirectory(directory);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#canExecute(java.lang.String)
	 */
	public boolean canExecute(String remotePath) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(remotePath);
		RemoteItem item = (RemoteItem) getItem(remotePath);
		return item.isExecutable();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#canRead(java.lang.String)
	 */
	public boolean canRead(String remotePath) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(remotePath);
		RemoteItem item = (RemoteItem) getItem(remotePath);
		return item.isReadable();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#canWrite(java.lang.String)
	 */
	public boolean canWrite(String remotePath) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(remotePath);
		RemoteItem item = (RemoteItem) getItem(remotePath);
		return item.isWritable();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#copyFile(java.lang.String, java.lang.String)
	 */
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#createDirectory(java.lang.String)
	 */
	public void createDirectory(String directory) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(directory);
		IRemotePathTools pathTool = manager.getRemotePathTools();
		
		String path = pathTool.quote(directory, true);
		String parent = pathTool.parent(path);
		
		RemoteFileAttributes attrs = fetchRemoteAttr(parent);
		if (attrs == null) {
			createDirectory(parent);
		}
		
		synchronized (this) {
			try {
				manager.getConnection().getDefaultSFTPChannel().mkdir(path);
			} catch (SftpException e) {
				if (e.id == ChannelSftp.SSH_FX_FAILURE) {
					try {
						executeCommand("mkdir -p " + path); //$NON-NLS-1$
					} catch (RemoteExecutionException e1) {
						throw new RemoteOperationException(e1);
					}
				} else {
					throw new RemoteOperationException(e);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#createFile(java.lang.String)
	 */
	public void createFile(String file) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(file);
		IRemotePathTools pathTool = manager.getRemotePathTools();
		String path = pathTool.quote(file, true);
		
		synchronized (this) {
			try {
				OutputStream os = manager.getConnection().getDefaultSFTPChannel().put(path);
				os.close();
			} catch (Exception e) {
				throw new RemoteOperationException(e);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#createFileEnumeration(java.lang.String)
	 */
	public IRemoteFileEnumeration createFileEnumeration(String path) throws RemoteOperationException, RemoteConnectionException, CancelException {
		return new RemoteFileEnumeration(this, path);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#createRecursiveFileEnumeration(java.lang.String)
	 */
	public IRemoteFileEnumeration createRecursiveFileEnumeration(String path) throws RemoteOperationException, RemoteConnectionException, CancelException {
		return new RemoteFileRecursiveEnumeration(this, path);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#getDirectory(java.lang.String)
	 */
	public IRemoteItem getDirectory(String directoryPath) throws RemoteOperationException, RemoteConnectionException, CancelException, RemoteOperationException {
		test();
		validateRemotePath(directoryPath);
		directoryPath = removeTrailingSlash(directoryPath);
		cacheUserData();
				
		IRemoteItem remfile = new RemoteItem(this, directoryPath);
		remfile.refreshAttributes();
		if (!remfile.isDirectory()) {
			throw new RemoteOperationException("Not a directory");
		}
		return remfile;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#getFile(java.lang.String)
	 */
	public IRemoteItem getFile(String filePath) throws RemoteOperationException, RemoteConnectionException, CancelException, RemoteOperationException {
		test();
		validateRemotePath(filePath);
		filePath = removeTrailingSlash(filePath);
		cacheUserData();
				
		IRemoteItem remfile = new RemoteItem(this, filePath);
		remfile.refreshAttributes();
		if (remfile.isDirectory()) {
			throw new RemoteOperationException("Not a file");
		}
		return remfile;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#getInputStream(java.lang.String, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public InputStream getInputStream(String file, IProgressMonitor monitor) throws RemoteOperationException,
			RemoteConnectionException, CancelException {
		test();
		validateRemotePath(file);
		IRemotePathTools pathTool = manager.getRemotePathTools();
		String path = pathTool.quote(file, true);

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		IRemoteScript script = manager.executionTools.createScript();
		script.setScript("cat " + path); //$NON-NLS-1$
		RemoteProcess proc = null;
		synchronized (this) {
			try {
				proc = manager.executionTools.executeProcess(script);
			} catch (RemoteExecutionException e) {
				throw new RemoteOperationException(e.getLocalizedMessage());
			}
			if (proc == null) {
				throw new RemoteOperationException("Unable to get input stream");
			}
		}
		return proc.getInputStream();
		
//		synchronized (manager) {
//			try {
//				ChannelSftp channel = (ChannelSftp)manager.getConnection().getNewSFTPChannel();
//				stream = new SftpBufferedInputStream(channel.get(path), channel);
//				
//				if (monitor.isCanceled()) {
//					throw new CancelException();
//				}
//				
//				return stream;	
//			} catch (SftpException e) {
//				throw new RemoteOperationException(e);
//			}
//		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#getItem(java.lang.String)
	 */
	public IRemoteItem getItem(String path) throws RemoteConnectionException, RemoteOperationException, CancelException, RemoteOperationException {
		test();
		validateRemotePath(path);
		path = removeTrailingSlash(path);
		cacheUserData();

		return new RemoteItem(this, path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#getOutputStream(java.lang.String, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public OutputStream getOutputStream(String file, int options, IProgressMonitor monitor) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(file);
		IRemotePathTools pathTool = manager.getRemotePathTools();
		String path = pathTool.quote(file, true);

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		RemoteProcess proc = null;
		IRemoteScript script = manager.executionTools.createScript();
		if ((options & IRemoteFileTools.APPEND) == 0) {
			script.setScript("cat > " + path); //$NON-NLS-1$
		} else {
			script.setScript("cat >> " + path); //$NON-NLS-1$
		}
		synchronized (this) {
			try {
				proc = manager.executionTools.executeProcess(script);
			} catch (RemoteExecutionException e) {
				throw new RemoteOperationException(e.getLocalizedMessage());
			}
			if (proc == null) {
				throw new RemoteOperationException("Unable to get output stream");
			}
		}
		return proc.getOutputStream();

//		SftpProgressMonitor sftpMonitor = new FileToolsProgressMonitor(monitor);
//		int mode;
//		if ((options & IRemoteFileTools.APPEND) == 0) {
//			mode = ChannelSftp.OVERWRITE;
//		} else {
//			mode = ChannelSftp.APPEND;
//		}
//		try {
//			ChannelSftp channel = (ChannelSftp)manager.getConnection().getNewSFTPChannel();
//			OutputStream stream = new SftpBufferedOutputStream(channel.put(path, sftpMonitor, mode), channel);
//			
//			if (monitor.isCanceled()) {
//				throw new CancelException();
//			}
//			
//			return stream;	
//		} catch (SftpException e) {
//			throw new RemoteOperationException(e);
//		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#getRemoteCopyTools()
	 */
	public IRemoteCopyTools getRemoteCopyTools() throws RemoteConnectionException {
		return manager.getRemoteCopyTools();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.internal.ssh.teste#hasDirectory(java.lang.String)
	 */
	public boolean hasDirectory(String directory) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(directory);
		RemoteItem item = (RemoteItem) getItem(directory);
		item.refreshAttributes();
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
		IRemoteItem item = getItem(file);
		item.refreshAttributes();
		if (item == null) {
			return false;
		}
		if (!item.exists()) {
			return false;
		}
		if (item.isDirectory()) {
			return false;
		}		
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#listItems(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public IRemoteItem[] listItems(String root) throws RemoteOperationException, RemoteConnectionException, CancelException {
		validateRemotePath(root);
		Vector files;

		synchronized (this) {
			try { 
				files = manager.getConnection().getDefaultSFTPChannel().ls(root);
			} catch (SftpException e) {
				if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
					IRemoteItem item = getItem(root);
					if (item.exists() && item.isDirectory()) {
						return new IRemoteItem[0];
					}
				}
				throw new RemoteOperationException(Messages.RemoteFileTools_ListFiles_FailedListRemote, e);
			}
		}
		
		cacheUserData();
		
		List<RemoteItem> result = new ArrayList<RemoteItem>();
		Enumeration enumeration = files.elements();
		while (enumeration.hasMoreElements()) {
			LsEntry entry = (LsEntry) enumeration.nextElement();
			String fileName = entry.getFilename();
			String pathName = concatenateRemotePath(root, fileName);
			if (fileName.equals(".") || fileName.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
				// Ignore parent and current dir entry.
				continue;
			}
			result.add(new RemoteItem(this, pathName, entry.getAttrs()));
		}
		
		IRemoteItem [] resultArray = new IRemoteItem[result.size()];
		result.toArray(resultArray);
		return resultArray;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#moveFile(java.lang.String, java.lang.String)
	 */
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

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#removeFile(java.lang.String)
	 */
	public void removeFile(String file) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(file);
		IRemotePathTools pathTool = manager.getRemotePathTools();
		String path = pathTool.quote(file, true);
		
		synchronized (this) {
			try {
				manager.getConnection().getDefaultSFTPChannel().rm(path);
			} catch (SftpException e) {
				if (e.id == ChannelSftp.SSH_FX_FAILURE) {
					try {
						executeCommand("rm -f " + path); //$NON-NLS-1$
					} catch (RemoteExecutionException e1) {
						throw new RemoteOperationException(e1);
					} 
				} else {
					throw new RemoteOperationException(e);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileTools#removeDirectory(java.lang.String)
	 */
	public void removeDirectory(String dir) throws RemoteOperationException, RemoteConnectionException, CancelException {
		test();
		validateRemotePath(dir);
		IRemotePathTools pathTool = manager.getRemotePathTools();
		String path = pathTool.quote(dir, true);
		
		synchronized (this) {
			try {
				manager.getConnection().getDefaultSFTPChannel().rmdir(path);
			} catch (SftpException e) {
				if (e.id == ChannelSftp.SSH_FX_FAILURE) {
					try {
						executeCommand("rm -rf " + path); //$NON-NLS-1$
					} catch (RemoteExecutionException e1) {
						throw new RemoteOperationException(e1);
					} 
				} else {
					throw new RemoteOperationException(e);
				}
			}
		}
	}

	private void cacheUserData() throws RemoteConnectionException, RemoteOperationException, CancelException {
		if (cachedGroupIDSet == null) {
			cachedGroupIDSet = manager.getRemoteStatusTools().getGroupIDSet();
			cachedUserID = manager.getRemoteStatusTools().getUserID();
		}
	}
		
	protected void executeCommand(String command) throws RemoteConnectionException, RemoteExecutionException, CancelException {
		manager.executionTools.executeBashCommand(command);
	}

	/**
	 * Read attributes of the remote file.
	 * @param path
	 * @return A Jsch data structure with attributes or null if path does not exist.
	 * @throws RemoteExecutionException
	 */
	protected RemoteFileAttributes fetchRemoteAttr(String path) throws RemoteOperationException {
		try {
			test();
		} catch (RemoteConnectionException e) {
			throw new RemoteOperationException(e.getLocalizedMessage());
		} catch (CancelException e) {
			throw new RemoteOperationException(e.getLocalizedMessage());
		}
		validateRemotePath(path);
		IRemotePathTools pathTool = manager.getRemotePathTools();
		String quotedPath = pathTool.quote(path, true);

		String statCmd;
		if (checkOSName("Darwin")) { //$NON-NLS-1$
			statCmd = "stat -f \"0%p %z %u %g %m %a\" " + quotedPath + " 2>&1"; //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			// Assume linux
			statCmd = "stat --format \"0x%f %s %u %g %X %Y\" " + quotedPath + " 2>&1"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		String result;
		try {
			result = manager.executionTools.executeWithOutput(statCmd);
		} catch (RemoteExecutionException e) {
			throw new RemoteOperationException(e.getLocalizedMessage());
		} catch (RemoteConnectionException e) {
			throw new RemoteOperationException(e.getLocalizedMessage());
		} catch (CancelException e) {
			throw new RemoteOperationException(e.getLocalizedMessage());
		}

		return RemoteFileAttributes.getAttributes(result.trim());
		
//		synchronized (manager) {
//			try {
//				SftpATTRS attrs = manager.getConnection().getDefaultSFTPChannel().stat(path);
//				return attrs;
//			} catch (SftpException e) {
//				if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
//					return null;
//				}
//				throw new RemoteOperationException(Messages.RemoteFileTools_FetchRemoteAttr_FailedFetchAttr + ": id=" + e.id);
//			}
//		}
	}
	
	public int getCachedUserID() {
		return cachedUserID;
	}
	
	public Set<Integer> getCachedGroupIDSet() {
		return cachedGroupIDSet;
	}

	protected void test() throws RemoteConnectionException, CancelException {
		manager.test();
		manager.testCancel();
	}
	
	private boolean checkOSName(String name) {
		if (fOSName == null) {
			try {
				fOSName = manager.getExecutionTools().executeWithOutput("uname").trim(); //$NON-NLS-1$
			} catch (RemoteExecutionException e) {
				return false;
			} catch (RemoteConnectionException e) {
				return false;
			} catch (CancelException e) {
				return false;
			}
		}
		return fOSName.equals(name);
	}
	
	public String addTrailingSlash(String path) {
		if (path.endsWith("/")) { //$NON-NLS-1$
			return path;
		} else {
			return path + "/"; //$NON-NLS-1$
		}
	}

	public String concatenateRemotePath(String p1, String p2) {
		if (p1.endsWith("/")) { //$NON-NLS-1$
			return p1 + p2;
		} else {
			return p1 + "/" + p2; //$NON-NLS-1$
		}
	}

	public String parentOfRemotePath(String path) {
		path = removeTrailingSlash(path);
		int index = path.lastIndexOf('/');
		if (index == -1) return null;
		return removeTrailingSlash(path.substring(0, index));
	}

	public String removeTrailingSlash(String path) {
		if (!path.equals("/") && path.endsWith("/")) { //$NON-NLS-1$ //$NON-NLS-2$
			return path.substring(0, path.length() - 1);
		} else {
			return path;
		}
	}

	public String suffixOfRemotePath(String path) {
		path = removeTrailingSlash(path);
		int index = path.lastIndexOf('/');
		if (index == -1) return null;
		return removeTrailingSlash(path.substring(index+1));
	}

	/**
	 * @throws CancelException 
	 * @throws RemoteOperationException 
	 * @throws RemoteConnectionException 
	 * @deprecated
	 */
	public void uploadPermissions(File file, String remoteFilePath) throws RemoteConnectionException, RemoteOperationException, CancelException {
		IRemoteItem item = getItem(remoteFilePath);
		item.setReadable(file.canRead());
		item.setWriteable(file.canWrite());
		item.commitAttributes();
	}
	
	public void validateRemotePath(String path) throws RemoteOperationException {
		if (! path.startsWith("/")) { //$NON-NLS-1$
			throw new RemoteOperationException(path + Messages.RemoteFileTools_ValidateRemotePath_NotValid);
		}
	}
}
