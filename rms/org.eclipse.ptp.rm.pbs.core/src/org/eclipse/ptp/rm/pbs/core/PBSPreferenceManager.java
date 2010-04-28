/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * 	IBM Corporation - Initial API and implementation
 * 	Albert L. Rossi (NCSA) - Updated attributes (bug 310189)
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.core;

import org.eclipse.core.runtime.Preferences;

public class PBSPreferenceManager {
	private static final String SELECTED = "selectedAttributes";
	private static final String TEMPLATE = "attributeConfiguration";

	public static Preferences getPreferences() {
		return Activator.getDefault().getPluginPreferences();
	}

	public static String getSelectedAttributes() {
		return getPreferences().getString(SELECTED);
	}

	public static String getTemplatePreference() {
		return getPreferences().getString(TEMPLATE);
	}

	public static void savePreferences() {
		Activator.getDefault().savePluginPreferences();
	}

	public static void setSelectedAttributes(String selected) {
		getPreferences().setValue(SELECTED, selected);
	}

	public static void setTemplatePreference(String attrConfig) {
		getPreferences().setValue(TEMPLATE, attrConfig);
	}

}