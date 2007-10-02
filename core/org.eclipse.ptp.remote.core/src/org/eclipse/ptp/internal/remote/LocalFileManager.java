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
package org.eclipse.ptp.internal.remote;

import java.io.File;

import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.PTPRemotePlugin;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;


public class LocalFileManager implements IRemoteFileManager {
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#browseRemoteFile(org.eclipse.swt.widgets.Shell, org.eclipse.ptp.remote.IRemoteConnection, java.lang.String, java.lang.String)
	 */
	public String browseRemoteFile(Shell shell, IRemoteConnection conn, 
			String message, String filterPath) {
		FileDialog dialog = new FileDialog(PTPRemotePlugin.getShell());
		dialog.setText(message);
		if (filterPath != null) {
			File path = new File(filterPath);
			if (path.exists()) {
				dialog.setFilterPath(path.isFile() ? filterPath : path.getParent());
			}
		}
	
		return dialog.open();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#browseRemoteDirectory(org.eclipse.swt.widgets.Shell, org.eclipse.ptp.remote.IRemoteConnection, java.lang.String, java.lang.String)
	 */
	public String browseRemoteDirectory(Shell shell, IRemoteConnection conn,
			String message, String filterPath) {
		DirectoryDialog dialog = new DirectoryDialog(PTPRemotePlugin.getShell());
		dialog.setText(message);
		if (filterPath != null) {
			File path = new File(filterPath);
			if (path.exists()) {
				dialog.setFilterPath(path.isFile() ? filterPath : path.getParent());
			}
		}
	
		return dialog.open();
	}
}
