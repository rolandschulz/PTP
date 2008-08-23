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
package org.eclipse.ptp.remote.remotetools.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.dialogs.RemoteResourceBrowser;
import org.eclipse.swt.widgets.Shell;

public class RemoteToolsUIFileManager implements IRemoteUIFileManager {
	private IRemoteServices services = null;
	private IRemoteConnection connection = null;
	private boolean showConnections;
	
	public RemoteToolsUIFileManager(IRemoteServices services, IRemoteConnection conn) {
		this.services = services;
		this.connection = conn;
		showConnections(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIFileManager#showConnections(boolean)
	 */
	public void showConnections(boolean enable) {
		/*
		 * Force connection list if no connection supplied
		 */
		showConnections = enable || (connection == null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIFileManager#getConnection()
	 */
	public IRemoteConnection getConnection() {
		return connection;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#browseDirectory(org.eclipse.swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	public IPath browseDirectory(Shell shell, String message, String filterPath) {
		RemoteResourceBrowser browser = new RemoteResourceBrowser(services, connection, shell);
		browser.setType(RemoteResourceBrowser.DIRECTORY_BROWSER);
		browser.setInitialPath(filterPath);
		browser.showConnections(showConnections);
		if (browser.open() == Window.CANCEL) {
			return null;
		}
		connection = browser.getConnection();
		String path = browser.getPath();
		if (path == null) {
			return null;
		}
		return new Path(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#browseFile(org.eclipse.swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	public IPath browseFile(Shell shell, String message, String filterPath) {
		RemoteResourceBrowser browser = new RemoteResourceBrowser(services, connection, shell);
		browser.setType(RemoteResourceBrowser.FILE_BROWSER);
		browser.setInitialPath(filterPath);
		browser.showConnections(showConnections);
		if (browser.open() == Window.CANCEL) {
			return null;
		}
		connection = browser.getConnection();
		String path = browser.getPath();
		if (path == null) {
			return null;
		}
		return new Path(path);
	}
}
