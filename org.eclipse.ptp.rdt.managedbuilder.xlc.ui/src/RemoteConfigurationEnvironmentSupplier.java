/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.internal.envvar.BuildEnvVar;
import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ProjectNotConfiguredException;
import org.eclipse.ptp.services.core.ServiceModelManager;


/**
 * Supplies remote environment variables on a per-configuration basis.
 * Right now it actually doesn't take into account service model configurations, so this just
 * acts the same as the RemoteProjectEnvironmentSupplier at the moment.
 * 
 * @author crecoskie
 *
 */
@SuppressWarnings("restriction")
public class RemoteConfigurationEnvironmentSupplier implements
		IConfigurationEnvironmentVariableSupplier {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier#getVariable(java.lang.String, org.eclipse.cdt.managedbuilder.core.IConfiguration, org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider)
	 */
	public IBuildEnvironmentVariable getVariable(String variableName,
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		Map<String, String> envMap = getRemoteEnvironment(configuration.getManagedProject());
		String value = envMap.get(variableName) == null ? new String() : envMap.get(variableName);
		IBuildEnvironmentVariable envVar = new BuildEnvVar(variableName, value);
		return envVar;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier#getVariables(org.eclipse.cdt.managedbuilder.core.IConfiguration, org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider)
	 */
	public IBuildEnvironmentVariable[] getVariables(
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		List<IBuildEnvironmentVariable> vars = new LinkedList<IBuildEnvironmentVariable>();
		Map<String, String> remoteEnvMap = null;
		
		remoteEnvMap = getRemoteEnvironment(configuration.getManagedProject());
		
		if (remoteEnvMap != null) {
			for (String var : remoteEnvMap.keySet()) {
				String value = remoteEnvMap.get(var);

				IBuildEnvironmentVariable buildEnvVar = new BuildEnvVar(var,
						value);
				vars.add(buildEnvVar);
			}

			return vars.toArray(new IBuildEnvironmentVariable[0]);
		}
		
		else
			return new IBuildEnvironmentVariable[0];
	}
	
	/**
	 * @param project
	 * @param remoteEnvMap
	 * @return
	 */
	private Map<String, String> getRemoteEnvironment(IManagedProject project) {
		IProject iProj = (IProject) project.getOwner();
		Map<String, String> remoteEnvMap = new HashMap<String, String>();

		ServiceModelManager smm = ServiceModelManager.getInstance();

		try {
			IServiceConfiguration serviceConfig = smm
					.getActiveConfiguration(iProj);
			IService buildService = smm
					.getService(IRDTServiceConstants.SERVICE_BUILD);
			IServiceProvider serviceProvider = serviceConfig
					.getServiceProvider(buildService);
			IRemoteExecutionServiceProvider executionProvider = null;
			if (serviceProvider instanceof IRemoteExecutionServiceProvider) {
				executionProvider = (IRemoteExecutionServiceProvider) serviceProvider;
			}

			if (executionProvider != null) {

				IRemoteServices remoteServices = executionProvider
						.getRemoteServices();

				IRemoteConnection connection = executionProvider
						.getConnection();

				if (!connection.isOpen()) {
					try {
						connection.open(null);
					} catch (RemoteConnectionException e) {
						RDTLog.logError(e);
					}
				}

				List<String> command = new LinkedList<String>();

				IRemoteProcessBuilder processBuilder = remoteServices
						.getProcessBuilder(connection, command);

				remoteEnvMap = processBuilder.environment();

			}
		} catch (ProjectNotConfiguredException e) {
			// We can get here when the environment supplier is called during
			// project creation, since the service model is not yet setup.
			// Swallow the error.
		}
		return remoteEnvMap;
	}

}
