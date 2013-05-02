/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class ImageManager {
    private static ImageRegistry imageRegistry = null;

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
