/**********************************************************************
 * Copyright (c) 2008,2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.upc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.pldt.upc.internal.UPCIDs;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class UPCPlugin extends AbstractUIPlugin {

	/** The shared instance. */
	private static UPCPlugin plugin;
	public static final String PLUGIN_ID = "org.eclipse.ptp.pldt.upc"; //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public UPCPlugin() {
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
	public static UPCPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ptp.pldt.upc", path); //$NON-NLS-1$
	}

	/**
	 * Returns the preference setting for UPC include paths
	 * 
	 * @return
	 */
	public List<String> getUPCIncludeDirs()
	{
		String stringList = getPluginPreferences().getString(UPCIDs.UPC_INCLUDES);
		StringTokenizer st = new StringTokenizer(stringList, File.pathSeparator + "\n\r");//$NON-NLS-1$
		List<String> dirs = new ArrayList<String>();
		while (st.hasMoreElements()) {
			dirs.add(st.nextToken());
		}
		return dirs;
	}

	public static String getPluginId()
	{
		return PLUGIN_ID;
	}
}
