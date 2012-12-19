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

import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.debug.internal.ui.PDebugImage;
import org.eclipse.ptp.debug.internal.ui.views.variable.PVariableView;
import org.eclipse.ptp.debug.ui.PVariableManager.PVariableInfo;
import org.eclipse.ptp.debug.ui.messages.Messages;

/**
 * @author Clement chu
 */
public class DeletePExpressionAction extends Action {
	public static final String name = Messages.DeletePExpressionAction_0;
	private PVariableView view = null;

	/**
	 * Constructor
	 * 
	 * @param view
	 */
	public DeletePExpressionAction(PVariableView view) {
		super(name, IAction.AS_PUSH_BUTTON);
		setImageDescriptor(PDebugImage.getDescriptor(PDebugImage.ICON_VAR_DELETE_NORMAL));
		// setDisabledImageDescriptor(PDebugImage.ID_ICON_VAR_DELETE_DISABLE);
		setToolTipText(name);
		setId(name);
		setEnabled(false);
		this.view = view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		ISelection selection = view.getSelection();
		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			IStructuredSelection structSelection = (IStructuredSelection) selection;
			for (Iterator<?> i = structSelection.iterator(); i.hasNext();) {
				PVariableInfo jVar = (PVariableInfo) i.next();
				try {
					view.getUIManager().getJobVariableManager().removeVariable(jVar.getJobId(), jVar.getName());
				} catch (CoreException e) {
					e.printStackTrace();
				}
				view.refresh();
			}
		}
	}
}
