/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.preferences;

import org.eclipse.osgi.util.NLS;

/**
 * @since 4.1
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rdt.ui.preferences.messages"; //$NON-NLS-1$
	public static String RemoteDevPreferencePage_ErrorReportingGroupLabel;
	public static String RemoteDevPreferencePage_IndexErrorLimitLabel;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
