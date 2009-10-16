/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Kushal Munir (IBM) - moved to internal package.
 * Martin Oberhuber (Wind River) - [181917] EFS Improvements: Avoid unclosed Streams,
 *    - Fix early startup issues by deferring FileStore evaluation and classloading,
 *    - Improve performance by RSEFileStore instance factory and caching IRemoteFile.
 *    - Also remove unnecessary class RSEFileCache and obsolete branding files.
 * Martin Oberhuber (Wind River) - [188360] renamed from plugin org.eclipse.rse.eclipse.filesystem
 * Martin Oberhuber (Wind River) - [189441] fix EFS operations on Windows (Local) systems
 * David Dykstal (IBM) - [235840] externalizing dialog title
 ********************************************************************************/

package org.eclipse.ptp.remote.remotetools.ui;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.remotetools.core.RemoteToolsAdapterCorePlugin;
import org.eclipse.ptp.remote.remotetools.core.RemoteToolsFileSystem;
import org.eclipse.ptp.remote.remotetools.ui.messages.Messages;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ide.fileSystem.FileSystemContributor;

public class RemoteToolsFileSystemContributor extends FileSystemContributor {
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ide.fileSystem.FileSystemContributor#browseFileSystem(java.lang.String, org.eclipse.swt.widgets.Shell)
	 */
	public URI browseFileSystem(String initialPath, Shell shell) {
		IRemoteServices services = PTPRemoteCorePlugin.getDefault()
				.getRemoteServices(RemoteToolsAdapterCorePlugin.SERVICES_ID);
		IRemoteUIServices uiServices = PTPRemoteUIPlugin.getDefault()
				.getRemoteUIServices(services);
		IRemoteUIFileManager uiFileMgr = uiServices.getUIFileManager();
		uiFileMgr.showConnections(true);
		String path = uiFileMgr.browseDirectory(shell,
				Messages.RemoteToolsFileSystemContributor_0, initialPath, 0);
		if (path != null) {
			IRemoteConnection conn = uiFileMgr.getConnection();
			return RemoteToolsFileSystem.getURIFor(conn.getName(), path);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.ide.fileSystem.FileSystemContributor#getURI(java.lang.
	 * String)
	 */
	@Override
	public URI getURI(String string) {
		try {
			return new URI(string);
		} catch (URISyntaxException e) {
		}
		return null;
	}
}