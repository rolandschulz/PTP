/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *

*****************************************************************************/
package org.eclipse.ptp.cell.pdt.xml.preferences.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.cell.pdt.xml.Activator;
import org.eclipse.ptp.cell.pdt.xml.core.PdtXmlBean;


/**
 * Preference initializer for the PDT plugin
 * 
 * @author Richard Maciel
 *
 */
public class PdtPreferenceInitializer extends AbstractPreferenceInitializer {
	
	/**
	 * 
	 */
	public PdtPreferenceInitializer() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		store.setDefault(PdtXmlBean.ATTR_EVENT_GROUP_DIR, PdtPreferencesDefaultValues.PdtPreferenceInitializer_Default_EventGroupDir);

	}

}
