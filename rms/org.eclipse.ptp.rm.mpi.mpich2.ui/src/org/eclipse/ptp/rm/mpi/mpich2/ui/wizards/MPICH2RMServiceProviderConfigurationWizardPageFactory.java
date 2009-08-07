/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
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

import org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem.MPICH2RMServiceProviderFactory;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPageFactory;


/**
 * This class is only used to bridge between the old RM factory wizard and the new RMs backed
 * by service configurations. It will be removed prior to the 3.0 release.
 * 
 * @author greg
 *
 */
public class MPICH2RMServiceProviderConfigurationWizardPageFactory extends RMConfigurationWizardPageFactory {

	public MPICH2RMServiceProviderConfigurationWizardPageFactory() {
		// no-op
	}

	protected RMConfigurationWizardPage[] getMPICH2ConfigurationWizardPages(IRMConfigurationWizard wizard) {
		return new RMConfigurationWizardPage[] {
				new MPICH2ConfigurationWizardPage(wizard)
		};
	}

	@Override
	public RMConfigurationWizardPage[] getPages(IRMConfigurationWizard wizard) {
		List<RMConfigurationWizardPage> list = new ArrayList<RMConfigurationWizardPage>();
		list.add(new MPICH2RMConfigurationWizardPage(wizard));
		list.addAll(Arrays.asList(getMPICH2ConfigurationWizardPages(wizard)));
		return list.toArray(new RMConfigurationWizardPage[list.size()]);
	}

	@Override
	public Class<? extends IResourceManagerFactory> getRMFactoryClass() {
		return MPICH2RMServiceProviderFactory.class;
	}
}
