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
package org.eclipse.ptp.rm.ibm.ll.ui.wizards;

import org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IBMLLServiceProviderFactory;
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
public class IBMLLRMServiceProviderConfigurationWizardPageFactory extends RMConfigurationWizardPageFactory {

	public IBMLLRMServiceProviderConfigurationWizardPageFactory() {
		// no-op
	}

	@Override
	public RMConfigurationWizardPage[] getPages(IRMConfigurationWizard wizard) {
		return new RMConfigurationWizardPage[]{new IBMLLResourceManagerConfigurationWizardPage(wizard), 
				new IBMLLResourceManagerOptionWizardPage(wizard) };
	}

	@Override
	public Class<? extends IResourceManagerFactory> getRMFactoryClass() {
		return IBMLLServiceProviderFactory.class;
	}
}
