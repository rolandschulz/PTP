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
package org.eclipse.ptp.cell.environment.cellsimulator.core.remote;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.cell.environment.cellsimulator.CellSimulatorTargetPlugin;
import org.eclipse.ptp.cell.environment.cellsimulator.conf.AttributeNames;
import org.eclipse.ptp.cell.environment.cellsimulator.conf.Parameters;
import org.eclipse.ptp.cell.environment.cellsimulator.core.common.CommonConfigFactory;
import org.eclipse.ptp.cell.environment.cellsimulator.core.common.CommonConfigurationBean;
import org.eclipse.ptp.cell.environment.cellsimulator.core.common.ConnectionConfig;
import org.eclipse.ptp.cell.environment.cellsimulator.core.local.LocalSimulatorConfiguration;
import org.eclipse.ptp.cell.simulator.core.ISimulatorParameters;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.utils.verification.ControlAttributes;
import org.eclipse.ptp.remotetools.utils.verification.IllegalAttributeException;

/**
 * Defines rules to build the target configuration from an attribute hash map.
 * The attributes are divided into three groups. One that are specific how to
 * launch the simulator, another that are related to the connection from the
 * environment to the simulator.
 * 
 * @author Daniel Felix Ferber
 * @since 1.0
 */
public class RemoteConfigFactory extends CommonConfigFactory {
	RemoteConfigurationBean bean;

	public RemoteConfigFactory(RemoteConfigurationBean bean) {
		super(bean);
		this.bean = bean;
	}

	public RemoteSimulatorConfiguration createRemoteSimulatorParameters(IRemoteExecutionManager executionManager) throws CoreException {
		RemoteSimulatorConfiguration parameters = new RemoteSimulatorConfiguration(executionManager);
		ControlAttributes attributes = bean.getAttributes();

		fillSimulatorParameters(parameters);
		
		try {
			parameters.setWorkDirectory(attributes.getString(CommonConfigurationBean.ATTR_WORK_DIRECTORY, null));				

			parameters.setShowSimulatorGUI(attributes.getBoolean(CommonConfigurationBean.ATTR_SHOW_SIMULATOR_GUI, false));

			/*
			 * Network configuration, currently only manual configl.
			 */
			parameters.setIpHost(attributes.getString(RemoteConfigurationBean.ATTR_IP_HOST, null));
			parameters.setIpSimulator(attributes.getString(RemoteConfigurationBean.ATTR_IP_SIMULATOR, null));
			parameters.setMacSimulator(attributes.getString(RemoteConfigurationBean.ATTR_MAC_SIMULATOR, null));
			parameters.setNetmaskSimulator(Parameters.SIMULATOR_NETMASK);
			
			/*
			 * Java API
			 * 
			 * By now, Java API will always be disabled due to some issues with
			 * the simulator GUI.
			 */
			if (org.eclipse.ptp.cell.simulator.conf.Parameters.doUseJavaAPI()) {
				parameters.setJavaApiSocketInit(true);
				parameters.setJavaApiPort(attributes.verifyInt(AttributeNames.JAVA_API_SOCKET_PORT, RemoteConfigurationBean.ATTR_JAVA_API_SOCKET_PORT));
				parameters.setJavaApiSocketPortTryWait(Integer.parseInt(Parameters.JAVA_API_SOCKET_PORT_TRY_WAIT));
				parameters.setJavaApiSocketPortMaxTries(Integer.parseInt(Parameters.JAVA_API_SOCKET_PORT_MAX_TRIES));
				if (org.eclipse.ptp.cell.simulator.conf.Parameters
						.doHandleJavaApiGuiIssue()) {
					parameters.setShowSimulatorGUI(false);
				}
			} else {
				parameters.setJavaApiSocketInit(false);
			}
			/*
			 * Console. Always enabled since it is always used by the
			 * SimulatorControl, even when the console is not shown.
			 */
			parameters.setConsoleSocketInit(true);
			parameters.setConsolePort(attributes.verifyInt(AttributeNames.CONSOLE_SOCKET_PORT, RemoteConfigurationBean.ATTR_CONSOLE_SOCKET_PORT));
			parameters.setConsoleSocketPortMaxTries(Integer.parseInt(Parameters.CONSOLE_SOCKET_PORT_MAX_TRIES));
			parameters.setConsoleSocketPortTryWait(Integer.parseInt(Parameters.CONSOLE_SOCKET_PORT_TRY_WAIT));
			parameters.setConsoleEcho(false);
			parameters.setConsoleTerminalInit(false);

			return parameters;
		} catch (IllegalAttributeException e) {
			throw new CoreException(new Status(Status.ERROR, CellSimulatorTargetPlugin.getDefault().getBundle().getSymbolicName(), 0, e.getMessage(), e));
		}
	}

	public ConnectionConfig createRemoteConnectionConfig() throws CoreException {
		ControlAttributes attributes = bean.getAttributes();
		try {
			ConnectionConfig config = new ConnectionConfig();
			config.setConnectionAddress(attributes.getString(RemoteConfigurationBean.ATTR_REMOTE_CONNECTION_ADDRESS));
			config.setConnectionPort(attributes.verifyInt(AttributeNames.REMOTE_CONNECTION_PORT, RemoteConfigurationBean.ATTR_REMOTE_CONNECTION_PORT));
			config.setConnectionTimeout(attributes.verifyInt(AttributeNames.REMOTE_TIMEOUT, RemoteConfigurationBean.ATTR_REMOTE_TIMEOUT));
			config.setCipherType(attributes.getString(RemoteConfigurationBean.ATTR_REMOTE_CIPHER_TYPE));
			config.setIsPasswordAuth(attributes.getBoolean(RemoteConfigurationBean.ATTR_REMOTE_IS_PASSWORD_AUTH));
			config.setKeyPassphrase(attributes.getString(RemoteConfigurationBean.ATTR_REMOTE_KEY_PASSPHRASE));
			config.setKeyPath(attributes.getString(RemoteConfigurationBean.ATTR_REMOTE_KEY_PATH));
			config.setLoginPassword(attributes.getString(RemoteConfigurationBean.ATTR_REMOTE_LOGIN_PASSWORD));
			config.setLoginUserName(attributes.getString(RemoteConfigurationBean.ATTR_REMOTE_LOGIN_USERNAME));

			return config;
		} catch (IllegalAttributeException e) {
			throw new CoreException(new Status(Status.ERROR, CellSimulatorTargetPlugin.getDefault().getBundle().getSymbolicName(), 0, e.getMessage(), e));
		}
	}

	public void completeSimulatorParametersWithConsoleConfig(LocalSimulatorConfiguration parameters, int port) {
		parameters.setConsoleSocketHost(null); // localhost
		parameters.setConsoleSocketPort(port);
	}

	public void completeSimulatorParametersWithJavaApiConfig(LocalSimulatorConfiguration parameters, int port) {
		parameters.setJavaApiSocketHost(null); // localhost
		parameters.setJavaApiSocketPort(port);
	}

	public ISimulatorParameters createSimulatorParameters() throws CoreException {
		return createRemoteSimulatorParameters(null);
	}
}
