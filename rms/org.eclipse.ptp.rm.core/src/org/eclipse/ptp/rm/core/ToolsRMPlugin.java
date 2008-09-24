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
package org.eclipse.ptp.rm.core;

import javax.print.attribute.HashDocAttributeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.rm.core.utils.DebugUtil;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class ToolsRMPlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ptp.rm.core";

	// The shared instance
	private static ToolsRMPlugin plugin;

	/**
	 * The constructor
	 */
	public ToolsRMPlugin() {
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		DebugUtil.configurePluginDebugOptions();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
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
	public static ToolsRMPlugin getDefault() {
		return plugin;
	}

	public static CoreException coreErrorException(String message) {
		return new CoreException(new Status(IStatus.ERROR, ToolsRMPlugin.getDefault().getBundle().getSymbolicName(), message));
	}

	public static CoreException coreErrorException(String message, Throwable t) {
		return new CoreException(new Status(IStatus.ERROR, ToolsRMPlugin.getDefault().getBundle().getSymbolicName(), message, t));
	}

}
