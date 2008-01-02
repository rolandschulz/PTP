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
package org.eclipse.ptp.remote.rse;

import java.io.IOException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFileDialog;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFolderDialog;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.widgets.Shell;

public class RSEFileManager implements IRemoteFileManager {
	private RSEConnection connection;

	public RSEFileManager(RSEConnection conn) {
		this.connection = conn;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#browseDirectory(org.eclipse.swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	public IPath browseDirectory(Shell shell, String message, String filterPath) {
		IHost host = connection.getHost();
		SystemRemoteFolderDialog dlg = new SystemRemoteFolderDialog(shell, message, host);
		dlg.setBlockOnOpen(true);
		if(dlg.open() == Window.OK) {
			Object retObj = dlg.getSelectedObject();
			if(retObj instanceof IRemoteFile) {
				IRemoteFile selectedFile = (IRemoteFile) retObj;
				return new Path(selectedFile.getAbsolutePath());
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#browseFile(org.eclipse.swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	public IPath browseFile(Shell shell, String message, String filterPath) {
		IHost host = connection.getHost();
		SystemRemoteFileDialog dlg = new SystemRemoteFileDialog(shell, message, host);
		dlg.setBlockOnOpen(true);
		if(dlg.open() == Window.OK) {
			Object retObj = dlg.getSelectedObject();
			if(retObj instanceof IRemoteFile) {
				IRemoteFile selectedFile = (IRemoteFile) retObj;
				return new Path(selectedFile.getAbsolutePath());
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#getResource(org.eclipse.core.runtime.IPath)
	 */
	public IFileStore getResource(IPath path, IProgressMonitor monitor) throws IOException {
		return connection.getFileSystem().getStore(connection.toURI(path));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#getWorkingDirectory(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IPath getWorkingDirectory() {
		return new Path("/"); // TODO: RSE doesn't provide any way to get this
	}
}