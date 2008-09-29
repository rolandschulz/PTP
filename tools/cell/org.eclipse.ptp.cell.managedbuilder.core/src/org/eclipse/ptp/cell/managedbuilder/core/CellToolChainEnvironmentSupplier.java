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
package org.eclipse.ptp.cell.managedbuilder.core;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.internal.envvar.BuildEnvVar;
import org.eclipse.cdt.utils.envvar.EnvVarOperationProcessor;
import org.eclipse.ptp.cell.managedbuilder.debug.Debug;


/**
 * @author laggarcia
 *
 */
public abstract class CellToolChainEnvironmentSupplier implements
		IConfigurationEnvironmentVariableSupplier {

	protected static final String PATH_ENVIRONMENT_VARIABLE = "PATH"; //$NON-NLS-1$

	public CellToolChainEnvironmentSupplier() {
		// Make default constructor available for extension point.
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier#getVariable(java.lang.String, org.eclipse.cdt.managedbuilder.core.IConfiguration, org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider)
	 */
	public IBuildEnvironmentVariable getVariable(String variableName,
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_SUPPLIER, variableName, configuration.getId(), provider.getClass().getName());
		
		String path;
		if ((EnvVarOperationProcessor.normalizeName(variableName)
				.equals(PATH_ENVIRONMENT_VARIABLE))
				&& ((path = getModifiedPathEnvironmentVariable(configuration,
						provider)) != null)) {
			// If the variable is PATH_ENVIRONMENT_VARIABLE and the provider can
			// get the PATH from the system
			IBuildEnvironmentVariable result = new BuildEnvVar(PATH_ENVIRONMENT_VARIABLE, path, provider
					.getDefaultDelimiter()); 
			Debug.POLICY.exit(Debug.DEBUG_SUPPLIER, result.getValue());
			return result;
		}
		Debug.POLICY.exit(Debug.DEBUG_SUPPLIER, null);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier#getVariables(org.eclipse.cdt.managedbuilder.core.IConfiguration, org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider)
	 */
	public IBuildEnvironmentVariable[] getVariables(
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_SUPPLIER, configuration.getId(), provider.getClass().getName());

		IBuildEnvironmentVariable[] envVars;
		String path;
		if ((path = getModifiedPathEnvironmentVariable(configuration, provider)) != null) {
			// If the variable is PATH_ENVIRONMENT_VARIABLE and the provider can
			// get the PATH from the system
			envVars = new IBuildEnvironmentVariable[1];
			envVars[0] = new BuildEnvVar(PATH_ENVIRONMENT_VARIABLE, path,
					provider.getDefaultDelimiter());
		} else {
			envVars = new IBuildEnvironmentVariable[0];
		}
		Debug.POLICY.exit(Debug.DEBUG_SUPPLIER, "# of variables: {0}", envVars.length); //$NON-NLS-1$
		return envVars;
	}

	/**
	 * Calculate the modified PATH environment variable with the path to the GNU
	 * tools
	 * 
	 * @param configuration
	 * @param provider
	 * @return the modified PATH environment variable or null if the provider
	 *         can't resolve the PATH environment variable
	 */
	protected abstract String getModifiedPathEnvironmentVariable(
			IConfiguration configuration, IEnvironmentVariableProvider provider);

}
