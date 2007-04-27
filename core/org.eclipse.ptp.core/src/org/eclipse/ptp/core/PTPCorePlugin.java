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
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.internal.core.ModelManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class PTPCorePlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.ptp.core";

	// The shared instance.
	private static PTPCorePlugin plugin;

	/**
	 * Returns the shared instance.
	 */
	public static PTPCorePlugin getDefault() {
		return plugin;
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

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(String msg) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, msg, null));
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, "Internal Error", e));
	}

	// Resource bundle.
	private ResourceBundle resourceBundle;

	private ModelManager modelManager;
	
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
	}
	
	public IModelManager getModelManager() {
		return modelManager;
	}
	
	/**
	 * @return Returns the modelManager.
	 */
	public IModelPresentation getModelPresentation() {
		return modelManager;
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/**
	 * Convenience function to return the universe
	 * @return the universe
	 */
	public IPUniverse getUniverse() {
		return getModelPresentation().getUniverse();
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
				 * Check each fragment that matches our os and arch for a bin directory.
				 */

				int idx = str_path.indexOf(frag_os_arch);
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
	
	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);

		modelManager = new ModelManager();
		new Job("Starting Model Manager"){

			protected IStatus run(IProgressMonitor monitor) {
				try {
					modelManager.start(monitor);
				} catch (CoreException e) {
					return e.getStatus();
				}
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		modelManager.shutdown();
		super.stop(context);
	}

}