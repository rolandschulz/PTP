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
package org.eclipse.ptp.remotetools.environment.launcher.macros;

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;

/**
 * This interface is similar to the one provided by
 * 
 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider.
 * 
 * @author laggarcia
 * @since 3.0.0
 */
public interface ILaunchVariableManager {

	public String resolveValue(String value, String nonexistentMacrosValue,
			String listDelimiter, int contextType, Object contextData)
			throws CdtVariableException;

}
