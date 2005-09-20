/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.external;

import org.eclipse.ui.plugin.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.BundleContext;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * The main plugin class to be used in the desktop.
 */
public class PTPDebugExternalPlugin extends AbstractUIPlugin {
	/**
	 * The plug-in identifier (value <code>"org.eclipse.ptp.debug.external.ui"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.ptp.debug.external.ui" ; //$NON-NLS-1$
	//The shared instance.
	private static PTPDebugExternalPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	private static Logger logger;
	
	/**
	 * The constructor.
	 */
	public PTPDebugExternalPlugin() {
		super();
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
		resourceBundle = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static PTPDebugExternalPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = PTPDebugExternalPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle.getBundle("org.eclipse.ptp.debug.external.ExternalPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ptp.debug.external", path);
	}
	
	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 * 
	 * @return the unique identifier of this plugin
	 */
	public static String getUniqueIdentifier() {
		if ( getDefault() == null ) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID; //$NON-NLS-1$
		}
		return getDefault().getBundle().getSymbolicName();
	}

	public Logger getLogger() {
		if (logger == null) {
			logger = Logger.getLogger(getClass().getName());
			Handler[] handlers = logger.getHandlers();
			for ( int index = 0; index < handlers.length; index++ ) {
				logger.removeHandler(handlers[index]);
			}
			Handler console = new ConsoleHandler();
			console.setFormatter(new Formatter() {
				public String format(LogRecord record) {
					String out = record.getLevel()  + " : "
						+ record.getSourceClassName()  + "."
						+ record.getSourceMethodName();
					if (!record.getMessage().equals(""))
						out = out + " : " + record.getMessage();
					out += "\n";
					return out;
				}});
			
			console.setLevel(Level.FINER);
			logger.addHandler(console);
			logger.setLevel(Level.FINER);
		}
		
		return logger;
	}
	
}
