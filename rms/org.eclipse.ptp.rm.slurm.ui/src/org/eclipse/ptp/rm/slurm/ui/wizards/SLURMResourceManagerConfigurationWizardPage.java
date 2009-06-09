/*******************************************************************************
 * Copyright (c) 2008,2009 
 * School of Computer, National University of Defense Technology, P.R.China
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 			Peichang Shi <pcmutates@163.com>/<pcshi@nudt.edu.cn>
 *******************************************************************************/
package org.eclipse.ptp.rm.slurm.ui.wizards;

import org.eclipse.ptp.rm.remote.ui.wizards.AbstractRemoteProxyResourceManagerConfigurationWizardPage;
import org.eclipse.ptp.rm.slurm.ui.internal.ui.Messages;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizard;

public final class SLURMResourceManagerConfigurationWizardPage extends
	AbstractRemoteProxyResourceManagerConfigurationWizardPage {
	
	public SLURMResourceManagerConfigurationWizardPage(RMConfigurationWizard wizard) {
		super(wizard, Messages.getString("SLURMResourceManagerConfigurationWizardPage.name")); //$NON-NLS-1$
		setTitle(Messages.getString("SLURMResourceManagerConfigurationWizardPage.title")); //$NON-NLS-1$
		setDescription(Messages.getString("SLURMResourceManagerConfigurationWizardPage.description")); //$NON-NLS-1$
	}
}
