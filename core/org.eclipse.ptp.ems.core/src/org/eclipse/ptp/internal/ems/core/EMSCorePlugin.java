/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 *     Jeff Overbey (Illinois/NCSA) - Design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.ems.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * Plug-in activator for {@value #PLUGIN_ID}.
 * 
 * @author (Generated)
 * @author Jeff Overbey
 */
public final class EMSCorePlugin extends Plugin {

	/** The unique ID for this plug-in. */
	public static final String PLUGIN_ID = "org.eclipse.ptp.ems.core"; //$NON-NLS-1$

	/** Singleton instance */
	private static EMSCorePlugin plugin = null;

	/** Default constructor */
	public EMSCorePlugin() {
		plugin = this; // the platform will only instantiate once
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static EMSCorePlugin getDefault() {
		return plugin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		try {
		} finally {
			super.stop(context);
		}
	}

	/**
	 * Writes an entry to the workbench error log for the given error
	 *  
	 * @param e error to log
	 */
	public static void log(String e) {
		log(createStatus(e));
	}

	/**
	 * Writes an entry to the workbench error log for the given error
	 *  
	 * @param e error to log
	 */
	public static void log(Throwable e) {
		log("Error", e); //$NON-NLS-1$
	}

	/**
	 * Writes an entry to the workbench error log for the given error
	 * 
	 * @param message a human-readable error message
	 * @param e error to log
	 */
	public static void log(String message, Throwable e) {
		log(createStatus(message, e));
	}

	/**
	 * Creates an error status with the given error message
	 * 
	 * @param msg
	 *            a human-readable error message
	 * @return {@link IStatus} (non-<code>null</code>)
	 */
	public static IStatus createStatus(String msg) {
		return createStatus(msg, null);
	}

	/**
	 * Creates an error status with the given error message
	 * 
	 * @param msg
	 *            a human-readable error message
	 * @param e
	 *            exception causing this error
	 * @return {@link IStatus} (non-<code>null</code>)
	 */
	public static IStatus createStatus(String msg, Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, msg, e);
	}

	/**
	 * Writes an entry to the workbench error log
	 * @param status status object to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
}
