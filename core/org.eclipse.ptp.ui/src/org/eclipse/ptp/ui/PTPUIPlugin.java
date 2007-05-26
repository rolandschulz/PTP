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

package org.eclipse.ptp.ui;

import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.internal.ui.adapters.PropertyAdapterFactory;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ptp.ui.managers.AbstractUIManager;
import org.eclipse.ptp.ui.managers.JobManager;
import org.eclipse.ptp.ui.managers.MachineManager;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPageFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class PTPUIPlugin extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "org.eclipse.ptp.ui";

	//The shared instance.
	private static PTPUIPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;

	private final HashMap<String, RMConfigurationWizardPageFactory> configurationWizardPageFactories = new HashMap<String, RMConfigurationWizardPageFactory>();
	
	private AbstractUIManager machineManager = null;
	private AbstractUIManager jobManager = null;

	public PTPUIPlugin() {
		super();
		plugin = this;
	}
	public void start(BundleContext context) throws Exception {
		super.start(context);
		registerAdapterFactories();
		retrieveConfigurationWizardPageFactories();
		machineManager = new MachineManager();
		jobManager = new JobManager();
	}

	private void registerAdapterFactories() {
		IAdapterManager manager = Platform.getAdapterManager();
		IAdapterFactory factory = new PropertyAdapterFactory();
		manager.registerAdapters(factory, IResourceManager.class);
		manager.registerAdapters(factory, IPElement.class);
	}
	
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		machineManager.shutdown();
		jobManager.shutdown();
		machineManager = null;
		jobManager = null;
		plugin = null;
		resourceBundle = null;
	}
	
	public RMConfigurationWizardPageFactory getRMConfigurationWizardPageFactory(IResourceManagerFactory factory) {
		return (RMConfigurationWizardPageFactory) configurationWizardPageFactories.get(factory.getClass().getName());
	}
	
	public static String getUniqueIdentifier() {
		if (getDefault() == null)
			return PLUGIN_ID;

		return getDefault().getBundle().getSymbolicName();
	}	
	
	public AbstractUIManager getMachineManager() {
		return machineManager;
	}
	
	public AbstractUIManager getJobManager() {
		return jobManager;
	}

	/**
	 * Returns the shared instance.
	 */
	public static PTPUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = PTPUIPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle.getBundle("org.eclipse.ptp.ui.UIPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ptp.ui", path);
	}
	
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}	
	
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow w = getActiveWorkbenchWindow();
		if (w != null) {
			return w.getActivePage();
		}
		return null;
	}
	/*
    public String getPluginPath() {
        try {
            return Platform.resolve(Platform.getBundle(PLUGIN_ID).getEntry("/")).getPath();
        } catch (IOException e) {
        	return null;
        }
    }
    */
	
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
	public static Shell getShell() {
		if (getActiveWorkbenchWindow() != null) {
			return getActiveWorkbenchWindow().getShell();
		}
		return null;
	}	
	
	public String getCurrentPerspectiveID() {
		return getActiveWorkbenchWindow().getActivePage().getPerspective().getId();
	}
	
	public void addPersepectiveListener(IPerspectiveListener listener) {
		getActiveWorkbenchWindow().addPerspectiveListener(listener);
	}
	public void removePersepectiveListener(IPerspectiveListener listener) {
		getActiveWorkbenchWindow().removePerspectiveListener(listener);
	}
	
	/***** LOG *****/
	public static void log(String msg) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, msg, null));
	}
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IPTPUIConstants.INTERNAL_ERROR, "Internal Error", e));
	}
	public static Display getDisplay() {
		Display display= Display.getCurrent();
		if (display == null) {
			display= Display.getDefault();
		}
		return display;		
	}		
	public static void errorDialog(Shell shell, String title, String message, Throwable t) {
		IStatus status;
		if (t instanceof CoreException) {
			status = ((CoreException)t).getStatus();
		} else {
			status = new Status(IStatus.ERROR, getUniqueIdentifier(), IPTPUIConstants.INTERNAL_ERROR, "Error within PTP UI: ", t);
			log(status);	
		}
		errorDialog(shell, title, message, status);
	}
	public static void errorDialog(Shell shell, String title, IStatus s) {
		errorDialog(shell, title, s.getMessage(), s);
	}
	public static void errorDialog(Shell shell, String title, String message, IStatus s) {
		if (s != null && message != null && message.equals(s.getMessage()))
			message = null;

		ErrorDialog.openError(shell, title, message, s);
	}

	private void retrieveConfigurationWizardPageFactories() {
    	configurationWizardPageFactories.clear();
    	
    	IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint("org.eclipse.ptp.ui.rmconfiguration");
		final IExtension[] extensions = extensionPoint.getExtensions();
		
		for (int iext = 0; iext < extensions.length; ++iext) {
			final IExtension ext = extensions[iext];
			
			final IConfigurationElement[] elements = ext.getConfigurationElements();
		
			for (int i=0; i< elements.length; i++)
			{
				IConfigurationElement ce = elements[i];
				try {
					RMConfigurationWizardPageFactory factory = (RMConfigurationWizardPageFactory) ce.createExecutableExtension("class");
					Class rmFactoryClass = factory.getRMFactoryClass();
					configurationWizardPageFactories.put(rmFactoryClass.getName(), factory);
				} catch (CoreException e) {
					log(e);
				}
			}
		}
    }
}
