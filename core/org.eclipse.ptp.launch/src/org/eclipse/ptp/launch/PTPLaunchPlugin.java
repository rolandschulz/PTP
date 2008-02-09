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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.launch.internal.ui.LaunchMessages;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory;
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteConnectionManager;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.IRemoteServices;
import org.eclipse.ptp.remote.PTPRemotePlugin;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class PTPLaunchPlugin extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "org.eclipse.ptp.launch";

	//The shared instance.
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
			ErrorDialog.openError(shell, LaunchMessages.getResourceString("LaunchUIPlugin.Error"), message, status); //$NON-NLS-1$
		}
	}

	/**
	 * Convenience method to create an error dialog given a message and Throwable.
	 * 
	 * @param message
	 * @param t
	 */
	public static void errorDialog(String message, Throwable t) {
		log(t);
		Shell shell = getActiveWorkbenchShell();
		if (shell != null) {
			IStatus status = new Status(IStatus.ERROR, getUniqueIdentifier(), 1, t.getMessage(), null); //$NON-NLS-1$	
			ErrorDialog.openError(shell, LaunchMessages.getResourceString("LaunchUIPlugin.Error"), message, status); //$NON-NLS-1$
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
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ptp.launch", path);
	}    

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
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
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e)); //$NON-NLS-1$
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
	
	// Resource bundle.
	private ResourceBundle resourceBundle;
	// Map of resource managers to launch configuration factories
	private final Map<Class<? extends IResourceManager>, AbstractRMLaunchConfigurationFactory> rmLaunchConfigurationFactories =
		new HashMap<Class<? extends IResourceManager>, AbstractRMLaunchConfigurationFactory>();

	/**
	 * The constructor.
	 */
	public PTPLaunchPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle.getBundle("org.eclipse.ptp.launch.LaunchPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}

	/**
	 * Find the launch configuration factory for a resource manager
	 * 
	 * @param rm resource manager
	 * @return launch configuration factory
	 */
	public AbstractRMLaunchConfigurationFactory getRMLaunchConfigurationFactory(IResourceManager rm) {
		if (rm == null) {
			return null;
		}
		return rmLaunchConfigurationFactories.get(rm.getClass());
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		retrieveRMLaunchConfigurationFactories();
	}
	
	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		rmLaunchConfigurationFactories.clear();
		plugin = null;
		resourceBundle = null;
	}
	
	/**
	 * Find the resource manager that corresponds to the unique name specified in the configuration
	 * 
	 * @param configuration launch configuration
	 * @return resource manager
	 * @throws CoreException
	 */
	public IResourceManager getResourceManager(ILaunchConfiguration configuration) throws CoreException {
		IPUniverse universe = PTPCorePlugin.getDefault().getUniverse();
		IResourceManager[] rms = universe.getResourceManagers();
		String rmUniqueName = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME, (String)null);
		for (IResourceManager rm : rms) {
			if (rm.getState() == ResourceManagerAttributes.State.STARTED &&
					rm.getUniqueName().equals(rmUniqueName)) {
				return rm;
			}
		}
		return null;
	}

	/**
	 * Verify that the resource "path" actually exists. This just checks
	 * that the path references something real.
	 * 
	 * @param path
	 * @param configuration
	 * @return IPath
	 * @throws CoreException
	 */
	public IPath verifyResource(String path, ILaunchConfiguration configuration) throws CoreException {
		IResourceManagerControl rm = (IResourceManagerControl)getResourceManager(configuration);
		if (rm != null) {
			IResourceManagerConfiguration conf = rm.getConfiguration();
			if (conf instanceof AbstractRemoteResourceManagerConfiguration) {
				AbstractRemoteResourceManagerConfiguration remConf = (AbstractRemoteResourceManagerConfiguration)conf;
				IRemoteServices remoteServices = PTPRemotePlugin.getDefault().getRemoteServices(remConf.getRemoteServicesId());
				if (remoteServices != null) {
					IRemoteConnectionManager connMgr = remoteServices.getConnectionManager();
					IRemoteConnection conn = connMgr.getConnection(remConf.getConnectionName());
					IRemoteFileManager fileManager = remoteServices.getFileManager(conn);
					try {
						IPath resPath = new Path(path);
						IFileStore res = fileManager.getResource(resPath, new NullProgressMonitor());
						if (res.fetchInfo().exists()) {
							return resPath;
						}
					} catch (IOException e) {
					}
				}
			} else {
				IPath resPath = new Path(path);
				if (resPath.toFile().exists()) {
					return resPath;
				}
			}
		}
		return null;
	}

	/**
	 * Find all launch configuration factory extensions that have been registered
	 */
	private void retrieveRMLaunchConfigurationFactories() {
    	rmLaunchConfigurationFactories.clear();
    	
    	IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(PLUGIN_ID + ".rmLaunchConfiguration");
		final IExtension[] extensions = extensionPoint.getExtensions();
		
		for (int iext = 0; iext < extensions.length; ++iext) {
			final IExtension ext = extensions[iext];
			
			final IConfigurationElement[] elements = ext.getConfigurationElements();
		
			for (int i=0; i< elements.length; i++)
			{
				IConfigurationElement ce = elements[i];
				try {
					AbstractRMLaunchConfigurationFactory factory = (AbstractRMLaunchConfigurationFactory) ce.createExecutableExtension("class");
					Class<? extends IResourceManager> resourceManagerClass = factory.getResourceManagerClass();
					rmLaunchConfigurationFactories.put(resourceManagerClass, factory);
				} catch (CoreException e) {
					log(e);
				}
			}
		}
    }
}
