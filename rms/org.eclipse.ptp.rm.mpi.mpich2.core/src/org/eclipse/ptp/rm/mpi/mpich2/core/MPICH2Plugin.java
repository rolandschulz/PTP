/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.mpich2.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.core.RMCorePlugin;
import org.eclipse.ptp.rm.mpi.mpich2.core.launch.MPICH2LaunchConfigurationDefaults;
import org.eclipse.ptp.rm.mpi.mpich2.core.messages.Messages;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Daniel Felix Ferber
 */
public class MPICH2Plugin extends Plugin {

	// The plug-in ID
	private static final String PLUGIN_ID = "org.eclipse.ptp.rm.mpi.mpich2.core"; //$NON-NLS-1$

	// The shared instance
	private static MPICH2Plugin fPlugin;

	/**
	 * The constructor
	 */
	public MPICH2Plugin() {
		// Nothing to do
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
		fPlugin = this;
		MPICH2Defaults.loadDefaults();
		MPICH2LaunchConfigurationDefaults.loadDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			Preferences.savePreferences(getUniqueIdentifier());
		} finally {
			fPlugin = null;
			super.stop(context);
		}
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static MPICH2Plugin getDefault() {
		return fPlugin;
	}

	/**
	 * Raise core exception.
	 * 
	 * @param message
	 * @return
	 */
	public static CoreException coreErrorException(String message) {
		return new CoreException(new Status(IStatus.ERROR, RMCorePlugin.getDefault().getBundle().getSymbolicName(), message));
	}

	/**
	 * Raise core exception.
	 * 
	 * @param message
	 * @param t
	 * @return
	 */
	public static CoreException coreErrorException(String message, Throwable t) {
		return new CoreException(new Status(IStatus.ERROR, RMCorePlugin.getDefault().getBundle().getSymbolicName(), message, t));
	}

	/**
	 * Create log entry from an IStatus
	 * 
	 * @param status
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Create log entry from a string
	 * 
	 * @param msg
	 */
	public static void log(String msg) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, msg, null));
	}

	/**
	 * Create log entry from a Throwable
	 * 
	 * @param e
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, Messages.MPICH2Plugin_Exception_InternalError, e));
	}

	/**
	 * Generate a unique identifier
	 * 
	 * @return unique identifier string
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}
}
