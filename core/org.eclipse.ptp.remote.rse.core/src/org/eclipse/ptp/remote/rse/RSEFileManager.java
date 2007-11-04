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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.eclipse.swt.widgets.Display;
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
	public IRemoteResource getResource(IPath path, IProgressMonitor monitor) throws IOException {
		IRemoteResource res = lookup(path);
		if (res != null) {
			return res;
		}
		
		IRemoteFileSubSystem fileSub = getConnectedRemoteFileSubsystem();
		if (fileSub == null) {
			throw new IOException("No connected file subsystem found!");
		}
			
		IRemoteFile file;
		try {
			file = fileSub.getRemoteFileObject(path.toString(), monitor);
		} catch (SystemMessageException e) {
			throw new IOException("Could not get remote resource: " + e.getMessage());
		}
		
		res = new RSEResource(this, file);
		cache(path, res);
		return res;
	}

	/**
	 * @return
	 */
	private IRemoteFileSubSystem getConnectedRemoteFileSubsystem() {
		IRemoteFileSubSystem subSystem = null;
		IHost currentConnection = connection.getHost();
		if (currentConnection != null) {
			ISubSystem[] subSystems = currentConnection.getSubSystems();
			for (ISubSystem sub : subSystems) {
				if (sub instanceof IRemoteFileSubSystem) {
					subSystem = (IRemoteFileSubSystem)sub;
					break;
				}
			}
			
			if (subSystem != null) {
				final ISubSystem ss = subSystem;
				// Need to run this in the UI thread
				Display.getDefault().syncExec(new Runnable()
				{
					public void run()
					{	try {
							ss.connect(new NullProgressMonitor(), false);
						} catch (Exception e) {
							// Ignore
							e.printStackTrace();
						}
					}
				});
				
				if(!subSystem.isConnected()) {
					return null;
				}
			}
		}
		return subSystem;
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
