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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.core.IRemoteItem;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;

public class RemoteToolsFileManager implements IRemoteFileManager {
	private IRemoteExecutionManager exeMgr;
	private RemoteToolsConnection connection;
	private Map<IPath, IFileStore> pathCache = new HashMap<IPath, IFileStore>();
	
	public RemoteToolsFileManager(RemoteToolsConnection conn, IRemoteExecutionManager exeMgr) {
		this.connection = conn;
		this.exeMgr = exeMgr;
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
	 * Get the execution manager for this file manager
	 * @return
	 */
	public IRemoteExecutionManager getExecutionManager() {
		return exeMgr;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#getResource(org.eclipse.core.runtime.IPath)
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
			IRemoteFileTools tools = exeMgr.getRemoteFileTools();
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
		
		res = new RemoteToolsFileStore(this, item, isDirectory);
		cache(path, res);
		return res;
	}
	
	public IPath getWorkingDirectory() {
		String cwd = "//";
		try {
			cwd = exeMgr.getExecutionTools().executeWithOutput("pwd").trim();
		} catch (RemoteExecutionException e) {
		} catch (RemoteConnectionException e) {
		} catch (CancelException e) {
		}
		return new Path(cwd);
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#toPath(java.net.URI)
	 */
	public IPath toPath(URI uri) {
		return new Path(uri.getPath());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#toURI(org.eclipse.core.runtime.IPath)
	 */
	public URI toURI(IPath path) {
		try {
			String auth = connection.getAddress();
			String user = connection.getUsername();
			if (user != null && !user.equals("")) {
				auth = user + "@" + auth;
			}
			return new URI("remotetools", auth, path.toPortableString(), null); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			return null;
		}
	}
}
