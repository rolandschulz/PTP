/******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.managedbuilder.gnu.core;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IConfigurationBuildMacroSupplier;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.cell.managedbuilder.gnu.core.debug.Debug;
import org.eclipse.ptp.cell.preferences.PreferencesPlugin;
import org.eclipse.ptp.cell.preferences.ui.PreferenceConstants;


/**
 * @author laggarcia
 *
 */
public class GnuToolChainMacroSupplier implements
		IConfigurationBuildMacroSupplier {

	private static final String SYSROOT_ECLIPSE_MACRO = "cell_sysroot_path"; // name of the macro

	public GnuToolChainMacroSupplier() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IConfigurationBuildMacroSupplier#getMacro(java.lang.String, org.eclipse.cdt.managedbuilder.core.IConfiguration, org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider)
	 */
	public IBuildMacro getMacro(String macroName, IConfiguration configuration,
			IBuildMacroProvider provider) {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_SUPPLIER, macroName, configuration.getId(), provider.getClass().getName());
		
		if( macroName.compareToIgnoreCase(SYSROOT_ECLIPSE_MACRO) == 0 ){
			assert( PreferencesPlugin.getDefault() != null );
			
			IPreferenceStore store = PreferencesPlugin.getDefault().getPreferenceStore();
			
			assert( store != null );
			
			String path = store.getString(PreferenceConstants.SDK_SYSROOT);
			
			IBuildMacro result = new BuildMacro(SYSROOT_ECLIPSE_MACRO,IBuildMacro.VALUE_PATH_DIR, path );
			
			Debug.POLICY.exit(Debug.DEBUG_SUPPLIER, path);
			
			return result;
		}
		
		Debug.POLICY.exit(Debug.DEBUG_SUPPLIER, null);		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IConfigurationBuildMacroSupplier#getMacros(org.eclipse.cdt.managedbuilder.core.IConfiguration, org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider)
	 */
	public IBuildMacro[] getMacros(IConfiguration configuration,
			IBuildMacroProvider provider) {
		
		IBuildMacro[] buildMacros = new BuildMacro[1];
		
		assert( PreferencesPlugin.getDefault() != null );
		
		IPreferenceStore store = PreferencesPlugin.getDefault().getPreferenceStore();
		
		assert( store != null );
		
		String path = store.getString(PreferenceConstants.SDK_SYSROOT);
		
		buildMacros[0] = new BuildMacro(SYSROOT_ECLIPSE_MACRO,IBuildMacro.VALUE_PATH_DIR, path);
		
		return buildMacros;
	}

}
