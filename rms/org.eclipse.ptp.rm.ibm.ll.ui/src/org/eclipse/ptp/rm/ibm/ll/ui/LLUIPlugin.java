/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.ptp.rm.ibm.ll.ui;

import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @since 5.0
 */
public class LLUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	private static final String PLUGIN_ID = "org.eclipse.ptp.rm.ibm.ll.ui"; //$NON-NLS-1$

	// The shared instance
	private static LLUIPlugin fPlugin;

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static LLUIPlugin getDefault() {
		return fPlugin;
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

	/**
	 * The constructor
	 */
	public LLUIPlugin() {
		fPlugin = this;
	}

	/**
	 * @since 4.0
	 */
	public void logError(String message) {
		getLog().log(new Status(Status.ERROR, getUniqueIdentifier(), message));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		fPlugin = null;
		super.stop(context);
	}

}
