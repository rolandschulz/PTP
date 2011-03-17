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
 * 
 * Modified by:
 * 		Claudia Knobloch, Foprschungszentrum Juelich GmbH
 *******************************************************************************/

package org.eclipse.ptp.rm.lml.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ptp.rm.lml.ui.messages.Messages;
import org.eclipse.ptp.rm.lml.ui.wizards.SelectFilesWizard;
import org.eclipse.swt.widgets.Shell;

public class AddLguiAction extends Action {
	
	private final Shell shell;

	public AddLguiAction(Shell shell) {
		super(Messages.AddLMLAction_0);
		this.shell = shell;
	}

	public void dispose() {
	}

	public void run() {
		final SelectFilesWizard wizard = new SelectFilesWizard();
		
		final WizardDialog dialog = new WizardDialog(shell, wizard);
		int status = dialog.open();
		if (status != Dialog.OK) {
			return;
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	
	}

}