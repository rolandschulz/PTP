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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.cell.debug.ui.DebugUiPlugin;


/**
 * 
 * @author Ricardo M. Matinata
 * @since 1.3
 */
public class DebugPreferencesConstants {
	
	public static String PPU_GDB_BIN = "debug-ppu-gdb-bin"; //$NON-NLS-1$
	
	public static String PPU_GDB_BIN_DEFAULT_VALUE = DebugPreferencesDefaults.getString("DebugPreferencesConstants.0"); //$NON-NLS-1$
	
	public static String PPU_GDBSERVER_BIN = "debug-ppu-gdbserver-bin"; //$NON-NLS-1$
	
	public static String PPU_GDBSERVER_BIN_DEFAULT_VALUE = DebugPreferencesDefaults.getString("DebugPreferencesConstants.1"); //$NON-NLS-1$
	
	public static String PPU_GDBSERVER_PORT = "debug-ppu-gdbserver-port"; //$NON-NLS-1$
	
	public static String PPU_GDBSERVER_PORT_DEFAULT_VALUE = DebugPreferencesDefaults.getString("DebugPreferencesConstants.2"); //$NON-NLS-1$
	
	public static String SPU_GDB_BIN = "debug-spu-gdb-bin"; //$NON-NLS-1$
	
	public static String SPU_GDB_BIN_DEFAULT_VALUE = DebugPreferencesDefaults.getString("DebugPreferencesConstants.3"); //$NON-NLS-1$
	
	public static String SPU_GDBSERVER_BIN = "debug-spu-gdbserver-bin"; //$NON-NLS-1$
	
	public static String SPU_GDBSERVER_BIN_DEFAULT_VALUE = DebugPreferencesDefaults.getString("DebugPreferencesConstants.4"); //$NON-NLS-1$
	
	public static String SPU_GDBSERVER_PORT = "debug-spu-gdbserver-port"; //$NON-NLS-1$
	
	public static String SPU_GDBSERVER_PORT_DEFAULT_VALUE = DebugPreferencesDefaults.getString("DebugPreferencesConstants.5"); //$NON-NLS-1$
	
	public static IPreferenceStore getStore() {
		return DebugUiPlugin.getDefault().getPreferenceStore();
	}

	public static String getPPU_GDB_BIN_VALUE() {
		return getStore().getString(PPU_GDB_BIN);
	}
	
	public static String getPPU_GDBSERVER_BIN_VALUE() {
		return getStore().getString(PPU_GDBSERVER_BIN);
	}
	
	public static String getPPU_GDBSERVER_PORT_VALUE() {
		return getStore().getString(PPU_GDBSERVER_PORT);
	}
	
	public static String getSPU_GDB_BIN_VALUE() {
		return getStore().getString(SPU_GDB_BIN);
	}
	
	public static String getSPU_GDBSERVER_BIN_VALUE() {
		return getStore().getString(SPU_GDBSERVER_BIN);
	}
	
	public static String getSPU_GDBSERVER_PORT_VALUE() {
		return getStore().getString(SPU_GDBSERVER_PORT);
	}
	
}
