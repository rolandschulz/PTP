/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.sync.ui.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.internal.rdt.sync.ui.SynchronizePropertiesRegistry;
import org.eclipse.ptp.internal.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeProperties;

public class AddSyncConfigWizard extends Wizard {
	private AddSyncConfigWizardPage fMainPage;
	private final IProject fProject;

	public AddSyncConfigWizard(IProject project) {
		super();
		fProject = project;
		setNeedsProgressMonitor(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		super.addPages();
		fMainPage = new AddSyncConfigWizardPage("AddSyncConfigWizardPage", fProject); //$NON-NLS-1$
		fMainPage.setTitle(Messages.AddSyncConfigWizard_title); 
		fMainPage.setDescription(Messages.AddSyncConfigWizard_description); 
		addPage(fMainPage);
		ISynchronizeProperties prop = SynchronizePropertiesRegistry.getSynchronizePropertiesForProject(fProject);
		if (prop != null) {
			WizardPage[] pages = prop.createAddWizardPages(fProject);
			if (pages != null) {
				for (WizardPage page : pages) {
					addPage(page);
				}
			}
		}
	}

	public SyncConfig getSyncConfig() {
		return fMainPage.getSyncConfig();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		return true;
	}
}
