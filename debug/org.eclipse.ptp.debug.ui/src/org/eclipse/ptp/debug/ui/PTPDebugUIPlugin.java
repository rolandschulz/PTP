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
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ptp.debug.internal.ui.PDebugImage;
import org.eclipse.ptp.debug.internal.ui.PDebugModelPresentation;
import org.eclipse.ptp.debug.internal.ui.UIDebugManager;
import org.eclipse.ptp.debug.ui.sourcelookup.DefaultSourceLocator;
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

	private static PTPDebugUIPlugin plugin;
	private static UIDebugManager uiDebugManager = null;
	
	/**
	 * Create a default source locator
	 * 
	 * @return default source locator
	 */
	public static IPersistableSourceLocator createDefaultSourceLocator() {
		return new DefaultSourceLocator();
	}
	
	/** 
	 * Show error dialog
	 * 
	 * @param shell
	 * @param title
	 * @param s
	 */
	public static void errorDialog(Shell shell, String title, IStatus s) {
		errorDialog(shell, title, s.getMessage(), s);
	}

	/** 
	 * Show error dialog
	 * 
	 * @param shell
	 * @param title
	 * @param message
	 * @param s
	 */
	public static void errorDialog(Shell shell, String title, String message, IStatus s) {
		if (s != null && message.equals(s.getMessage()))
			message = null;

		ErrorDialog.openError(shell, title, message, s);
	}
	
    /** 
	 * Show error dialog
	 * 
	 * @param shell
	 * @param title
	 * @param message
	 * @param t
	 */
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

	/** 
	 * Show error dialog
	 * 
	 * @param shell
	 * @param title
	 * @param t
	 */
	public static void errorDialog(Shell shell, String title, Throwable t) {
		errorDialog(shell, title, t.getMessage(), t);
	}
	
	/** 
	 * Show error dialog
	 * 
	 * @param title
	 * @param s
	 */
	public static void errorDialog(String title, IStatus s) {
		errorDialog(getActiveWorkbenchShell(), title, s.getMessage(), s);
	}
	
	/** 
	 * Show error dialog
	 * 
	 * @param title
	 * @param t
	 */
	public static void errorDialog(String title, Throwable t) {
		errorDialog(getActiveWorkbenchShell(), title, t.getMessage(), t);
	}

	/** 
	 * Get active workbench shell
	 * 
	 * @return active workbench shell
	 */
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}

	/** 
	 * Get active workbench window
	 * 
	 * @return active workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	/**
	 * @return
	 */
	public static PDebugModelPresentation getDebugModelPresentation() {
		return PDebugModelPresentation.getDefault();
	}

	/**
	 * Returns the shared instance.
	 */
	public static PTPDebugUIPlugin getDefault() {
		return plugin;
	}

	/** 
	 * Get display
	 * 
	 * @return display
	 */
	public static Display getDisplay() {
		Display display= Display.getCurrent();
		if (display == null) {
			display= Display.getDefault();
		}
		return display;		
	}
	
	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
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
	 * Get shell
	 * 
	 * @return shell
	 */
	public static Shell getShell() {
		if (getActiveWorkbenchWindow() != null) {
			return getActiveWorkbenchWindow().getShell();
		}
		return null;
	}
	
	/** 
	 * Get standard display
	 * 
	 * @return display
	 */
	public static Display getStandardDisplay() {
		Display display;
		display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		return display;
	}
	
	public static UIDebugManager getUIDebugManager() {
		return uiDebugManager;
	}
	
	/** Get unique identifier of plugin
	 * 
	 * @return plugin identifier
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null)
			return PLUGIN_ID;

		return getDefault().getBundle().getSymbolicName();
	}
	
	/** 
	 * Test if the current perspective is the PTP debug perspective
	 * 
	 * @return true if the current perspective is the PTP debug perspective
	 */
	public static boolean isPTPDebugPerspective() {
		return getCurrentPerspectiveID().equals(IPTPDebugUIConstants.ID_PERSPECTIVE_DEBUG);		
	}
	
	/**
	 * Test if the current perspective is one supplied by PTP
	 * 
	 * @return true if the current perspective is supplied by PTP
	 */
	public static boolean isPTPPerspective() {
		String curID = getCurrentPerspectiveID();
		if (curID == null)
			return false;
		return (curID.equals(IPTPUIConstants.PERSPECTIVE_RUN) || curID.equals(IPTPDebugUIConstants.ID_PERSPECTIVE_DEBUG));
	}
	
	/** 
	 * Log
	 * 
	 * @param status
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	/** 
	 * Log 
	 * 
	 * @param msg
	 */
	public static void log(String msg) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, msg, null));
	}
	
	/** 
	 * Log
	 * 
	 * @param e
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IPTPUIConstants.INTERNAL_ERROR, "Internal Error", e));
	}
	
	/** 
	 * Get current perspective ID
	 * 
	 * @return current perspective ID
	 */
	private static String getCurrentPerspectiveID() {
		return getActiveWorkbenchWindow().getActivePage().getPerspective().getId();
	}
	
	public static final String PDEBUGGERCONFIGURATION_EXTENSION_POINT_ID = "debuggerConfigurations";
	public static final String DEBUGGERID_ELEMENT = "debuggerID";

	//Resource bundle.
	private ResourceBundle resourceBundle;
	protected Map<String, IConfigurationElement> fDebuggerPageMap;

	/**
	 * The constructor.
	 */
	public PTPDebugUIPlugin() {
		super();
		plugin = this;
	}
	
	/** 
	 * Get launch debugger tab
	 * 
	 * @param debuggerID
	 * @return
	 * @throws CoreException
	 */
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
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		uiDebugManager = new UIDebugManager();
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
	 * Initialize launch debugger page  
	 * 
	 */
	protected void initializeDebuggerPageMap() {
		fDebuggerPageMap = new HashMap<String, IConfigurationElement>(10);
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, PDEBUGGERCONFIGURATION_EXTENSION_POINT_ID);
		IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
		for(int i = 0; i < infos.length; i++) {
			String id = infos[i].getAttribute(DEBUGGERID_ELEMENT);
			fDebuggerPageMap.put(id, infos[i]);
		}
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
	 */
	protected void initializeImageRegistry(ImageRegistry reg) {
    	PDebugImage.initializeImageRegistry(reg);
    }
}
