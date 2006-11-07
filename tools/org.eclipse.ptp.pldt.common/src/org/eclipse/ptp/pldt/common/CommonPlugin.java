/**********************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.common;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;



/**
 * The main plugin class for PLDT
 * @author Beth Tibbitts
 */
public class CommonPlugin extends AbstractUIPlugin {

	//The shared instance.
	private static CommonPlugin plugin;
    // Resource bundle.
    private ResourceBundle       resourceBundle;

	
	/**
	 * The constructor.
	 */
	public CommonPlugin() {
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
	public static CommonPlugin getDefault() {
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
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ptp.pldt.common", path);
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
     * BRT using this?
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle()
    {
        try {
            if (resourceBundle == null)
                resourceBundle = ResourceBundle.getBundle("org.ptp.pldt.common.CommonPluginResources");
        } catch (MissingResourceException x) {
            resourceBundle = null;
        }
        return resourceBundle;
    }

    /**
     * BRT using this?
     * Returns the string from the plugin's resource bundle, or 'key' if not found.
     */
    public static String getResourceString(String key)
    {
        ResourceBundle bundle = CommonPlugin.getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /**
     * Write to the ".log" file
     * @param type type of log entry, e.g. IStatus.ERROR, IStatus.WARNING, etc.
     * @param msg the message to be displayed
     */
    public static void log(int type, String msg) {
    	String pluginID=getDefault().getBundle().getSymbolicName();
    	Exception exc = null;// ignore exception for now
		IStatus status = new Status(type, pluginID, type, msg,exc);
		getDefault().getLog().log(status);

    }
}
