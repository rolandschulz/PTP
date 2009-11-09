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
package org.eclipse.ptp.remote.remotetools.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.IRemoteServicesDescriptor;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
import org.eclipse.ptp.remotetools.environment.core.TargetEnvironmentManager;
import org.eclipse.ptp.remotetools.environment.core.TargetTypeElement;


public class RemoteToolsServices implements IRemoteServices {
	private static String TARGET_ELEMENT_NAME = "PTP Remote Host"; //$NON-NLS-1$
	private static String REMOTE_TOOLS_ID = "org.eclipse.ptp.remote.RemoteTools"; //$NON-NLS-1$
	
	private static RemoteToolsServices instance = null;
	
	private RemoteToolsConnectionManager connMgr = null;
	private Map<String, RemoteToolsFileManager> fileMgrs = new HashMap<String, RemoteToolsFileManager>();
	
	/**
	 * Get shared instance of this class
	 * 
	 * @return instance
	 */
	public static RemoteToolsServices getInstance() {
		if (instance == null) {
			IRemoteServicesDescriptor descriptor = PTPRemoteCorePlugin.getDefault().getRemoteServicesDescriptor(REMOTE_TOOLS_ID);
			instance = new RemoteToolsServices(descriptor);
		}
		return instance;
	}
	
	/**
	 * Find the target type element for the PTP remote services target type.
	 * 
	 * @return PTP target type element or null if none can be found (shouldn't happen)
	 */
	public static TargetTypeElement getTargetTypeElement() {
		TargetEnvironmentManager targetMgr = EnvironmentPlugin.getDefault().getTargetsManager();
		for (Object obj : targetMgr.getTypeElements()) {
			TargetTypeElement element = (TargetTypeElement)obj;
			if (element.getName().equals(TARGET_ELEMENT_NAME)) {
				return element;
			}
		}
		return null;
	}
	
	private final IRemoteServicesDescriptor fDescriptor;
	
	public RemoteToolsServices(IRemoteServicesDescriptor descriptor) {
		fDescriptor = descriptor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#getConnectionManager()
	 */
	public IRemoteConnectionManager getConnectionManager() {
		return connMgr;
	}

	public String getDirectorySeparator(IRemoteConnection conn) {
		// dunno if there is a way to do this for Remote Tools... just return the forward slash
		return "/"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#getFileManager(org.eclipse.ptp.remote.core.IRemoteConnection)
	 */
	public IRemoteFileManager getFileManager(IRemoteConnection conn) {
		RemoteToolsFileManager fileMgr = fileMgrs.get(conn.getName());
		if (fileMgr == null) {
			fileMgr = new RemoteToolsFileManager((RemoteToolsConnection)conn);
			fileMgrs.put(conn.getName(), fileMgr);
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
		return new RemoteToolsProcessBuilder((RemoteToolsConnection)conn, command);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#getProcessBuilder(org.eclipse.ptp.remote.core.IRemoteConnection, java.lang.String[])
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, String... command) {
		return new RemoteToolsProcessBuilder((RemoteToolsConnection)conn, command);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#getScheme()
	 */
	public String getScheme() {
		return fDescriptor.getScheme();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#initialize()
	 */
	public void initialize() {
		if (connMgr == null) {
			connMgr = new RemoteToolsConnectionManager();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteServicesDescriptor#isInitialized()
	 */
	public boolean isInitialized() {
		initialize();
		return connMgr != null;
	}
}
