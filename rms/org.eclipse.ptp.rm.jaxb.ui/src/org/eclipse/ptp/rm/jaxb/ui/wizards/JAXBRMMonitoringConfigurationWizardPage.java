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

import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.ui.dialogs.ConnectionChoiceContainer;
import org.eclipse.ptp.rm.ui.wizards.AbstractRemoteResourceManagerConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;

/**
 * Generic Wizard for the JAXB Resource Manager Monitoring.
 * 
 * @author arossi
 * 
 */
public final class JAXBRMMonitoringConfigurationWizardPage extends AbstractRemoteResourceManagerConfigurationWizardPage {

	private class JAXBRMConnectionChoiceContainer extends RMConnectionChoiceContainer {

		protected JAXBRMConnectionChoiceContainer(AbstractRemoteResourceManagerConfigurationWizardPage page) {
			super(page);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rm.ui.wizards.
		 * AbstractRemoteResourceManagerConfigurationWizardPage
		 * #handleNewRemoteConnectionSelected()
		 */
		@Override
		protected void handleNewRemoteConnectionSelected() {
			if (getRemoteUIConnectionManager() != null) {
				String[] hints = new String[] { IRemoteUIConnectionManager.CONNECTION_ADDRESS_HINT,
						IRemoteUIConnectionManager.CONNECTION_PORT_HINT };
				String[] defaults = new String[] { getConfiguration().getDefaultMonitorHost(),
						getConfiguration().getDefaultMonitorPort() };
				handleRemoteServiceSelected(getRemoteUIConnectionManager().newConnection(getShell(), hints, defaults));
			}
		}
	}

	public JAXBRMMonitoringConfigurationWizardPage(IRMConfigurationWizard wizard) {
		this(wizard, Messages.JAXBRMMonitoringConfigurationWizardPage_Title);
	}

	public JAXBRMMonitoringConfigurationWizardPage(IRMConfigurationWizard wizard, String pageName) {
		super(wizard, pageName);
		setPageComplete(false);
		setTitle(Messages.JAXBRMMonitoringConfigurationWizardPage_Title);
		setDescription(Messages.JAXBConnectionWizardPage_Description);
		choiceContainer.setEnableUseDefault(Messages.AbstractRemoteProxyResourceManagerConfigurationWizardPage_3b);
	}

	@Override
	protected ConnectionChoiceContainer getChoiceContainer() {
		if (choiceContainer == null) {
			choiceContainer = new JAXBRMConnectionChoiceContainer(this);
		}
		return choiceContainer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage#getConfiguration()
	 */
	@Override
	protected IJAXBResourceManagerConfiguration getConfiguration() {
		return (IJAXBResourceManagerConfiguration) super.getConfiguration();
	}
}
