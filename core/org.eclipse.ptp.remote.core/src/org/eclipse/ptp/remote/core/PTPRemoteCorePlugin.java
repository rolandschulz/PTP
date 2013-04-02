/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.remote.core.DebugUtil;
import org.eclipse.ptp.internal.remote.core.LocalServices;
import org.eclipse.ptp.internal.remote.core.RemoteServicesProxy;
import org.eclipse.ptp.internal.remote.core.messages.Messages;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PTPRemoteCorePlugin extends Plugin {

	private class RemoteServicesSorter implements Comparator<IRemoteServices> {
		@Override
		public int compare(IRemoteServices o1, IRemoteServices o2) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
	}

	private static final String PLUGIN_ID = "org.eclipse.ptp.remote.core"; //$NON-NLS-1$
	/**
	 * @since 4.0
	 */
	public static final String REMOTE_SERVICES_EXTENSION_POINT_ID = "remoteServices"; //$NON-NLS-1$

	// The shared instance
	private static PTPRemoteCorePlugin plugin;

	/**
	 * If it is possible to adapt the given object to the given type, this returns the adapter. Performs the following checks:
	 * 
	 * <ol>
	 * <li>Returns <code>sourceObject</code> if it is an instance of the adapter type.</li>
	 * <li>If sourceObject implements IAdaptable, it is queried for adapters.</li>
	 * <li>If sourceObject is not an instance of PlatformObject (which would have already done so), the adapter manager is queried
	 * for adapters</li>
	 * </ol>
	 * 
	 * Otherwise returns null.
	 * 
	 * @param sourceObject
	 *            object to adapt, or null
	 * @param adapterType
	 *            type to adapt to
	 * @return a representation of sourceObject that is assignable to the adapter type, or null if no such representation exists
	 */
	@SuppressWarnings("rawtypes")
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
	 * Get unique identifier for this plugin
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
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

	// Intialized remote services
	private final Map<String, IRemoteServices> allInitializedRemoteServices = new HashMap<String, IRemoteServices>();

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
	 * Note that these services are not initialized and clients must call {@link IRemoteServices#initialized} before they are used.
	 * 
	 * Alternatively, use {@link #getAllRemoteServices(IProgressMonitor)} to obtain all initialized services.
	 * 
	 * @return remote services
	 */
	public synchronized IRemoteServices[] getAllRemoteServices() {
		retrieveRemoteServices();
		List<IRemoteServices> services = new ArrayList<IRemoteServices>();
		for (RemoteServicesProxy proxy : allRemoteServicesById.values()) {
			services.add(proxy.getServices());
		}
		Collections.sort(services, new RemoteServicesSorter());
		return services.toArray(new IRemoteServices[0]);
	}

	/**
	 * Retrieve a sorted list of remote services. The remote services are guaranteed to have been initialized.
	 * 
	 * Note that this will trigger plugin loading for all remote services implementations.
	 * 
	 * @return remote services
	 * @since 5.0
	 */
	public synchronized IRemoteServices[] getAllRemoteServices(IProgressMonitor monitor) {
		if (allInitializedRemoteServices.isEmpty()) {
			retrieveRemoteServices();
			SubMonitor progress = SubMonitor.convert(monitor, allRemoteServicesById.size());
			try {
				for (RemoteServicesProxy proxy : allRemoteServicesById.values()) {
					IRemoteServices services = proxy.getServices();
					if (!services.isInitialized()) {
						initializeRemoteServices(services, progress.newChild(1));
					}
					allInitializedRemoteServices.put(services.getId(), services);
				}
			} finally {
				if (monitor != null) {
					monitor.done();
				}
			}
		}
		IRemoteServices[] res = allInitializedRemoteServices.values().toArray(new IRemoteServices[0]);
		Arrays.sort(res, new RemoteServicesSorter());
		return res;
	}

	/**
	 * Retrieve the default remote services plugin. The default is the LocalServices provider, which is guaranteed to exist and be
	 * initialized.
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
	 * Get the remote services implementation identified by id. The remote services retrieved may not have been initialized.
	 * {@link IRemoteServices#initialize()} must be called before any attempt is made to use the services.
	 * 
	 * @param id
	 *            id of the remote services
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
	 * Get the remote services implementation identified by id and ensure that it is initialized. This method will present the user
	 * with a dialog box that can be canceled.
	 * 
	 * @param id
	 *            id of remote services to retrieve
	 * @param monitor
	 *            progress monitor to allow user to cancel operation
	 * @return initialized remote services, or null if the services cannot be found or initialized
	 * @since 5.0
	 */
	public IRemoteServices getRemoteServices(String id, IProgressMonitor monitor) {
		IRemoteServices services = getRemoteServices(id);
		if (services != null && !services.isInitialized()) {
			initializeRemoteServices(services, monitor);
		}
		return services;
	}

	/**
	 * Get the remote services identified by a URI. The remote services retrieved may not have been initialized.
	 * {@link IRemoteServices#initialize()} must be called before any attempt is made to use the services.
	 * 
	 * @param uri
	 *            URI of remote services to retrieve
	 * @return remote services, or null if no corresponding services found
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

	/**
	 * Get the remote services implementation identified by URI and ensure that it is initialized. This method will present the user
	 * with a dialog box that can be canceled.
	 * 
	 * @param uri
	 *            URI of remote services to retrieve
	 * @param monitor
	 *            progress monitor to allow user to cancel operation
	 * @return initialized remote services, or null if the services cannot be found or initialized
	 * @since 5.0
	 */
	public IRemoteServices getRemoteServices(URI uri, IProgressMonitor monitor) {
		IRemoteServices services = getRemoteServices(uri);
		if (services != null && !services.isInitialized()) {
			initializeRemoteServices(services, monitor);
		}
		return services;
	}

	/**
	 * Get the remote services descriptor identified by id. The remote services retrieved may not have been initialized.
	 * {@link IRemoteServices#initialize()} must be called before any attempt is made to use the services.
	 * 
	 * @param id
	 *            id of the remote services
	 * @return remote services descriptor
	 */
	public IRemoteServicesDescriptor getRemoteServicesDescriptor(String id) {
		retrieveRemoteServices();
		return allRemoteServicesById.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		DebugUtil.configurePluginDebugOptions();
		ResourcesPlugin.getWorkspace().addSaveParticipant(getUniqueIdentifier(), new ISaveParticipant() {
			@Override
			public void saving(ISaveContext saveContext) throws CoreException {
				Preferences.savePreferences(getUniqueIdentifier());
			}

			@Override
			public void rollback(ISaveContext saveContext) {
			}

			@Override
			public void prepareToSave(ISaveContext saveContext) throws CoreException {
			}

			@Override
			public void doneSaving(ISaveContext saveContext) {
			}
		});
		defaultRemoteServices = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		Preferences.savePreferences(getUniqueIdentifier());
		plugin = null;
		super.stop(context);
	}

	/**
	 * Ensure the remote services is initialized. This method will present the user with a dialog box that can be canceled.
	 * 
	 * @param services
	 *            remote services to initialize
	 * @param monitor
	 *            progress monitor to show initialization progress. Note that initialization cannot be cancelled to prevent the
	 *            remote system from being left in an undefined state.
	 * @return true if the remote services was initialized, or false otherwise
	 */
	private void initializeRemoteServices(IRemoteServices services, IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		progress.setTaskName(NLS.bind(Messages.PTPRemoteCorePlugin_0, services.getName()));
		try {
			while (!services.isInitialized()) {
				progress.setWorkRemaining(9);
				services.initialize();
				if (!services.isInitialized()) {
					try {
						synchronized (this) {
							wait(1000);
						}
					} catch (InterruptedException e) {
						// Ignore
					}
				}
				progress.worked(1);
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Find and load all remoteServices plugins.
	 */
	private void retrieveRemoteServices() {
		if (allRemoteServicesById.isEmpty()) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(PLUGIN_ID, REMOTE_SERVICES_EXTENSION_POINT_ID);
			final IExtension[] extensions = extensionPoint.getExtensions();

			for (IExtension ext : extensions) {
				final IConfigurationElement[] elements = ext.getConfigurationElements();

				for (IConfigurationElement ce : elements) {
					RemoteServicesProxy proxy = new RemoteServicesProxy(ce);
					allRemoteServicesById.put(proxy.getId(), proxy);
					allRemoteServicesByScheme.put(proxy.getScheme(), proxy);
				}
			}
		}
	}

}
