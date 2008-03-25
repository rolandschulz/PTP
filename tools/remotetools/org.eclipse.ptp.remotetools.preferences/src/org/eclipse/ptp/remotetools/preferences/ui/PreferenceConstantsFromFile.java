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
package org.eclipse.ptp.remotetools.preferences.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author Richard Maciel
 *
 * @since 1.2.3
 */
public class PreferenceConstantsFromFile extends NLS
{
	private static final String BUNDLE_ID = "org.eclipse.ptp.remotetools.preferences.PluginResources"; //$NON-NLS-1$
	
	public static String TIMING_SPUBIN_VALUE;
	
	public static String searchButtonText;
	
	public static String searchEngineInformationTitle;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_ID, PreferenceConstantsFromFile.class);
	}

	private PreferenceConstantsFromFile() {
		// cannot create new instance
	}
}
