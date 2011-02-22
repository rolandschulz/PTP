/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California and others.
 * This material was produced under U.S. Government contract W-7405-ENG-36
 * for Los Alamos National Laboratory, which is operated by the University
 * of California for the U.S. Department of Energy. The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 * Contributors:
 *    Albert L. Rossi (NCSA)  -- modified to disable proxy path for
 *    							 automatically deployed RMs
 *    						  -- modified to allow subclasses to expose extra properties/widgets (2010/11/04)
 *******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.wizards;

import org.eclipse.ptp.remote.core.IRemoteProxyOptions;
import org.eclipse.ptp.remotetools.environment.generichost.core.ConfigFactory;
import org.eclipse.ptp.rm.jaxb.core.rm.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;

/**
 * Generic Wizard for the JAXB Resource Manager Control.
 * 
 * @author arossi
 * 
 */
public final class JAXBRMControlConfigurationWizardPage extends AbstractControlMonitorRMConfigurationWizardPage {

	private IJAXBResourceManagerConfiguration jaxbConfig;

	public JAXBRMControlConfigurationWizardPage(IRMConfigurationWizard wizard) {
		super(wizard, Messages.JAXBRMControlConfigurationWizardPage_Title);
		setPageComplete(false);
		isValid = false;
		setTitle(Messages.JAXBRMControlConfigurationWizardPage_Title);
		setDescription(Messages.JAXBConnectionWizardPage_Description);
		connectionSharingEnabled = false;
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
			String[] defaults = new String[] { jaxbConfig.getDefaultControlHost(), jaxbConfig.getDefaultControlPort() };
			handleRemoteServiceSelected(uiConnectionManager.newConnection(getShell(), hints, defaults));
		}
	}

	/*
	 * The ResourceManagerData is unmarshaled here. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.wizards.
	 * AbstractControlMonitorRMConfigurationWizardPage#initContents()
	 */
	@Override
	protected void initContents() {
		super.initContents();
		jaxbConfig = (IJAXBResourceManagerConfiguration) config;
		try {
			jaxbConfig.realizeRMDataFromXML();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		targetPath = jaxbConfig.getDefaultControlPath();
		defaultSetting();
	}

	@Override
	protected void loadConnectionOptions() {
		targetPath = config.getControlPath();
		targetArgs = config.getControlInvocationOptionsStr();
		localAddr = config.getLocalAddress();
		int options = config.getControlOptions();
		muxPortFwd = (options & IRemoteProxyOptions.PORT_FORWARDING) == IRemoteProxyOptions.PORT_FORWARDING;
		manualLaunch = (options & IRemoteProxyOptions.MANUAL_LAUNCH) == IRemoteProxyOptions.MANUAL_LAUNCH;

	}

	@Override
	protected void setConnectionName(String name) {
		if (name != null) {
			jaxbConfig.setConnectionName(name, CONTROL_CONNECTION_NAME);
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
		config.setControlPath(targetPath);
		config.setControlInvocationOptions(targetArgs);
		config.setControlOptions(options);
		config.setLocalAddress(localAddr);
	}
}
