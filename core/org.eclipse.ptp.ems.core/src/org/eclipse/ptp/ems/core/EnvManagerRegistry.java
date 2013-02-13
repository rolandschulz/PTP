/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeff Overbey (Illinois) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.ems.core;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.internal.ems.core.EMSCorePlugin;
import org.eclipse.ptp.internal.ems.core.managers.NullEnvManager;
import org.eclipse.ptp.remote.core.IRemoteConnection;

/**
 * Provides {@link IEnvManager} objects, which provide access to remote machines' environment management systems.
 * <p>
 * The set of supported environment management systems (and, consequently, the set of possible {@link IEnvManager} objects) is
 * determined by contributions to an extension point. See {@link IEnvManager}.
 * 
 * @author Jeff Overbey
 * 
 * @since 6.0
 */
public final class EnvManagerRegistry {

	/**
	 * @return an object which represents the absence of a supported environment management system (a Null Object).
	 */
	public static IEnvManager getNullEnvManager() {
		return new NullEnvManager();
	}

	/**
	 * Detects the environment management system on the remote machine, if any, and returns an {@link IEnvManager} capable of
	 * interfacing with that system.
	 * <p>
	 * If no supported environment managment system was detected, the result will be equal to {@link #getNullEnvManager()}.
	 * 
	 * @param pm
	 *            progress monitor used to report the status of potentially long-running operations to the user (non-
	 *            <code>null</code>)
	 * @param remoteConnection
	 *            {@link IRemoteConnection} providing a connection to a particular remote machine (non-<code>null</code>)
	 * 
	 * @return {@link IEnvManager} (non-<code>null</code>)
	 */
	public static IEnvManager getEnvManager(IProgressMonitor pm, IRemoteConnection remoteConnection) {
		if (remoteConnection != null) {
			final IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(IEnvManager.ENV_MANAGER_EXTENSION_POINT_ID);
			for (final IConfigurationElement element : extensionPoint.getConfigurationElements()) {
				try {
					final IEnvManager manager = (IEnvManager) element.createExecutableExtension("class"); //$NON-NLS-1$
					manager.configure(remoteConnection);
					if (manager.checkForCompatibleInstallation(pm)) {
						return manager;
					}
				} catch (final Exception e) {
					EMSCorePlugin.log(e);
				}
			}
		}

		return getNullEnvManager();
	}

	private EnvManagerRegistry() {
		throw new UnsupportedOperationException("Instantiation prohibited"); //$NON-NLS-1$
	}
}
