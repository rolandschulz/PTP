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

import java.net.URI;
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
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.remote.internal.core.LocalServices;
import org.eclipse.ptp.remote.internal.core.RemoteServicesProxy;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PTPRemoteCorePlugin extends Plugin {

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
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
	}    

	// Active remote services plugins (not necessarily loaded)
	private final Map<String, RemoteServicesProxy> allRemoteServicesById = new HashMap<String, RemoteServicesProxy>();	
	private final Map<String, RemoteServicesProxy> allRemoteServicesByScheme = new HashMap<String, RemoteServicesProxy>();	

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
		retrieveRemoteServices();
		IRemoteServices[] services = new IRemoteServices[allRemoteServicesById.size()];
		int i = 0;
		for (RemoteServicesProxy proxy : allRemoteServicesById.values()) {
			services[i++] = proxy.getServices();
		}
		Arrays.sort(services, new RemoteServicesSorter());
		return services;
	}
	
	/**
	 * Get the connection identified by a URI
	 * 
	 * @param uri
	 * @return connection
	 */
	public IRemoteConnection getConnection(URI uri) {
		IRemoteServices services = getRemoteServices(uri);
		if (services != null) {
			return services.getConnectionManager().getConnection(uri.getHost());
		}
		return null;
	}

	/**
	 * Retrieve the default remote services plugin. The default is the LocalServices
	 * provider, which is guaranteed to exist.
	 * 
	 * @return default remote services provider
	 */
	public IRemoteServices getDefaultServices() {
		if (defaultRemoteServices == null) {
			defaultRemoteServices = getRemoteServices(LocalServices.LocalServicesId);
		}
		return defaultRemoteServices;
	}
	
	/**
	 * Get the remote services descriptor identified by id
	 * 
	 * @param id id of the remote services
	 * @return remote services descriptor
	 */
	public IRemoteServicesDescriptor getRemoteServicesDescriptor(String id) {
		retrieveRemoteServices();
		return allRemoteServicesById.get(id);
	}
	
	/**
	 * Get the remote services implementation identified by id
	 * 
	 * @param id id of the remote services
	 * @return remote services
	 */
	public synchronized IRemoteServices getRemoteServices(String id) {
		retrieveRemoteServices();
		RemoteServicesProxy proxy = allRemoteServicesById.get(id);
		if (proxy != null) {
			return proxy.getServices();
		}
		return null;
	}
	
	/**
	 * Get the remote services identified by a URI
	 * 
	 * @param uri
	 * @return remote services
	 */
	public IRemoteServices getRemoteServices(URI uri) {
		retrieveRemoteServices();
		String scheme = uri.getScheme();
		if (scheme != null) {
			RemoteServicesProxy proxy = allRemoteServicesByScheme.get(uri.getScheme());
			if (proxy != null) {
				return proxy.getServices();
			}
		}
		return null;
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
	private void retrieveRemoteServices() {
		if (allRemoteServicesById.isEmpty()) {
	    	IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(PLUGIN_ID, EXTENSION_POINT_ID);
			final IExtension[] extensions = extensionPoint.getExtensions();
			
			for (IExtension ext : extensions) {
				final IConfigurationElement[] elements = ext.getConfigurationElements();
			
				for (IConfigurationElement ce : elements)
				{
					RemoteServicesProxy proxy = new RemoteServicesProxy(ce);
					allRemoteServicesById.put(proxy.getId(), proxy);
					allRemoteServicesByScheme.put(proxy.getScheme(), proxy);
				}
			}
		}
    }
}
