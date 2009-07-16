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
package org.eclipse.ptp.remote.internal.core;

import java.io.IOException;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.remote.core.IRemoteFileManager;

public class LocalFileManager implements IRemoteFileManager {
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#getResource(org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IFileStore getResource(IPath path, IProgressMonitor monitor) throws IOException {
		try {
			return EFS.getLocalFileSystem().getStore(path);
		} finally {
			monitor.done();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#getWorkingDirectory(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IPath getWorkingDirectory() {
		return new Path(System.getProperty("user.dir")); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#toPath(java.net.URI)
	 */
	public IPath toPath(URI uri) {
		return URIUtil.toPath(uri);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#toURI(org.eclipse.core.runtime.IPath)
	 */
	public URI toURI(IPath path) {
		return URIUtil.toURI(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#toURI(java.lang.String)
	 */
	public URI toURI(String path) {
		return URIUtil.toURI(path);
	}
}
