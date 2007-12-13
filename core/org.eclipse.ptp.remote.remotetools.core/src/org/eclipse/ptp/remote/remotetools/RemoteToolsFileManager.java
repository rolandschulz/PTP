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
package org.eclipse.ptp.remote.remotetools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.ui.RemoteResourceBrowser;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.core.IRemoteItem;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;
import org.eclipse.swt.widgets.Shell;

public class RemoteToolsFileManager implements IRemoteFileManager {
	private RemoteToolsConnection connection;
	private Map<IPath, IFileStore> pathCache = new HashMap<IPath, IFileStore>();
	
	public RemoteToolsFileManager(RemoteToolsConnection conn) {
		this.connection = conn;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#browseDirectory(org.eclipse.swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	public IPath browseDirectory(Shell shell, String message, String filterPath) {
		RemoteResourceBrowser browser = new RemoteResourceBrowser(this, new Path(filterPath), shell);
		browser.setType(RemoteResourceBrowser.DIRECTORY_BROWSER);
		if (browser.open() == Window.CANCEL) {
			return null;
		}
		IPath path = browser.getPath();
		if (path == null) {
			return null;
		}
		return path;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#browseFile(org.eclipse.swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	public IPath browseFile(Shell shell, String message, String filterPath) {
		RemoteResourceBrowser browser = new RemoteResourceBrowser(this, new Path(filterPath), shell);
		browser.setType(RemoteResourceBrowser.FILE_BROWSER);
		if (browser.open() == Window.CANCEL) {
			return null;
		}
		IPath path = browser.getPath();
		if (path == null) {
			return null;
		}
		return path;
	}

	/**
	 * Store an IRemoteResource in the cache
	 * 
	 * @param path path to the remote resource
	 * @param resource resource to add to the cache
	 */
	public void cache(IPath path, IFileStore resource) {
		synchronized (pathCache) {
			pathCache.put(path, resource);
		}
	}

	/**
	 * @param path
	 * @throws RemoteOperationException
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 */
	public void delete(String path) throws RemoteOperationException, RemoteConnectionException, CancelException {
		connection.getExecutionManager().getRemoteFileTools().removeFile(path);
	}
	
	/**
	 * @param path
	 * @return
	 * @throws RemoteConnectionException
	 */
	public String getParent(String path) throws RemoteConnectionException {
		return connection.getExecutionManager().getRemotePathTools().parent(path);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#getResource(org.eclipse.core.runtime.IPath)
	 */
	public IFileStore getResource(IPath path, IProgressMonitor monitor) throws IOException {
		IFileStore res = lookup(path);
		if (res != null) {
			return res;
		}
		
		IRemoteItem item;
		boolean isDirectory = false;
		try {
			String pathStr = path.toString();
			IRemoteFileTools tools = connection.getExecutionManager().getRemoteFileTools();
			item = tools.getItem(pathStr);
			if (tools.hasDirectory(pathStr)) {
				isDirectory = true;
			}
		} catch (RemoteConnectionException e) {
			throw new IOException(e.getMessage());
		} catch (RemoteOperationException e) {
			throw new IOException(e.getMessage());
		} catch (CancelException e) {
			throw new IOException(e.getMessage());
		}
		
		res = new RemoteToolsFileStore(connection, this, item, isDirectory);
		cache(path, res);
		return res;
	}
	
	public IPath getWorkingDirectory(IProgressMonitor monitor)
			throws IOException {
		String cwd = "//";
		try {
			cwd = connection.getExecutionManager().getExecutionTools().executeWithOutput("pwd").trim();
		} catch (RemoteExecutionException e) {
		} catch (RemoteConnectionException e) {
		} catch (CancelException e) {
		}
		return new Path(cwd);
	}
	
	/**
	 * @param path
	 * @return
	 * @throws RemoteConnectionException
	 * @throws RemoteOperationException
	 * @throws CancelException
	 */
	public IRemoteItem[] listItems(String path) throws RemoteConnectionException, RemoteOperationException, CancelException {
		return connection.getExecutionManager().getRemoteFileTools().listItems(path);
	}
	
	/**
	 * Look up a cached IRemoteResource
	 * 
	 * @param path path to the remote resource
	 * @return cached IRemoteResource or null
	 */
	public IFileStore lookup(IPath path) {
		synchronized (pathCache) {
			return pathCache.get(path);
		}
	}
	
	/**
	 * @param path
	 * @throws RemoteOperationException
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 */
	public void mkdir(String path) throws RemoteOperationException, RemoteConnectionException, CancelException {
		connection.getExecutionManager().getRemoteFileTools().createDirectory(path);
	}
	
	/**
	 * @param path
	 * @return
	 * @throws RemoteConnectionException
	 * @throws IOException
	 */
	public InputStream openInputStream(String path) throws RemoteConnectionException, IOException {
		return connection.getExecutionManager().getRemoteCopyTools().executeDownload(path).getInputStreamFromProcessRemoteFile();
	}

	/**
	 * @param path
	 * @return
	 * @throws RemoteConnectionException
	 * @throws IOException
	 */
	public OutputStream openOutputStream(String path) throws RemoteConnectionException, IOException {
		return connection.getExecutionManager().getRemoteCopyTools().executeUpload(path).getOutputStreamToProcessRemoteFile();
	}
}
