package org.eclipse.ptp.rdt.services.core.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.rdt.services.core.ServiceModelManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


public class PrintServiceModelAction implements IWorkbenchWindowActionDelegate {

	public void run(IAction action) {
		ServiceModelManager.getInstance().printServiceModel();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}
}
