/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.services.ui;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceProvider;

/**
 * Interface that supplies wizard pages to configure a service.
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @see IServiceProvider
 */
public interface IServiceContributor {
	/**
	 * Retrieve the wizard pages to be displayed prior to configuring a provider
	 * for the service.
	 * 
	 * @param service
	 * @return wizard pages for the services.
	 */
	public WizardPage[] getWizardPages(IService service);
}
