/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch, FZ Juelich
 */
package org.eclipse.ptp.rm.lml.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.rm.lml.ui.messages.Messages;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class UpdateLguiAction extends Action{

	private final Shell shell;

	public UpdateLguiAction(Shell shell) {
		super(Messages.UpdateLguiAction_0);
		this.shell = shell;
	}

	public void dispose() {
	}

	public void run() {
		MessageBox messageBox = new MessageBox(shell);
		messageBox.setMessage("Later there will be an update");
		messageBox.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	
	}
}
