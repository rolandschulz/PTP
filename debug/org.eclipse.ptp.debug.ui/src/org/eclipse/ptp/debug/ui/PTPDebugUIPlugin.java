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
package org.eclipse.ptp.debug.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.debug.internal.ui.UIDebugManager;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author clement chu
 * The main plugin class to be used in the desktop.
 */
public class PTPDebugUIPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.ptp.debug.ui";
	//The shared instance.
	private static PTPDebugUIPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	private UIDebugManager uiDebugManager = null;
	protected Map fDebuggerPageMap;

	/**
	 * The constructor.
	 */
	public PTPDebugUIPlugin() {
		super();
		plugin = this;
	}

	public static String getUniqueIdentifier() {
		if (getDefault() == null)
			return PLUGIN_ID;

		return getDefault().getBundle().getSymbolicName();
	}
	
	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		uiDebugManager = new UIDebugManager();
		//refreshPluginActions();
	}
	
	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		uiDebugManager.shutdown();
		uiDebugManager = null;
		super.stop(context);
		plugin = null;
		resourceBundle = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static PTPDebugUIPlugin getDefault() {
		return plugin;
	}

	public UIDebugManager getUIDebugManager() {
		return uiDebugManager;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = PTPDebugUIPlugin.getDefault().getResourceBundle();
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
				resourceBundle = ResourceBundle.getBundle("org.eclipse.ptp.debug.ui.UiPluginResources");
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
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ptp.debug.ui", path);
	}
	
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}
	
	public static Display getDisplay() {
		Display display= Display.getCurrent();
		if (display == null) {
			display= Display.getDefault();
		}
		return display;		
	}	
	
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}
	
	public static String getCurrentPerspectiveID() {
		return getActiveWorkbenchWindow().getActivePage().getPerspective().getId();
	}
	public static boolean isPTPDebugPerspective() {
		return getCurrentPerspectiveID().equals(IPTPDebugUIConstants.ID_PERSPECTIVE_DEBUG);		
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
	public static void errorDialog(String title, Throwable t) {
		errorDialog(getActiveWorkbenchShell(), title, t.getMessage(), t);
	}
	public static void errorDialog(Shell shell, String title, Throwable t) {
		errorDialog(shell, title, t.getMessage(), t);
	}
	public static void errorDialog(Shell shell, String title, String message, Throwable t) {
		IStatus status;
		if (t instanceof CoreException) {
			status = ((CoreException)t).getStatus();
		} else {
			status = new Status(IStatus.ERROR, getUniqueIdentifier(), IPTPUIConstants.INTERNAL_ERROR, "Error within Debug UI: ", t);
			log(status);	
		}
		errorDialog(shell, title, message, status);
	}
	public static void errorDialog(String title, IStatus s) {
		errorDialog(getActiveWorkbenchShell(), title, s.getMessage(), s);
	}
	public static void errorDialog(Shell shell, String title, IStatus s) {
		errorDialog(shell, title, s.getMessage(), s);
	}
	public static void errorDialog(Shell shell, String title, String message, IStatus s) {
		if (s != null && message.equals(s.getMessage()))
			message = null;

		ErrorDialog.openError(shell, title, message, s);
	}
	public ILaunchConfigurationTab getDebuggerPage(String debuggerID) throws CoreException {
		if (fDebuggerPageMap == null) {
			initializeDebuggerPageMap();
		}
		IConfigurationElement configElement = (IConfigurationElement)fDebuggerPageMap.get(debuggerID);
		ILaunchConfigurationTab tab = null;
		if (configElement != null) {
			tab = (ILaunchConfigurationTab)configElement.createExecutableExtension("class");
		}
		return tab;
	}
	protected void initializeDebuggerPageMap() {
		fDebuggerPageMap = new HashMap(10);
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "PDebuggerPage");
		IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
		for(int i = 0; i < infos.length; i++) {
			String id = infos[i].getAttribute("debuggerID");
			fDebuggerPageMap.put(id, infos[i]);
		}
	}
	
	public static Display getStandardDisplay() {
		Display display;
		display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		return display;
	}
	
	public static Shell getShell() {
		if (getActiveWorkbenchWindow() != null) {
			return getActiveWorkbenchWindow().getShell();
		}
		return null;
	}	
}
