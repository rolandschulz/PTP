/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.cell.utils;

import java.util.Date;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ptp.cell.utils.debug.Debug;
import org.eclipse.ptp.cell.utils.platform.Platform;
import org.osgi.framework.BundleContext;



/**
 * The main plug-in class to be used in the desktop.
 */
public class UtilsPlugin extends Plugin {

	private static final String PLUGIN_ID = "org.eclipse.ptp.cell.utils"; //$NON-NLS-1$
	
	//The shared instance.
	private static UtilsPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public UtilsPlugin() {
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
		/*if (Debug.DEBUG_PLATFORM) {
			Platform.getOSArch();
			Platform.getOSLinuxDistro();
		}*/

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

	public static String getUniqueIdentifier() {
		if (plugin == null) {
			// If the default instance is not yet initialized,
			// return a static identifier.
			return PLUGIN_ID;
		}
		return plugin.getBundle().getSymbolicName();
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static UtilsPlugin getDefault() {
		return plugin;
	}

}
