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
package org.eclipse.ptp.internal.remote.rse.core;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.rse.core.IRSESystemType;

public class RSEFileManager implements IRemoteFileManager {
	private final RSEConnection fConnection;

	public RSEFileManager(RSEConnection conn) {
		fConnection = conn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteFileManager#getDirectorySeparator()
	 */
	/**
	 * @since 4.0
	 */
	public String getDirectorySeparator() {
		IRSESystemType systemType = fConnection.getHost().getSystemType();

		if (systemType.isWindows()) {
			return "\\"; //$NON-NLS-1$
		} else {
			return "/"; //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteFileManager#getResource(java.lang.
	 * String)
	 */
	public IFileStore getResource(String pathStr) {
		IPath path = new Path(pathStr);
		if (!path.isAbsolute()) {
			path = new Path(fConnection.getWorkingDirectory()).append(path);
		}
		try {
			return EFS.getFileSystem("rse").getStore(toURI(path)); //$NON-NLS-1$
		} catch (CoreException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#toPath(java.net.URI)
	 */
	public String toPath(URI uri) {
		return uri.getPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.IRemoteFileManager#toURI(org.eclipse.core.runtime
	 * .IPath)
	 */
	public URI toURI(IPath path) {
		try {
			return new URI("rse", fConnection.getHost().getHostName(), path.toString(), fConnection.getHost().getAliasName(), null); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteFileManager#toURI(java.lang.String)
	 */
	public URI toURI(String path) {
		return toURI(new Path(path));
	}
}