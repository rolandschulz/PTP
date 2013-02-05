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
package org.eclipse.ptp.rm.jaxb.control;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.JAXBExtensionUtils;
import org.eclipse.ptp.rm.jaxb.core.JAXBRMPreferenceConstants;

public class LaunchControllerManager {

	private static final LaunchControllerManager fInstance = new LaunchControllerManager();
	private static final Map<String, ILaunchController> fControllers = Collections
			.synchronizedMap(new HashMap<String, ILaunchController>());

	public static String generateControlId(String remoteServicesId, String connectionName, String configName) {
		String controlBytes = remoteServicesId + "/" + connectionName + "/" + configName; //$NON-NLS-1$ //$NON-NLS-2$
		return UUID.nameUUIDFromBytes(controlBytes.getBytes()).toString();
	}

	public static LaunchControllerManager getInstance() {
		return fInstance;
	}

	private LaunchControllerManager() {
	}

	/**
	 * Look up a controller using the control ID
	 * 
	 * @param controlId
	 *            control ID of controller
	 * @return controller or null if no controller found
	 */
	public ILaunchController getLaunchController(String controlId) {
		return fControllers.get(controlId);
	}

	/**
	 * Look up a controller using the remote services ID, the connection name, and the configuration name. Creates a new controller
	 * if one doesn't already exist.
	 * 
	 * @param remoteServicesId
	 *            remote services Id
	 * @param connectionName
	 *            connection name
	 * @param configName
	 *            configuration name
	 * @return launch controller or null if the launch controller can't be created or initialized
	 * @throws CoreException
	 */
	public ILaunchController getLaunchController(String remoteServicesId, String connectionName, String configName)
			throws CoreException {
		if (remoteServicesId != null && connectionName != null && configName != null) {
			String controlId = generateControlId(remoteServicesId, connectionName, configName);
			ILaunchController controller = fControllers.get(controlId);
			if (controller == null) {
				URL url = JAXBExtensionUtils.getConfigurationURL(configName);
				if (url != null) {
					controller = new LaunchController();
					controller.setRMConfigurationURL(url);
					if (connectionName != null && remoteServicesId != null) {
						controller.setConnectionName(connectionName);
						controller.setRemoteServicesId(remoteServicesId);
					}
					controller.initialize();
					fControllers.put(controlId, controller);
				}
			}
			boolean reload = Preferences.getBoolean(JAXBCorePlugin.getUniqueIdentifier(),
					JAXBRMPreferenceConstants.FORCE_XML_RELOAD);
			if (controller != null && (!controller.isInitialized() || reload)) {
				controller.initialize();
			}
			return controller;
		}
		return null;
	}
}
