/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.jaxb.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.rm.jaxb.ui.wizards.JAXBRMConfigurationSelectionWizardPage;
import org.eclipse.ptp.rm.jaxb.ui.wizards.JAXBRMControlConfigurationWizardPage;
import org.eclipse.ptp.rm.jaxb.ui.wizards.JAXBRMMonitoringConfigurationWizardPage;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderWorkingCopy;
import org.eclipse.ptp.services.ui.IServiceProviderContributor;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMServiceProviderConfigurationWizard;
import org.eclipse.swt.widgets.Composite;

public class JAXBProviderContributor implements IServiceProviderContributor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.ui.IServiceProviderContributor#
	 * configureServiceProvider
	 * (org.eclipse.ptp.services.core.IServiceProviderWorkingCopy,
	 * org.eclipse.swt.widgets.Composite)
	 */
	/**
	 * @since 2.0
	 */
	public void configureServiceProvider(IServiceProviderWorkingCopy provider, Composite comp) {
		// Nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.ui.IServiceProviderContributor#getWizard(org
	 * .eclipse.ptp.services.core.IServiceProvider,
	 * org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizard getWizard(IServiceProvider provider, IWizardPage page) {
		return new RMServiceProviderConfigurationWizard(provider, page);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.services.ui.IServiceProviderContributor#getWizardPages
	 * (org.eclipse.jface.wizard.IWizard,
	 * org.eclipse.ptp.services.core.IServiceProvider)
	 */
	public WizardPage[] getWizardPages(IWizard wizard, IServiceProvider provider) {
		List<WizardPage> pages = new ArrayList<WizardPage>();
		pages.add(new JAXBRMConfigurationSelectionWizardPage((IRMConfigurationWizard) wizard));
		pages.add(new JAXBRMControlConfigurationWizardPage((IRMConfigurationWizard) wizard));
		pages.add(new JAXBRMMonitoringConfigurationWizardPage((IRMConfigurationWizard) wizard));
		return pages.toArray(new WizardPage[0]);
	}
}
