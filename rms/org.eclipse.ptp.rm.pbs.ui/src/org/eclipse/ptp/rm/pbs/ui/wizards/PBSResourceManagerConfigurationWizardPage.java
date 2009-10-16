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
package org.eclipse.ptp.rm.pbs.ui.wizards;

import org.eclipse.ptp.rm.pbs.ui.messages.Messages;
import org.eclipse.ptp.rm.ui.wizards.AbstractRemoteProxyResourceManagerConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;

public final class PBSResourceManagerConfigurationWizardPage extends
	AbstractRemoteProxyResourceManagerConfigurationWizardPage {
	
	public PBSResourceManagerConfigurationWizardPage(IRMConfigurationWizard wizard) {
		super(wizard, Messages.PBSResourceManagerConfigurationWizardPage_name);
		setTitle(Messages.PBSResourceManagerConfigurationWizardPage_title);
		setDescription(Messages.PBSResourceManagerConfigurationWizardPage_description);
	}
}
