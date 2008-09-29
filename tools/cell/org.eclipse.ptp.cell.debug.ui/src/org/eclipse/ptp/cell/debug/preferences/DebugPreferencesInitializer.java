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
package org.eclipse.ptp.cell.debug.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.cell.debug.ui.DebugUiPlugin;


/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 */
public class DebugPreferencesInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore store = DebugUiPlugin.getDefault().getPreferenceStore();
		store.setDefault(DebugPreferencesConstants.PPU_GDB_BIN,DebugPreferencesConstants.PPU_GDB_BIN_DEFAULT_VALUE);
		store.setDefault(DebugPreferencesConstants.PPU_GDBSERVER_BIN,DebugPreferencesConstants.PPU_GDBSERVER_BIN_DEFAULT_VALUE);
		store.setDefault(DebugPreferencesConstants.PPU_GDBSERVER_PORT,DebugPreferencesConstants.PPU_GDBSERVER_PORT_DEFAULT_VALUE);
		store.setDefault(DebugPreferencesConstants.SPU_GDB_BIN,DebugPreferencesConstants.SPU_GDB_BIN_DEFAULT_VALUE);
		store.setDefault(DebugPreferencesConstants.SPU_GDBSERVER_BIN,DebugPreferencesConstants.SPU_GDBSERVER_BIN_DEFAULT_VALUE);
		store.setDefault(DebugPreferencesConstants.SPU_GDBSERVER_PORT,"0"); //$NON-NLS-1$
		boolean save = false;
		if (store.isDefault(DebugPreferencesConstants.SPU_GDBSERVER_PORT)) {
			store.setValue(DebugPreferencesConstants.SPU_GDBSERVER_PORT,DebugPreferencesConstants.SPU_GDBSERVER_PORT_DEFAULT_VALUE);
			save = true;
		}
		if (save)
			DebugUiPlugin.getDefault().savePluginPreferences();

	}

}
