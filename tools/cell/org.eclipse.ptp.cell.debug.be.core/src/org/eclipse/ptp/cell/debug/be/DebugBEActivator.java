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
package org.eclipse.ptp.cell.debug.be;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ptp.cell.debug.be.cdi.model.spu.SPUEnhancementsProcessor;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 * 
 */
public class DebugBEActivator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ptp.cell.debug.be"; //$NON-NLS-1$

	// The shared instance
	private static DebugBEActivator plugin;
	
	private SPUEnhancementsProcessor spuProcessor;
	
	/**
	 * The constructor
	 */
	public DebugBEActivator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static DebugBEActivator getDefault() {
		return plugin;
	}

	public SPUEnhancementsProcessor getSPUProcessor() {
		if (spuProcessor == null)
			spuProcessor = new SPUEnhancementsProcessor();
		return spuProcessor;
	}
}
