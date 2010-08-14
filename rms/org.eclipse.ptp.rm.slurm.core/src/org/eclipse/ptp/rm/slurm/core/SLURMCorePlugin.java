/*******************************************************************************
 * Copyright (c) 2008,2009 School of Computer Science,
 * National University of Defense Technology, P.R.China.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Jie Jiang, National University of Defense Technology
 *******************************************************************************/
package org.eclipse.ptp.rm.slurm.core;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ptp.core.Preferences;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @since 5.0
 */
public class SLURMCorePlugin extends Plugin {

	// The plug-in ID
	private static final String PLUGIN_ID = "org.eclipse.ptp.rm.slurm.core"; //$NON-NLS-1$

	// The shared instance
	private static SLURMCorePlugin fPlugin;

	/**
	 * The constructor
	 */
	public SLURMCorePlugin() {
		fPlugin = this;
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
	public static SLURMCorePlugin getDefault() {
		return fPlugin;
	}

	/**
	 * Generate a unique identifier
	 * 
	 * @return unique identifier string
	 * @since 5.0
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}
}
