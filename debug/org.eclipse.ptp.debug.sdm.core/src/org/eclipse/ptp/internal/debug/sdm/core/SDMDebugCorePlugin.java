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
package org.eclipse.ptp.internal.debug.sdm.core;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.ptp.internal.debug.sdm.core.utils.DebugUtil;
import org.osgi.framework.BundleContext;

/**
 * @author clement The main plugin class to be used in the desktop.
 */
public class SDMDebugCorePlugin extends Plugin {
	private class BackendSorter implements Comparator<String> {
		public int compare(String o1, String o2) {
			int pri1 = 0;
			try {
				pri1 = Integer.parseInt(debuggerBackends.get(o1));
			} catch (NumberFormatException e) {
			}
			int pri2 = 0;
			try {
				pri2 = Integer.parseInt(debuggerBackends.get(o2));
			} catch (NumberFormatException e) {
			}
			return pri2 - pri1;
		}

	}

	public static final String PLUGIN_ID = "org.eclipse.ptp.debug.sdm.core"; //$NON-NLS-1$
	/**
	 * @since 5.0
	 */
	public static final String SDM_DEBUGGER_EXTENSION_POINT_ID = "SDMDebugger"; //$NON-NLS-1$

	/**
	 * @since 5.0
	 */
	public static final String DEBUGGER_ELEMENT = "debugger"; //$NON-NLS-1$

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String ATTR_SDM_PATH = "sdmPath"; //$NON-NLS-1$
	private static final String ATTR_PRIORITY = "priority"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_BACKEND_PATH = "backendPath"; //$NON-NLS-1$

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

	private Map<String, String> debuggerBackends = null;
	private Map<String, String> debuggerBackendPaths = null;
	private Map<String, String> debuggerSDMPaths = null;

	/**
	 * The constructor.
	 */
	public SDMDebugCorePlugin() {
		super();
		plugin = this;
	}

	/**
	 * @since 6.0
	 */
	public String getDebuggerBackendPath(String backend) {
		initializeDebuggerBackends();
		return debuggerBackendPaths.get(backend);
	}

	/**
	 * @since 5.0
	 */
	public String[] getDebuggerBackends() {
		initializeDebuggerBackends();
		String[] backends = debuggerBackends.keySet().toArray(new String[debuggerBackends.size()]);
		Arrays.sort(backends, new BackendSorter());
		return backends;
	}

	/**
	 * @since 6.0
	 */
	public String getDebuggerSDMPath(String backend) {
		initializeDebuggerBackends();
		return debuggerSDMPaths.get(backend);
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

	/**
	 * Compare two strings as integers. Returns the difference between the values. If either string is not parseable as an integer,
	 * it is assumed to be 0.
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	private int compare(String p1, String p2) {
		int pri1 = 0;
		int pri2 = 0;
		try {
			pri1 = Integer.parseInt(p1);
		} catch (NumberFormatException e) {
		}
		try {
			pri2 = Integer.parseInt(p2);
		} catch (NumberFormatException e) {
		}
		return pri1 - pri2;
	}

	private void initializeDebuggerBackends() {
		if (debuggerBackends == null) {
			debuggerBackends = new HashMap<String, String>();
			debuggerBackendPaths = new HashMap<String, String>();
			debuggerSDMPaths = new HashMap<String, String>();
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(getUniqueIdentifier(),
					SDM_DEBUGGER_EXTENSION_POINT_ID);
			IConfigurationElement[] configs = extensionPoint.getConfigurationElements();
			for (IConfigurationElement configurationElement : configs) {
				if (configurationElement.getName().equals(DEBUGGER_ELEMENT)) {
					String newPriority = configurationElement.getAttribute(ATTR_PRIORITY);
					if (newPriority == null) {
						newPriority = "0"; //$NON-NLS-1$
					}
					String backend = configurationElement.getAttribute(ATTR_NAME);
					String oldPriority = debuggerBackends.get(backend);
					if (oldPriority == null || compare(newPriority, oldPriority) > 0) {
						debuggerBackends.put(backend, newPriority);
						String path = configurationElement.getAttribute(ATTR_BACKEND_PATH);
						debuggerBackendPaths.put(backend, path == null ? EMPTY_STRING : path);
						String sdm_path = configurationElement.getAttribute(ATTR_SDM_PATH);
						debuggerSDMPaths.put(backend, sdm_path == null ? EMPTY_STRING : sdm_path);
					}
				}
			}
		}
	}
}
