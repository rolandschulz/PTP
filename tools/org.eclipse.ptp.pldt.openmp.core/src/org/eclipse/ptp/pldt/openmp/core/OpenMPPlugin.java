/**********************************************************************
 * Copyright (c) 2005,2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.openmp.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.eclipse.ui.plugin.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class OpenMPPlugin extends AbstractUIPlugin {

	//The shared instance.
	private static OpenMPPlugin plugin;
    public static final String PLUGIN_ID = "org.eclipse.ptp.pldt.openmp.core";
	
    // Constants
    // preference page name for OpenMP
    public static final String OPEN_MP_INCLUDES    = "OpenMP Includes";
    
    public static final String MARKER_ID       = "org.eclipse.ptp.pldt.openmp.core.openMPMarker";
   
    // artifact view id
    public static final String VIEW_ID = "org.eclipse.ptp.pldt.openmp.core.views.OpenMPArtifactView";
	private static final String BUNDLE_NAME = "org.eclipse.ptp.pldt.openmp.core.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	/**
	 * The constructor.
	 */
	public OpenMPPlugin() {
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
	public static OpenMPPlugin getDefault() {
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
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ptp.pldt.openmp.core", path);
	}
    /**
     * Returns the preference setting for OpenMP include paths
     * 
     * @return
     */
    public List<String> getIncludeDirs()
    {
        String stringList = getPluginPreferences().getString(OPEN_MP_INCLUDES);
        StringTokenizer st = new StringTokenizer(stringList, File.pathSeparator + "\n\r");//$NON-NLS-1$
        List<String> dirs = new ArrayList<String>();
        while (st.hasMoreElements()) {
            dirs.add(st.nextToken());
        }
        return dirs;
    }
	public static String getResourceString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
    public static String getPluginId()
    {
        return PLUGIN_ID;
    }

}
