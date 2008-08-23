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
package org.eclipse.ptp.remote.rse.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.rse.core.RSEConnection;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFileDialog;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFolderDialog;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.widgets.Shell;

public class RSEUIFileManager implements IRemoteUIFileManager {
	private IRemoteConnectionManager connMgr;
	private IRemoteConnection connection;
	private IHost connHost;

	public RSEUIFileManager(IRemoteServices services, RSEConnection conn) {
		this.connMgr = services.getConnectionManager();
		this.connection = conn;
		this.connHost = conn.getHost();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#browseDirectory(org.eclipse.swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	public IPath browseDirectory(Shell shell, String message, String filterPath) {
		SystemRemoteFolderDialog dlg = new SystemRemoteFolderDialog(shell, message, connHost);
		dlg.setBlockOnOpen(true);
		if(dlg.open() == Window.OK) {
			connHost = dlg.getSelectedConnection();
			connection = connMgr.getConnection(connHost.getName());
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
		SystemRemoteFileDialog dlg = new SystemRemoteFileDialog(shell, message, connHost);
		dlg.setBlockOnOpen(true);
		if(dlg.open() == Window.OK) {
			connHost = dlg.getSelectedConnection();
			connection = connMgr.getConnection(connHost.getName());
			Object retObj = dlg.getSelectedObject();
			if(retObj instanceof IRemoteFile) {
				IRemoteFile selectedFile = (IRemoteFile) retObj;
				return new Path(selectedFile.getAbsolutePath());
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIFileManager#getConnection()
	 */
	public IRemoteConnection getConnection() {
		return connection;
	}
}