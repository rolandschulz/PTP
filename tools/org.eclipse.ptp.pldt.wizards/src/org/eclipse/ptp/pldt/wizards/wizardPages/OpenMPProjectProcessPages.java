/**********************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.wizards.wizardPages;
import org.eclipse.cdt.ui.templateengine.IPagesAfterTemplateSelectionProvider;
import org.eclipse.cdt.ui.templateengine.IWizardDataPage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;


public class OpenMPProjectProcessPages implements IPagesAfterTemplateSelectionProvider {
	IWizardDataPage[] pages;
	
	public IWizardDataPage[] createAdditionalPages(IWorkbenchWizard wizard,
			IWorkbench workbench, IStructuredSelection selection) {
		try {
			pages= new IWizardDataPage[] {new OpenMPProjectWizardPage()};
			// TODO - log error
		} catch(CoreException ce) {
			pages= new IWizardDataPage[0];
		}
		return pages;
	}

	public IWizardDataPage[] getCreatedPages(IWorkbenchWizard wizard) {
		return pages;
	}
}
