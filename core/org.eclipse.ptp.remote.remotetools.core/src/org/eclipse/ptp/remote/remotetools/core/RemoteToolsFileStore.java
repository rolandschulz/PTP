/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote.remotetools.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteFile;
import org.eclipse.ptp.remotetools.core.IRemoteItem;
import org.eclipse.ptp.remotetools.core.IRemoteUploadExecution;

public class RemoteToolsFileStore extends FileStore {

	/**
	 * Public factory method for obtaining RemoteToolsFileStore instances.
	 * @param uri URI to get a fileStore for
	 * @return an RemoteToolsFileStore instance for the URI.
	 */
	public static RemoteToolsFileStore getInstance(URI uri) throws IOException {
		String name = uri.getAuthority();
		String path = uri.getPath();
		IRemoteConnectionManager connMgr = RemoteToolsServices.getInstance().getConnectionManager();
		RemoteToolsConnection conn = (RemoteToolsConnection)connMgr.getConnection(name);
		IRemoteFileManager fileMgr = RemoteToolsServices.getInstance().getFileManager(conn);
		return (RemoteToolsFileStore)fileMgr.getResource(new Path(path), new NullProgressMonitor());
	}

	private IRemoteItem remoteItem;
	private URI remoteURI;
	private RemoteToolsFileManager fileMgr;
	private IRemoteExecutionManager exeMgr;
	private boolean isDirectory;
	private IRemoteUploadExecution uploadExecution = null;

	public RemoteToolsFileStore(RemoteToolsFileManager mgr, IRemoteItem remoteItem,
			boolean isDirectory) {
		this.fileMgr = mgr;
		this.exeMgr = mgr.getExecutionManager();
		this.remoteItem = remoteItem;
		this.isDirectory = isDirectory;
		this.remoteURI = mgr.toURI(new Path(remoteItem.getPath()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#childNames(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
		IRemoteItem[] items;
		try {
			items = exeMgr.getRemoteFileTools().listItems(remoteItem.getPath());
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
					e.getMessage()));
		}

		String[] names = new String[items.length];

		for (int i = 0; i < items.length; i++)
		{
			IPath path = new Path(items[i].getPath());
			names[i] = path.lastSegment();
		}

		return names;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#delete(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void delete(int options, IProgressMonitor monitor) throws CoreException {
		try {
			exeMgr.getRemoteFileTools().removeFile(remoteItem.getPath());
		}
		catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
					"Could not delete file", e));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#fetchInfo(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		FileInfo info = new FileInfo(new Path(remoteItem.getPath()).lastSegment());

		try {
			remoteItem.refreshAttributes();
		} catch(Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
					e.getMessage(), e));
		}
		
		if (!remoteItem.exists()) {
			info.setExists(false);
			return info;
		}	
		
		info.setExists(true);
		info.setLastModified(remoteItem.getModificationTime());
		info.setDirectory(isDirectory);
		info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, !remoteItem.isWritable());

		if (!isDirectory) {
			IRemoteFile file = (IRemoteFile)remoteItem;
			info.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, file.isExecutable());
			info.setLength(file.getSize());
		}

		return info;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#getChild(java.lang.String)
	 */
	@Override
	public IFileStore getChild(String name) {
		IPath childPath = new Path(remoteItem.getPath()).append(name);

		IFileStore resource = fileMgr.lookup(childPath);
		if (resource != null) {
			return resource;
		}

		try {
			return fileMgr.getResource(childPath, new NullProgressMonitor());
		} catch (IOException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteResource#getName()
	 */
	@Override
	public String getName() {
		IPath path = new Path(remoteItem.getPath());
		if (path.isRoot()) {
			return path.toString();
		}
		return new Path(remoteItem.getPath()).lastSegment();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteResource#getParent()
	 * 
	 * FIXME: should take a progress monitor as argument
	 * FIXME: should throw a core exception
	 */
	@Override
	public IFileStore getParent() {
		IPath parent = new Path(exeMgr.getRemotePathTools().parent(remoteItem.getPath()));

		IFileStore resource = fileMgr.lookup(parent);
		if (resource != null) {
			return resource;
		}
		try {
			return fileMgr.getResource(parent, new NullProgressMonitor());
		} catch (IOException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteResource#mkdir(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
		try {
			exeMgr.getRemoteFileTools().createDirectory(remoteItem.getPath());
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
					"The directory could not be created", e));
		}

		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteResource#openInputStream(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
		if (isDirectory) {
			throw new CoreException(new Status(IStatus.ERROR,
					RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
			"Is a directory"));
		}
		try {
			return exeMgr.getRemoteCopyTools().executeDownload(remoteItem.getPath()).getInputStreamFromProcessRemoteFile();
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
					"Could not get input stream", e));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteResource#openOutputStream(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
		if (isDirectory) {
			throw new CoreException(new Status(IStatus.ERROR,
					RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
			"Is a directory"));
		}
		
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		try {
			uploadExecution = exeMgr.getRemoteCopyTools().executeUpload(remoteItem.getPath());

			synchronized (remoteItem) {
				while (!monitor.isCanceled() && !remoteItem.exists()) {
					remoteItem.wait(500);
					
					try {
						remoteItem.refreshAttributes();
					} catch(Exception e) {
						throw new CoreException(new Status(IStatus.ERROR,
								RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
								e.getMessage(), e));
					}
				}
			}

			if (monitor.isCanceled()) {
				uploadExecution.cancel();
				return null;
			}
			
			return uploadExecution.getOutputStreamToProcessRemoteFile();
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
					"Could not open output stream", e));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteResource#putInfo(org.eclipse.ptp.remote.core.IRemoteResourceInfo, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
		try {
			if (uploadExecution != null) {
				if (!uploadExecution.wasFinished()) {
					uploadExecution.waitForEndOfExecution();
				}
				uploadExecution = null;
			}
			boolean modified = false;
			remoteItem.refreshAttributes();
			if ((options & EFS.SET_ATTRIBUTES) != 0) {
				//We cannot currently write isExecutable(), isHidden()
				boolean writeable = !info.getAttribute(EFS.ATTRIBUTE_READ_ONLY);
				remoteItem.setWriteable(writeable);
				if(!this.isDirectory) {
					boolean executable = info.getAttribute(EFS.ATTRIBUTE_EXECUTABLE);
					IRemoteFile file = (IRemoteFile)remoteItem;
					file.setExecutable(executable);
				}
				modified = true;
			}
			if ((options & EFS.SET_LAST_MODIFIED) != 0) {
				long modtime = info.getLastModified();
				remoteItem.setModificationTime(modtime);
				modified = true;
			}
			if (modified) {
				remoteItem.commitAttributes();
			}
		} catch(Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
					e.getMessage(), e));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteResource#toURI()
	 */
	@Override
	public URI toURI() {
		return remoteURI;
	}
}
