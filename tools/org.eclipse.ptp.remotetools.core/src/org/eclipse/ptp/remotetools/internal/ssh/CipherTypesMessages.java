/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.internal.ssh;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class CipherTypesMessages {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.remotetools.internal.ssh.ciphertypesmessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private CipherTypesMessages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
