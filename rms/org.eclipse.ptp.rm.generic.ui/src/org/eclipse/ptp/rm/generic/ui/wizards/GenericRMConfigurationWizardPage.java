/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.generic.ui.wizards;

import org.eclipse.ptp.rm.generic.ui.messages.Messages;
import org.eclipse.ptp.rm.ui.wizards.AbstractRemoteResourceManagerConfigurationWizardPage;
import org.eclipse.ptp.ui.wizards.IRMConfigurationWizard;

public final class GenericRMConfigurationWizardPage extends AbstractRemoteResourceManagerConfigurationWizardPage {

	public GenericRMConfigurationWizardPage(IRMConfigurationWizard wizard) {
		super(wizard, Messages.GenericRMConfigurationWizardPage_Title);
		setTitle(Messages.GenericRMConfigurationWizardPage_Title);
		setDescription(Messages.GenericRMConfigurationWizardPage_Description);
	}

}
