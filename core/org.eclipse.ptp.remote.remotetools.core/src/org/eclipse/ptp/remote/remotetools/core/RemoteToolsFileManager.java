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

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;

public class RemoteToolsFileManager implements IRemoteFileManager {
	private IRemoteExecutionManager fExeMgr;
	private RemoteToolsConnection fConnection;
	
	public RemoteToolsFileManager(RemoteToolsConnection conn, IRemoteExecutionManager exeMgr) {
		fConnection = conn;
		fExeMgr = exeMgr;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#getResource(org.eclipse.core.runtime.IPath)
	 */
	public IFileStore getResource(IPath path, IProgressMonitor monitor) throws IOException {
		return new RemoteToolsFileStore(fConnection.getName(), path.toString());
	}
	
	public IPath getWorkingDirectory() {
		String cwd = "//"; //$NON-NLS-1$
		try {
			cwd = fExeMgr.getExecutionTools().executeWithOutput("pwd").trim(); //$NON-NLS-1$
		} catch (RemoteExecutionException e) {
		} catch (RemoteConnectionException e) {
		} catch (CancelException e) {
		}
		return new Path(cwd);
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
		return RemoteToolsFileSystem.getURIFor(fConnection.getName(), path.toString());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#toURI(java.lang.String)
	 */
	public URI toURI(String path) {
		return toURI(new Path(path));
	}
}
