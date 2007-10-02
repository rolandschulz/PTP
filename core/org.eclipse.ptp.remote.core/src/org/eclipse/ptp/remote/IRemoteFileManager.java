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

import org.eclipse.swt.widgets.Shell;

public interface IRemoteFileManager {
	/**
	 * Browse for a remote file. The return value is the the path to the remote file,
	 * on the remote system. Equivalent to {@link FileDialog}.
	 * 
	 * @param shell workbench shell
	 * @param conn remote connection to use for browsing
	 * @param message message to display in dialog
	 * @param filterPath initial path to use when displaying files
	 * @return a string representing the path to the file
	 */
	public String browseRemoteFile(Shell shell, IRemoteConnection conn, 
			String message, String filterPath);

	/**
	 * Browse for a remote directory. The return value is the the path to the remote directory,
	 * on the remote system. Equivalent to {@link DirectoryDialog}.
	 * 
	 * @param shell workbench shell
	 * @param conn remote connection to use for browsing
	 * @param message message to display in dialog
	 * @param filterPath initial path to use when displaying files
	 * @return a string representing the path to the directory
	 */
	public String browseRemoteDirectory(Shell shell, IRemoteConnection conn, 
			String message, String filterPath);

}
