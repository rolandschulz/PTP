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
package org.eclipse.cldt.internal.core;

import java.util.HashSet;

import org.eclipse.cldt.core.FortranCorePlugin;
import org.eclipse.cldt.core.FortranCorePreferenceConstants;
import org.eclipse.cldt.internal.core.model.CModelManager;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;


public class FortranCorePreferenceInitializer extends AbstractPreferenceInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
        Preferences preferences = FortranCorePlugin.getDefault().getPluginPreferences();
        HashSet optionNames = CModelManager.OptionNames;
    
        // Compiler settings
        preferences.setDefault(FortranCorePreferenceConstants.TRANSLATION_TASK_TAGS, FortranCorePreferenceConstants.DEFAULT_TASK_TAG); 
        optionNames.add(FortranCorePreferenceConstants.TRANSLATION_TASK_TAGS);

        preferences.setDefault(FortranCorePreferenceConstants.TRANSLATION_TASK_PRIORITIES, FortranCorePreferenceConstants.DEFAULT_TASK_PRIORITY); 
        optionNames.add(FortranCorePreferenceConstants.TRANSLATION_TASK_PRIORITIES);
        
        preferences.setDefault(FortranCorePreferenceConstants.CODE_FORMATTER, FortranCorePreferenceConstants.DEFAULT_CODE_FORMATTER); 
        optionNames.add(FortranCorePreferenceConstants.CODE_FORMATTER);
                
	}

}
