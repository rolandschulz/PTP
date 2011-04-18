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

import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.data.Site;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.RemoteUIServicesUtils;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.ptp.rm.ui.wizards.AbstractRemoteResourceManagerConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Generic Wizard for the JAXB Resource Manager Control. Provides connection
 * configuration.
 * 
 * @author arossi
 * 
 */
public final class JAXBRMControlConfigurationWizardPage extends AbstractRemoteResourceManagerConfigurationWizardPage implements
		IJAXBUINonNLSConstants {

	private final IJAXBResourceManagerConfiguration baseConfiguration;

	/**
	 * @param wizard
	 */
	public JAXBRMControlConfigurationWizardPage(IRMConfigurationWizard wizard) {
		super(wizard, Messages.JAXBRMControlConfigurationWizardPage_Title);
		baseConfiguration = (IJAXBResourceManagerConfiguration) wizard.getBaseConfiguration();

		setPageComplete(false);
		setTitle(Messages.JAXBRMControlConfigurationWizardPage_Title);
		setDescription(Messages.JAXBConnectionWizardPage_Description);
		/*
		 * in order to make the Site information available to the wizard, we
		 * need to realize the data object here.
		 */
		try {
			if (baseConfiguration.getResourceManagerData() == null) {
				baseConfiguration.realizeRMDataFromXML();
			}
		} catch (Throwable t) {
			WidgetActionUtils.errorMessage(Display.getCurrent().getActiveShell(), t, Messages.InvalidConfiguration
					+ baseConfiguration.getName(), Messages.InvalidConfiguration_title, false);
		}
	}

	/*
	 * Exports as default remote connection information whatever is set in the
	 * Site subtree of the JAXB data tree. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ui.wizards.
	 * AbstractRemoteResourceManagerConfigurationWizardPage
	 * #doCreateContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Composite doCreateContents(Composite parent) {
		Composite comp = super.doCreateContents(parent);
		Site site = baseConfiguration.getResourceManagerData().getSiteData();
		if (site != null) {
			try {
				RemoteUIServicesUtils.setConnectionHints(connectionWidget, site.getControlConnection());
			} catch (Throwable t) {
				JAXBUIPlugin.log(t);
			}
		}
		return comp;
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