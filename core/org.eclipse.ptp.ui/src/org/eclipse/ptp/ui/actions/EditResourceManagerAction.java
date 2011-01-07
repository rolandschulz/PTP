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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.ui.messages.Messages;
import org.eclipse.ptp.ui.wizards.RMServicesConfigurationWizard;
import org.eclipse.swt.widgets.Shell;

public class EditResourceManagerAction extends Action {

	private IResourceManagerConfiguration fSelectedRM;
	private final Shell fShell;

	public EditResourceManagerAction(Shell shell) {
		super(Messages.EditResourceManagerAction_0);
		fShell = shell;
	}

	public void dispose() {
	}

	@Override
	public void run() {
		final RMServicesConfigurationWizard wizard = new RMServicesConfigurationWizard(fSelectedRM);

		final WizardDialog dialog = new WizardDialog(fShell, wizard);
		int status = dialog.open();
		if (status != Dialog.OK) {
			return;
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {

	}

	/**
	 * @since 5.0
	 */
	public void setResourceManager(IResourceManagerConfiguration config) {
		fSelectedRM = config;
	}
}
