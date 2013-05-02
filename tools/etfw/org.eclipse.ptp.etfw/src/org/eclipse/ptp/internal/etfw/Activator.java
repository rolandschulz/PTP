/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.internal.etfw;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.etfw.ETFWUtils;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ptp.etfw"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		ETFWUtils.refreshTools(getPreferenceStore());
		ETFWUtils.getPerfTabs();
		ETFWUtils.getPerfConfManagers();
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
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Create log entry from a Throwable
	 * 
	 * @param e
	 * @since 7.0
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, "Internal Error", e)); //$NON-NLS-1$
	}

	/**
	 * Create log entry from an IStatus
	 * 
	 * @param status
	 * @since 7.0
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Generate a unique identifier
	 * 
	 * @return unique identifier string
	 * @since 7.0
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

}
