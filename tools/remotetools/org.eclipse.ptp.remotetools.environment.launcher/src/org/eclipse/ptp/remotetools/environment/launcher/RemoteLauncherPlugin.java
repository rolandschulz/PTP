/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.environment.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchIntegration;
import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchObserver;
import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchProcess;
import org.eclipse.ptp.remotetools.environment.launcher.data.ExecutionConfiguration;
import org.eclipse.ptp.remotetools.environment.launcher.internal.LaunchObserverIterator;
import org.eclipse.ptp.remotetools.environment.launcher.internal.RemoteLaunchProcess;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class RemoteLauncherPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ptp.remotetools.environment.launcher"; //$NON-NLS-1$
	public static final String OBERVER_EXTENSION_ID = "org.eclipse.ptp.remotetools.environment.launcher.observer"; //$NON-NLS-1$

	// The shared instance
	private static RemoteLauncherPlugin plugin;

	/**
	 * The constructor
	 */
	public RemoteLauncherPlugin() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static RemoteLauncherPlugin getDefault() {
		return plugin;
	}

	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

	public static LaunchObserverIterator getLaunchObserverIterator() {
		return new LaunchObserverIterator();
	}

	public static ILaunchObserver getLaunchObserverByID(String id) {
		if (id == null)
			return null;
		LaunchObserverIterator iterator = getLaunchObserverIterator();
		while (iterator.hasMoreElements()) {
			iterator.nextElement();
			if (iterator.getName().equals(id)) {
				return iterator.getInstance();
			}
		}
		return null;
	}

	public static void throwCoreException(String message) throws CoreException {
		Status status = new Status(IStatus.ERROR, PLUGIN_ID, 0, message, null);
		CoreException exception = new CoreException(status);
		throw exception;
	}

	public static void throwCoreException(String message, int errorCode) throws CoreException {
		Status status = new Status(IStatus.ERROR, PLUGIN_ID, errorCode, message, null);
		CoreException exception = new CoreException(status);
		throw exception;
	}

	public static ILaunchProcess createRemoteLaunchProcess(ILaunch launch, ExecutionConfiguration configuration,
			ILaunchIntegration launchIntegration) {
		return new RemoteLaunchProcess(launch, configuration, launchIntegration);
	}

	public static void throwCoreException(String message, RemoteConnectionException e) throws CoreException {
		Status status = new Status(IStatus.ERROR, PLUGIN_ID, 0, message, e);
		CoreException exception = new CoreException(status);
		throw exception;
	}

	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status
	 *            status to log
	 * @since 3.0
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Logs an internal error with the specified message.
	 * 
	 * @param message
	 *            the error message to log
	 * @since 3.0
	 */
	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, message, null));
	}

	/**
	 * Logs an internal error with the specified throwable
	 * 
	 * @param e
	 *            the exception to be logged
	 * @since 3.0
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
	}
}
