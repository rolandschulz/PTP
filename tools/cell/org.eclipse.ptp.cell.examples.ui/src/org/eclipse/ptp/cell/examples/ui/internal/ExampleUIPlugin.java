/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.examples.ui.internal;

import java.util.Date;

import org.eclipse.ptp.cell.examples.ui.debug.Debug;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 * 
 * @author laggarcia
 * @since 1.1.1
 */
public class ExampleUIPlugin extends AbstractUIPlugin {

	public static final String ID = "org.eclipse.ptp.cell.examples.ui"; //$NON-NLS-1$

	// The shared instance.
	private static ExampleUIPlugin plugin;

	/**
	 * The constructor.
	 */
	public ExampleUIPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		Debug.read();
		if (Debug.DEBUG) {
			Date date = new Date();
			Debug.POLICY.trace("Bundle started at {0}", date.toString()); //$NON-NLS-1$
		}
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		
		Debug.read();
		if (Debug.DEBUG) {
			Date date = new Date();
			Debug.POLICY.trace("Bundle stopped at {0}", date.toString()); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static ExampleUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(ID, path);
	}
}
