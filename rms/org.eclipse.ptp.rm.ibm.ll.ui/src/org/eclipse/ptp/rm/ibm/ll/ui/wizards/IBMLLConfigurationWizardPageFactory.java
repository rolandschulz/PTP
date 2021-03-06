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
package org.eclipse.ptp.rm.ibm.ll.ui.wizards;

import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPageFactory;

public class IBMLLConfigurationWizardPageFactory extends RMConfigurationWizardPageFactory {

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

		wizardPages = new RMConfigurationWizardPage[2];
		wizardPages[0] = new IBMLLResourceManagerConfigurationWizardPage(wizard);
		wizardPages[1] = new IBMLLResourceManagerOptionWizardPage(wizard);
		return wizardPages;
	}

}
