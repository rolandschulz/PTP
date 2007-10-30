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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.remotetools.preferences.PreferencesPlugin;


/**
 * Constant definitions for plug-in preferences
 * 
 * @author Ricardo M. Matinata
 * @since 1.0
 */
public class PreferenceConstants {

	//keys
	public static final String TIMING_SPUBIN = "timing-spubin";
		
	private IPreferenceStore preferences = null;
	private static PreferenceConstants instance = null;

	public PreferenceConstants() {
		super();
		preferences = PreferencesPlugin.getDefault().getPreferenceStore();
	}
	
	public static PreferenceConstants getInstance() {
		if (instance == null)
			instance = new PreferenceConstants();
		return instance;
	}
	
	public IPath getTIMING_SPUBIN() {
		return new Path(preferences.getString(TIMING_SPUBIN));
	}
	
	public IPath getDefaultTIMING_SPUBIN() {
		return new Path("spu_timing");
	}
	
	public void setTIMING_SPUBIN(IPath value) {
		preferences.setValue(TIMING_SPUBIN, value.toOSString());
		PreferencesPlugin.getDefault().savePluginPreferences();
	}
}
