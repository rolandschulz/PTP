/**
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.internal.services.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.internal.services.ui.messages.Messages;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class RemoveServiceConfigurationAction implements IObjectActionDelegate {
	
	private IServiceModelManager fManager = ServiceModelManager.getInstance();
	private List<IServiceConfiguration> fConfigurations = new ArrayList<IServiceConfiguration>();
	private IWorkbenchPart fWorkbenchPart;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		String names = ""; //$NON-NLS-1$
		for (int i = 0; i < fConfigurations.size(); i++) {
			if (i > 0) {
				names += ", "; //$NON-NLS-1$
			}
			names += "\n\t" + fConfigurations.get(i).getName(); //$NON-NLS-1$
		}
		
		boolean remove = MessageDialog.openConfirm(fWorkbenchPart.getSite().getShell(),
				Messages.RemoveServiceConfigurationAction_0,
				Messages.RemoveServiceConfigurationAction_1
				+ names);
		
		if (remove) {
			for (IServiceConfiguration conf : fConfigurations) {
				fManager.remove(conf);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fConfigurations.clear();
		IStructuredSelection structSel = (IStructuredSelection)selection;
		for (Object selected : structSel.toArray()) {
			if (selected instanceof IServiceConfiguration) {
				fConfigurations.add((IServiceConfiguration)selected);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		fWorkbenchPart = targetPart;
	}
}
