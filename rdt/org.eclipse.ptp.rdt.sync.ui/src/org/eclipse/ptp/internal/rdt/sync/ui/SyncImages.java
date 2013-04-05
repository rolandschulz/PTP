/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ptp.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.swt.graphics.Image;

public class SyncImages {
	private static final String NAME_PREFIX = RDTSyncUIPlugin.PLUGIN_ID + '.';
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	// The plugin registry
	private static ImageRegistry imageRegistry = new ImageRegistry();

	// Subdirectory (under the icons directory within this plugin) where 16 color images are
	private static final String T_TABS = "obj16/"; //$NON-NLS-1$
	
	private static URL fgIconBaseURL;
	static {
		try {
			fgIconBaseURL = new URL(RDTSyncUIPlugin.getDefault().getBundle().getEntry("/"), "icons/");//$NON-NLS-1$ //$NON-NLS-2$
		} catch (MalformedURLException e) {
			RDTSyncUIPlugin.log(e);
		} 	
	}

	public static String EXCLUDE = NAME_PREFIX + "exclude_minus.png"; //$NON-NLS-1$
	public static String INCLUDE = NAME_PREFIX + "include_plus.png"; //$NON-NLS-1$
	// alternate images to consider
	public static String EXCLUDE_X = NAME_PREFIX + "exclude_X.png"; //$NON-NLS-1$
	public static String INCLUDE_CHECK = NAME_PREFIX + "include_check.png"; //$NON-NLS-1$

	public static final ImageDescriptor DESC_EXCLUDE = createManaged(T_TABS, EXCLUDE);
	public static final ImageDescriptor DESC_INCLUDE = createManaged(T_TABS, INCLUDE);
	public static final ImageDescriptor DESC_EXCLUDE_X = createManaged(T_TABS, EXCLUDE_X);
	public static final ImageDescriptor DESC_INCLUDE_CHECK = createManaged(T_TABS, INCLUDE_CHECK);

	private static ImageDescriptor createManaged(String prefix, String name) {
		return createManaged(imageRegistry, prefix, name);
	}

	private static ImageDescriptor createManaged(ImageRegistry registry, String prefix, String name) {
		ImageDescriptor result = ImageDescriptor.createFromURL(makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
		registry.put(name, result);
		return result;
	}

	public static Image get(String key) {
		return imageRegistry.get(key);
	}

	private static URL makeIconFileURL(String prefix, String name) {
		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append(name);
		try {
			return new URL(fgIconBaseURL, buffer.toString());
		} catch (MalformedURLException e) {
			RDTSyncUIPlugin.log(e);
			return null;
		}
	}

	/**
	 * Helper method to access the image registry from the JavaPlugin class.
	 */
	static ImageRegistry getImageRegistry() {
		return imageRegistry;
	}
}
