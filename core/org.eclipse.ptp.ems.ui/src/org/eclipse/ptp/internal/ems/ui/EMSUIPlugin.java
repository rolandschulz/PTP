/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 *     Jeff Overbey (Illinois/NCSA) - Design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.ems.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Plug-in activator for {@value #PLUGIN_ID}.
 * 
 * @author (Generated)
 * @author Jeff Overbey
 */
public class EMSUIPlugin extends AbstractUIPlugin {

	/** The unique ID for this plug-in. */
	public static final String PLUGIN_ID = "org.eclipse.ptp.ems.ui"; //$NON-NLS-1$

	/** Singleton instance */
	private static EMSUIPlugin plugin;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
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
	public static EMSUIPlugin getDefault() {
		return plugin;
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
	 * @param message human-readable error message
	 * @param e error to log
	 */
	public static void log(String message, Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, e));
	}

	/**
	 * Writes an entry to the workbench error log
	 * 
	 * @param status status object to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
}
