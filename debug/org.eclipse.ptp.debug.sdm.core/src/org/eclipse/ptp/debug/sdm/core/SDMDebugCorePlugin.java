package org.eclipse.ptp.debug.sdm.core;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ptp.debug.sdm.core.utils.DebugUtil;
import org.osgi.framework.BundleContext;

/**
 * @author clement The main plugin class to be used in the desktop.
 */
public class SDMDebugCorePlugin extends Plugin {
	public static final String PLUGIN_ID = "org.eclipse.ptp.debug.sdm.core";

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

	/**
	 * The constructor.
	 */
	public SDMDebugCorePlugin() {
		super();
		plugin = this;
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
}
