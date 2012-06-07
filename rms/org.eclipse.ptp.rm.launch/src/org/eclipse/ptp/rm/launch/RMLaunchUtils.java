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
package org.eclipse.ptp.rm.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.util.LaunchUtils;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rm.jaxb.control.ILaunchController;
import org.eclipse.ptp.rm.jaxb.control.LaunchControllerManager;

/**
 * @since 6.0
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
			IRemoteServices services = PTPRemoteCorePlugin.getDefault().getRemoteServices(remId, monitor);
			if (services != null) {
				String name = LaunchUtils.getConnectionName(configuration);
				if (name != null) {
					return services.getConnectionManager().getConnection(name);
				}
			}
		}
		return null;
	}

	/**
	 * Constructor
	 */
	public RMLaunchUtils() {
	}
}
