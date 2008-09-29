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
package org.eclipse.ptp.cell.managedbuilder.gnu.core;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.utils.envvar.EnvVarOperationProcessor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.cell.managedbuilder.core.CellToolChainEnvironmentSupplier;
import org.eclipse.ptp.cell.managedbuilder.gnu.core.debug.Debug;
import org.eclipse.ptp.cell.managedbuilder.gnu.core.preferences.GnuToolsProperties;


/**
 * @author laggarcia
 * 
 */
public class GnuToolChainEnvironmentSupplier extends
		CellToolChainEnvironmentSupplier {

	private static IPreferenceStore store;

	public static void setPreferenceStore(IPreferenceStore store) {
		GnuToolChainEnvironmentSupplier.store = store;
	}

	protected String getModifiedPathEnvironmentVariable(
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_SUPPLIER, configuration.getId(), provider.getClass().getName());
		
		IEnvironmentVariable path = provider.getVariable(
				PATH_ENVIRONMENT_VARIABLE, configuration, true);
		if (path != null) {
			Debug.POLICY.trace(Debug.DEBUG_SUPPLIER, "Path: {0}", path.getValue()); //$NON-NLS-1$
			String result = EnvVarOperationProcessor.performAppend(path.getValue(),
					store.getString(GnuToolsProperties.gnuToolsPath), provider
							.getDefaultDelimiter());
			Debug.POLICY.exit(Debug.DEBUG_SUPPLIER, result);
			return result;
		} else {
			Debug.POLICY.trace(Debug.DEBUG_SUPPLIER, "Path: <null>"); //$NON-NLS-1$
		}
		Debug.POLICY.exit(Debug.DEBUG_SUPPLIER, null);
		return null;
	}

}
