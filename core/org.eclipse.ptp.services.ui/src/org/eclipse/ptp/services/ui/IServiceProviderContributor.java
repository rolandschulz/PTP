/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.services.ui;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderWorkingCopy;
import org.eclipse.swt.widgets.Composite;

/**
 * Interface that supplies a UI which can be launched to configure a service
 * provider.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the RDT team.
 * 
 * @see IServiceProvider
 */
public interface IServiceProviderContributor {

	/**
	 * Creates a section on the service model properties page to configure the
	 * service provider working copy. The composite does not have a layout
	 * manager attached.
	 * 
	 * @param provider
	 *            working copy
	 * @param composite
	 * @since 2.0
	 */
	public void configureServiceProvider(IServiceProviderWorkingCopy provider, Composite composite);

	/**
	 * Retrieve the wizard for a service provider
	 * 
	 * @param provider
	 *            the provider being configured
	 * @param page
	 *            next ServiceConfigurationWizardPage if we are being called by
	 *            a ServiceConfigurationWizard, or null otherwise
	 * @return the wizard to configure this provider
	 */
	public IWizard getWizard(IServiceProvider provider, IWizardPage page);

	/**
	 * Retrieve the wizard pages for a service provider
	 * 
	 * @param wizard
	 *            the wizard that will display the pages
	 * @param provider
	 *            the provider being configured
	 * @return wizard pages for the provider
	 */
	public WizardPage[] getWizardPages(IWizard wizard, IServiceProvider provider);

}
