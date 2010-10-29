/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation 
 *     Albert L. Rossi (NCSA) - full implementation (bug 310188)
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @since 4.0
 */
public class PBSUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	private static final String PLUGIN_ID = "org.eclipse.ptp.rm.pbs.ui"; //$NON-NLS-1$

	// The shared instance
	private static PBSUIPlugin plugin;

	/**
	 * The constructor
	 */
	public PBSUIPlugin() {
		plugin = this;
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
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the active workbench shell or <code>null</code> if none
	 * 
	 * @return the active workbench shell or <code>null</code> if none
	 * @since 5.0
	 */
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null)
			return window.getShell();
		return null;
	}

	/**
	 * Convenience method to get the currently active workbench window
	 * 
	 * @return currently active workbench window
	 * @since 5.0
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static PBSUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Generate a unique identifier
	 * 
	 * @return unique identifier string
	 * @since 5.0
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null)
			return PLUGIN_ID;
		return getDefault().getBundle().getSymbolicName();
	}
}
