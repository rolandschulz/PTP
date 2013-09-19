/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.remote.server.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.internal.remote.server.core.Activator;
import org.eclipse.remote.core.IRemoteConnection;

/**
 * @since 5.0
 */
public class RemoteServerManager {
	private static final String REMOTE_SERVER_EXTENSION_POINT_ID = "remoteServer"; //$NON-NLS-1$
	private static final String REMOTE_SERVER_EXTENSION_ID = "remoteServer"; //$NON-NLS-1$
	private static final String REMOTE_SERVER_OVERRIDE_EXTENSION_ID = "remoteServerOverride"; //$NON-NLS-1$

	private static final Map<String, AbstractRemoteServerRunner> fServerMap = new HashMap<String, AbstractRemoteServerRunner>();

	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_LAUNCH_COMMAND = "launchCommand"; //$NON-NLS-1$
	private static final String ATTR_UNPACK_COMMAND = "unpackCommand"; //$NON-NLS-1$
	private static final String ATTR_PAYLOAD = "payload"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_CONTINUOUS = "continuous"; //$NON-NLS-1$
	private static final String ATTR_VERIFY_LAUNCH_COMMAND = "verifyLaunchCommand"; //$NON-NLS-1$
	private static final String ATTR_VERIFY_LAUNCH_PATTERN = "verifyLaunchPattern"; //$NON-NLS-1$
	private static final String ATTR_VERIFY_LAUNCH_FAILMESSAGE = "verifyLaunchFailMessage"; //$NON-NLS-1$
	private static final String ATTR_VERIFY_UNPACK_COMMAND = "verifyUnpackCommand"; //$NON-NLS-1$
	private static final String ATTR_VERIFY_UNPACK_PATTERN = "verifyUnpackPattern"; //$NON-NLS-1$
	private static final String ATTR_VERIFY_UNPACK_FAILMESSAGE = "verifyUnpackFailMessage"; //$NON-NLS-1$

	/**
	 * Create the remote server give its id.
	 * 
	 * @param id
	 *            id of the remote server
	 * @return new instance of the remote server or null if no extension can be found
	 */
	private static AbstractRemoteServerRunner createServer(String id) {
		AbstractRemoteServerRunner server = null;
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(Activator.PLUGIN_ID, REMOTE_SERVER_EXTENSION_POINT_ID);
		if (extensionPoint != null) {
			final IExtension[] extensions = extensionPoint.getExtensions();

			for (IExtension ext : extensions) {
				final IConfigurationElement[] elements = ext.getConfigurationElements();

				for (IConfigurationElement ce : elements) {
					if (ce.getName().equals(REMOTE_SERVER_EXTENSION_ID) && id.equals(ce.getAttribute(ATTR_ID))) {
						try {
							Object exec = ce.createExecutableExtension(ATTR_CLASS);
							if (exec instanceof AbstractRemoteServerRunner) {
								server = (AbstractRemoteServerRunner) exec;
								server.setBundleId(ce.getContributor().getName());
								server.setLaunchCommand(ce.getAttribute(ATTR_LAUNCH_COMMAND));
								server.setUnpackCommand(ce.getAttribute(ATTR_UNPACK_COMMAND));
								server.setPayload(ce.getAttribute(ATTR_PAYLOAD));
								server.setContinuous(Boolean.parseBoolean(ce.getAttribute(ATTR_CONTINUOUS)));
								server.setVerifyLaunchCommand(ce.getAttribute(ATTR_VERIFY_LAUNCH_COMMAND));
								server.setVerifyLaunchPattern(ce.getAttribute(ATTR_VERIFY_LAUNCH_PATTERN));
								server.setVerifyLaunchFailMessage(ce.getAttribute(ATTR_VERIFY_LAUNCH_FAILMESSAGE));
								server.setVerifyUnpackCommand(ce.getAttribute(ATTR_VERIFY_UNPACK_COMMAND));
								server.setVerifyUnpackPattern(ce.getAttribute(ATTR_VERIFY_UNPACK_PATTERN));
								server.setVerifyUnpackFailMessage(ce.getAttribute(ATTR_VERIFY_UNPACK_FAILMESSAGE));
								checkForOverrides(id, server);
								return server;
							}
						} catch (CoreException e) {
							Activator.log(e);
						}
					}
				}
			}
		}
		return null;
	}

	private static void checkForOverrides(String id, AbstractRemoteServerRunner server) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(Activator.PLUGIN_ID, REMOTE_SERVER_EXTENSION_POINT_ID);
		if (extensionPoint != null) {
			final IExtension[] extensions = extensionPoint.getExtensions();

			for (IExtension ext : extensions) {
				final IConfigurationElement[] elements = ext.getConfigurationElements();

				for (IConfigurationElement ce : elements) {
					if (ce.getName().equals(REMOTE_SERVER_OVERRIDE_EXTENSION_ID) && id.equals(ce.getAttribute(ATTR_ID))) {
						String attr = ce.getAttribute(ATTR_LAUNCH_COMMAND);
						if (attr != null) {
							server.setLaunchCommand(attr);
						}
						attr = ce.getAttribute(ATTR_UNPACK_COMMAND);
						if (attr != null) {
							server.setUnpackCommand(attr);
						}
						attr = ce.getAttribute(ATTR_PAYLOAD);
						if (attr != null) {
							server.setPayload(attr);
						}
						attr = ce.getAttribute(ATTR_VERIFY_LAUNCH_COMMAND);
						if (attr != null) {
							server.setVerifyLaunchCommand(attr);
						}
						attr = ce.getAttribute(ATTR_VERIFY_LAUNCH_PATTERN);
						if (attr != null) {
							server.setVerifyLaunchPattern(attr);
						}
						attr = ce.getAttribute(ATTR_VERIFY_LAUNCH_FAILMESSAGE);
						if (attr != null) {
							server.setVerifyLaunchFailMessage(attr);
						}
						attr = ce.getAttribute(ATTR_VERIFY_UNPACK_COMMAND);
						if (attr != null) {
							server.setVerifyUnpackCommand(attr);
						}
						attr = ce.getAttribute(ATTR_VERIFY_UNPACK_PATTERN);
						if (attr != null) {
							server.setVerifyUnpackPattern(attr);
						}
						attr = ce.getAttribute(ATTR_VERIFY_UNPACK_FAILMESSAGE);
						if (attr != null) {
							server.setVerifyUnpackFailMessage(attr);
						}
					}
				}
			}
		}
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

	/**
	 * Get the remote server identified by id using the remote connection.
	 * 
	 * @param id
	 *            id of the remote server
	 * @param connection
	 *            connection used to launch server
	 * @return instance of the remote server, or null if no extension can be found
	 * @since 2.0
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
}
