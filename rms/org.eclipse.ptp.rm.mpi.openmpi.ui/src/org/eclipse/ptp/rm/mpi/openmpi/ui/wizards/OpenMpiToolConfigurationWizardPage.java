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

import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.OpenMpiResourceManagerConfiguration;
import org.eclipse.ptp.rm.ui.wizards.AbstractToolRMConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizard;

public class OpenMpiToolConfigurationWizardPage extends
		AbstractToolRMConfigurationWizardPage {

	public OpenMpiToolConfigurationWizardPage(RMConfigurationWizard wizard) {
		super(wizard, OpenMpiResourceManagerConfiguration.OPENMPI_CAPABILITIES , "Open MPI", "Open MPI tool configuration", "Enter information to configure the Open MPI tool");
	}

}
