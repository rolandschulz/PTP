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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.IRemoteResource;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFileDialog;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFolderDialog;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.swt.widgets.Shell;

public class RSEFileManager implements IRemoteFileManager {
	private RSEConnection connection;
	private Map<IPath, IRemoteResource> pathCache = new HashMap<IPath, IRemoteResource>();
	
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
	public IRemoteResource getResource(IPath path, IProgressMonitor monitor) {
		ISubSystem[] subs = connection.getHost().getSubSystems();
		for (ISubSystem sub : subs) {
			if (sub instanceof IRemoteFileSubSystem) {
				IRemoteFileSubSystem fileSub = (IRemoteFileSubSystem)sub;
				IRemoteFile file;
				try {
					file = fileSub.getRemoteFileObject(path.toString(), monitor);
				} catch (SystemMessageException e) {
					return null;
				}
				if (file != null) {
					IRemoteResource res = lookup(path);
					if (res == null) {
						res = new RSEResource(this, file);
						cache(path, res);
					}
					return res;
				}
			}
		}
		return null;
	}

	/**
	 * Look up a cached IRemoteResource
	 * 
	 * @param path path to the remote resource
	 * @return cached IRemoteResource or null
	 */
	public IRemoteResource lookup(IPath path) {
		synchronized (pathCache) {
			return pathCache.get(path);
		}
	}
	
	/**
	 * Store an IRemoteResource in the cache
	 * 
	 * @param path path to the remote resource
	 * @param resource resource to add to the cache
	 */
	public void cache(IPath path, IRemoteResource resource) {
		synchronized (pathCache) {
			pathCache.put(path, resource);
		}
	}
}
