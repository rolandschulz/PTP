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
package org.eclipse.ptp.launch.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.swt.graphics.Image;

public class LaunchImages {
	private static final String NAME_PREFIX = PTPLaunchPlugin.getUniqueIdentifier() + '.';
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	// The plugin registry
	private static ImageRegistry imageRegistry = new ImageRegistry();

	// Subdirectory (under the package containing this class) where 16 color
	// images are
	private static URL fgIconBaseURL;
	static {
		fgIconBaseURL = Platform.getBundle(PTPLaunchPlugin.getUniqueIdentifier()).getEntry("/icons/"); //$NON-NLS-1$
	}

	public static final String IMG_PARALLEL_TAB = NAME_PREFIX + "parallel_tab.gif"; //$NON-NLS-1$
	public static final String IMG_ARGUMENTS_TAB = NAME_PREFIX + "arguments_tab.gif"; //$NON-NLS-1$
	public static final String IMG_MAIN_TAB = NAME_PREFIX + "main_tab.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUGGER_TAB = NAME_PREFIX + "debugger_tab.gif"; //$NON-NLS-1$

	public static final ImageDescriptor DESC_PARALLEL_TAB = createManaged(IMG_PARALLEL_TAB);
	public static final ImageDescriptor DESC_ARGUMENTS_TAB = createManaged(IMG_ARGUMENTS_TAB);
	public static final ImageDescriptor DESC_MAIN_TAB = createManaged(IMG_MAIN_TAB);
	public static final ImageDescriptor DESC_DEBUGGER_TAB = createManaged(IMG_DEBUGGER_TAB);

	/**
	 * Returns the image managed under the given key in this registry.
	 * 
	 * @param key
	 *            the image's key
	 * @return the image managed under the given key
	 */
	public static Image getImage(String key) {
		return getImageRegistry().get(key);
	}

	public static ImageDescriptor getDescriptor(String key) {
		return getImageRegistry().getDescriptor(key);
	}

	/**
	 * Helper method to access the image registry from the JavaPlugin class.
	 */
	static ImageRegistry getImageRegistry() {
		return imageRegistry;
	}

	private static ImageDescriptor createManaged(String name) {
		return createManaged(imageRegistry, name);
	}

	private static ImageDescriptor createManaged(ImageRegistry registry, String name) {
		ImageDescriptor result = ImageDescriptor.createFromURL(makeIconFileURL(name.substring(NAME_PREFIX_LENGTH)));
		registry.put(name, result);
		return result;
	}

	public static Image get(String key) {
		return imageRegistry.get(key);
	}

	private static URL makeIconFileURL(String name) {
		try {
			return new URL(fgIconBaseURL, name);
		} catch (MalformedURLException e) {
			PTPLaunchPlugin.log(e);
			return null;
		}
	}
}
