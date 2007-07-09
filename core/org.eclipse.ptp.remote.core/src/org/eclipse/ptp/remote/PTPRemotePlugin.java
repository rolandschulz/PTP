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

import java.util.ArrayList;
import java.util.List;

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
	
	// Active remote services plugins
	private RemoteServicesProxy[] remoteServices;
	
	// Selected remote service plugin
	private IRemoteServices selectedServices;
	
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
		selectedServices = null;
		remoteServices = null;
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
	 * 
	 */
	private RemoteServicesProxy[] retrieveRemoteServices() {
    	IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(PLUGIN_ID + ".remoteServices");
		final IExtension[] extensions = extensionPoint.getExtensions();
		
		List<RemoteServicesProxy> services = new ArrayList<RemoteServicesProxy>(5);
		
		for (IExtension ext : extensions) {
			final IConfigurationElement[] elements = ext.getConfigurationElements();
		
			for (IConfigurationElement ce : elements)
			{
				RemoteServicesProxy proxy = new RemoteServicesProxy(ce);
				if (proxy != null) {
					services.add(proxy);
				}
			}
		}
		
		return services.toArray(new RemoteServicesProxy[services.size()]);
    }
	
	public String[] getRemoteServicesNames() {
		if (remoteServices == null) {
			remoteServices = retrieveRemoteServices();
			for (RemoteServicesProxy p : remoteServices) {
				System.out.println("found remote services: " + p.getName());
			}
		}
		String[] names = new String[remoteServices.length];
		for (int i = 0; i < remoteServices.length; i++) {
			names[i] = remoteServices[i].getName();
		}
		return names;
	}
	
	public void setSelectedServices(String id) {
		for (RemoteServicesProxy proxy : remoteServices) {
			if (proxy.getId().equals(id)) {
				AbstractRemoteServicesFactory factory = proxy.getFactory();
				selectedServices = factory.create();
			}
		}
	}
	
	public IRemoteServices getSelectedServices() {
		return selectedServices;
	}
}
