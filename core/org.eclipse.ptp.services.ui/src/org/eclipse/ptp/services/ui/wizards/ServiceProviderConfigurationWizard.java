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
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.ui.IServiceProviderContributor;
import org.eclipse.ptp.services.ui.ServiceModelUIManager;

public class ServiceProviderConfigurationWizard extends Wizard {

	private final IWizardPage fPageLink;
	private final IServiceProvider fProvider;
	
	/**
	 * Create wizard with pages from a single service provider.
	 * 
	 * @param provider service provider we are configuring
	 * @param page next ServiceConfigurationWizardPage if we are being called by a ServiceConfigurationWizard, or null otherwise
	 */
	public ServiceProviderConfigurationWizard(IServiceProvider provider, IWizardPage page) {
		fProvider = provider;
		fPageLink = page;
		setForcePreviousAndNextButtons(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		IServiceProviderContributor contrib = ServiceModelUIManager.getInstance().getServiceProviderContributor(fProvider);
		if (contrib != null) {
			for (IWizardPage page : contrib.getWizardPages(fProvider)) {
				addPage(page);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage nextPage = super.getNextPage(page);
		if (nextPage == null) {
			return fPageLink;
		}
		return nextPage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		return true;
	}
}
