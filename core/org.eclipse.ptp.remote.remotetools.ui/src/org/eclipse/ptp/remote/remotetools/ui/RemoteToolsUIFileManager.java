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
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.dialogs.RemoteResourceBrowser;
import org.eclipse.swt.widgets.Shell;

public class RemoteToolsUIFileManager implements IRemoteUIFileManager {
	private IRemoteFileManager fileMgr;
	
	public RemoteToolsUIFileManager(IRemoteFileManager fileMgr) {
		this.fileMgr = fileMgr;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#browseDirectory(org.eclipse.swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	public IPath browseDirectory(Shell shell, String message, String filterPath) {
		RemoteResourceBrowser browser = new RemoteResourceBrowser(fileMgr, shell);
		browser.setType(RemoteResourceBrowser.DIRECTORY_BROWSER);
		browser.setInitialPath(filterPath);
		if (browser.open() == Window.CANCEL) {
			return null;
		}
		IPath path = browser.getPath();
		if (path == null) {
			return null;
		}
		return path;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteFileManager#browseFile(org.eclipse.swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	public IPath browseFile(Shell shell, String message, String filterPath) {
		RemoteResourceBrowser browser = new RemoteResourceBrowser(fileMgr, shell);
		browser.setType(RemoteResourceBrowser.FILE_BROWSER);
		browser.setInitialPath(filterPath);
		if (browser.open() == Window.CANCEL) {
			return null;
		}
		IPath path = browser.getPath();
		if (path == null) {
			return null;
		}
		return path;
	}
}
