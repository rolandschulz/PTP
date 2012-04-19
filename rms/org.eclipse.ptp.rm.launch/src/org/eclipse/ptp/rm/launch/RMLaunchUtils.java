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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rm.jaxb.control.IJAXBLaunchControl;
import org.eclipse.ptp.rm.jaxb.control.JAXBLaunchControl;
import org.eclipse.ptp.rm.jaxb.ui.util.JAXBExtensionUtils;
import org.eclipse.ptp.rm.launch.internal.ProviderInfo;

/**
 * @since 6.0
 * 
 */
public class RMLaunchUtils {

	private static Map<String, URL> fJAXBConfigurations = null;

	/**
	 * @param configuration
	 * @return
	 */
	public static String getConnectionName(ILaunchConfiguration configuration) {
		try {
			return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_CONNECTION_NAME, (String) null);
		} catch (Exception e) {
			return null;
		}
	}

	public static String getControlId(ILaunchConfiguration configuration) {
		final String type;
		try {
			type = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME, (String) null);
		} catch (CoreException e) {
			return null;
		}
		return type;
	}

	/**
	 * Looks up the XML configuration and returns its location
	 * 
	 * @param name
	 * @return URL of the configuration
	 */
	public static URL getJAXBConfigurationURL(String name) {
		loadJAXBResourceManagers(false);
		if (fJAXBConfigurations != null) {
			return fJAXBConfigurations.get(name);
		}
		return null;
	}

	/**
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public static IJAXBLaunchControl getLaunchControl(ILaunchConfiguration configuration) throws CoreException {
		String type = getResourceManagerType(configuration);
		if (type != null) {
			ProviderInfo provider = ProviderInfo.getProvider(type);
			if (provider != null) {
				String controlId = getControlId(configuration);
				IJAXBLaunchControl control = getLaunchControl(provider.getName(), controlId);
				String name = getConnectionName(configuration);
				String id = getRemoteServicesId(configuration);
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
	public static IJAXBLaunchControl getLaunchControl(String name, String controlId) {
		IJAXBLaunchControl control;
		if (controlId == null) {
			control = new JAXBLaunchControl();
		} else {
			control = new JAXBLaunchControl(controlId);
		}
		control.setRMConfigurationURL(getJAXBConfigurationURL(name));
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
		String remId = getRemoteServicesId(configuration);
		if (remId != null) {
			IRemoteServices services = PTPRemoteCorePlugin.getDefault().getRemoteServices(remId, monitor);
			if (services != null) {
				String name = getConnectionName(configuration);
				if (name != null) {
					return services.getConnectionManager().getConnection(name);
				}
			}
		}
		return null;
	}

	/**
	 * @param configuration
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	public static IRemoteFileManager getRemoteFileManager(ILaunchConfiguration configuration, IProgressMonitor monitor)
			throws CoreException {
		IRemoteConnection conn = getRemoteConnection(configuration, monitor);
		if (conn != null) {
			return conn.getRemoteServices().getFileManager(conn);
		}
		return null;
	}

	/**
	 * @param configuration
	 * @return
	 */
	public static String getRemoteServicesId(ILaunchConfiguration configuration) {
		try {
			return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_REMOTE_SERVICES_ID, (String) null);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public static String getResourceManagerType(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_TYPE, (String) null);
	}

	/**
	 * Wrapper method. Calls {@link JAXBExtensionUtils#loadJAXBResourceManagers(Map, boolean)}
	 */
	private static void loadJAXBResourceManagers(boolean showError) {
		if (fJAXBConfigurations == null) {
			fJAXBConfigurations = new HashMap<String, URL>();
		} else {
			fJAXBConfigurations.clear();
		}

		JAXBExtensionUtils.loadJAXBResourceManagers(fJAXBConfigurations, showError);
	}

	/**
	 * Constructor
	 */
	public RMLaunchUtils() {
	}
}
