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

import java.io.File;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.internal.core.ModelManager;
import org.eclipse.swt.widgets.Display;
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
	public static Display getDisplay() {
		Display display= Display.getCurrent();
		if (display == null) {
			display= Display.getDefault();
		}
		return display;		
	}		
	public static void errorDialog(final String title, final String message, final Throwable t) {
		getDisplay().syncExec(new Runnable() {
			public void run() {
				errorDialog(getDisplay().getActiveShell(), title, message, t);
			}
		});
	}
	public static void errorDialog(Shell shell, String title, String message, Throwable t) {
		IStatus status;
		if (t instanceof CoreException) {
			status = ((CoreException)t).getStatus();
		} else {
			status = new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, "Error within PTP Core: ", t);
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

	public static void warningDialog(final String title, final String message) {
		getDisplay().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openWarning(getDisplay().getActiveShell(), title, message);
			}
		});
	}
	public static void informationDialog(final String title, final String message) {
		getDisplay().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(getDisplay().getActiveShell(), title, message);
			}
		});
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
	public String locateFragmentFile(String fragment, String file) {
		String	filePath = null;
		URL		url = Platform.find(Platform.getBundle(PTPCorePlugin.PLUGIN_ID), new Path("/"));

		if (url != null) {
			try {
				File path = new File(Platform.asLocalURL(url).getPath());
				String ipath = path.getAbsolutePath();
				System.out.println("Plugin install dir = '"+ipath+"'");
				
				/* org.eclipse.ptp.orte.linux.x86_64_1.0.0
				   org.eclipse.ptp.orte.$(OS).$(ARCH)_$(VERSION) */
				String ptp_version = (String)getDefault().getBundle().getHeaders().get("Bundle-Version");
				System.out.println("PTP Version = "+ptp_version);
				Properties p = System.getProperties();
				String os = p.getProperty("osgi.os");
				String arch = p.getProperty("osgi.arch");
				System.out.println("osgi.os = "+os);
				System.out.println("osgi.arch = "+arch);
				if(os != null && arch != null && ptp_version != null) {
					String combo = PLUGIN_ID;
					System.out.println("[1] Searching for plug-in directory: "+combo);
					int idx = ipath.indexOf(combo);
					/* if we found it */
					if(idx > 0) {
						String ipath2 = ipath.substring(0, idx)+fragment+"."+os+"."+arch+"_"+ptp_version+"/bin/"+file;
						System.out.println("[2] Searching for '"+file+"' in: "+ipath2);
						File f = new File(ipath2);
						if(f.exists()) {
							filePath = ipath2;
							System.out.println("\tFOUND HERE!");
						}
						else {
							ipath2 = ipath.substring(0, idx)+fragment+"."+os+"."+arch+"/bin/"+file;
							System.out.println("[3] Searching for '"+file+"' in: "+ipath2);
							f = new File(ipath2);
							if(f.exists()) {
								filePath = ipath2;
								System.out.println("\tFOUND HERE!");
							}
						}
					}
				}
				
				if(filePath == null) {
					int idx = ipath.indexOf(PLUGIN_ID);
					String ipath2 = ipath.substring(0, idx) + fragment+"/"+file;
					System.out.println("[4] Searching for: "+ipath2);
					File f = new File(ipath2);
					if(f.exists()) {
						filePath = ipath2;
						System.out.println("\tFOUND HERE!");
					}
				}
			} catch(Exception e) { 
			}
		}
		return filePath;
	}
}