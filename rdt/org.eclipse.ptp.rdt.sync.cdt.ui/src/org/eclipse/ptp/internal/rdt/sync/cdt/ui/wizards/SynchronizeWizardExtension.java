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
 * Synchronize Properties page extension for CDT to specify default build configurations *
 */
public class SynchronizeWizardExtension extends AbstractSynchronizeWizardExtension {
	private SyncConfigToBuildConfigWizardPage fWizardPage;

	public SynchronizeWizardExtension(ISynchronizeWizardExtensionDescriptor descriptor) {
		super(descriptor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.sync.ui.ISynchronizeWizardExtension#createNewProjectWizardPages()
	 */
	@Override
	public WizardPage[] createNewProjectWizardPages() {
		fWizardPage = new SyncConfigToBuildConfigWizardPage(WizardMode.NEW);
		return new WizardPage[] {fWizardPage};
	}

	@Override
	public WizardPage[] createConvertProjectWizardPages() {
		fWizardPage = new SyncConfigToBuildConfigWizardPage(WizardMode.ADD_SYNC);
		return new WizardPage[] {fWizardPage};
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