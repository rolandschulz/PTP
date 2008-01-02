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
package org.eclipse.ptp.remote;

import java.io.IOException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;

public interface IRemoteFileManager {
	/**
	 * Browse for a remote file. The return value is the path of the file
	 * <i>on the remote system</i>. 
	 * 
	 * Equivalent to {@link org.eclipse.swt.widgets.FileDialog}.
	 * 
	 * @param shell workbench shell
	 * @param message message to display in dialog
	 * @param initialPath initial path to use when displaying files
	 * @return the path to the file relative to the remote system
	 */
	public IPath browseFile(Shell shell, String message, String initialPath);

	/**
	 * Browse for a remote directory. The return value is the path of the directory
	 * <i>on the remote system</i>.
	 * 
	 * Equivalent to {@link org.eclipse.swt.widgets.DirectoryDialog}.
	 * 
	 * @param shell workbench shell
	 * @param message message to display in dialog
	 * @param initialPath initial path to use when displaying files
	 * @return the path to the directory relative to the remote system
	 */
	public IPath browseDirectory(Shell shell, String message, String initialPath);

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
}
