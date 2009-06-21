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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.OpenMPIResourceManagerFactory;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPageFactory;


/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class OpenMPIRMConfigurationWizardPageFactory extends
RMConfigurationWizardPageFactory {

	public OpenMPIRMConfigurationWizardPageFactory() {
		// no-op
	}

	protected RMConfigurationWizardPage[] getOpenMPIConfigurationWizardPages(RMConfigurationWizard wizard) {
		return new RMConfigurationWizardPage[] {
				new OpenMPIConfigurationWizardPage(wizard)
		};
	}

	@Override
	public RMConfigurationWizardPage[] getPages(RMConfigurationWizard wizard) {
		List<RMConfigurationWizardPage> list = new ArrayList<RMConfigurationWizardPage>();
		list.add(new OpenMPIRMConfigurationWizardPage(wizard));
		list.addAll(Arrays.asList(getOpenMPIConfigurationWizardPages(wizard)));
		return list.toArray(new RMConfigurationWizardPage[list.size()]);
	}

	@Override
	public Class<? extends IResourceManagerFactory> getRMFactoryClass() {
		return OpenMPIResourceManagerFactory.class;
	}
}
