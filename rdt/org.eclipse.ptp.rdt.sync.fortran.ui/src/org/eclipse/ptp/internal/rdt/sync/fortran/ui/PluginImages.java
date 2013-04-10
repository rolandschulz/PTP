/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.fortran.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;

public class PluginImages {
	// Subdirectory (under the package containing this class) where 16 color
	// images are
	private static URL fgIconBaseURL;

	static {
		try {
			fgIconBaseURL = new URL(Activator.getDefault().getBundle().getEntry("/"), "icons/"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (MalformedURLException e) {
			Activator.log(e);
		}
	}
	private static final String T_WIZBAN = "wizban/"; //$NON-NLS-1$

	/**
	 * new sync project icon
	 */
	public static final ImageDescriptor DESC_WIZBAN_NEW_REMOTE_C_PROJ = create(T_WIZBAN, "newremote_proj_wiz.gif"); //$NON-NLS-1$

	private static ImageDescriptor create(String prefix, String name) {
		return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
	}

	private static URL makeIconFileURL(String prefix, String name) {
		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append(name);
		try {
			return new URL(fgIconBaseURL, buffer.toString());
		} catch (MalformedURLException e) {
			Activator.log(e);
			return null;
		}
	}
}
