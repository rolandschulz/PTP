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
	private IRemoteExecutionManager fExeMgr;
	private final RemoteToolsConnection fConnection;
	
	public RemoteToolsFileManager(RemoteToolsConnection conn) {
		fConnection = conn;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#getResource(java.lang.String)
	 */
	public IFileStore getResource(String path) {
		return new RemoteToolsFileStore(fConnection.getName(), path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#getWorkingDirectory()
	 */
	public String getWorkingDirectory() {
		if (fExeMgr == null) {
			try {
				fExeMgr = fConnection.createExecutionManager();
			} catch (RemoteConnectionException e) {
				// Ignore
			}
		}

		String cwd = "//"; //$NON-NLS-1$
		if (fExeMgr != null) {
			try {
				cwd = fExeMgr.getExecutionTools().executeWithOutput("pwd").trim(); //$NON-NLS-1$
			} catch (RemoteExecutionException e) {
			} catch (RemoteConnectionException e) {
			} catch (CancelException e) {
			}
		}
		return cwd;
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
		return RemoteToolsFileSystem.getURIFor(fConnection.getName(), path.toString());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#toURI(java.lang.String)
	 */
	public URI toURI(String path) {
		return toURI(new Path(path));
	}
}
