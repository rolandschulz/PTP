package org.eclipse.ptp.pldt.wizards.wizardPages;

import org.eclipse.cdt.ui.templateengine.IPagesAfterTemplateSelectionProvider;
import org.eclipse.cdt.ui.templateengine.IWizardDataPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

public class MPIPageProvider implements IPagesAfterTemplateSelectionProvider {
	protected IWizardDataPage[] pages;

	public IWizardDataPage[] createAdditionalPages(IWorkbenchWizard wizard, IWorkbench workbench,
			IStructuredSelection selection) {
		System.out.println("createAdditionalPages");
		pages = new IWizardDataPage[0];
		
		return pages;
	}

	public IWizardDataPage[] getCreatedPages(IWorkbenchWizard wizard) {
		System.out.println("getCreatedPages");
		return pages;
	}

}
