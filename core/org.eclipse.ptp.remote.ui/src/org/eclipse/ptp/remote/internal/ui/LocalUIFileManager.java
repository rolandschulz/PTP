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
package org.eclipse.ptp.remote.internal.ui;

import java.io.File;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class LocalUIFileManager implements IRemoteUIFileManager {
	private IRemoteConnection connection = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteFileManager#browseDirectory(org.eclipse
	 * .swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	public String browseDirectory(Shell shell, String message,
			String filterPath, int flags) {
		DirectoryDialog dialog = new DirectoryDialog(PTPRemoteUIPlugin
				.getShell());
		dialog.setText(message);
		if (filterPath != null) {
			File path = new File(filterPath);
			if (path.exists()) {
				dialog.setFilterPath(path.isFile() ? path.getParent()
						: filterPath);
			}
		}

		String path = dialog.open();
		if (path == null) {
			return null;
		}

		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteFileManager#browseFile(org.eclipse
	 * .swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	public String browseFile(Shell shell, String message, String filterPath,
			int flags) {
		FileDialog dialog = new FileDialog(PTPRemoteUIPlugin.getShell(),
				SWT.SINGLE);
		dialog.setText(message);
		if (filterPath != null) {
			File path = new File(filterPath);
			if (path.exists()) {
				dialog.setFilterPath(path.isFile() ? path.getParent()
						: filterPath);
			}
		}

		String path = dialog.open();
		if (path == null) {
			return null;
		}

		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteFileManager#browseFile(org.eclipse
	 * .swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	public String[] browseFiles(Shell shell, String message, String filterPath,
			int flags) {
		FileDialog dialog = new FileDialog(PTPRemoteUIPlugin.getShell(),
				SWT.MULTI);
		dialog.setText(message);
		if (filterPath != null) {
			File path = new File(filterPath);
			if (path.exists()) {
				dialog.setFilterPath(path.isFile() ? path.getParent()
						: filterPath);
			}
		}

		String path = dialog.open();
		if (path == null) {
			return null;
		}

		return dialog.getFileNames();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIFileManager#getConnection()
	 */
	public IRemoteConnection getConnection() {
		return connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.ui.IRemoteUIFileManager#setConnection(org.eclipse
	 * .ptp.remote.core.IRemoteConnection)
	 */
	public void setConnection(IRemoteConnection connection) {
		this.connection = connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.ui.IRemoteUIFileManager#showConnections(boolean)
	 */
	public void showConnections(boolean enable) {
	}
}
