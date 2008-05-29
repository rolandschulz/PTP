/**********************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used for MPI development Tools.
 */
public class MpiPlugin extends AbstractUIPlugin
{
    /** The shared instance */
    private static MpiPlugin plugin;
    /** Resource bundle */
    private ResourceBundle   resourceBundle;
    protected static final boolean traceOn=false;
    
    private static final String PLUGIN_ID = "org.eclipse.ptp.pldt.mpi.core"; //$NON-NLS-1$

    /**
     * The constructor.
     */
    public MpiPlugin()
    {
        super();
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception
    {
        super.start(context);
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception
    {
        super.stop(context);
        plugin = null;
        resourceBundle = null;
    }

    /**
     * Returns the shared instance.
     */
    public static MpiPlugin getDefault()
    {
        return plugin;
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not found.
     */
    public static String getResourceString(String key)
    {
        ResourceBundle bundle = MpiPlugin.getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle()
    {
        try {
            if (resourceBundle == null)
                resourceBundle = ResourceBundle.getBundle("org.eclipse.ptp.pldt.mpi.core.MpiPluginResources"); //$NON-NLS-1$
        } catch (MissingResourceException x) {
            resourceBundle = null;
        }
        return resourceBundle;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative path.
     * 
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path)
    {
        return AbstractUIPlugin.imageDescriptorFromPlugin(MpiIDs.MPI_VIEW_ID, path);
    }
    /**
     * so that other plugins can access the icon that represents this plugin
     * @return
     */
    public ImageDescriptor getIconImageDescriptor(){
    	ImageDescriptor id = getImageDescriptor("icons/mpi.gif");
    	return id;
    }

    /**
     * Returns the workspace instance.
     */
    public static IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }

    /**
     * Returns the standard display to be used. The method first checks, if the thread calling this method has an
     * associated display. If so, this display is returned. Otherwise the method returns the default display.
     */
    public static Display getStandardDisplay()
    {
        Display display;
        display = Display.getCurrent();
        if (display == null) display = Display.getDefault();
        return display;
    }
    /**
     * Returns the preference setting for MPI include paths
     * 
     * @return
     */
    public List<String> getMpiIncludeDirs()
    {
        String stringList = getPluginPreferences().getString(MpiIDs.MPI_INCLUDES);
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
