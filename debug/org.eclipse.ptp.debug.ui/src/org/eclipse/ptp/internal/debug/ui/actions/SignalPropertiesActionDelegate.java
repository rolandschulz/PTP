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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ptp.debug.core.model.IPSignal;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.dialogs.PropertyDialogAction;

/**
 * @author Clement chu
 */
public class SignalPropertiesActionDelegate extends ActionDelegate implements IObjectActionDelegate {
	private IPSignal fSignal;
	private IWorkbenchPart fTargetPart;

	/**
	 * Constructor for SignalPropertiesActionDelegate.
	 * 
	 */
	public SignalPropertiesActionDelegate() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		fTargetPart = targetPart;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if (element instanceof IPSignal) {
				action.setEnabled(true);
				setSignal((IPSignal)element);
				return;
			}
		}
		action.setEnabled(false);
		setSignal(null);
	}

	protected IPSignal getSignal() {
		return this.fSignal;
	}

	private void setSignal(IPSignal signal) {
		this.fSignal = signal;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		PropertyDialogAction propertyAction = new PropertyDialogAction(getActivePart().getSite(), new ISelectionProvider() {
			public void addSelectionChangedListener(ISelectionChangedListener listener) {
			}

			public ISelection getSelection() {
				return new StructuredSelection(getSignal());
			}

			public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			}

			public void setSelection(ISelection selection) {
			}
		});
		propertyAction.run();
	}

	protected IWorkbenchPart getActivePart() {
		return fTargetPart;
	}
}
