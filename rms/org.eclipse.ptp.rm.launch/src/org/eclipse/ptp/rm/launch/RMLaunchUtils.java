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
import org.eclipse.ptp.rm.jaxb.control.LaunchController;
import org.eclipse.ptp.rm.jaxb.ui.util.JAXBExtensionUtils;
import org.eclipse.ptp.rm.launch.internal.ProviderInfo;

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
	public static ILaunchController getLaunchControl(ILaunchConfiguration configuration) throws CoreException {
		String type = LaunchUtils.getTemplateName(configuration);
		if (type != null) {
			ProviderInfo provider = ProviderInfo.getProvider(type);
			if (provider != null) {
				String controlId = LaunchUtils.getResourceManagerUniqueName(configuration);
				ILaunchController control = getLaunchControl(provider.getName(), controlId);
				String name = LaunchUtils.getConnectionName(configuration);
				String id = LaunchUtils.getRemoteServicesId(configuration);
				if (name != null && id != null) {
					control.setConnectionName(name);
					control.setRemoteServicesId(id);
					return control;
				}
			}
		}
		return null;
	}

	/**
	 * @param provider
	 * @param configuration
	 * @return
	 */
	public static ILaunchController getLaunchControl(String name, String controlId) {
		ILaunchController control;
		if (controlId == null) {
			control = new LaunchController();
		} else {
			control = new LaunchController(controlId);
		}
		control.setRMConfigurationURL(JAXBExtensionUtils.getConfigurationURL(name));
		return control;
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
