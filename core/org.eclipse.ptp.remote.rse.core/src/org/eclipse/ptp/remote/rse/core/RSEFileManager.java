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
package org.eclipse.ptp.remote.rse.core;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.remote.core.IRemoteFileManager;

public class RSEFileManager implements IRemoteFileManager {
	private RSEConnection connection;

	public RSEFileManager(RSEConnection conn) {
		this.connection = conn;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#getResource(org.eclipse.core.runtime.IPath)
	 */
	public IFileStore getResource(IPath path, IProgressMonitor monitor) throws IOException {
		return connection.getFileSystem().getStore(toURI(path));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#getWorkingDirectory(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IPath getWorkingDirectory() {
		return new Path("/"); // TODO: RSE doesn't provide any way to get this //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#toPath(java.net.URI)
	 */
	public IPath toPath(URI uri) {
		return new Path(uri.getPath());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#toURI(org.eclipse.core.runtime.IPath)
	 */
	public URI toURI(IPath path) {
		try {
			return new URI("rse", connection.getHost().getHostName(), path.toPortableString(), null); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#toURI(java.lang.String)
	 */
	public URI toURI(String path) {
		try {
			return new URI("rse", connection.getHost().getHostName(), path, null); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			return null;
		}
	}
}