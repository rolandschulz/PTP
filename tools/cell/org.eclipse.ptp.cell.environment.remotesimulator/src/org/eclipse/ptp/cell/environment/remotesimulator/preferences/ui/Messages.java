/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.environment.remotesimulator.preferences.ui;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Leonardo Garcia, Daniel Felix Ferber
 * @since 3.0.0
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.environment.remotesimulator.preferences.ui.messages"; //$NON-NLS-1$
	public static String PreferencePage_Title;
	public static String PreferencePage_Description;
	public static String PreferencePage_HeaderLaunch;
	public static String PreferencePage_LabelSystemWorkspace;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME,
				Messages.class);
	}

	private Messages() {
	}
}
