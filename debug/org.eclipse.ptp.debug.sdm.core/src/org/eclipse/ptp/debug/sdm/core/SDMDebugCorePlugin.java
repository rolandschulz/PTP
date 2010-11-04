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
package org.eclipse.ptp.debug.sdm.core;

import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.debug.sdm.core.utils.DebugUtil;
import org.osgi.framework.BundleContext;

/**
 * @author clement The main plugin class to be used in the desktop.
 */
public class SDMDebugCorePlugin extends Plugin {
	public static final String PLUGIN_ID = "org.eclipse.ptp.debug.sdm.core"; //$NON-NLS-1$

	/**
	 * @since 5.0
	 */
	public static final String SDM_DEBUGGER_EXTENSION_POINT_ID = "SDMDebugger"; //$NON-NLS-1$
	/**
	 * @since 5.0
	 */
	public static final String DEBUGGER_ELEMENT = "debugger"; //$NON-NLS-1$

	private static SDMDebugCorePlugin plugin;

	/**
	 * Returns the shared instance.
	 */
	public static SDMDebugCorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Get a unique identifier for this plugin
	 * 
	 * @return
	 */
	public static String getUniqueIdentifier() {
		return PLUGIN_ID;
	}

	private ArrayList<String> debuggerBackends = null;

	/**
	 * The constructor.
	 */
	public SDMDebugCorePlugin() {
		super();
		plugin = this;
	}

	/**
	 * @since 5.0
	 */
	public String[] getDebuggerBackends() {
		if (debuggerBackends == null) {
			initializeDebuggerBackends();
		}
		return debuggerBackends.toArray(new String[debuggerBackends.size()]);
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		DebugUtil.configurePluginDebugOptions();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	private void initializeDebuggerBackends() {
		debuggerBackends = new ArrayList<String>();
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(getUniqueIdentifier(),
				SDM_DEBUGGER_EXTENSION_POINT_ID);
		IConfigurationElement[] configs = extensionPoint.getConfigurationElements();
		for (int i = 0; i < configs.length; i++) {
			IConfigurationElement configurationElement = configs[i];
			if (configurationElement.getName().equals(DEBUGGER_ELEMENT)) {
				String backend = configurationElement.getAttribute("name"); //$NON-NLS-1$
				debuggerBackends.add(backend);
				String isDefault = configurationElement.getAttribute("default"); //$NON-NLS-1$
				if (isDefault != null && isDefault.equals("true")) { //$NON-NLS-1$
					Preferences.setDefaultString(getUniqueIdentifier(), SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_TYPE, backend);
				}
			}
		}
	}
}
