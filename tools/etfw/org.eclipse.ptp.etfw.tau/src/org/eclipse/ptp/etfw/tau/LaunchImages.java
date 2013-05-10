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
package org.eclipse.ptp.etfw.tau;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
//import org.eclipse.ptp.etfw.tau.core.TAULaunchPlugin;
//import org.eclipse.ptp.etfw.tau.Activator;
import org.eclipse.ptp.etfw.tau.messages.Messages;
import org.eclipse.swt.graphics.Image;

/**
 * Manages graphics/icons for the TAU launch system
 * 
 * @author wspear
 * 
 */
public class LaunchImages {
	private static final String NAME_PREFIX = "org.eclipse.ptp.etfw.tau.core."; //$NON-NLS-1$
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	private static URL iconBaseURL = null;

	static {
		final String pathSuffix = "icons/"; //$NON-NLS-1$
		iconBaseURL = Activator.getDefault().getBundle().getEntry(pathSuffix);
	}

	// The plugin registry
	private static ImageRegistry imageRegistry = null;
	private static HashMap<String, ImageDescriptor> avoidSWTErrorMap = null;

	public static final String IMG_PARALLEL_TAB = NAME_PREFIX + "parallel_tab.gif"; //$NON-NLS-1$
	public static final String IMG_ANALYSIS_TAB = NAME_PREFIX + "tauLogo.gif";//parallel_tab.gif //$NON-NLS-1$
	public static final String IMG_ARGUMENTS_TAB = NAME_PREFIX + "arguments_tab.gif"; //$NON-NLS-1$
	public static final String IMG_MAIN_TAB = NAME_PREFIX + "main_tab.gif"; //$NON-NLS-1$
	public static final String IMG_DEBUGGER_TAB = NAME_PREFIX + "debugger_tab.gif"; //$NON-NLS-1$

	public static final ImageDescriptor DESC_PARALLEL_TAB = createManaged(IMG_PARALLEL_TAB);
	public static final ImageDescriptor DESC_ANALYSIS_TAB = createManaged(IMG_ANALYSIS_TAB);
	public static final ImageDescriptor DESC_ARGUMENTS_TAB = createManaged(IMG_ARGUMENTS_TAB);
	public static final ImageDescriptor DESC_MAIN_TAB = createManaged(IMG_MAIN_TAB);
	public static final ImageDescriptor DESC_DEBUGGER_TAB = createManaged(IMG_DEBUGGER_TAB);

	private static ImageDescriptor createManaged(String name) {
		try {
			final ImageDescriptor result = ImageDescriptor.createFromURL(makeIconFileURL(name.substring(NAME_PREFIX_LENGTH)));
			if (avoidSWTErrorMap == null) {
				avoidSWTErrorMap = new HashMap<String, ImageDescriptor>();
			}
			avoidSWTErrorMap.put(name, result);
			if (imageRegistry != null) {
				System.out.println(Messages.LaunchImages_InternalErrorRegDefined);
			}
			return result;
		} catch (final MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	/*
	 * private static ImageDescriptor create(String name) {
	 * try {
	 * return ImageDescriptor.createFromURL(makeIconFileURL(name));
	 * } catch (MalformedURLException e) {
	 * return ImageDescriptor.getMissingImageDescriptor();
	 * }
	 * }
	 */
	public static ImageDescriptor getDescriptor(String key) {
		return getImageRegistry().getDescriptor(key);
	}

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

	private static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
			for (final String string : avoidSWTErrorMap.keySet()) {
				final String key = string;
				imageRegistry.put(key, avoidSWTErrorMap.get(key));
			}
			avoidSWTErrorMap = null;
		}
		return imageRegistry;
	}

	private static URL makeIconFileURL(String name) throws MalformedURLException {
		if (iconBaseURL == null) {
			throw new MalformedURLException();
		}

		return new URL(iconBaseURL, name);
	}
}
