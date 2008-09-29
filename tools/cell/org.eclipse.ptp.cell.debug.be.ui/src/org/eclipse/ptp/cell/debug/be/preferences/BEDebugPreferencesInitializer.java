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
package org.eclipse.ptp.cell.debug.be.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.cell.debug.be.ui.Activator;


/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 *
 */
public class BEDebugPreferencesInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(BEDebugPreferencesConstants.BE_GDB_BIN,BEDebugPreferencesConstants.BE_GDB_BIN_DEFAULT_VALUE);
		store.setDefault(BEDebugPreferencesConstants.BE_GDBSERVER_BIN,BEDebugPreferencesConstants.BE_GDBSERVER_BIN_DEFAULT_VALUE);
		store.setDefault(BEDebugPreferencesConstants.BE_GDBSERVER_PORT,"0"); //$NON-NLS-1$
		boolean save = false;
		if (store.isDefault(BEDebugPreferencesConstants.BE_GDBSERVER_PORT)) {
			store.setValue(BEDebugPreferencesConstants.BE_GDBSERVER_PORT,BEDebugPreferencesConstants.BE_GDBSERVER_PORT_DEFAULT_VALUE);
			save = true;
		}
		if (save)
			Activator.getDefault().savePluginPreferences();

	}

}
