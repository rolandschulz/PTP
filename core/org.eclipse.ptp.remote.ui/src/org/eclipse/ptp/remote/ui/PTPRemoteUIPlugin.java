package org.eclipse.ptp.remote.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.ui.messages.Messages;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
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

	// Cache of initialized services
	private IRemoteServices[] fInitializedServices = null;
	// Remote service lookup result
	private IRemoteServices fRemoteService = null;

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
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static PTPRemoteUIPlugin getDefault() {
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
	 * Generate a log message given an IStatus object
	 * 
	 * @param status
	 *            IStatus object
	 * @since 5.0
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Generate a log message
	 * 
	 * @param msg
	 *            message to log
	 * @since 5.0
	 */
	public static void log(String msg) {
		log(new Status(IStatus.ERROR, getDefault().getBundle().getSymbolicName(), IStatus.ERROR, msg, null));
	}

	/**
	 * Generate a log message for an exception
	 * 
	 * @param e
	 *            exception used to generate message
	 * @since 5.0
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getDefault().getBundle().getSymbolicName(), IStatus.ERROR, Messages.PTPRemoteUIPlugin_3, e));
	}

	private Map<String, RemoteUIServicesProxy> remoteUIServices = null;

	/**
	 * The constructor
	 */
	public PTPRemoteUIPlugin() {
	}

	/**
	 * Helper method to find UI services that correspond to a particular remote
	 * services implementation
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
		if (proxy != null) {
			return proxy.getUIServices(services);
		}
		return null;
	}

	/**
	 * Get all the remote service providers and ensure that they are
	 * initialized. The method will use the supplied container's progress
	 * service, or, if null, the platform progress service, in order to allow
	 * the initialization to be canceled.
	 * 
	 * @param context
	 *            context with progress service, or null to use the platform
	 *            progress service
	 * @return array containing initialized services
	 * @since 5.0
	 */
	public synchronized IRemoteServices[] getRemoteServices(IRunnableContext context) {
		if (fInitializedServices == null) {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					SubMonitor progress = SubMonitor.convert(monitor, Messages.PTPRemoteUIPlugin_4, 10);
					try {
						fInitializedServices = PTPRemoteCorePlugin.getDefault().getAllRemoteServices(progress.newChild(10));
					} finally {
						monitor.done();
					}
				}
			};
			try {
				/*
				 * See if the services have already been initialized. If any
				 * haven't, then initialize them all.
				 */
				fInitializedServices = PTPRemoteCorePlugin.getDefault().getAllRemoteServices();
				for (IRemoteServices services : fInitializedServices) {
					if (!services.isInitialized()) {
						if (context != null) {
							context.run(true, false, runnable);
						} else {
							PlatformUI.getWorkbench().getProgressService().busyCursorWhile(runnable);
						}
						break;
					}
				}
			} catch (InvocationTargetException e) {
				log(e);
			} catch (InterruptedException e) {
				// cancelled
			}
		}

		return fInitializedServices;
	}

	/**
	 * Look up a remote service provider and ensure it is initialized. The
	 * method will use the supplied container's progress service, or, if null,
	 * the platform progress service, in order to allow the initialization to be
	 * canceled.
	 * 
	 * @param id
	 *            id of service to locate
	 * @param context
	 *            context with progress service, or null to use the platform
	 *            progress service
	 * @return initialized remote services or null if the service can't be
	 *         located or the progress monitor was canceled
	 * @since 5.0
	 */
	public synchronized IRemoteServices getRemoteServices(final String id, IRunnableContext context) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				SubMonitor progress = SubMonitor.convert(monitor, Messages.PTPRemoteUIPlugin_4, 10);
				try {
					fRemoteService = PTPRemoteCorePlugin.getDefault().getRemoteServices(id, progress.newChild(10));
				} finally {
					monitor.done();
				}
			}
		};
		try {
			fRemoteService = PTPRemoteCorePlugin.getDefault().getRemoteServices(id);
			if (!fRemoteService.isInitialized()) {
				if (context != null) {
					context.run(true, false, runnable);
				} else {
					PlatformUI.getWorkbench().getProgressService().busyCursorWhile(runnable);
				}
			}
		} catch (InvocationTargetException e) {
			log(e);
		} catch (InterruptedException e) {
			// cancelled
		}

		return fRemoteService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
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

			for (IConfigurationElement ce : elements) {
				RemoteUIServicesProxy proxy = new RemoteUIServicesProxy(ce);
				services.put(proxy.getId(), proxy);
			}
		}

		return services;
	}

}
