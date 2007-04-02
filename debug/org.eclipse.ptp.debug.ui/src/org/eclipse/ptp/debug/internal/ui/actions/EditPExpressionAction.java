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
package org.eclipse.ptp.debug.internal.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.debug.internal.ui.PDebugImage;
import org.eclipse.ptp.debug.internal.ui.PJobVariableManager.JobVariable;
import org.eclipse.ptp.debug.internal.ui.views.variable.PVariableDialog;
import org.eclipse.ptp.debug.internal.ui.views.variable.PVariableView;

/**
 * @author Clement chu
 */
public class EditPExpressionAction extends Action {
	public static final String name = "Edit";
	private PVariableView view = null;

	/** Constructor
	 * @param view
	 */
	public EditPExpressionAction(PVariableView view) {
		super(name, IAction.AS_PUSH_BUTTON);
	    setImageDescriptor(PDebugImage.getDescriptor(PDebugImage.ICON_VAR_EDIT_NORMAL));
	    //setDisabledImageDescriptor(PDebugImage.ID_ICON_VAR_EDIT_DISABLE);
	    setToolTipText(name);
	    setId(name);
	    setEnabled(false);
	    this.view = view;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		ISelection selection = view.getSelection();
		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			JobVariable jVar = (JobVariable)((IStructuredSelection)selection).getFirstElement();
			if (!jVar.getJob().getIDString().equals(view.getUIManager().getCurrentJobId())) {
				MessageDialog.openError(view.getViewSite().getShell(), "Not allow editing", "Selected item does not belong to current Job");
				return;
			}
			if (new PVariableDialog(view, PVariableDialog.EDIT_MODE).open() == Window.OK) {
				view.refresh();
				view.getUIManager().updateVariableValue(true, null);
			}
		}		
	}
}
