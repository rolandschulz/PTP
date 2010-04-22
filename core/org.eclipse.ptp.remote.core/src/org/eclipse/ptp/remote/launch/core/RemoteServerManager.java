/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.remote.launch.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;

public class RemoteServerManager {
	private final static String REMOTE_SERVER_EXTENSION_POINT_ID = "remoteServer"; //$NON-NLS-1$
	private final static Map<String, AbstractRemoteServerRunner> fServerMap = new HashMap<String, AbstractRemoteServerRunner>();
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_LAUNCH_COMMAND = "launchCommand"; //$NON-NLS-1$
	private static final String ATTR_PAYLOAD = "payload"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$

	/**
	 * Get the remote server identified by id using the remote connection.
	 * 
	 * @param id
	 *            id of the remote server
	 * @param connection
	 *            connection used to launch server
	 * @return instance of the remote server, or null if no extension can be
	 *         found
	 */
	public static AbstractRemoteServerRunner getServer(String id, IRemoteConnection connection) {
		AbstractRemoteServerRunner server = fServerMap.get(getKey(id, connection));
		if (server == null) {
			server = createServer(id);
			if (server != null) {
				server.setRemoteConnection(connection);
				fServerMap.put(getKey(id, connection), server);
			}
		}
		return server;
	}

	/**
	 * Create the remote server give its id.
	 * 
	 * @param id
	 *            id of the remote server
	 * @return new instance of the remote server or null if no extension can be
	 *         found
	 */
	private static AbstractRemoteServerRunner createServer(String id) {
		AbstractRemoteServerRunner server = null;
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry
				.getExtensionPoint(PTPRemoteCorePlugin.PLUGIN_ID, REMOTE_SERVER_EXTENSION_POINT_ID);
		final IExtension[] extensions = extensionPoint.getExtensions();

		for (IExtension ext : extensions) {
			final IConfigurationElement[] elements = ext.getConfigurationElements();

			for (IConfigurationElement ce : elements) {
				if (ce.getAttribute(ATTR_ID).equals(id)) {
					try {
						Object exec = ce.createExecutableExtension(ATTR_CLASS);
						if (exec instanceof AbstractRemoteServerRunner) {
							server = (AbstractRemoteServerRunner) exec;
							server.setBundleId(ce.getDeclaringExtension().getNamespaceIdentifier());
							server.setLaunchCommand(ce.getAttribute(ATTR_LAUNCH_COMMAND));
							server.setPayload(ce.getAttribute(ATTR_PAYLOAD));
							return server;
						}
					} catch (CoreException e) {
						PTPRemoteCorePlugin.log(e);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Generate a unique key given the server id and connection
	 * 
	 * @param id
	 *            id of remote server
	 * @param connection
	 *            connection used by server
	 * @return unique key
	 */
	private static String getKey(String id, IRemoteConnection connection) {
		return id + ";" + connection.getRemoteServices().getId() + "." + connection.getName(); //$NON-NLS-1$//$NON-NLS-2$
	}
}
