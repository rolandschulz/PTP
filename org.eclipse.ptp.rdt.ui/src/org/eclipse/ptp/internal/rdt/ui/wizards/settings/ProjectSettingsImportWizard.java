/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.ui.wizards.settings;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.internal.rdt.ui.RDTPluginImages;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

/**
 */
public class ProjectSettingsImportWizard extends ProjectSettingsWizard implements IImportWizard {


	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
		setWindowTitle(Messages.ProjectSettingsWizardPage_Import_title);
		setDefaultPageImageDescriptor(RDTPluginImages.DESC_WIZBAN_IMPORT_C_SETTINGS);
	}

	@Override
	public ProjectSettingsWizardPage getPage() {
		return ProjectSettingsWizardPage.createImportWizardPage();
	}

}
