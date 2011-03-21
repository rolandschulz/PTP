/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.ui.rservices;

import org.eclipse.jface.window.Window;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.dialogs.RemoteResourceBrowser;
import org.eclipse.swt.widgets.Shell;

/**
 * GUI control allowing file browsing, selecting a single/multiple
 * file/directory.
 * 
 * Current implementation does not provide selecting connection.
 */
public class SMOAUIFileManager implements IRemoteUIFileManager {
	private IRemoteServices services = null;
	private IRemoteConnection connection = null;
	private boolean showConnections = false;

	public SMOAUIFileManager(IRemoteServices services) {
		this.services = services;
	}

	public String browseDirectory(Shell shell, String message,
			String filterPath, int flags) {
		final RemoteResourceBrowser browser = new RemoteResourceBrowser(services,
				connection, shell, RemoteResourceBrowser.SINGLE);
		browser.setType(RemoteResourceBrowser.DIRECTORY_BROWSER);
		browser.setInitialPath(filterPath);
		browser.showConnections(showConnections);
		if (browser.open() == Window.CANCEL) {
			return null;
		}
		connection = browser.getConnection();
		final String path = browser.getPath();
		if (path == null) {
			return null;
		}
		return path;
	}

	public String browseFile(Shell shell, String message, String filterPath,
			int flags) {
		final RemoteResourceBrowser browser = new RemoteResourceBrowser(services,
				connection, shell, RemoteResourceBrowser.SINGLE);
		browser.setType(RemoteResourceBrowser.FILE_BROWSER);
		browser.setInitialPath(filterPath);
		browser.showConnections(showConnections);
		if (browser.open() == Window.CANCEL) {
			return null;
		}
		connection = browser.getConnection();
		final String path = browser.getPath();
		if (path == null) {
			return null;
		}
		return path;
	}

	public String[] browseFiles(Shell shell, String message, String filterPath,
			int flags) {
		final RemoteResourceBrowser browser = new RemoteResourceBrowser(services,
				connection, shell, RemoteResourceBrowser.MULTI);
		browser.setType(RemoteResourceBrowser.FILE_BROWSER);
		browser.setInitialPath(filterPath);
		browser.showConnections(showConnections);
		if (browser.open() == Window.CANCEL) {
			return null;
		}
		connection = browser.getConnection();
		final String path[] = browser.getPaths();
		if (path == null) {
			return null;
		}
		return path;
	}

	public IRemoteConnection getConnection() {
		return connection;
	}

	public void setConnection(IRemoteConnection connection) {
		this.connection = connection;
	}

	public void showConnections(boolean enable) {
		showConnections = enable;
	}
}
