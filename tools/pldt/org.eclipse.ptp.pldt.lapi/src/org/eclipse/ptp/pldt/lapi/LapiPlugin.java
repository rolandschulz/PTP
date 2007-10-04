/**********************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.lapi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class LapiPlugin extends AbstractUIPlugin {

	//The shared instance.
	private static LapiPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public LapiPlugin() {
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
	public static LapiPlugin getDefault() {
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
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ptp.pldt.lapi", path);
	}
    
    /**
     * Returns the preference setting for LAPI include paths
     * 
     * @return
     */
    public List /* of String */getLapiIncludeDirs()
    {
        String stringList = getPluginPreferences().getString(LapiIDs.LAPI_INCLUDES);
        StringTokenizer st = new StringTokenizer(stringList, File.pathSeparator + "\n\r");//$NON-NLS-1$
        List dirs = new ArrayList();
        while (st.hasMoreElements()) {
            dirs.add(st.nextToken());
        }
        return dirs;
    }
    
    public static String getPluginId()
    {
        return "org.eclipse.ptp.pldt.lapi";
    }
}
