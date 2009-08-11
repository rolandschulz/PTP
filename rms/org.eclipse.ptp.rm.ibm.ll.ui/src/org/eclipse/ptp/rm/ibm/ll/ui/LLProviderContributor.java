/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.ll.ui;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.rm.ibm.ll.ui.wizards.IBMLLResourceManagerConfigurationWizardPage;
import org.eclipse.ptp.rm.ibm.ll.ui.wizards.IBMLLResourceManagerOptionWizardPage;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.ui.IServiceProviderContributor;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMServiceProviderConfigurationWizard;
import org.eclipse.swt.widgets.Shell;

public class LLProviderContributor implements IServiceProviderContributor {

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.ui.IServiceProviderContributor#configureServiceProvider(org.eclipse.ptp.services.core.IServiceProvider, org.eclipse.swt.widgets.Shell)
	 */
	public void configureServiceProvider(IServiceProvider provider, Shell parentShell) 
	{
		// Nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.ui.IServiceProviderContributor#getWizard(org.eclipse.ptp.services.core.IServiceProvider, org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizard getWizard(IServiceProvider provider, IWizardPage page) {
		return new RMServiceProviderConfigurationWizard(provider, page);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.ui.IServiceProviderContributor#getWizardPages(org.eclipse.ptp.services.core.IServiceProvider)
	 */
	public WizardPage[] getWizardPages(IWizard wizard, IServiceProvider provider) 
	{
		WizardPage wizardPages[];
		
		wizardPages = new WizardPage[2];
		wizardPages[0] = new IBMLLResourceManagerConfigurationWizardPage((IRMConfigurationWizard)wizard);
		wizardPages[1] = new IBMLLResourceManagerOptionWizardPage((IRMConfigurationWizard)wizard);
		return wizardPages;
	}

}
