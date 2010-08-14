/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.ptp.rm.ibm.ll.core;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ptp.core.Preferences;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @since 5.0
 */
public class IBMLLCorePlugin extends Plugin {

	// The plug-in ID
	private static final String PLUGIN_ID = "org.eclipse.ptp.rm.ibm.ll.core"; //$NON-NLS-1$

	// The shared instance
	private static IBMLLCorePlugin fPlugin;

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static IBMLLCorePlugin getDefault() {
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

	/**
	 * The constructor
	 */
	public IBMLLCorePlugin() {
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
			super.stop(context);
			fPlugin = null;
		}
	}

}
