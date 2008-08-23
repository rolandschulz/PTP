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
package org.eclipse.ptp.remote.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.swt.widgets.Shell;

public interface IRemoteUIFileManager {
	/**
	 * Browse for a remote directory. The return value is the path of the directory
	 * <i>on the remote system</i>.
	 * 
	 * Equivalent to {@link org.eclipse.swt.widgets.DirectoryDialog}.
	 * 
	 * @param shell workbench shell
	 * @param message message to display in dialog
	 * @param initialPath initial path to use when displaying files
	 * @return the path to the directory relative to the remote system or null if the browser was cancelled
	 */
	public IPath browseDirectory(Shell shell, String message, String initialPath);

	/**
	 * Browse for a remote file. The return value is the path of the file
	 * <i>on the remote system</i>. 
	 * 
	 * Equivalent to {@link org.eclipse.swt.widgets.FileDialog}.
	 * 
	 * @param shell workbench shell
	 * @param message message to display in dialog
	 * @param initialPath initial path to use when displaying files
	 * @return the path to the file relative to the remote system or null if the browser was cancelled
	 */
	public IPath browseFile(Shell shell, String message, String initialPath);
	
	/**
	 * Get the last connection that was selected in the browser.
	 * 
	 * @return selected connection
	 */
	public IRemoteConnection getConnection();
}
