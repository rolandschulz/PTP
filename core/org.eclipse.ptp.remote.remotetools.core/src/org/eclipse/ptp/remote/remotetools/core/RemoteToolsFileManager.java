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

import java.net.URI;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;

public class RemoteToolsFileManager implements IRemoteFileManager {
	private String fWorkingDir = null;
	private final RemoteToolsConnection fConnection;

	public RemoteToolsFileManager(RemoteToolsConnection conn) {
		fConnection = conn;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#getResource(java.lang.String)
	 */
	public IFileStore getResource(String pathStr) {
		IPath path = new Path(pathStr);
		if (!path.isAbsolute()) {
			path = new Path(getWorkingDirectory()).append(path);
		}
		return new RemoteToolsFileStore(fConnection.getName(), path.toString());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#getWorkingDirectory()
	 */
	public String getWorkingDirectory() {
		if (!fConnection.isOpen()) {
			return "/"; //$NON-NLS-1$
		}
		if (fWorkingDir == null) {
			IRemoteExecutionManager exeMgr = null;
			try {
				exeMgr = fConnection.createExecutionManager();
			} catch (RemoteConnectionException e) {
				// Ignore
			}
			if (exeMgr != null) {
				try {
					fWorkingDir = exeMgr.getExecutionTools().executeWithOutput("pwd").trim(); //$NON-NLS-1$
				} catch (RemoteExecutionException e) {
				} catch (RemoteConnectionException e) {
				} catch (CancelException e) {
				}
			}
			if (fWorkingDir == null) {
				return "/"; //$NON-NLS-1$
			}
		}
		return fWorkingDir;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#setWorkingDirectory(java.lang.String)
	 */
	public void setWorkingDirectory(String path) {
		if (new Path(path).isAbsolute()) {
			fWorkingDir = path;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#toPath(java.net.URI)
	 */
	public String toPath(URI uri) {
		return uri.getPath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#toURI(org.eclipse.core.runtime.IPath)
	 */
	public URI toURI(IPath path) {
		try {
			return RemoteToolsFileSystem.getURIFor(fConnection.getName(), path.toString());
		} catch (Exception e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#toURI(java.lang.String)
	 */
	public URI toURI(String path) {
		return toURI(new Path(path));
	}
}
