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

package org.eclipse.ptp.pldt.common;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class for PLDT (PTP's Parallel Language Development Tools)
 * @author Beth Tibbitts
 */
public class CommonPlugin extends AbstractUIPlugin {

	/** The shared instance. */
	private static CommonPlugin plugin;
	/** Resource bundle */
	private ResourceBundle resourceBundle;

	private static boolean eclipseTraceOn = false;
	private static boolean haveReadTraceStatus = false;
	public static final String PLUGIN_ID = "org.eclipse.ptp.pldt.common";
	
  /**
   * To use dynamic tracing (User instructions):
   * Create a file ".options" in the
   * same directory as your eclipse executable. Put this in the file:
   * 
   * <pre>
   *  org.eclipse.ptp.pldt.common/debug = true
   *  org.eclipse.ptp.pldt.common/debug/pldtTrace = true
   * </pre>
   * 
   * Then launch eclipse in debug mode using the -debug option, and mirror the
   * Console view output to the command line console using the -consoleLog
   * option. (Maybe Linux already spits out the console, not sure.)
   * 
   * <pre>
   * eclipse - debug - consoleLog
   * </pre>
   * 
   * When you run with this tracing enabled, it will print out a bunch of
   * trace information to the console. At least MPI artifact analysis has been
   * enabled for user-directed tracing.
   * 
   * @return
   */
	public static boolean getTraceOn() {
		if (!haveReadTraceStatus) {
			String traceFilter = Platform.getDebugOption("org.eclipse.ptp.pldt.common/debug/pldtTrace");
			if (traceFilter != null) {
				System.out.println("CommonPlugin.getTraceOn(): pldtTrace trace filtering is on; traceFilter= " + traceFilter);
				eclipseTraceOn = true;
			}
			haveReadTraceStatus = true;
		}
		return eclipseTraceOn;
	}

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
		// there's probably a better place to put this, but...
		getPreferenceStore().setDefault(IDs.SHOW_ANALYSIS_CONFIRMATION, true);
		
		Bundle cdtBundle=Platform.getBundle("org.eclipse.cdt.core");
		String version = (String) cdtBundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
		System.out.println("CDT version: "+version);
		boolean versOK = version.startsWith("4.0.2")|| version.startsWith("4.0.3") || version.startsWith("5");
		if(!versOK){
			System.out.println("**Warning, wrong version of CDT.  Version 4.0.2 or higher is required with PLDT 2.0");
			String msg="This is PLDT 2.0 which requires CDT Version 4.0.2 or higher.";
				msg+="\nThis eclipse installation contains CDT version "+version;
			MessageDialog.openError(null, "Version mismatch", msg);
		}		
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
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
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
    /**
     * Write to the ".log" file with default status type of IStatus.INFO
     * @param msg the message to be displayed.
     */
    public static void log(String msg){
    	log(IStatus.INFO, msg);
    }
}
