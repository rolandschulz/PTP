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
package org.eclipse.ptp.remotetools.environment.launcher.internal.macros;

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.internal.core.cdtvariables.CoreVariableSubstitutor;
import org.eclipse.cdt.internal.core.cdtvariables.EclipseVariablesVariableSupplier;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;
import org.eclipse.cdt.utils.cdtvariables.IVariableContextInfo;
import org.eclipse.cdt.utils.cdtvariables.IVariableSubstitutor;
import org.eclipse.ptp.remotetools.environment.launcher.macros.ILaunchVariableManager;


/**
 * This class is similar to
 * org.eclipse.cdt.internal.core.cdtvariables.CdtVariableManager
 * 
 * @author laggarcia
 * @since 3.0.0
 */
public class LaunchVariableManager implements ILaunchVariableManager {

	private static LaunchVariableManager instance;

	public static LaunchMacroSupplier launchMacroSupplier = LaunchMacroSupplier
			.getInstance();

	public static EclipseVariablesVariableSupplier eclipseVariablesMacroSupplier = EclipseVariablesVariableSupplier
			.getInstance();

	public LaunchVariableManager() {
	}

	public static LaunchVariableManager getDefault() {
		if (instance == null)
			instance = new LaunchVariableManager();
		return instance;
	}

	public String resolveValue(String value, String nonexistentMacrosValue,
			String listDelimiter, int contextType, Object contextData)
			throws CdtVariableException {

		IVariableContextInfo info = getMacroContextInfo(contextType, contextData);
		if (info != null)
			return CdtVariableResolver.resolveToString(value, getMacroSubstitutor(
					info, nonexistentMacrosValue, listDelimiter));
		return null;

	}

	public IVariableContextInfo getMacroContextInfo(int contextType,
			Object contextData) {
		LaunchVariableContextInfo info = new LaunchVariableContextInfo(contextType,
				contextData);
		if (info.getSuppliers() != null)
			return info;
		return null;
	}

	public IVariableSubstitutor getMacroSubstitutor(IVariableContextInfo info,
			String inexistentMacroValue, String listDelimiter) {
		return new CoreVariableSubstitutor(info, inexistentMacroValue,
				listDelimiter);
	}

}
