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
package org.eclipse.ptp.services.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ptp.internal.services.ui.ServicesUIPlugin;
import org.eclipse.swt.graphics.Image;

public class ServiceModelImages {

	// ==== URLs for Icon Folders ==== 
	public final static URL ICON_URL = ServicesUIPlugin.getDefault().getBundle().getEntry("icons/etool16/"); //$NON-NLS-1$
	
	// ===== Icon Files =====
	public static final String IMG_SERVICE = "service.gif"; //$NON-NLS-1$
	public static final String IMG_SERVICE_CATEGORY = "service-category.gif"; //$NON-NLS-1$
	public static final String IMG_SERVICE_DISABLED = "service-disabled.gif"; //$NON-NLS-1$
	
	private static ImageRegistry imageRegistry = null;

	static {
		createImageDescriptor(ICON_URL, IMG_SERVICE, IMG_SERVICE);
		createImageDescriptor(ICON_URL, IMG_SERVICE_CATEGORY, IMG_SERVICE_CATEGORY);
		createImageDescriptor(ICON_URL, IMG_SERVICE_DISABLED, IMG_SERVICE_DISABLED);
	}
	
	/** 
	 * Create an image descriptor and add it to the registry
	 * 
	 * @param iconURL url for image
	 * @param key image name
	 * @param name file name of image
	 * @return image descriptor for image
	 */
	public static ImageDescriptor createImageDescriptor(URL iconURL, String key, String name) {
		try {
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(getIconURL(iconURL, name));
			addImageDescriptor(key, imageDescriptor);
			return imageDescriptor;
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}
	
	/** 
	 * Get image from the registry
	 * 
	 * @param key image name
	 * @return image
	 */
	public static Image getImage(String key) {
		return getImageRegistry().get(key);
	}
	
	/** 
	 * Get image descriptor from the registry
	 * 
	 * @param key image name
	 * @return image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		return getImageRegistry().getDescriptor(key);
	}
	
	/** 
	 * Add image descriptor to registry
	 * 
	 * @param key image name
	 * @param imageDescriptor
	 */
	private static void addImageDescriptor(String key, ImageDescriptor imageDescriptor) {
		getImageRegistry().put(key, imageDescriptor);
	}
		
	/** 
	 * Create URL for icon
	 * 
	 * @param baseURL base URL for icon
	 * @param name name of icon file
	 * @return URL pointing to icon file
	 * @throws MalformedURLException
	 */
	private static URL getIconURL(URL baseURL, String name) throws MalformedURLException {
		if (baseURL == null) {
			throw new MalformedURLException();
		}
		return new URL(baseURL, name);
	}
	
	/** 
	 * Get the image registry
	 * 
	 * @return image registry
	 */
	private static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
		}
		return imageRegistry;
	}
}
