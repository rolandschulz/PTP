/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *  
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.pe.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.rm.core.rmsystem.IRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rm.ibm.pe.core.rmsystem.PEResourceManagerConfiguration;
import org.eclipse.ptp.rm.ibm.pe.ui.messages.Messages;
import org.eclipse.ptp.rm.ui.wizards.AbstractProxyOptions;
import org.eclipse.ptp.rm.ui.wizards.AbstractRemoteProxyResourceManagerConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;

public final class PEResourceManagerConfigurationWizardPage extends AbstractRemoteProxyResourceManagerConfigurationWizardPage {

	public PEResourceManagerConfigurationWizardPage(IRMConfigurationWizard wizard) {
		super(wizard, Messages.getString("PEDialogs.ConfigurationTitle")); //$NON-NLS-1$
		setTitle(Messages.getString("PEDialogs.ConfigurationTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("PEDialogs.Configuration")); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ui.wizards.
	 * AbstractRemoteProxyResourceManagerConfigurationWizardPage
	 * #createProxyOptions(org.eclipse.jface.wizard.WizardPage,
	 * org.eclipse.ptp.rmsystem.IRemoteResourceManagerConfiguration,
	 * java.lang.String)
	 */
	@Override
	protected AbstractProxyOptions createProxyOptions(WizardPage wizardPage, IRemoteResourceManagerConfiguration config) {
		return new PEResourceManagerOptions(wizardPage, (PEResourceManagerConfiguration) config);
	}
}
