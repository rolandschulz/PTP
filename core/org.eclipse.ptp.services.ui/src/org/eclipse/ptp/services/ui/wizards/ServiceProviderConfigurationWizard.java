/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
 *******************************************************************************/
package org.eclipse.ptp.services.ui.wizards;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.ui.IServiceProviderContributor;
import org.eclipse.ptp.services.ui.ServiceModelUIManager;

public class ServiceProviderConfigurationWizard extends Wizard {

	/**
	 * Create wizard with pages from a single service provider. Don't add intro and
	 * services pages.
	 * 
	 * NOTE: The service provider MUST provide a configuration that supports the wizard.
	 * 
	 * @param serviceConfiguration service configuration this wizard is for
	 * @param provider service provider we are configuring
	 * @param page next ServiceConfigurationWizardPage if we are being called by a ServiceConfigurationWizard, null otherwise
	 */
	public ServiceProviderConfigurationWizard(IServiceConfiguration serviceConfiguration, IServiceProvider provider, IWizardPage page) {
		setForcePreviousAndNextButtons(true);
		IServiceProviderContributor config = ServiceModelUIManager.getInstance().getServiceProviderContributor(provider);
		if (config != null) {
			setWizardPages(config.getWizardPages(this, provider));
		}
		if (page != null) {
			addPage(page);
		}
	}
	
	public boolean performFinish() {
		return true;
	}

	private void setWizardPages(WizardPage[] pages) {
		for (IWizardPage page : pages) {
			addPage(page);
		}
	}
}
