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
package org.eclipse.ptp.rm.mpi.mpich2.ui.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem.MPICH2ResourceManagerFactory;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPageFactory;


/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class MPICH2RMConfigurationWizardPageFactory extends RMConfigurationWizardPageFactory {

	public MPICH2RMConfigurationWizardPageFactory() {
		// no-op
	}

	protected RMConfigurationWizardPage[] getMPICH2ConfigurationWizardPages(RMConfigurationWizard wizard) {
		return new RMConfigurationWizardPage[] {
				new MPICH2ConfigurationWizardPage(wizard)
		};
	}

	//	@Override
	@Override
	public RMConfigurationWizardPage[] getPages(RMConfigurationWizard wizard) {
		List<RMConfigurationWizardPage> list = new ArrayList<RMConfigurationWizardPage>();
		list.add(new MPICH2RMConfigurationWizardPage(wizard));
		list.addAll(Arrays.asList(getMPICH2ConfigurationWizardPages(wizard)));
		return list.toArray(new RMConfigurationWizardPage[list.size()]);
	}

	//	@Override
	@Override
	public Class<? extends IResourceManagerFactory> getRMFactoryClass() {
		return MPICH2ResourceManagerFactory.class;
	}
}
