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
import java.util.ArrayList;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.internal.core.ModelManager;
import org.eclipse.ptp.internal.rmsystem.NullResourceManager;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerChangedListener;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class PTPCorePlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.ptp.core";

	// The shared instance.
	private static PTPCorePlugin plugin;

	public static void errorDialog(Shell shell, String title, IStatus s) {
		errorDialog(shell, title, s.getMessage(), s);
	}
	
	public static void errorDialog(Shell shell, String title, String message, IStatus s) {
		if (s != null && message != null && message.equals(s.getMessage()))
			message = null;

		ErrorDialog.openError(shell, title, message, s);
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

	public static void errorDialog(final String title, final String message, final Throwable t) {
		getDisplay().syncExec(new Runnable() {
			public void run() {
				errorDialog(getDisplay().getActiveShell(), title, message, t);
			}
		});
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

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static PTPCorePlugin getDefault() {
		return plugin;
	}

	public static Display getDisplay() {
		Display display= Display.getCurrent();
		if (display == null) {
			display= Display.getDefault();
		}
		return display;		
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

	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

	public static void informationDialog(final String title, final String message) {
		getDisplay().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(getDisplay().getActiveShell(), title, message);
			}
		});
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(String msg) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, msg, null));
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, "Internal Error", e));
	}

	public static void warningDialog(final String title, final String message) {
		getDisplay().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openWarning(getDisplay().getActiveShell(), title, message);
			}
		});
	}

	// Resource bundle.
	private ResourceBundle resourceBundle;

	private IDGenerator IDGen;

	private IResourceManagerFactory[] resourceManagerFactories;
	private IResourceManager[] resourceManagers;
	private IResourceManager currentResourceManager;
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
	public void addResourceManagerChangedListener(IResourceManagerChangedListener listener) {
		// TODO
	}		
	public IResourceManager getCurrentResourceManager() {
		return currentResourceManager;
	}
	/**
	 * @return Returns the modelManager.
	 */
	public IModelPresentation getModelPresentation() {
		return currentResourceManager.getModelPresentation();
	}
	
	public int getNewID() {
		return IDGen.getNewID();
	}
	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public IResourceManagerFactory[] getResourceManagerFactories()
	{
		ArrayList configList = new ArrayList();

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint("org.eclipse.ptp.core.resourcemanager");
		final IExtension[] extensions = extensionPoint.getExtensions();
		
		for (int iext = 0; iext < extensions.length; ++iext) {
			final IExtension ext = extensions[iext];
			
			IConfigurationElement[] elements = ext.getConfigurationElements();
		
			for (int i=0; i< elements.length; i++)
			{
				IConfigurationElement ce = elements[i];
				try {
					AbstractResourceManagerFactory factory = (AbstractResourceManagerFactory) ce.createExecutableExtension("class");
					factory.setName(ce.getAttribute("name"));
					factory.setId(ce.getAttribute("id"));
					factory.setSupportLocal(Boolean.valueOf(ce.getAttribute("supportLocal")).booleanValue());
					factory.setSupportRemote(Boolean.valueOf(ce.getAttribute("supportRemote")).booleanValue());
					factory.setNeedPort(Boolean.valueOf(ce.getAttribute("needPort")).booleanValue());
					configList.add(factory);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		return (IResourceManagerFactory[]) configList.toArray(new IResourceManagerFactory[configList.size()]);
	}
	public IResourceManagerFactory getResourceManagerFactory(String id)
	{
		IResourceManagerFactory[] factories = getResourceManagerFactories();
		for (int i=0; i<factories.length; i++)
		{
			if (factories[i].getId().equals(id)) return factories[i];
		}
		
		return null;
	}
	public IResourceManager[] getResourceManagers() {
		return resourceManagers;
	}

	public String locateFragmentFile(String fragment, String file) {		
		Bundle[] frags = Platform.getFragments(Platform.getBundle(PTPCorePlugin.PLUGIN_ID));
		String os = Platform.getOS();
		String arch = Platform.getOSArch();
		String frag_os_arch = fragment+"."+os+"."+arch;
		System.out.println("OS = '"+os+"', Architecture = '"+arch+"', OS_ARCH combo = '"+frag_os_arch+"'");
		String ptp_version = (String)getDefault().getBundle().getHeaders().get("Bundle-Version");
		System.out.println("PTP Version = "+ptp_version);
		
		System.out.println("All Found Fragments:");
		for(int i=0; i<frags.length; i++) {
			System.out.println("\t"+frags[i].toString());
		}
		
		for(int i=0; i<frags.length; i++) {
			Bundle frag = frags[i];
			URL path = frag.getEntry("/");
			try {
				URL local_path = FileLocator.toFileURL(path);
				String str_path = local_path.getPath();
				System.out.println("Testing fragment "+(i+1)+" with this OS/arch - path: '"+str_path+"'");
				
				/* 
				 * OK so now we know where the absolute path of this fragment is -
				 * but is this the fragment for the machine we're running on?
				 * 
				 * First: check for a 'bin' directory in the fragment. This may be an architecture
				 * 		  independent fragment.
				 * 
				 * Second: check for a 'bin' directory in the fragment with the os and arch appended.
				 */
				int idx = str_path.indexOf(fragment);
				if(idx > 0) {
					String file_path = str_path + "bin/"+file;
					System.out.println("\tSearching for file in '"+file_path+"'");
					File f = new File(file_path);
					if(f.exists()) {
						System.out.println("\t\t**** FOUND IT!");
						return file_path;
					}
				}
				idx = str_path.indexOf(frag_os_arch);
				if(idx > 0) {
					/* found it!  This is the right fragment for our OS & arch */
					System.out.println("\tCorrect fragment for our OS & arch");
					String file_path = str_path + "bin/"+file;
					System.out.println("\tSearching for file in '"+file_path+"'");
					File f = new File(file_path);
					if(f.exists()) {
						System.out.println("\t\t**** FOUND IT!");
						return file_path;
					}
				}

			} catch(Exception e) { }
		}
		
		/* guess we never found it.... */
		return null;
	}
	
	public void refreshParallelPluginActions() {
		refreshPluginActions();
	}
	
	public void removeResourceManagerChangedListener(IResourceManagerChangedListener listener) {
		// TODO
	}
	
	public void setCurrentResourceManager(IResourceManager rmManager) {
		try {
			currentResourceManager.stop();
		} catch (CoreException e) {
			log(e.getMessage());
		}
		
		currentResourceManager = rmManager;
		try {
			currentResourceManager.start();
		} catch (CoreException e) {
			log(e.getMessage());
		}		
	}
	
	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		resourceManagerFactories = getResourceManagerFactories();
		// TODO RMR this must be changed!
		currentResourceManager = new NullResourceManager(new ModelManager());
		resourceManagers = new IResourceManager[]{currentResourceManager};
	}
	
	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		stopResourceManagers();
		super.stop(context);
	}
	
	/**
	 * stops all of the resource managers.
	 * 
	 * @throws CoreException
	 */
	private void stopResourceManagers() throws CoreException {
		for (int i = 0; i<resourceManagers.length; ++i) {
			resourceManagers[i].stop();
		}
	}

	public IModelManager getModelManager() {
		return getCurrentResourceManager().getModelManager();
	}
}