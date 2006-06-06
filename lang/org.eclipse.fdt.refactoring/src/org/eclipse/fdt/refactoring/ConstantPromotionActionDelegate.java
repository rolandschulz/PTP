package org.eclipse.fdt.refactoring;

import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class ConstantPromotionActionDelegate implements IObjectActionDelegate {
	private IWorkbenchPart fPart;
	private IContainer fContainer;	// TODO search container for TranslationUnits?
	private IStructuredSelection fSelection;
	private ConstantPromotionAction fAction;
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// not sure what I need with this
		fPart = targetPart;
	}

	public void run(IAction action) {
		if (fAction == null) {
			fAction = new ConstantPromotionAction();
		}
		fAction.run(fSelection);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Fix to work properly with folders
		boolean enabled = false;
		if (selection instanceof IStructuredSelection) {
			fSelection = (IStructuredSelection) selection;
			Object obj = fSelection.getFirstElement();
			if (obj instanceof ICElement) {
				if (obj instanceof ICContainer || obj instanceof ICProject) {
					fContainer = (IContainer) ((ICElement) obj).getUnderlyingResource();
				} else {
					obj = ((ICElement)obj).getResource();
					if (obj instanceof IFile) {
						// TODO check for Fortran file type
						fContainer = ((IResource)obj).getParent();
						enabled = true;
					}
				}
			} else if (obj instanceof IResource) {
				if (obj instanceof IContainer) {
					fContainer = (IContainer) obj;
				} else {
					fContainer = ((IResource)obj).getParent();
				}
			} else {
				fContainer = null;
			}
//			if (fContainer != null && MakeCorePlugin.getDefault().getTargetManager().hasTargetBuilder(fContainer.getProject())) {
//				enabled = true;
//			}
		} else {
			fSelection = null;
		}
		action.setEnabled(enabled);

	}

}
