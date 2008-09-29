/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.environment.ui.deploy.events;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ptp.cell.environment.ui.deploy.debug.Debug;
import org.eclipse.ptp.cell.environment.ui.deploy.wizard.CellManagerSelectionPage;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.control.ITargetStatus;
import org.eclipse.ptp.remotetools.environment.core.ITargetElement;
import org.eclipse.ptp.remotetools.environment.core.TargetElement;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;


public class ManageEnvironmentAction implements IViewActionDelegate {
	
	IViewPart view = null;
	ISelection selection = null;
	
	public void init(IViewPart v) {
		view = v;
	}

	public void run(IAction action) {
		Debug.POLICY.enter(Debug.DEBUG_JOBS);
		Shell shell = view.getViewSite().getShell();
		
		final Object obj = ((IStructuredSelection)selection).getFirstElement();
		if(obj != null){
			if (TargetElement.class.isAssignableFrom(obj.getClass())){
				try {
					ITargetElement element = (ITargetElement)obj;
					ITargetControl control = element.getControl();
					if((control.query() == ITargetStatus.RESUMED) || (control.query() == ITargetStatus.PAUSED)){
//						if(control.query() == ICellStatus.PAUSED)
//							control.resume(new NullProgressMonitor());						
						openWizard(control);
					}
					else{
						MessageDialog.openInformation(shell, Messages.ManageEnvironmentAction_0, Messages.ManageEnvironmentAction_1);
					}
					return;
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		MessageDialog.openInformation(shell, Messages.ManageEnvironmentAction_2, Messages.ManageEnvironmentAction_3);
		Debug.POLICY.exit(Debug.DEBUG_JOBS);
	}
	
	private void openWizard(final ITargetControl control){
		
		Wizard wizard = new Wizard(){			
			public void addPages(){	addPage(new CellManagerSelectionPage("CellManagerSelectionPage", control)); } //$NON-NLS-1$
			
			public boolean canFinish(){	return false; }

			public boolean performFinish() { return false; }
		};
		wizard.setWindowTitle(Messages.ManageEnvironmentAction_4);
		wizard.setForcePreviousAndNextButtons(true);
		
		WizardDialog dialog = new WizardDialog(view.getViewSite().getShell(), wizard);
		dialog.open();
	}

	public void selectionChanged(IAction action, ISelection newSelection) {
		this.selection = newSelection;
	}

}
