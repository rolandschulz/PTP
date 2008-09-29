/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.environment.ui.deploy;

import java.util.Date;

import org.eclipse.ptp.cell.environment.ui.deploy.debug.Debug;
import org.eclipse.ui.plugin.*;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class DeployPlugin extends AbstractUIPlugin {
	
	public static String SETTING_EXPORT_OVERWRITE = "org.eclipse.ptp.remotetools.environment.ui.deploy.copyTo.overwrite"; //$NON-NLS-1$
	public static String SETTING_EXPORT_CREATE_DIR_STRUCTURE = "org.eclipse.ptp.remotetools.environment.ui.deploy.copyTo.createDirStructure"; //$NON-NLS-1$
	public static String SETTING_EXPORT_CREATE_SELECTED_DIR = "org.eclipse.ptp.remotetools.environment.ui.deploy.copyTo.createSelectedDirStructure"; //$NON-NLS-1$
	public static String SETTING_EXPORT_DESTINATION = "org.eclipse.ptp.remotetools.environment.ui.deploy.copyTo.destination"; //$NON-NLS-1$

	public static String SETTING_IMPORT_DESTINATION = "org.eclipse.ptp.remotetools.environment.ui.deploy.copyFrom.destination"; //$NON-NLS-1$
	
	//The shared instance.
	private static DeployPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public DeployPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		IDialogSettings settings = getDialogSettings();
		
		//If the dialog box has not been opened yet, then the settings will not have been set. So set them to default values
		if(!settings.getBoolean(SETTING_EXPORT_CREATE_DIR_STRUCTURE) && !settings.getBoolean(SETTING_EXPORT_CREATE_SELECTED_DIR))
			settings.put(SETTING_EXPORT_CREATE_SELECTED_DIR, true);
		
		if(settings.get(SETTING_EXPORT_DESTINATION) == null){
			settings.put(SETTING_EXPORT_DESTINATION, ""); //$NON-NLS-1$
		}	
		
		if(settings.get(SETTING_IMPORT_DESTINATION) == null){
			settings.put(SETTING_IMPORT_DESTINATION, ""); //$NON-NLS-1$
		}
		
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
	public static DeployPlugin getDefault() {
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
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ptp.remotetools.environment.ui.deploy", path); //$NON-NLS-1$
	}
}