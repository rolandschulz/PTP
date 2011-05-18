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
package org.eclipse.ptp.remotetools.environment.generichost.conf;

import org.eclipse.osgi.util.NLS;

/**
 * Defines names of attributes. Intended for an uniform and consistent use of
 * names for attributes in GUI, error messages and configuration files across
 * plugins.
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class AttributeNames extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.remotetools.environment.generichost.conf.attribute_names"; //$NON-NLS-1$

	public static String CONNECTION_PORT;
	public static String CONNECTION_TIMEOUT;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, AttributeNames.class);
	}

	private AttributeNames() {
	}
}
