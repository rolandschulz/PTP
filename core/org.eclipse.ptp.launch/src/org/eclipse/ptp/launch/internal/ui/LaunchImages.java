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
package org.eclipse.ptp.launch.internal.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.swt.graphics.Image;

public class LaunchImages {
	private static final String NAME_PREFIX = "org.eclipse.ptp.launch.";
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();
    
	private static URL iconBaseURL = null;
	
	static {
		String pathSuffix = "icons/";
		iconBaseURL = PTPLaunchPlugin.getDefault().getBundle().getEntry(pathSuffix);
	}
	
	// The plugin registry
	private static ImageRegistry imageRegistry = null;
	private static HashMap avoidSWTErrorMap = null;

	public static final String IMG_PARALLEL_TAB = NAME_PREFIX + "parallel_tab.gif";
	public static final String IMG_ARGUMENT_TAB = NAME_PREFIX + "arguments_tab.gif";
	public static final String IMG_MAIN_TAB = NAME_PREFIX + "main_tab.gif";
	
	public static final ImageDescriptor DESC_PARALLEL_TAB = createManaged(IMG_PARALLEL_TAB);
	public static final ImageDescriptor DESC_ARGUMENT_TAB = createManaged(IMG_ARGUMENT_TAB);
	public static final ImageDescriptor DESC_MAIN_TAB = createManaged(IMG_MAIN_TAB);	
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
	
	private static ImageDescriptor create(String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(name));
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}
	
	private static URL makeIconFileURL(String name) throws MalformedURLException {
		if (iconBaseURL == null)
			throw new MalformedURLException();
			
		return new URL(iconBaseURL, name);
	}	
}
