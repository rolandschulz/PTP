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
import java.io.IOException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.IRemoteResource;
import org.eclipse.ptp.remote.PTPRemotePlugin;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;


public class LocalFileManager implements IRemoteFileManager {
	public LocalFileManager() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#browseFile(org.eclipse.swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	public IPath browseFile(Shell shell, String message, String filterPath) {
		FileDialog dialog = new FileDialog(PTPRemotePlugin.getShell());
		dialog.setText(message);
		if (filterPath != null) {
			File path = new File(filterPath);
			if (path.exists()) {
				dialog.setFilterPath(path.isFile() ? filterPath : path.getParent());
			}
		}
	
		String path = dialog.open();
		if (path == null) {
			return null;
		}

		return new Path(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#browseDirectory(org.eclipse.swt.widgets.Shell, java.lang.String, java.lang.String)
	 */
	public IPath browseDirectory(Shell shell, String message, String filterPath) {
		DirectoryDialog dialog = new DirectoryDialog(PTPRemotePlugin.getShell());
		dialog.setText(message);
		if (filterPath != null) {
			File path = new File(filterPath);
			if (path.exists()) {
				dialog.setFilterPath(path.isFile() ? filterPath : path.getParent());
			}
		}
	
		
		String path = dialog.open();
		if (path == null) {
			return null;
		}
		
		return new Path(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteFileManager#getResource(org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IRemoteResource getResource(IPath path, IProgressMonitor monitor) throws IOException {
		try {
			return new LocalResource(EFS.getLocalFileSystem().getStore(path));
		} finally {
			monitor.done();
		}
	}
}
