/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.fdt.make.internal.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.fdt.make.internal.ui.preferences.MakePreferencePage;
import org.eclipse.fdt.make.internal.ui.preferences.MakefileEditorPreferenceConstants;
import org.eclipse.fdt.make.internal.ui.preferences.MakefileEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;


public class MakeUIPreferenceInitializer extends AbstractPreferenceInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = MakeUIPlugin.getDefault().getPreferenceStore();
		MakePreferencePage.initDefaults(store);
		MakefileEditorPreferenceConstants.initializeDefaultValues(store);
		MakefileEditorPreferencePage.initDefaults(store);
	}

}
