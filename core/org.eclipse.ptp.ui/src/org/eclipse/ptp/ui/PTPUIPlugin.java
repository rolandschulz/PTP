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
import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.internal.ui.adapters.PropertyAdapterFactory;
import org.eclipse.ptp.internal.ui.adapters.WorkbenchAdapterAdapterFactory;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ptp.ui.consoles.ConsoleManager;
import org.eclipse.ptp.ui.managers.AbstractUIManager;
import org.eclipse.ptp.ui.managers.JobManager;
import org.eclipse.ptp.ui.managers.MachineManager;
import org.eclipse.ptp.ui.managers.RMManager;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.utils.DebugUtil;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPageFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main PTP user interface plugin.
 */
public class PTPUIPlugin extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "org.eclipse.ptp.ui"; //$NON-NLS-1$
    public static final String EXTENSION_POINT_ID = "rmConfigurations"; //$NON-NLS-1$
    
	//The shared instance.
	private static PTPUIPlugin plugin;
	
	/**
	 * Get the currently active workbench page from the active workbench window.
	 * 
	 * @return currently active workbench page
	 */
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow w = getActiveWorkbenchWindow();
		if (w != null) {
			return w.getActivePage();
		}
		return null;
	}

	/**
	 * Gets the currently active workbench window
	 * 
	 * @return the active workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static PTPUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Get the display instance.
	 * 
	 * @return the display instance
	 */
	public static Display getDisplay() {
		Display display= Display.getCurrent();
		if (display == null) {
			display= Display.getDefault();
		}
		return display;		
	}
	
	/**
	 * Get a unique identifier for this plugin (used for logging)
	 * 
	 * @return unique identifier
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null)
			return PLUGIN_ID;

		return getDefault().getBundle().getSymbolicName();
	}
	
	/**
	 * Generate a log message given an IStatus object
	 * 
	 * @param status IStatus object
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Generate a log message
	 * 
	 * @param msg message to log
	 */
	public static void log(String msg) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, msg, null));
	}

	/**
	 * Generate a log message for an exception
	 * 
	 * @param e exception used to generate message
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IPTPUIConstants.INTERNAL_ERROR, "Internal Error", e)); //$NON-NLS-1$
	}

	//Resource bundle.
	private final HashMap<String, RMConfigurationWizardPageFactory> configurationWizardPageFactories = new HashMap<String, RMConfigurationWizardPageFactory>();
	private AbstractUIManager machineManager = null;	
	private AbstractUIManager jobManager = null;
	private ConsoleManager consoleManager = null;
	private RMManager rmManager = null;
	
	public PTPUIPlugin() {
		super();
		plugin = this;
	}	
	
	/**
	 * Get the console manager instance
	 * 
	 * @return console manager
	 */
	public ConsoleManager getConsoleManager() {
		return consoleManager;
	}
	
	/**
	 * Get the job manager instance
	 * 
	 * @return job manager
	 */
	public AbstractUIManager getJobManager() {
		return jobManager;
	}
	
	/**
	 * Get the machine manager instance
	 * 
	 * @return machine manager
	 */
	public AbstractUIManager getMachineManager() {
		return machineManager;
	}
	
	/**
	 * Get the RM manager instance
	 * 
	 * @return RM manager
	 */
	public RMManager getRMManager() {
		return rmManager;
	}
	
	/**
	 * Get the wizard page factory associated with a resource manager factory
	 * 
	 * @param factory resource manager factory
	 * @return factory for creating wizard pages for this resource manager
	 */
	public RMConfigurationWizardPageFactory getRMConfigurationWizardPageFactory(IResourceManagerFactory factory) {
		return (RMConfigurationWizardPageFactory) configurationWizardPageFactories.get(factory.getClass().getName());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		DebugUtil.configurePluginDebugOptions();
		registerAdapterFactories();
		retrieveConfigurationWizardPageFactories();
		machineManager = new MachineManager();
		jobManager = new JobManager();
		consoleManager = new ConsoleManager();
		rmManager = new RMManager();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		machineManager.shutdown();
		jobManager.shutdown();
		consoleManager.shutdown();
		rmManager.shutdown();
		machineManager = null;
		jobManager = null;
		consoleManager = null;
		rmManager = null;
		plugin = null;
	}
	
	/**
	 * Register adapter factories
	 */
	private void registerAdapterFactories() {
		IAdapterManager manager = Platform.getAdapterManager();
		IAdapterFactory factory = new PropertyAdapterFactory();
		manager.registerAdapters(factory, IPElement.class);
		manager.registerAdapters(factory, IElement.class);
		factory = new WorkbenchAdapterAdapterFactory();
		manager.registerAdapters(factory, IPElement.class);
	}

	/**
	 * Locate and load wizard page factory extensions
	 */
	private void retrieveConfigurationWizardPageFactories() {
    	configurationWizardPageFactories.clear();
    	
    	IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(PLUGIN_ID, EXTENSION_POINT_ID);
		final IExtension[] extensions = extensionPoint.getExtensions();
		
		for (int iext = 0; iext < extensions.length; ++iext) {
			final IExtension ext = extensions[iext];
			
			final IConfigurationElement[] elements = ext.getConfigurationElements();
		
			for (int i=0; i< elements.length; i++)
			{
				IConfigurationElement ce = elements[i];
				try {
					RMConfigurationWizardPageFactory factory = (RMConfigurationWizardPageFactory) ce.createExecutableExtension("class"); //$NON-NLS-1$
					Class rmFactoryClass = factory.getRMFactoryClass();
					configurationWizardPageFactories.put(rmFactoryClass.getName(), factory);
				} catch (CoreException e) {
					log(e);
				}
			}
		}
    }
}
