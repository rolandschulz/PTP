/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.internal.core;

import java.util.HashSet;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.fdt.core.CCorePlugin;
import org.eclipse.fdt.core.CCorePreferenceConstants;
import org.eclipse.fdt.internal.core.model.CModelManager;


public class CCorePreferenceInitializer extends AbstractPreferenceInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
        Preferences preferences = CCorePlugin.getDefault().getPluginPreferences();
        HashSet optionNames = CModelManager.OptionNames;
    
        // Compiler settings
        preferences.setDefault(CCorePreferenceConstants.TRANSLATION_TASK_TAGS, CCorePreferenceConstants.DEFAULT_TASK_TAG); 
        optionNames.add(CCorePreferenceConstants.TRANSLATION_TASK_TAGS);

        preferences.setDefault(CCorePreferenceConstants.TRANSLATION_TASK_PRIORITIES, CCorePreferenceConstants.DEFAULT_TASK_PRIORITY); 
        optionNames.add(CCorePreferenceConstants.TRANSLATION_TASK_PRIORITIES);
        
        preferences.setDefault(CCorePreferenceConstants.CODE_FORMATTER, CCorePreferenceConstants.DEFAULT_CODE_FORMATTER); 
        optionNames.add(CCorePreferenceConstants.CODE_FORMATTER);
                
	}

}
