/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.sync.cdt.ui.wizards;

import org.eclipse.cdt.ui.wizards.conversion.ConversionWizard;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.messages.Messages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * Wizard that enables the conversion of existing C/C++ or Fortran projects to synchronized projects
 * @since 1.0
 * 
 */
public class ConvertLocalToSyncProjectWizard extends ConversionWizard {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.ui.wizards.conversion.ConversionWizard#init(org.eclipse
	 * .ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		super.init(workbench, currentSelection);
		setWindowTitle(getWindowTitleResource());
	}

	protected static String getWindowTitleResource() {
		return Messages.WizardProjectConversion_windowLabel;
	}

	@Override
	public void addPages() {
		addPage(mainPage = new ConvertLocalToSyncProjectWizardPage(getPrefix()));
	}

	@Override
	public String getProjectID() {
		return "org.eclipse.ptp.rdt.sync"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.wizards.NewCProjectWizard#
	 * initializeDefaultPageImageDescriptor()
	 */
	@Override
	protected void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/new_wiz.png");//$NON-NLS-1$
		setDefaultPageImageDescriptor(desc);
	}
}
