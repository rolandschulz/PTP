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
package org.eclipse.ptp.internal.remote.rse.ui;

import java.util.Vector;

import org.eclipse.jface.window.Window;
import org.eclipse.ptp.internal.remote.rse.core.RSEConnection;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFileDialog;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFolderDialog;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.widgets.Shell;

public class RSEUIFileManager implements IRemoteUIFileManager {
	private final IRemoteConnectionManager connMgr;
	private IRemoteConnection connection = null;
	private IHost connHost = null;
	private boolean onlyConnection = true;

	public RSEUIFileManager(IRemoteServices services) {
		this.connMgr = services.getConnectionManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#browseDirectory(org.eclipse .swt.widgets.Shell, java.lang.String,
	 * java.lang.String)
	 */
	public String browseDirectory(Shell shell, String message, String filterPath, int flags) {
		SystemRemoteFolderDialog dlg = new SystemRemoteFolderDialog(shell, message, connHost);
		dlg.setDefaultSystemConnection(connHost, onlyConnection);
		dlg.setBlockOnOpen(true);
		if (dlg.open() == Window.OK) {
			connHost = dlg.getSelectedConnection();
			connection = connMgr.getConnection(connHost.getName());
			Object retObj = dlg.getSelectedObject();
			if (retObj instanceof IRemoteFile) {
				IRemoteFile selectedFile = (IRemoteFile) retObj;
				return selectedFile.getAbsolutePath();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#browseFile(org.eclipse.swt. widgets.Shell, java.lang.String, java.lang.String)
	 */
	public String browseFile(Shell shell, String message, String filterPath, int flags) {
		SystemRemoteFileDialog dlg = new SystemRemoteFileDialog(shell, message, connHost);
		dlg.setDefaultSystemConnection(connHost, onlyConnection);
		dlg.setBlockOnOpen(true);
		if (dlg.open() == Window.OK) {
			connHost = dlg.getSelectedConnection();
			connection = connMgr.getConnection(connHost.getName());
			Object retObj = dlg.getSelectedObject();
			if (retObj instanceof IRemoteFile) {
				IRemoteFile selectedFile = (IRemoteFile) retObj;
				return selectedFile.getAbsolutePath();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#browseFile(org.eclipse.swt. widgets.Shell, java.lang.String, java.lang.String)
	 */
	public String[] browseFiles(Shell shell, String message, String filterPath, int flags) {
		SystemRemoteFileDialog dlg = new SystemRemoteFileDialog(shell, message, connHost);
		dlg.setDefaultSystemConnection(connHost, onlyConnection);
		dlg.setBlockOnOpen(true);
		dlg.setMultipleSelectionMode(true);
		if (dlg.open() == Window.OK) {
			connHost = dlg.getSelectedConnection();
			connection = connMgr.getConnection(connHost.getName());
			Object retObj[] = dlg.getSelectedObjects();
			Vector<String> selections = new Vector<String>(retObj.length);
			for (int i = 0; i < retObj.length; i++) {
				if (retObj[i] instanceof IRemoteFile) {
					selections.add(((IRemoteFile) retObj[i]).getAbsolutePath());
				}
			}
			String remotePaths[] = new String[selections.size()];
			int i = 0;
			for (String s : selections) {
				remotePaths[i++] = s;
			}
			return remotePaths;
		}
		return null;
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
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIFileManager#setConnection(org.eclipse .ptp.remote.core.IRemoteConnection)
	 */
	public void setConnection(IRemoteConnection connection) {
		this.connection = connection;
		this.connHost = ((RSEConnection) connection).getHost();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIFileManager#showConnections(boolean)
	 */
	public void showConnections(boolean enable) {
		onlyConnection = !enable;
	}
}