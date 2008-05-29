/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corp. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - initial implementation
 *********************************************************************************/

package org.eclipse.ptp.pldt.wizards.wizardPages;
import org.eclipse.cdt.ui.templateengine.IPagesAfterTemplateSelectionProvider;
import org.eclipse.cdt.ui.templateengine.IWizardDataPage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

/**
 * Wizard Pages for MPI C++ projects as opposed to C projects
 * @author Beth Tibbitts
 *
 */
public class MPIProjectProcessPagesCPP implements IPagesAfterTemplateSelectionProvider {
	IWizardDataPage[] pages;
	
	public IWizardDataPage[] createAdditionalPages(IWorkbenchWizard wizard,
			IWorkbench workbench, IStructuredSelection selection) {
		try {
			pages= new IWizardDataPage[] {new MPIProjectWizardPageCPP()};
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
