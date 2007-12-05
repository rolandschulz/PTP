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

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.util.DebugUtil;
import org.eclipse.ptp.internal.core.ModelManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class PTPCorePlugin extends Plugin {
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

	/**
	 * Generate a unique identifier
	 * 
	 * @return unique identifier string
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
	 * Create log entry from an IStatus
	 * 
	 * @param status
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Create log entry from a string
	 * 
	 * @param msg
	 */
	public static void log(String msg) {
		if (DebugUtil.RM_MESSAGES) {
			System.err.println(msg);
		}
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, msg, null));
	}

	/**
	 * Create log entry from a Throwable
	 * 
	 * @param e
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, "Internal Error", e));
	}

	/*
	 * Resource bundle
	 */
	private ResourceBundle resourceBundle;
	/*
	 * ModelManager for this Eclipse session
	 */
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
	
	/**
	 * Get the model manager
	 * 
	 * @return the model manager
	 */
	public IModelManager getModelManager() {
		return modelManager;
	}
	
	/**
	 * Get the presentation manager. This is now the model manager.
	 * 
	 * @return the presentation manager
	 */
	public IModelPresentation getModelPresentation() {
		return getModelManager();
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

	/**
	 * Locate the fragment for our architecture. This should really be phased out, since
	 * there is now no guarantee that there will be local executables for the proxy
	 * server or debugger.
	 * 
	 * @param fragment
	 * @param file
	 * @return path to "bin" directory in fragment
	 */
	public String locateFragmentFile(String fragment, String file) {		
		Bundle[] frags = Platform.getFragments(Platform.getBundle(PTPCorePlugin.PLUGIN_ID));
		
		if (frags != null) {
			String os = Platform.getOS();
			String arch = Platform.getOSArch();
			String frag_os_arch = fragment+"."+os+"."+arch;
			
			for (int i=0; i<frags.length; i++) {
				Bundle frag = frags[i];
				URL path = frag.getEntry("/");
				try {
					URL local_path = FileLocator.toFileURL(path);
					String str_path = local_path.getPath();
					
					/* 
					 * Check each fragment that matches our os and arch for a bin directory.
					 */
	
					int idx = str_path.indexOf(frag_os_arch);
					if (idx > 0) {
						/* found it!  This is the right fragment for our OS & arch */
						String file_path = str_path + "bin/"+file;
						File f = new File(file_path);
						if (f.exists()) {
							return file_path;
						}
					}
	
				} catch(Exception e) { 
				}
			}
		}
		
		/* guess we never found it.... */
		return null;
	}
	
	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		DebugUtil.configurePluginDebugOptions();
		modelManager = new ModelManager();
		modelManager.start();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		modelManager.shutdown();
	}

}