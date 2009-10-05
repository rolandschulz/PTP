/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.mpich2.ui;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.rm.mpi.mpich2.ui.wizards.MPICH2ConfigurationWizardPage;
import org.eclipse.ptp.rm.mpi.mpich2.ui.wizards.MPICH2RMConfigurationWizardPage;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.ui.IServiceProviderContributor;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMServiceProviderConfigurationWizard;
import org.eclipse.swt.widgets.Composite;

public class MPICH2ProviderContributor implements IServiceProviderContributor 
{

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.ui.IServiceProviderContributor#configureServiceProvider(org.eclipse.ptp.services.core.IServiceProvider, org.eclipse.swt.widgets.Composite)
	 */
	public void configureServiceProvider(IServiceProvider provider, Composite comp) 
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
	 * @see org.eclipse.ptp.services.ui.IServiceProviderContributor#getWizardPages(org.eclipse.jface.wizard.IWizard, org.eclipse.ptp.services.core.IServiceProvider)
	 */
	public WizardPage[] getWizardPages(IWizard wizard, IServiceProvider provider) 
	{
		WizardPage wizardPages[];
		
		wizardPages = new WizardPage[2];
		wizardPages[0] = new MPICH2RMConfigurationWizardPage((IRMConfigurationWizard)wizard);
		wizardPages[1] = new MPICH2ConfigurationWizardPage((IRMConfigurationWizard)wizard);
		return wizardPages;
	}

}
