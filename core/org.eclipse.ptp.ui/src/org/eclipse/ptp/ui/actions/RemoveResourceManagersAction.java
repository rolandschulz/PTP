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
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.ui.messages.Messages;
import org.eclipse.swt.widgets.Shell;

public class RemoveResourceManagersAction extends Action {

	private IResourceManagerConfiguration[] fSelectedRMs;
	private final Shell fShell;

	public RemoveResourceManagersAction(Shell shell) {
		super(Messages.RemoveResourceManagersAction_0);
		fShell = shell;
	}

	@Override
	public void run() {
		String rmNames = ""; //$NON-NLS-1$
		for (int i = 0; i < fSelectedRMs.length; i++) {
			if (i > 0) {
				rmNames += ","; //$NON-NLS-1$
			}
			rmNames += "\n\t" + fSelectedRMs[i].getName(); //$NON-NLS-1$
		}

		boolean remove = MessageDialog.openConfirm(fShell, Messages.RemoveResourceManagersAction_0,
				Messages.RemoveResourceManagersAction_1 + rmNames);

		if (remove) {
			PTPCorePlugin.getDefault().getModelManager().removeResourceManagers(fSelectedRMs);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {

	}

	/**
	 * @since 5.0
	 */
	public void setResourceManagers(IResourceManagerConfiguration[] rms) {
		fSelectedRMs = rms;
	}

}
