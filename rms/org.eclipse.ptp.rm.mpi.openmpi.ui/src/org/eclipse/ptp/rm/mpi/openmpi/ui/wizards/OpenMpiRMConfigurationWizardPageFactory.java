/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.ui.wizards;

import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.OpenMpiResourceManagerFactory;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPageFactory;


public class OpenMpiRMConfigurationWizardPageFactory extends
	RMConfigurationWizardPageFactory {

	public OpenMpiRMConfigurationWizardPageFactory() {
		// no-op
	}

	public RMConfigurationWizardPage[] getPages(RMConfigurationWizard wizard) {
		return new RMConfigurationWizardPage[] {
				new OpenMpiRMConfigurationWizardPage(wizard),
				new OpenMpiToolConfigurationWizardPage(wizard) };
	}

	public Class<OpenMpiResourceManagerFactory> getRMFactoryClass() {
		return OpenMpiResourceManagerFactory.class;
	}
}
