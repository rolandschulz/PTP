/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
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
package org.eclipse.ptp.internal.debug.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.internal.debug.ui.PDebugImage;
import org.eclipse.ptp.internal.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.internal.debug.ui.dialogs.ArrayVariableDialog;
import org.eclipse.ptp.internal.debug.ui.messages.Messages;
import org.eclipse.ptp.internal.debug.ui.views.PTabFolder;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Clement chu
 * 
 */
public class AddVariableAction extends Action {
	protected PTabFolder folder = null;
	public static final String name = Messages.AddVariableAction_0;

	/**
	 * Constructor
	 * 
	 * @param folder
	 */
	public AddVariableAction(PTabFolder folder) {
		super(name, IAction.AS_PUSH_BUTTON);
		setImageDescriptor(PDebugImage.getDescriptor(PDebugImage.ICON_ADD_VAR_NORMAL));
		setToolTipText(name);
		this.folder = folder;
	}

	/**
	 * Get shell
	 * 
	 * @return
	 */
	public Shell getShell() {
		return folder.getViewSite().getShell();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				try {
					openDialog(folder.getStackFrame());
				} catch (CoreException e) {
					PTPDebugUIPlugin.errorDialog(getShell(), Messages.AddVariableAction_1, e.getStatus());
				}
			}
		});
	}

	/**
	 * Open array variable dialog
	 * 
	 * @param frame
	 * @throws DebugException
	 */
	protected void openDialog(IPStackFrame frame) throws DebugException {
		if (frame != null) {
			ArrayVariableDialog dialog = new ArrayVariableDialog(getShell(), frame);
			if (dialog.open() == Window.OK) {
				IVariable variable = dialog.getSelectedVariable();
				if (variable != null) {
					folder.createTabItem(variable.getName(), variable);
				}
			}
		}
	}
}
