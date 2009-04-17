package org.eclipse.ptp.remote.remotetools.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class RemoteToolsAdapterCorePlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ptp.remote.core.remotetools"; //$NON-NLS-1$

	// The remote services ID
	public static final String SERVICES_ID = "org.eclipse.ptp.remote.RemoteTools"; //$NON-NLS-1$

	// The shared instance
	private static RemoteToolsAdapterCorePlugin plugin;
	
	/**
	 * The constructor
	 */
	public RemoteToolsAdapterCorePlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static RemoteToolsAdapterCorePlugin getDefault() {
		return plugin;
	}

}
