/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.jaxb.ui.wizards;

import org.eclipse.ptp.remote.core.IRemoteProxyOptions;
import org.eclipse.ptp.remotetools.environment.generichost.core.ConfigFactory;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;

/**
 * Generic Wizard for the JAXB Resource Manager Monitoring.
 * 
 * @author arossi
 * 
 */
public final class JAXBRMMonitoringConfigurationWizardPage extends AbstractControlMonitorRMConfigurationWizardPage {

	private IJAXBResourceManagerConfiguration jaxbConfig;

	public JAXBRMMonitoringConfigurationWizardPage(IRMConfigurationWizard wizard) {
		this(wizard, Messages.JAXBRMMonitoringConfigurationWizardPage_Title);
	}

	public JAXBRMMonitoringConfigurationWizardPage(IRMConfigurationWizard wizard, String pageName) {
		super(wizard, pageName);
		setPageComplete(false);
		isValid = false;
		setTitle(Messages.JAXBRMMonitoringConfigurationWizardPage_Title);
		setDescription(Messages.JAXBConnectionWizardPage_Description);
	}

	@Override
	protected void configureInternal() {
		jaxbConfig = (IJAXBResourceManagerConfiguration) config;
	}

	/**
	 * Handle creation of a new connection by pressing the 'New...' button.
	 * Calls handleRemoteServicesSelected() to update the connection combo with
	 * the new connection.
	 * 
	 */
	@Override
	protected void handleNewRemoteConnectionSelected() {
		if (uiConnectionManager != null) {
			String[] hints = new String[] { ConfigFactory.ATTR_CONNECTION_ADDRESS, ConfigFactory.ATTR_CONNECTION_PORT };
			String[] defaults = new String[] { jaxbConfig.getDefaultMonitorHost(), jaxbConfig.getDefaultMonitorPort() };
			handleRemoteServiceSelected(uiConnectionManager.newConnection(getShell(), hints, defaults));
		}
	}

	@Override
	protected void loadConnectionOptions() {
		targetPath = config.getMonitorPath();
		targetArgs = config.getMonitorInvocationOptionsStr();
		localAddr = config.getLocalAddress();
		int options = config.getMonitorOptions();
		muxPortFwd = (options & IRemoteProxyOptions.PORT_FORWARDING) == IRemoteProxyOptions.PORT_FORWARDING;
		manualLaunch = (options & IRemoteProxyOptions.MANUAL_LAUNCH) == IRemoteProxyOptions.MANUAL_LAUNCH;
		if (ZEROSTR.equals(targetPath)) {
			targetPath = jaxbConfig.getDefaultMonitorPath();
		}
	}

	@Override
	protected void setConnectionName(String name) {
		String connectionName = name == null ? config.getConnectionName(CONTROL_CONNECTION_NAME) : name;
		if (connectionName != null) {
			config.setConnectionName(connectionName, MONITOR_CONNECTION_NAME);
		}
	}

	@Override
	protected void setConnectionOptions() {
		int options = 0;
		if (muxPortFwd) {
			options |= IRemoteProxyOptions.PORT_FORWARDING;
		}
		if (manualLaunch) {
			options |= IRemoteProxyOptions.MANUAL_LAUNCH;
		}
		config.setMonitorPath(targetPath);
		config.setMonitorInvocationOptions(targetArgs);
		config.setMonitorOptions(options);
		config.setLocalAddress(localAddr);
		if (connection != null) {
			config.setMonitorUserName(connection.getUsername());
			config.setMonitorAddress(connection.getAddress());
		}
	}

	@Override
	protected void updateSettings() {
		if (loading) {
			shareConnectionButton.setSelection(true);
		}
		super.updateSettings();
	}
}
