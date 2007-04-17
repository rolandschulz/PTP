/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - current implementation
 *    modified from version provided by LANL
 *******************************************************************************/
package org.eclipse.ptp.tau.core.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ptp.tau.core.TAULaunchPlugin;
import org.eclipse.swt.graphics.Image;

public class LaunchImages {
	private static final String NAME_PREFIX = "org.eclipse.ptp.tau.core.";
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();
    
	private static URL iconBaseURL = null;
	
	static {
		String pathSuffix = "icons/";
		iconBaseURL = TAULaunchPlugin.getDefault().getBundle().getEntry(pathSuffix);
	}
	
	// The plugin registry
	private static ImageRegistry imageRegistry = null;
	private static HashMap avoidSWTErrorMap = null;

	public static final String IMG_PARALLEL_TAB = NAME_PREFIX + "parallel_tab.gif";
	public static final String IMG_ANALYSIS_TAB = NAME_PREFIX + "tauLogo.gif";//parallel_tab.gif
	public static final String IMG_ARGUMENTS_TAB = NAME_PREFIX + "arguments_tab.gif";
	public static final String IMG_MAIN_TAB = NAME_PREFIX + "main_tab.gif";
	public static final String IMG_DEBUGGER_TAB = NAME_PREFIX + "debugger_tab.gif";
	
	public static final ImageDescriptor DESC_PARALLEL_TAB = createManaged(IMG_PARALLEL_TAB);
	public static final ImageDescriptor DESC_ANALYSIS_TAB = createManaged(IMG_ANALYSIS_TAB);
	public static final ImageDescriptor DESC_ARGUMENTS_TAB = createManaged(IMG_ARGUMENTS_TAB);
	public static final ImageDescriptor DESC_MAIN_TAB = createManaged(IMG_MAIN_TAB);	
	public static final ImageDescriptor DESC_DEBUGGER_TAB = createManaged(IMG_DEBUGGER_TAB);	
	/**
	 * Returns the image managed under the given key in this registry.
	 * 
	 * @param key the image's key
	 * @return the image managed under the given key
	 */ 
	public static Image getImage(String key) {
		return getImageRegistry().get(key);
	}
	public static ImageDescriptor getDescriptor(String key) {
		return getImageRegistry().getDescriptor(key);
	}
	
	private static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
			for (Iterator iter = avoidSWTErrorMap.keySet().iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				imageRegistry.put(key, (ImageDescriptor) avoidSWTErrorMap.get(key));
			}
			avoidSWTErrorMap = null;
		}
		return imageRegistry;
	}
	
	private static ImageDescriptor createManaged(String name) {
		try {
			ImageDescriptor result = ImageDescriptor.createFromURL(makeIconFileURL(name.substring(NAME_PREFIX_LENGTH)));
			if (avoidSWTErrorMap == null) {
				avoidSWTErrorMap = new HashMap(); 
			}
			avoidSWTErrorMap.put(name, result);
			if (imageRegistry != null) {
			    System.out.println("Internal Error: Image registry already defined");
			}
			return result;
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}
	/*
	private static ImageDescriptor create(String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(name));
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}*/
	
	private static URL makeIconFileURL(String name) throws MalformedURLException {
		if (iconBaseURL == null)
			throw new MalformedURLException();
			
		return new URL(iconBaseURL, name);
	}	
}
