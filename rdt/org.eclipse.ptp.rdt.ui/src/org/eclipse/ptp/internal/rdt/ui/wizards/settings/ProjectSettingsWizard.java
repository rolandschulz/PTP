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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;

/**
 */
public abstract class ProjectSettingsWizard extends Wizard {

	private ProjectSettingsWizardPage mainPage;
	private IStructuredSelection selection;
	
	public abstract ProjectSettingsWizardPage getPage(); 

	@Override
	public void addPages() {
		super.addPages();
		mainPage = getPage();
		
		// happens if the user invoked the wizard by right clicking on a project element
		if(selection != null) {
			IProject project = (IProject)selection.getFirstElement();
			mainPage.setInitialProject(project);
		}
		
		addPage(mainPage);
	}

	@Override
	public boolean performFinish() {
		return mainPage.finish();
	}
	

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		setNeedsProgressMonitor(true);
	}
	
	
}
