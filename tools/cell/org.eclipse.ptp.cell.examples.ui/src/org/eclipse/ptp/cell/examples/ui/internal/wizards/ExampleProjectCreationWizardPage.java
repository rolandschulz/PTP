/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.examples.ui.internal.wizards;

import org.eclipse.ptp.cell.examples.ui.internal.ExampleMessages;
import org.eclipse.ptp.cell.examples.ui.internal.ProjectWizardDefinition;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;


/**
 * @author laggarcia
 * @since 1.1.1
 */
public class ExampleProjectCreationWizardPage extends
		WizardNewProjectCreationPage {

	private ProjectWizardDefinition projectWizardDefinition;

	private String initialProjectName;

	/**
	 * 
	 */
	public ExampleProjectCreationWizardPage(
			ProjectWizardDefinition projectWizardDefinition) {
		super(projectWizardDefinition.getName());

		this.projectWizardDefinition = projectWizardDefinition;
		initialProjectName = projectWizardDefinition.getName();
		setTitle(projectWizardDefinition.getPageTitle());
		setDescription(projectWizardDefinition.getPageDescription());
		setInitialProjectName(initialProjectName);
	}

	/**
	 * @see org.eclipse.ui.dialogs.WizardNewProjectCreationPage#validatePage()
	 */
	protected boolean validatePage() {
		if (!super.validatePage()) {
			return false;
		}

		String projectName = getProjectName();
		if (projectName == null) {
			return false;
		}

		IWizard wizard = getWizard();
		if (wizard instanceof ExampleProjectCreationWizard) {
			IWizardPage[] pages = wizard.getPages();
			for (int i = 0; i < pages.length; i++) {
				if ((pages[i] != this)
						&& (pages[i] instanceof ExampleProjectCreationWizardPage)
						&& (projectName
								.equals(((ExampleProjectCreationWizardPage) pages[i])
										.getProjectName()))) {
					setErrorMessage(ExampleMessages.wizardPageErrorAlreadyExists);
					return false;
				}
			}
			if (!this.getProjectName().equals(initialProjectName)) {
				setMessage(ExampleMessages.wizardPageWarningNameChanged);
			}
		}

		return true;
	}

	public ProjectWizardDefinition getProjectWizardDefinition() {
		return projectWizardDefinition;
	}

}
