/*******************************************************************************
 * Copyright (c) 2007 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.wizards.wizardPages;

import org.eclipse.cdt.ui.templateengine.IPagesAfterTemplateSelectionProvider;
import org.eclipse.cdt.ui.templateengine.IWizardDataPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

/**
 * 
 * @author tibbitts
 *
 */
public class MPIPageProvider implements IPagesAfterTemplateSelectionProvider {
	protected IWizardDataPage[] pages;

	public IWizardDataPage[] createAdditionalPages(IWorkbenchWizard wizard, IWorkbench workbench,
			IStructuredSelection selection) {
		System.out.println("MPIPageProvider.createAdditionalPages()...");
		pages = new IWizardDataPage[0];// empty now
		
		return pages;
	}

	public IWizardDataPage[] getCreatedPages(IWorkbenchWizard wizard) {
		System.out.println("MPIPageProvider.getCreatedPages()...");
		return pages;
	}

}
