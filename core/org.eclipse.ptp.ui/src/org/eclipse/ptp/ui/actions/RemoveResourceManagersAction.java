/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.ui.messages.Messages;
import org.eclipse.swt.widgets.Shell;

public class RemoveResourceManagersAction extends Action {
	
	private IResourceManagerControl[] selectedRMManagers;
	@SuppressWarnings("unused")
	private final Shell shell;

	public RemoveResourceManagersAction(Shell shell) {
		super(Messages.RemoveResourceManagersAction_0);
		this.shell = shell;
	}

	public void run() {
		String rmNames = ""; //$NON-NLS-1$
		for (int i = 0; i < selectedRMManagers.length; i++) {
			if (i > 0) {
				rmNames += ","; //$NON-NLS-1$
			}
			rmNames += "\n\t" + selectedRMManagers[i].getName(); //$NON-NLS-1$
		}
		
		boolean remove = MessageDialog.openConfirm(shell,
				Messages.RemoveResourceManagersAction_0,
				Messages.RemoveResourceManagersAction_1
				+ rmNames);
		
		if (remove) {
			PTPCorePlugin.getDefault().getModelManager().removeResourceManagers(selectedRMManagers);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	
	}

	public void setResourceManager(IResourceManagerControl[] rmManagers) {
		this.selectedRMManagers = rmManagers.clone();
	}

}
