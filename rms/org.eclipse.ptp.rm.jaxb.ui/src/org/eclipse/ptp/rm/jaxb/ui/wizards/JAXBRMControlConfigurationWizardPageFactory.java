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
package org.eclipse.ptp.rm.jaxb.ui.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPageFactory;

public class JAXBRMControlConfigurationWizardPageFactory extends RMConfigurationWizardPageFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.wizards.RMConfigurationWizardPageFactory#getPages(
	 * org.eclipse.ptp.ui.wizards.IRMConfigurationWizard)
	 */
	@Override
	public RMConfigurationWizardPage[] getPages(IRMConfigurationWizard wizard) {
		List<RMConfigurationWizardPage> pages = new ArrayList<RMConfigurationWizardPage>();
		pages.add(new JAXBRMControlConfigurationWizardPage(wizard));
		return pages.toArray(new RMConfigurationWizardPage[0]);
	}

}
