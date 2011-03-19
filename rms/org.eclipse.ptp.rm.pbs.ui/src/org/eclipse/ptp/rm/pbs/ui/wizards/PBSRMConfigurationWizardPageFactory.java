/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.wizards;

import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPageFactory;

public class PBSRMConfigurationWizardPageFactory extends RMConfigurationWizardPageFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.wizards.RMConfigurationWizardPageFactory#getPages(
	 * org.eclipse.ptp.ui.wizards.IRMConfigurationWizard)
	 */
	@Override
	public RMConfigurationWizardPage[] getPages(IRMConfigurationWizard wizard) {
		RMConfigurationWizardPage wizardPages[];

		wizardPages = new RMConfigurationWizardPage[1];
		wizardPages[0] = new PBSResourceManagerConfigurationWizardPage(wizard);
		return wizardPages;
	}

}
