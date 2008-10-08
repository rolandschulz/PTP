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

import org.eclipse.ptp.rm.remote.ui.wizards.AbstractRemoteResourceManagerConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizard;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public final class OpenMPIRMConfigurationWizardPage extends
AbstractRemoteResourceManagerConfigurationWizardPage {

	public OpenMPIRMConfigurationWizardPage(RMConfigurationWizard wizard) {
		super(wizard, Messages.OpenMPIRMConfigurationWizardPage_Title);
		setTitle(Messages.OpenMPIRMConfigurationWizardPage_Title);
		setDescription(Messages.OpenMPIRMConfigurationWizardPage_Description);
	}

}
