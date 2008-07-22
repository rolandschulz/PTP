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
package org.eclipse.ptp.remote.core;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.internal.remote.core.LocalServices;
import org.eclipse.ptp.internal.remote.core.RemoteServicesProxy;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PTPRemoteCorePlugin extends AbstractUIPlugin {

	private class RemoteServicesSorter implements Comparator<IRemoteServices> {
		public int compare(IRemoteServices o1, IRemoteServices o2) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	}

	public static final String PLUGIN_ID = "org.eclipse.ptp.remote.core"; //$NON-NLS-1$
	public static final String EXTENSION_POINT_ID = "remoteServices"; //$NON-NLS-1$
	
	// The shared instance
	private static PTPRemoteCorePlugin plugin;
	
	/**
	 * @return
	 */
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}
	
	/**
	 * @return
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	/**
     * If it is possible to adapt the given object to the given type, this
     * returns the adapter. Performs the following checks:
     * 
     * <ol>
     * <li>Returns <code>sourceObject</code> if it is an instance of the
     * adapter type.</li>
     * <li>If sourceObject implements IAdaptable, it is queried for adapters.</li>
     * <li>If sourceObject is not an instance of PlatformObject (which would have
     * already done so), the adapter manager is queried for adapters</li>
     * </ol>
     * 
     * Otherwise returns null.
     * 
     * @param sourceObject
     *            object to adapt, or null
     * @param adapterType
     *            type to adapt to
     * @return a representation of sourceObject that is assignable to the
     *         adapter type, or null if no such representation exists
     */
    public static Object getAdapter(Object sourceObject, Class adapterType) {
    	Assert.isNotNull(adapterType);
        if (sourceObject == null) {
            return null;
        }
        if (adapterType.isInstance(sourceObject)) {
            return sourceObject;
        }

        if (sourceObject instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable) sourceObject;

            Object result = adaptable.getAdapter(adapterType);
            if (result != null) {
                // Sanity-check
                Assert.isTrue(adapterType.isInstance(result));
                return result;
            }
        } 
        
        if (!(sourceObject instanceof PlatformObject)) {
            Object result = Platform.getAdapterManager().getAdapter(sourceObject, adapterType);
            if (result != null) {
                return result;
            }
        }

        return null;
    }
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static PTPRemoteCorePlugin getDefault() {
		return plugin;
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

	// Active remote services plugins (not necessarily loaded)
	private Map<String, RemoteServicesProxy> allRemoteServices;	
	
	// Default remote services for new RM wizard
	private IRemoteServices defaultRemoteServices;
	
	/**
	 * The constructor
	 */
	public PTPRemoteCorePlugin() {
	}
	
	/**
	 * Retrieve a sorted list of remote services.
	 * 
	 * @return remote services
	 */
	public synchronized IRemoteServices[] getAllRemoteServices() {
		if (allRemoteServices == null) {
			allRemoteServices = retrieveRemoteServices();
		}
		IRemoteServices[] services = allRemoteServices.values().toArray(new IRemoteServices[allRemoteServices.size()]);
		Arrays.sort(services, new RemoteServicesSorter());
		return services;
	}
	
	/**
	 * Retrieve the default remote services plugin. The default is the LocalServices
	 * provider if it exists, the last plugin otherwise.
	 * 
	 * @return default remote services provider
	 */
	public synchronized IRemoteServices getDefaultServices() {
		if (defaultRemoteServices == null) {
			IRemoteServices[] allServices = getAllRemoteServices();
			defaultRemoteServices = allServices[allServices.length-1];
			for (IRemoteServices services : allServices) {
				if (services.getId().equals(LocalServices.LocalServicesId)) {
					defaultRemoteServices = services;
					break;
				}
			}
		}
		
		return defaultRemoteServices;
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
	 * Find and load all remoteServices plugins.
	 */
	private Map<String, RemoteServicesProxy> retrieveRemoteServices() {
    	IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(PLUGIN_ID, EXTENSION_POINT_ID);
		final IExtension[] extensions = extensionPoint.getExtensions();
		
		Map<String, RemoteServicesProxy> services = new HashMap<String, RemoteServicesProxy>(5);
		
		for (IExtension ext : extensions) {
			final IConfigurationElement[] elements = ext.getConfigurationElements();
		
			for (IConfigurationElement ce : elements)
			{
				RemoteServicesProxy proxy = new RemoteServicesProxy(ce);
				if (proxy.initialize()) {
					services.put(proxy.getId(), proxy);
				} else {
					log("Failed to initialize remote service: " + proxy.getId() + "(" + proxy.getName() + ")");  //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
				}
			}
		}
		
		return services;
    }
}
