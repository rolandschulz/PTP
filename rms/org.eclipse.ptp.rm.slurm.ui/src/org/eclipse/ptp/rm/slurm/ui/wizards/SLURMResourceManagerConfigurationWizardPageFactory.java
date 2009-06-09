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

import org.eclipse.ptp.rm.slurm.core.rmsystem.SLURMResourceManagerFactory;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizard;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.RMConfigurationWizardPageFactory;

public class SLURMResourceManagerConfigurationWizardPageFactory extends
		RMConfigurationWizardPageFactory {

	public SLURMResourceManagerConfigurationWizardPageFactory() {
		// no-op
	}

	public RMConfigurationWizardPage[] getPages(RMConfigurationWizard wizard) {
		return new RMConfigurationWizardPage[]{
				new SLURMResourceManagerConfigurationWizardPage(wizard),
				//new SLURMConfigurationWizardPage(wizard)
		};
	}

	public Class<SLURMResourceManagerFactory> getRMFactoryClass() {
		return SLURMResourceManagerFactory.class;
	}
}
