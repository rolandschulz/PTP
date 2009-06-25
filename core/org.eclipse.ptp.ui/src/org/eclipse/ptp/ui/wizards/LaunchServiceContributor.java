/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.ui.IServiceContributor;

/**
 * Allows the user to configure a project location.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 */
public class LaunchServiceContributor implements IServiceContributor {

	public LaunchServiceContributor() {
	}

	public WizardPage[] getWizardPages(IService service) {
		return new WizardPage[]{
				new LaunchServiceWizardPage(service, "Launch Service Configuration") //$NON-NLS-1$
			}; 
	}

}
