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

import org.eclipse.ptp.rm.mpi.mpich2.ui.messages.Messages;
import org.eclipse.ptp.rm.ui.wizards.AbstractRemoteResourceManagerConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;

/**
 * 
 * @author Daniel Felix Ferber
 * 
 */
public final class MPICH2RMConfigurationWizardPage extends AbstractRemoteResourceManagerConfigurationWizardPage {

	public MPICH2RMConfigurationWizardPage(IRMConfigurationWizard wizard) {
		super(wizard, Messages.MPICH2RMConfigurationWizardPage_Title);
		setTitle(Messages.MPICH2RMConfigurationWizardPage_Title);
		setDescription(Messages.MPICH2RMConfigurationWizardPage_Description);
	}

}
