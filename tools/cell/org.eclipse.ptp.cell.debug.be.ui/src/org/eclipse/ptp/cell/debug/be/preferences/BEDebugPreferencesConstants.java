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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.cell.debug.be.ui.Activator;


/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 *
 */
public class BEDebugPreferencesConstants {
	
	public static String BE_GDB_BIN = "debug-be-gdb-bin"; //$NON-NLS-1$
	
	public static String BE_GDB_BIN_DEFAULT_VALUE = BEDebugPreferencesDefaults.getString("DebugPreferencesConstants.0"); //$NON-NLS-1$
	
	public static String BE_GDBSERVER_BIN = "debug-be-gdbserver-bin"; //$NON-NLS-1$
	
	public static String BE_GDBSERVER_BIN_DEFAULT_VALUE = BEDebugPreferencesDefaults.getString("DebugPreferencesConstants.4"); //$NON-NLS-1$
	
	public static String BE_GDBSERVER32_BIN = "debug-be-gdbserver32-bin"; //$NON-NLS-1$
	
	// public static String BE_GDBSERVER32_BIN_DEFAULT_VALUE = BEDebugPreferencesDefaults.getString("DebugPreferencesConstants.1"); //$NON-NLS-1$
	
	public static String BE_GDBSERVER_PORT = "debug-be-gdbserver-port"; //$NON-NLS-1$
	
	public static String BE_GDBSERVER_PORT_DEFAULT_VALUE = BEDebugPreferencesDefaults.getString("DebugPreferencesConstants.2"); //$NON-NLS-1$
	
	
	public static IPreferenceStore getStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	public static String getBE_GDB_BIN_VALUE() {
		return getStore().getString(BE_GDB_BIN);
	}
	
	public static String getBE_GDBSERVER_BIN_VALUE() {
		return getStore().getString(BE_GDBSERVER_BIN);
	}
	
	public static String getBE_GDBSERVER32_BIN_VALUE() {
		return getStore().getString(BE_GDBSERVER32_BIN);
	}
	
	public static String getBE_GDBSERVER_PORT_VALUE() {
		return getStore().getString(BE_GDBSERVER_PORT);
	}
	
}
