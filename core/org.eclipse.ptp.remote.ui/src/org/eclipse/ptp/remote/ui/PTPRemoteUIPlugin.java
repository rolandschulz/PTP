package org.eclipse.ptp.remote.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PTPRemoteUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ptp.remote.ui"; //$NON-NLS-1$
	// UI extension point
	public static final String EXTENSION_POINT_ID = "remoteUIServices"; //$NON-NLS-1$

	// The shared instance
	private static PTPRemoteUIPlugin plugin;
	
	private Map<String, RemoteUIServicesProxy> remoteUIServices = null;
	
	/**
	 * The constructor
	 */
	public PTPRemoteUIPlugin() {
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
	public static PTPRemoteUIPlugin getDefault() {
		return plugin;
	}
	
	/**
	 * Helper method to find UI services that correspond to a particular remote services
	 * implementation
	 * 
	 * @param services
	 * @return remote UI services
	 */
	public IRemoteUIServices getRemoteUIServices(IRemoteServices services) {
		if (remoteUIServices == null) {
			remoteUIServices = retrieveRemoteUIServices();
		}
		
		/*
		 * Find the UI services corresponding to services.
		 */
		RemoteUIServicesProxy proxy = remoteUIServices.get(services.getId());
		if (proxy == null) {
			return null;
		}
		proxy.setServices(services);
		return proxy;
	}

	/**
	 * Find and load all remoteUIServices plugins.
	 */
	private Map<String, RemoteUIServicesProxy> retrieveRemoteUIServices() {
    	IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(PLUGIN_ID, EXTENSION_POINT_ID);
		final IExtension[] extensions = extensionPoint.getExtensions();
		
		Map<String, RemoteUIServicesProxy> services = new HashMap<String, RemoteUIServicesProxy>(5);
		
		for (IExtension ext : extensions) {
			final IConfigurationElement[] elements = ext.getConfigurationElements();
		
			for (IConfigurationElement ce : elements)
			{
				RemoteUIServicesProxy proxy = new RemoteUIServicesProxy(ce);
				services.put(proxy.getId(), proxy);
			}
		}
		
		return services;
    }
}
