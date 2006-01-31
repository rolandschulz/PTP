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
package org.eclipse.ptp.core;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.internal.core.ModelManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class PTPCorePlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.ptp.core";

	private IModelManager modelManager = null;

	// The shared instance.
	private static PTPCorePlugin plugin;

	// Resource bundle.
	private ResourceBundle resourceBundle;
	
	private IDGenerator IDGen;

	/**
	 * The constructor.
	 */
	public PTPCorePlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle(PLUGIN_ID
					+ ".ParallelPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		IDGen = new IDGenerator();
	}
	
	public int getNewID() {
		return IDGen.getNewID();
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		modelManager = new ModelManager();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		modelManager.shutdown();
		super.stop(context);
	}

	/**
	 * @return Returns the modelManager.
	 */
	public IModelManager getModelManager() {
		return modelManager;
	}

	/**
	 * Returns the shared instance.
	 */
	public static PTPCorePlugin getDefault() {
		return plugin;
	}

	public void refreshParallelPluginActions() {
		refreshPluginActions();
	}

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
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = PTPCorePlugin.getDefault().getResourceBundle();
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
		return resourceBundle;
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
	public static void log(String msg) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, msg, null));
	}
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, "Internal Error", e));
	}
	

	/*
	public void addPerspectiveListener(final IPerspectiveListener perspectiveListener) {
		IWorkbenchWindow workBenchWindow = PTPCorePlugin.getActiveWorkbenchWindow();
		if (workBenchWindow instanceof WorkbenchWindow) {
			workBenchWindow.addPerspectiveListener(perspectiveListener);
		}
	}
	public void removePerspectiveListener(final IPerspectiveListener perspectiveListener) {
		IWorkbenchWindow workBenchWindow = PTPCorePlugin.getActiveWorkbenchWindow();
		if (workBenchWindow instanceof WorkbenchWindow) {
			workBenchWindow.removePerspectiveListener(perspectiveListener);
		}
	}
	*/
}