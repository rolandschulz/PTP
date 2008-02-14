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

import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * 
 * This interface is to be implemented by the launch-integrator
 * for supplying the configuration-specific macros
 * 
 * @author laggarcia
 * @since 3.0.0
 */
public interface ILaunchConfigurationVariableSupplier {

	/**
	 *
	 * @param variableName the variable name
	 * @param launchConfiguration launch configuration
	 * @return the reference to the ICdtVariable interface representing 
	 * the variable of a given name or null if the variable of that name is not defined
	 */
	public IBuildMacro getVariable(String variableName,
			ILaunchConfiguration launchConfiguration);

	/**
	 *
	 * @param launchConfiguration launch configuration
	 * @return the ICdtVariable[] array representing defined variables 
	 */
	public IBuildMacro[] getVariables(ILaunchConfiguration launchConfiguration);

}
