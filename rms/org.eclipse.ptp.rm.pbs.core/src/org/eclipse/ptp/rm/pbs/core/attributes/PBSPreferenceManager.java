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
package org.eclipse.ptp.rm.pbs.core.attributes;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.pbs.core.Activator;

/**
 * @since 5.0
 */
public class PBSPreferenceManager extends AbstractPreferenceInitializer {
	private static final String SELECTED = "selectedAttributes"; //$NON-NLS-1$
	private static final String TEMPLATE = "attributeConfiguration"; //$NON-NLS-1$

	/**
	 * @since 4.0
	 */
	public static String getSelectedAttributes() {
		return Preferences.getString(Activator.getUniqueIdentifier(), SELECTED);
	}

	/**
	 * @since 4.0
	 */
	public static String getTemplatePreference() {
		return Preferences.getString(Activator.getUniqueIdentifier(), TEMPLATE);
	}

	public static void savePreferences() {
		Preferences.savePreferences(Activator.getUniqueIdentifier());
	}

	/**
	 * @since 4.0
	 */
	public static void setSelectedAttributes(String selected) {
		Preferences.setString(Activator.getUniqueIdentifier(), SELECTED, selected);
	}

	/**
	 * @since 4.0
	 */
	public static void setTemplatePreference(String attrConfig) {
		Preferences.setString(Activator.getUniqueIdentifier(), TEMPLATE, attrConfig);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		Preferences.setDefaultString(Activator.getUniqueIdentifier(), SELECTED, ""); //$NON-NLS-1$
		Preferences.setDefaultString(Activator.getUniqueIdentifier(), TEMPLATE, ""); //$NON-NLS-1$
	}

}