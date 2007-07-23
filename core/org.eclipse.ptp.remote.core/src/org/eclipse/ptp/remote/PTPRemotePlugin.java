/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.internal.remote.RemoteServicesProxy;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PTPRemotePlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ptp.remote";

	// The shared instance
	private static PTPRemotePlugin plugin;
	
	// Active remote services plugins (not necessarily loaded)
	private Map<String, RemoteServicesProxy> allRemoteServices;
	
	// Default remote services for new RM wizard
	private IRemoteServices defaultRemoteServices;
	
	/**
	 * The constructor
	 */
	public PTPRemotePlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		defaultRemoteServices = null;
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
	public static PTPRemotePlugin getDefault() {
		return plugin;
	}

	/**
	 * Get unique identifier
	 * 
	 * @return
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}    

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}	
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}
	/**
	 * Returns the active workbench shell or <code>null</code> if none
	 * 
	 * @return the active workbench shell or <code>null</code> if none
	 */
	public static Shell getShell() {
		if (getActiveWorkbenchWindow() != null) {
			return getActiveWorkbenchWindow().getShell();
		}
		return null;
	}
	
	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status
	 *            status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	/**
	 * Logs an internal error with the specified message.
	 * 
	 * @param message
	 *            the error message to log
	 */
	public static void log(String message) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, message, null));
	}

	/**
	 * Logs an internal error with the specified throwable
	 * 
	 * @param e
	 *            the exception to be logged
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e)); //$NON-NLS-1$
	}

	/**
	 * Find and load all remoteServices plugins.
	 */
	private Map<String, RemoteServicesProxy> retrieveRemoteServices() {
    	IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(PLUGIN_ID + ".remoteServices");
		final IExtension[] extensions = extensionPoint.getExtensions();
		
		Map<String, RemoteServicesProxy> services = new HashMap<String, RemoteServicesProxy>(5);
		
		for (IExtension ext : extensions) {
			final IConfigurationElement[] elements = ext.getConfigurationElements();
		
			for (IConfigurationElement ce : elements)
			{
				RemoteServicesProxy proxy = new RemoteServicesProxy(ce);
				if (proxy.initialize()) {
					System.out.println("Adding remote service: " + proxy.getId() + "(" + proxy.getName() + ")");
					services.put(proxy.getId(), proxy);
				} else {
					System.out.println("Failed to initialize remote service: " + proxy.getId() + "(" + proxy.getName() + ")");
				}
			}
		}
		
		return services;
    }
	
	/**
	 * Retrieve a list of remote services.
	 * 
	 * @return remote services
	 */
	public synchronized IRemoteServices[] getAllRemoteServices() {
		if (allRemoteServices == null) {
			allRemoteServices = retrieveRemoteServices();
		}
		return allRemoteServices.values().toArray(new IRemoteServices[allRemoteServices.size()]);
	}
	
	/**
	 * Retrieve the default remote services plugin.
	 * 
	 * @return 
	 */
	public synchronized IRemoteServices getDefaultServices() {
		if (defaultRemoteServices == null) {
			IRemoteServices[] services = getAllRemoteServices();
			defaultRemoteServices = services[services.length-1];
		}
		
		return defaultRemoteServices;
	}
	
	/**
	 * Set the default services plugin
	 * 
	 * @param services
	 */
	public synchronized void setDefaultServices(IRemoteServices services) {
		defaultRemoteServices = services;
	}
		
	/**
	 * Get the remote services identified by id
	 * 
	 * @return services
	 */
	public synchronized IRemoteServices getRemoteServices(String id) {
		if (allRemoteServices == null) {
			allRemoteServices = retrieveRemoteServices();
		}
		return allRemoteServices.get(id);
	}
}
