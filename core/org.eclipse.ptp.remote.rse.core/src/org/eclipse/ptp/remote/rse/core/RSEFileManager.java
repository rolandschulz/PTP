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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.servicesubsystem.IFileServiceSubSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;

public class RSEFileManager implements IRemoteFileManager {
	private IPath fWorkingDir = null;
	private final RSEConnection fConnection;

	public RSEFileManager(RSEConnection conn) {
		fConnection = conn;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#getResource(java.lang.String)
	 */
	public IFileStore getResource(String pathStr) {
		IPath path = new Path(pathStr);
		if (!path.isAbsolute()) {
			path = fWorkingDir.append(path);
		}
		return fConnection.getFileSystem().getStore(toURI(path));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#getWorkingDirectory(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public String getWorkingDirectory() {
		if (fWorkingDir == null) {
			IFileServiceSubSystem fileService = getFileServiceSubSystem(fConnection.getHost());
			if (fileService != null) {
				fWorkingDir = new Path(fileService.getFileService().getUserHome().getAbsolutePath());
			} else {
				fWorkingDir = new Path("/"); //$NON-NLS-1$
			}
		}
		return fWorkingDir.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#setWorkingDirectory(java.lang.String)
	 */
	public void setWorkingDirectory(String pathStr) {
		IPath path = new Path(pathStr);
		if (path.isAbsolute()) {
			fWorkingDir = path;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#toPath(java.net.URI)
	 */
	public String toPath(URI uri) {
		return uri.getPath();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#toURI(org.eclipse.core.runtime.IPath)
	 */
	public URI toURI(IPath path) {
		String authority = fConnection.getHost().getHostName();
		try {
			authority = URLEncoder.encode(authority, "UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			// Should not happen
		}
		try {
			return new URI("rse", authority, path.makeAbsolute().toPortableString(), null, null); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#toURI(java.lang.String)
	 */
	public URI toURI(String path) {
		return toURI(new Path(path));
	}
	
	/**
	 * Find the file service subsystem from the IHost
	 * 
	 * @param host
	 * @return
	 */
	private IFileServiceSubSystem getFileServiceSubSystem(IHost host) {
		IRemoteFileSubSystem[] fileSubsystems = RemoteFileUtility.getFileSubSystems(host);
		for(IRemoteFileSubSystem subsystem : fileSubsystems) {
			if(subsystem instanceof IFileServiceSubSystem && subsystem.isConnected()) {
				return (IFileServiceSubSystem) subsystem;
			}
		}
		return null;
	}
}