/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.debug.be.launch.remote.debugger;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 *
 */
public class RemoteDBGBEDefaults {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.debug.be.launch.remote.debugger.RemoteDBGBEDefaults"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private RemoteDBGBEDefaults() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
