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
package org.eclipse.ptp.remote.core;

import java.io.IOException;
import java.net.URI;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IRemoteFileManager {
	/**
	 * Get the resource associated with path. IFileStore can then
	 * be used to perform operations on the file.
	 * 
	 * @param path path to resource
	 * @param monitor progress monitor
	 * @return the file store representing the remote path
	 * @throws IOException if the associated resource cannot be located
	 */
	public IFileStore getResource(IPath path, IProgressMonitor monitor) throws IOException;
	
	/**
	 * Get the working directory. Relative paths will be resolved using this path.
	 * 
	 * @return IPath representing the current working directory
	 */
	public IPath getWorkingDirectory();
	
	/**
	 * Convert URI to a remote path. This path is suitable for
	 * direct file operations <i>on the remote system</i>.
	 * 
	 * @return IPath representing the remote path
	 */
	public IPath toPath(URI uri);
	
	/**
	 * Convert remote path to equivalent URI. This URI is suitable
	 * for EFS operations <i>on the local system</i>.
	 * 
	 * @param path path on remote system
	 * @return URI representing path on remote system
	 */
	public URI toURI(IPath path);
	
	/**
	 * Convert string representation of a remote path to equivalent URI. This URI is suitable
	 * for EFS operations <i>on the local system</i>.
	 * 
	 * @param path path on remote system
	 * @return URI representing path on remote system
	 */
	public URI toURI(String path);
}
