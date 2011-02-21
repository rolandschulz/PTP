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
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.swt.widgets.Composite;

/**
 * Generic Wizard for the JAXB Resource Manager.
 * 
 * @author arossi
 * 
 */
public final class JAXBRMMonitoringConfigurationWizardPage extends AbstractControlMonitorRMConfigurationWizardPage {

	public JAXBRMMonitoringConfigurationWizardPage(IRMConfigurationWizard wizard) {
		this(wizard, Messages.JAXBRMMonitoringConfigurationWizardPage_Title);
	}

	public JAXBRMMonitoringConfigurationWizardPage(IRMConfigurationWizard wizard, String pageName) {
		super(wizard, pageName);
		setPageComplete(false);
		isValid = false;
		setTitle(Messages.JAXBRMMonitoringConfigurationWizardPage_Title);
		setDescription(Messages.JAXBConfigurationWizardPage_Description);
	}

	@Override
	protected void addCustomWidgets(Composite parent) {
		// NOP
	}

	@Override
	protected void loadConnectionOptions() {
		targetPath = config.getMonitorPath();
		targetArgs = config.getMonitorInvocationOptionsStr();
		localAddr = config.getLocalAddress();
		int options = config.getMonitorOptions();
		muxPortFwd = (options & IRemoteProxyOptions.PORT_FORWARDING) == IRemoteProxyOptions.PORT_FORWARDING;
		manualLaunch = (options & IRemoteProxyOptions.MANUAL_LAUNCH) == IRemoteProxyOptions.MANUAL_LAUNCH;
	}

	@Override
	protected void setConnectionName(String name) {
		String connectionName = name == null ? config.getConnectionName(CONTROL_CONNECTION_NAME) : name;
		if (connectionName != null)
			config.setConnectionName(connectionName, MONITOR_CONNECTION_NAME);
	}

	@Override
	protected void setConnectionOptions() {
		int options = 0;
		if (muxPortFwd)
			options |= IRemoteProxyOptions.PORT_FORWARDING;
		if (manualLaunch)
			options |= IRemoteProxyOptions.MANUAL_LAUNCH;
		config.setMonitorPath(targetPath);
		config.setMonitorInvocationOptions(targetArgs);
		config.setMonitorOptions(options);
		config.setLocalAddress(localAddr);
	}
}
