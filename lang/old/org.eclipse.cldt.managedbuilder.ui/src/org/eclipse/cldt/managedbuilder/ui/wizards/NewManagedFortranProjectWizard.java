package org.eclipse.cldt.managedbuilder.ui.wizards;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import org.eclipse.cldt.managedbuilder.internal.ui.ManagedBuilderUIMessages;

public class NewManagedFortranProjectWizard extends NewManagedProjectWizard {
	// String constants
	private static final String WZ_TITLE = "MngFortranWizard.title"; //$NON-NLS-1$
	private static final String WZ_DESC = "MngFortranWizard.description"; //$NON-NLS-1$
	private static final String SETTINGS_TITLE= "MngFortranWizardSettings.title"; //$NON-NLS-1$
	private static final String SETTINGS_DESC= "MngFortranWizardSettings.description"; //$NON-NLS-1$

	public NewManagedFortranProjectWizard() {
		this(ManagedBuilderUIMessages.getResourceString(WZ_TITLE), ManagedBuilderUIMessages.getResourceString(WZ_DESC));
	}

	public NewManagedFortranProjectWizard(String title, String description) {
		super(title, description);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		// Add the default page for all new managed projects 
		super.addPages();
	}

}
