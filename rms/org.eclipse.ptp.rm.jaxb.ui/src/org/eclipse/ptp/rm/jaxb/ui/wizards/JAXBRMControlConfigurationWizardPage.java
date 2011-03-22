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

import org.eclipse.ptp.remotetools.environment.generichost.core.ConfigFactory;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.ui.dialogs.ConnectionChoiceContainer;
import org.eclipse.ptp.rm.ui.wizards.AbstractRemoteResourceManagerConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;

/**
 * Generic Wizard for the JAXB Resource Manager Control.
 * 
 * @author arossi
 * 
 */
public final class JAXBRMControlConfigurationWizardPage extends AbstractRemoteResourceManagerConfigurationWizardPage {

	private class JAXBRMConnectionChoiceContainer extends RMConnectionChoiceContainer {

		private class JAXBRMDataSource extends RMDataSource {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ptp.rm.ui.wizards.
			 * AbstractRemoteResourceManagerConfigurationWizardPage
			 * .RMDataSource#loadFromStorage()
			 */
			@Override
			protected void loadFromStorage() {
				IJAXBResourceManagerConfiguration config = (IJAXBResourceManagerConfiguration) getConfiguration();
				try {
					config.realizeRMDataFromXML();
				} catch (Throwable t) {
					JAXBUIPlugin.log(t);
				}
				super.loadFromStorage();
			}
		}

		protected JAXBRMConnectionChoiceContainer(AbstractRemoteResourceManagerConfigurationWizardPage page) {
			super(page);
		}

		@Override
		public RMDataSource getDataSource() {
			if (dataSource == null) {
				dataSource = new JAXBRMDataSource();
			}
			return dataSource;
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
				String[] hints = new String[] { ConfigFactory.ATTR_CONNECTION_ADDRESS, ConfigFactory.ATTR_CONNECTION_PORT };
				String[] defaults = new String[] { getConfiguration().getDefaultControlHost(),
						getConfiguration().getDefaultControlPort() };
				handleRemoteServiceSelected(getRemoteUIConnectionManager().newConnection(getShell(), hints, defaults));
			}
		}

	}

	public JAXBRMControlConfigurationWizardPage(IRMConfigurationWizard wizard) {
		super(wizard, Messages.JAXBRMControlConfigurationWizardPage_Title);
		setPageComplete(false);
		setTitle(Messages.JAXBRMControlConfigurationWizardPage_Title);
		setDescription(Messages.JAXBConnectionWizardPage_Description);
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
