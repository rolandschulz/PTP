/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.cdt.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.wizards.SyncConfigToBuildConfigWizardPage.WizardMode;
import org.eclipse.ptp.rdt.sync.ui.AbstractSynchronizeWizardExtension;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeWizardExtensionDescriptor;

/**
 * Synchronize conversion wizard extension for converting a CDT project to a synchronized CDT project
 */
public class SynchronizeWizardExtension extends AbstractSynchronizeWizardExtension {
	private SyncConfigToBuildConfigWizardPage fWizardPage;

	public SynchronizeWizardExtension(ISynchronizeWizardExtensionDescriptor descriptor) {
		super(descriptor);
	}

	@Override
	public WizardPage createConvertProjectWizardPage() {
		fWizardPage = new SyncConfigToBuildConfigWizardPage(WizardMode.ADD_SYNC);
		return fWizardPage;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeWizardExtension#performFinish()
	 */
	@Override
	public void performFinish() {
		fWizardPage.run();
	}
}