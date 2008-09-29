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
package org.eclipse.ptp.cell.environment.cellsimulator.core.local;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.cell.environment.cellsimulator.CellSimulatorTargetPlugin;
import org.eclipse.ptp.cell.environment.cellsimulator.conf.AttributeNames;
import org.eclipse.ptp.cell.environment.cellsimulator.conf.Parameters;
import org.eclipse.ptp.cell.environment.cellsimulator.core.common.CommonConfigFactory;
import org.eclipse.ptp.cell.environment.cellsimulator.core.common.CommonConfigurationBean;
import org.eclipse.ptp.cell.simulator.core.ISimulatorParameters;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
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
public class LocalConfigFactory extends CommonConfigFactory {
	private LocalConfigurationBean bean;
	private String pluginId;

	public LocalConfigFactory(LocalConfigurationBean bean, String pluginId) {
		super(bean);
		this.bean = bean;
		this.pluginId = pluginId;
	}
	
	public LocalSimulatorConfiguration createLocalSimulatorParameters() throws CoreException {
		LocalSimulatorConfiguration parameters = new LocalSimulatorConfiguration();

		ControlAttributes attributes = bean.getAttributes();
		// LocalLaunchAutomaticAttributeGenerator attrGen =
		// LocalLaunchAutomaticAttributeGenerator.getAutomaticAttributeGenerator();
		// String environmentID =
		// attributes.getString(EnvironmentPlugin.ATTR_CORE_ENVIRONMENTID);

		fillSimulatorParameters(parameters);

		try {
			if (attributes.getBoolean(LocalConfigurationBean.AUTOMATIC_WORK_DIRECTORY, true)) {
				IPath path = CellSimulatorTargetPlugin.getDefault().getStateLocation();
				if (pluginId == null) {
					pluginId = "new-cell-target"; //$NON-NLS-1$
				}
				path = path.append(pluginId);
				parameters.setWorkDirectory(path.toOSString());
			} else {
				parameters.setWorkDirectory(attributes.getString(CommonConfigurationBean.ATTR_WORK_DIRECTORY, null));				
			}
			

			parameters.setShowSimulatorGUI(attributes.getBoolean(CommonConfigurationBean.ATTR_SHOW_SIMULATOR_GUI, false));

			/*
			 * Network configuration
			 */
			if (attributes.getBoolean(LocalConfigurationBean.ATTR_AUTOMATIC_NETWORK, true)) {
				// NOTHING, will be set later
			} else {
				parameters.setIpHost(attributes.getString(LocalConfigurationBean.ATTR_IP_HOST, null));
				parameters.setIpSimulator(attributes.getString(LocalConfigurationBean.ATTR_IP_SIMULATOR, null));
				parameters.setMacSimulator(attributes.getString(LocalConfigurationBean.ATTR_MAC_SIMULATOR, null));
			}
			parameters.setNetmaskSimulator(Parameters.SIMULATOR_NETMASK);

			/*
			 * Java API
			 * 
			 * By now, Java API will always be disabled due to some issues with
			 * the simulator GUI.
			 */
			if (org.eclipse.ptp.cell.simulator.conf.Parameters.doUseJavaAPI()) {
				parameters.setJavaApiSocketInit(true);
				if (attributes.getBoolean(LocalConfigurationBean.ATTR_AUTOMATIC_PORTCONFIG, true)) {
					// NOTHING, will be set later
				} else {
					parameters.setJavaApiPort(attributes.verifyInt(AttributeNames.JAVA_API_SOCKET_PORT, LocalConfigurationBean.ATTR_JAVA_API_SOCKET_PORT));
					// parameters.setJavaApiSocketPort() will be called later
				}
				parameters.setJavaApiSocketPortTryWait(Integer.parseInt(Parameters.JAVA_API_SOCKET_PORT_TRY_WAIT));
				parameters.setJavaApiSocketPortMaxTries(Integer.parseInt(Parameters.JAVA_API_SOCKET_PORT_MAX_TRIES));
				if (org.eclipse.ptp.cell.simulator.conf.Parameters.doHandleJavaApiGuiIssue()) {
					parameters.setShowSimulatorGUI(false);
				}
			} else {
				parameters.setJavaApiSocketInit(false);
				// ignore other java api parameters, since the are not used when init is false.
			}

			/*
			 * Console. Always enabled since it is always used by the
			 * SimulatorControl, even when the console is not shown.
			 */
			parameters.setConsoleSocketInit(true);
			if (attributes.getBoolean(LocalConfigurationBean.ATTR_AUTOMATIC_PORTCONFIG, true)) {
				// NOTHING, will be set later
			} else {
				parameters.setConsolePort(attributes.verifyInt(AttributeNames.CONSOLE_SOCKET_PORT, LocalConfigurationBean.ATTR_CONSOLE_SOCKET_PORT));
				// parameters.setConsoleSocketPort will be called later
			}
			parameters.setConsoleSocketPortMaxTries(Integer.parseInt(Parameters.CONSOLE_SOCKET_PORT_MAX_TRIES));
			parameters.setConsoleSocketPortTryWait(Integer.parseInt(Parameters.CONSOLE_SOCKET_PORT_TRY_WAIT));
			parameters.setConsoleEcho(false);
			parameters.setConsoleTerminalInit(false);

			return parameters;
		} catch (IllegalAttributeException e) {
			throw new CoreException(new Status(Status.ERROR, CellSimulatorTargetPlugin.getDefault().getBundle().getSymbolicName(), 0, e.getMessage(), e));
		}
	}

	public void completeSimulatorParametersWithConsoleConfig(LocalSimulatorConfiguration parameters) {
		completeSimulatorParametersWithJavaApiConfig(parameters, parameters.getConsolePort());
	}

	public void completeSimulatorParametersWithConsoleConfig(LocalSimulatorConfiguration parameters, int port) {
		parameters.setConsoleSocketHost(null); // localhost
		parameters.setConsoleSocketPort(port);
		parameters.setConsolePort(port);
	}

	public void completeSimulatorParametersWithJavaApiConfig(LocalSimulatorConfiguration parameters) {
		completeSimulatorParametersWithJavaApiConfig(parameters, parameters.getJavaApiPort());
	}

	public void completeSimulatorParametersWithJavaApiConfig(LocalSimulatorConfiguration parameters, int port) {
		parameters.setJavaApiSocketHost(null); // localhost
		parameters.setJavaApiSocketPort(port);
		parameters.setJavaApiPort(port);
	}

	public void completeSimulatorParametersWithNetworkConfig(LocalSimulatorConfiguration parameters, String ipSimulator, String macSimulator, String ipHost) {
		parameters.setIpHost(ipHost);
		parameters.setIpSimulator(ipSimulator);
		parameters.setMacSimulator(macSimulator);
	}

	public String getTargetID() {
		return bean.getAttributes().getString(EnvironmentPlugin.ATTR_CORE_ENVIRONMENTID);
	}

	public ISimulatorParameters createSimulatorParameters() throws CoreException {
		return createLocalSimulatorParameters();
	}
}
