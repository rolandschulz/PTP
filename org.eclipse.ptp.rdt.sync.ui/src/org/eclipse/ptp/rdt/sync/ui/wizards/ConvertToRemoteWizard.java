/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rdt.sync.ui.wizards;

import org.eclipse.cdt.ui.wizards.conversion.ConversionWizard;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * Wizard that enables the conversion of existing projects to RDT projects
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the RDT team.
 * 
 * @author vkong
 */
public class ConvertToRemoteWizard extends ConversionWizard {

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
		addPage(mainPage = new ConvertToRemoteWizardPage(getPrefix()));
	}

	@Override
	public String getProjectID() {
		return "org.eclipse.ptp.rdt.remote"; //$NON-NLS-1$
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
