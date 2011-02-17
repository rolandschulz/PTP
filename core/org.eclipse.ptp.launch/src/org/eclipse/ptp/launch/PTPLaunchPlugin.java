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

package org.eclipse.ptp.launch;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.launch.messages.Messages;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class PTPLaunchPlugin extends AbstractUIPlugin {
	private static final String PLUGIN_ID = "org.eclipse.ptp.launch"; //$NON-NLS-1$

	public static final String EXTENSION_POINT_ID = "rmLaunchConfigurations"; //$NON-NLS-1$
	public static final String RESOURCE_BUNDLE = PLUGIN_ID + ".LaunchPluginResources"; //$NON-NLS-1$

	// The shared instance.
	private static PTPLaunchPlugin plugin;

	/**
	 * Convenience method to create an error dialog given an IStatus.
	 * 
	 * @param message
	 * @param status
	 */
	public static void errorDialog(String message, IStatus status) {
		log(status);
		Shell shell = getActiveWorkbenchShell();
		if (shell != null) {
			ErrorDialog.openError(shell, Messages.Launch_common_Error, message, status);
		}
	}

	/**
	 * Convenience method to create an error dialog given a message and
	 * Throwable.
	 * 
	 * @param message
	 * @param t
	 */
	public static void errorDialog(String message, Throwable t) {
		log(t);
		Shell shell = getActiveWorkbenchShell();
		if (shell != null) {
			IStatus status = new Status(IStatus.ERROR, getUniqueIdentifier(), 1, t.getMessage(), null);
			ErrorDialog.openError(shell, Messages.Launch_common_Error, message, status);
		}
	}

	/**
	 * Convenience method to get the currently active page
	 * 
	 * @return currently active page
	 */
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow w = getActiveWorkbenchWindow();
		if (w != null) {
			return w.getActivePage();
		}
		return null;
	}

	/**
	 * Returns the active workbench shell or <code>null</code> if none
	 * 
	 * @return the active workbench shell or <code>null</code> if none
	 */
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}

	/**
	 * Convenience method to get the currently active workbench window
	 * 
	 * @return currently active workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	/**
	 * Returns the shared instance.
	 */
	public static PTPLaunchPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = PTPLaunchPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Get a unique identifier for this plugin
	 * 
	 * @return unique identifier
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
	 * Logs an internal error with the specified throwable
	 * 
	 * @param e
	 *            the exception to be logged
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
	}

	/**
	 * Logs an internal error with the specified message.
	 * 
	 * @param message
	 *            the error message to log
	 */
	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, message, null));
	}

	/*
	 * Launch notification listeners
	 */
	private final ListenerList listeners = new ListenerList();

	// Resource bundle.
	private ResourceBundle resourceBundle;

	// Map of resource managers to launch configuration factories
	private final Map<Class<? extends IResourceManagerControl>, AbstractRMLaunchConfigurationFactory> rmLaunchConfigurationFactories = new HashMap<Class<? extends IResourceManagerControl>, AbstractRMLaunchConfigurationFactory>();

	/**
	 * The constructor.
	 */
	public PTPLaunchPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Add a listener for ILaunchNotification events
	 * 
	 * @param listener
	 *            listener to add
	 */
	public void addLaunchNotificationListener(ILaunchNotification listener) {
		listeners.add(listener);
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null) {
				resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
			}
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}

	/**
	 * Find the resource manager that corresponds to the unique name specified
	 * in the configuration
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return resource manager
	 * @throws CoreException
	 * @since 5.0
	 */
	public IResourceManagerControl getResourceManager(ILaunchConfiguration configuration) throws CoreException {
		String rmUniqueName = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME,
				(String) null);
		IResourceManagerControl rm = PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(rmUniqueName);
		if (rm.getState().equals(IResourceManagerControl.STARTED_STATE)) {
			return rm;
		}
		return null;
	}

	/**
	 * Find the launch configuration factory for a resource manager
	 * 
	 * @param rm
	 *            resource manager
	 * @return launch configuration factory
	 * @since 5.0
	 */
	public AbstractRMLaunchConfigurationFactory getRMLaunchConfigurationFactory(IResourceManagerControl rm) {
		if (rm == null) {
			return null;
		}
		return rmLaunchConfigurationFactories.get(rm.getClass());
	}

	/**
	 * Notify listeners when a job changes status
	 * 
	 * @param job
	 *            job that has changed status
	 */
	public void notifyJobStateChange(IPJob job, JobAttributes.State state) {
		for (Object listener : listeners.getListeners()) {
			try {
				((ILaunchNotification) listener).jobStateChange(job, state);
			} catch (Exception e) {
				PTPLaunchPlugin.log(e);
			}
		}
	}

	/**
	 * Remove a listener for ILaunchNotification events
	 * 
	 * @param listener
	 *            listener to remove
	 */
	public void removeLaunchNotificationListener(ILaunchNotification listener) {
		listeners.remove(listener);
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		retrieveRMLaunchConfigurationFactories();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		rmLaunchConfigurationFactories.clear();
		plugin = null;
		resourceBundle = null;
	}

	/**
	 * Verify that the resource "path" actually exists. This just checks that
	 * the path references something real.
	 * 
	 * @param path
	 *            path to check
	 * @param configuration
	 *            launch configuration
	 * @param monitor
	 *            progress monitor
	 * @return IPath representation of path
	 * @throws CoreException
	 *             if the resource doesn't exist or the monitor is cancelled
	 * @since 5.0
	 */
	public IPath verifyResource(String path, ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		IResourceManagerControl rm = getResourceManager(configuration);
		if (rm == null) {
			throw new CoreException(new Status(IStatus.ERROR, getUniqueIdentifier(), Messages.PTPLaunchPlugin_4));
		}
		IResourceManagerConfiguration conf = rm.getConfiguration();
		IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(conf.getRemoteServicesId(), monitor);
		if (monitor.isCanceled()) {
			throw new CoreException(new Status(IStatus.ERROR, getUniqueIdentifier(),
					Messages.PTPLaunchPlugin_Operation_cancelled_by_user));
		}
		if (remoteServices == null) {
			throw new CoreException(new Status(IStatus.ERROR, getUniqueIdentifier(), Messages.PTPLaunchPlugin_0));
		}
		IRemoteConnectionManager connMgr = remoteServices.getConnectionManager();
		if (connMgr == null) {
			throw new CoreException(new Status(IStatus.ERROR, getUniqueIdentifier(), Messages.PTPLaunchPlugin_1));
		}
		IRemoteConnection conn = connMgr.getConnection(conf.getConnectionName());
		if (conn == null) {
			throw new CoreException(new Status(IStatus.ERROR, getUniqueIdentifier(), Messages.PTPLaunchPlugin_2));
		}
		IRemoteFileManager fileManager = remoteServices.getFileManager(conn);
		if (fileManager == null) {
			throw new CoreException(new Status(IStatus.ERROR, getUniqueIdentifier(), Messages.PTPLaunchPlugin_3));
		}
		if (!fileManager.getResource(path).fetchInfo().exists()) {
			throw new CoreException(new Status(IStatus.INFO, getUniqueIdentifier(), NLS.bind(Messages.PTPLaunchPlugin_5,
					new Object[] { path })));
		}
		return new Path(path);
	}

	/**
	 * Find all launch configuration factory extensions that have been
	 * registered
	 */
	private void retrieveRMLaunchConfigurationFactories() {
		rmLaunchConfigurationFactories.clear();

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(PLUGIN_ID, EXTENSION_POINT_ID);
		final IExtension[] extensions = extensionPoint.getExtensions();

		for (int iext = 0; iext < extensions.length; ++iext) {
			final IExtension ext = extensions[iext];

			final IConfigurationElement[] elements = ext.getConfigurationElements();

			for (int i = 0; i < elements.length; i++) {
				IConfigurationElement ce = elements[i];
				try {
					AbstractRMLaunchConfigurationFactory factory = (AbstractRMLaunchConfigurationFactory) ce
							.createExecutableExtension("class"); //$NON-NLS-1$
					Class<? extends IResourceManagerControl> resourceManagerClass = factory.getResourceManagerClass();
					rmLaunchConfigurationFactories.put(resourceManagerClass, factory);
				} catch (CoreException e) {
					log(e);
				}
			}
		}
	}
}
