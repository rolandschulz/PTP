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
package org.eclipse.ptp.cell.environment.cellsimulator;

import org.eclipse.ptp.cell.environment.cellsimulator.core.local.LocalLaunchEnvironment;
import org.eclipse.ptp.cell.environment.cellsimulator.core.remote.RemoteLaunchEnvironment;
import org.eclipse.ptp.cell.preferences.PreferencesPlugin;
import org.eclipse.ptp.cell.preferences.events.ICellPreferencesChangeListener;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 * 
 * @author Daniel Felix Ferber
 * @since 1.2
 */
public class CellSimulatorTargetPlugin extends AbstractUIPlugin {//Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ptp.remotetools.environment.cellsimulator"; //$NON-NLS-1$
	
	//The shared instance.
	private static CellSimulatorTargetPlugin plugin;
	
	ICellPreferencesChangeListener propListener = null;
	
	/**
	 * The constructor.
	 */
	public CellSimulatorTargetPlugin() {
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
//		 Notifies the core plugin to destroy all environments.
		EnvironmentPlugin corePlugin = EnvironmentPlugin.getDefault();
		corePlugin.destroyTypeElements(LocalLaunchEnvironment.class);
		corePlugin.destroyTypeElements(RemoteLaunchEnvironment.class);
		
		super.stop(context);
		if (propListener != null)
			PreferencesPlugin.getDefault().removeListener(propListener);
		
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static CellSimulatorTargetPlugin getDefault() {
		return plugin;
	}

	public ICellPreferencesChangeListener getPrefPropListener() {
		return propListener;
	}

	public void setPrefPropListener(ICellPreferencesChangeListener propListener) {
		
		if (this.propListener != null)
			PreferencesPlugin.getDefault().removeListener(propListener);
			
		this.propListener = propListener;
		
		if (this.propListener != null)
			PreferencesPlugin.getDefault().addListener(propListener);
	}


}
