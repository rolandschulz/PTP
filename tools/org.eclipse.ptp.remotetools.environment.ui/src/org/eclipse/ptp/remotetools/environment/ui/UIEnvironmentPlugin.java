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
package org.eclipse.ptp.remotetools.environment.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.remotetools.environment.ui.extension.DoubleClickHandlerManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author Ricardo M. Matinata
 * @since 1.1
 */
public class UIEnvironmentPlugin extends AbstractUIPlugin {

	//The shared instance.
	private static UIEnvironmentPlugin plugin;
	
	private DoubleClickHandlerManager doubleClickHandlerMgr;
	
	/**
	 * The constructor.
	 */
	public UIEnvironmentPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static UIEnvironmentPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ptp.remotetools.environment", path);
	}
	
	public DoubleClickHandlerManager getDoubleClickHandlerManager() {
	    if (doubleClickHandlerMgr == null) {
	        doubleClickHandlerMgr = new DoubleClickHandlerManager();
	    }
	    return doubleClickHandlerMgr;
	}
}
