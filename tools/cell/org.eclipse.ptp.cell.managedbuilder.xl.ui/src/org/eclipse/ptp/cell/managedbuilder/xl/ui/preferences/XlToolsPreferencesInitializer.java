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
package org.eclipse.ptp.cell.managedbuilder.xl.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.cell.managedbuilder.xl.core.preferences.XlToolsProperties;
import org.eclipse.ptp.cell.managedbuilder.xl.ui.XlManagedBuilderUIPlugin;


/**
 * @author laggarcia
 * @since 3.0.0
 */
public class XlToolsPreferencesInitializer extends
		AbstractPreferenceInitializer {

	/**
	 * 
	 */
	public XlToolsPreferencesInitializer() {
		// Make standard constructor available for extension point.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = XlManagedBuilderUIPlugin.getDefault()
				.getPreferenceStore();
		store.setDefault(XlToolsProperties.xlToolsPath,
				XlToolsProperties.xlToolsPathDefaultValue);
	}

}
