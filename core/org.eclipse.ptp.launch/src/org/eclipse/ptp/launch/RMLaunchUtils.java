/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.util.LaunchUtils;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.RemoteServices;
import org.eclipse.ptp.rm.jaxb.control.core.ILaunchController;
import org.eclipse.ptp.rm.jaxb.control.core.LaunchControllerManager;

/**
 * @since 7.0
 * 
 */
public class RMLaunchUtils {

	/**
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public static ILaunchController getLaunchController(ILaunchConfiguration configuration) throws CoreException {
		String type = LaunchUtils.getTemplateName(configuration);
		if (type != null) {
			String connName = LaunchUtils.getConnectionName(configuration);
			String remId = LaunchUtils.getRemoteServicesId(configuration);
			if (connName != null && remId != null) {
				return LaunchControllerManager.getInstance().getLaunchController(remId, connName, type);
			}
		}
		return null;
	}

	/**
	 * Get the remote connection that was selected in the resources tab
	 * 
	 * @param configuration
	 *            launch configuration
	 * @param monitor
	 *            progress monitor
	 * @return remote connection or null if it is invalid or not specified
	 * @throws CoreException
	 */
	public static IRemoteConnection getRemoteConnection(ILaunchConfiguration configuration, IProgressMonitor monitor)
			throws CoreException {
		String remId = LaunchUtils.getRemoteServicesId(configuration);
		if (remId != null) {
			IRemoteServices services = RemoteServices.getRemoteServices(remId, monitor);
			if (services != null) {
				String name = LaunchUtils.getConnectionName(configuration);
				if (name != null) {
					return services.getConnectionManager().getConnection(name);
				}
			}
		}
		return null;
	}

	public static IRemoteFileManager getLocalFileManager(ILaunchConfiguration configuration) throws CoreException {
		IRemoteServices localServices = RemoteServices.getLocalServices();
		IRemoteConnectionManager lconnMgr = localServices.getConnectionManager();
		IRemoteConnection lconn = lconnMgr.getConnection(IRemoteConnectionManager.LOCAL_CONNECTION_NAME);
		return localServices.getFileManager(lconn);
	}

	public static IRemoteFileManager getRemoteFileManager(ILaunchConfiguration configuration, IProgressMonitor monitor)
			throws CoreException {
		IRemoteConnection conn = getRemoteConnection(configuration, monitor);
		if (!monitor.isCanceled()) {
			return conn.getRemoteServices().getFileManager(conn);
		}
		return null;
	}

	/**
	 * Constructor
	 */
	public RMLaunchUtils() {
	}
}
