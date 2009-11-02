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
package org.eclipse.ptp.remote.internal.core;

import java.util.List;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.IRemoteServicesDescriptor;

public class LocalServices implements IRemoteServices {
	public static final String LocalServicesId = "org.eclipse.ptp.remote.LocalServices"; //$NON-NLS-1$

	private IRemoteConnectionManager connMgr = new LocalConnectionManager();
	private IRemoteFileManager fileMgr = new LocalFileManager();
	
	private final IRemoteServicesDescriptor fDescriptor;
	
	public LocalServices(IRemoteServicesDescriptor descriptor) {
		fDescriptor = descriptor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#getConnectionManager()
	 */
	public IRemoteConnectionManager getConnectionManager() {
		return connMgr;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#getDirectorySeparator(org.eclipse.ptp.remote.core.IRemoteConnection)
	 */
	public String getDirectorySeparator(IRemoteConnection conn) {
		return System.getProperty("file.separator", "/"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#getFileManager(org.eclipse.ptp.remote.core.IRemoteConnection)
	 */
	public IRemoteFileManager getFileManager(IRemoteConnection conn) {
		if (!(conn instanceof LocalConnection)) {
			return null;
		}
		return fileMgr;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#getId()
	 */
	public String getId() {
		return fDescriptor.getId();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#getName()
	 */
	public String getName() {
		return fDescriptor.getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#getProcessBuilder(org.eclipse.ptp.remote.core.IRemoteConnection, java.util.List)
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, List<String>command) {
		return new LocalProcessBuilder(conn, command);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#getProcessBuilder(org.eclipse.ptp.remote.core.IRemoteConnection, java.lang.String[])
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, String... command) {
		return new LocalProcessBuilder(conn, command);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#getScheme()
	 */
	public String getScheme() {
		return fDescriptor.getScheme();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#getServicesExtension(org.eclipse.ptp.remote.core.IRemoteConnection, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getServicesExtension(IRemoteConnection conn, Class extension) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#initialize()
	 */
	public void initialize() {
		// No initialization to do
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#isInitialized()
	 */
	public boolean isInitialized() {
		return true;
	}
}
