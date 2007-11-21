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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.IRemoteResource;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.core.IRemoteItem;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;
import org.eclipse.swt.widgets.Shell;

public class RemoteToolsFileManager implements IRemoteFileManager {
	private RemoteToolsConnection connection;
	private Map<IPath, IRemoteResource> pathCache = new HashMap<IPath, IRemoteResource>();
	
	public RemoteToolsFileManager(RemoteToolsConnection conn) {
		this.connection = conn;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#browseDirectory(org.eclipse.swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	public IPath browseDirectory(Shell shell, String message, String filterPath) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#browseFile(org.eclipse.swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	public IPath browseFile(Shell shell, String message, String filterPath) {
		return null;
	}

	/**
	 * Store an IRemoteResource in the cache
	 * 
	 * @param path path to the remote resource
	 * @param resource resource to add to the cache
	 */
	public void cache(IPath path, IRemoteResource resource) {
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
	public IRemoteResource getResource(IPath path, IProgressMonitor monitor) throws IOException {
		IRemoteResource res = lookup(path);
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
		
		res = new RemoteToolsResource(connection, this, item, isDirectory);
		cache(path, res);
		return res;
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
	public IRemoteResource lookup(IPath path) {
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
