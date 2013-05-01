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
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.PropertyDialogAction;

/**
 * @author clement chu
 * 
 */
public class PBreakpointPropertiesAction implements IObjectActionDelegate {
	private IWorkbenchPart part;
	private IPBreakpoint breakpoint;

	/**
	 * Constructor
	 * 
	 */
	public PBreakpointPropertiesAction() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		part = targetPart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		PropertyDialogAction propertyAction = new PropertyDialogAction(getActivePart().getSite(), new ISelectionProvider() {
			public void addSelectionChangedListener(ISelectionChangedListener listener) {
			}

			public ISelection getSelection() {
				return new StructuredSelection(getBreakpoint());
			}

			public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			}

			public void setSelection(ISelection selection) {
			}
		});
		propertyAction.run();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			if (ss.isEmpty() || ss.size() > 1) {
				return;
			}
			Object element = ss.getFirstElement();
			if (element instanceof IPBreakpoint) {
				setBreakpoint((IPBreakpoint) element);
			}
		}
	}

	/**
	 * Get active workbench part
	 * 
	 * @return
	 */
	protected IWorkbenchPart getActivePart() {
		return part;
	}

	/**
	 * Get ptp breakpoint
	 * 
	 * @return
	 */
	protected IPBreakpoint getBreakpoint() {
		return breakpoint;
	}

	/**
	 * Set ptp breakpoint
	 * 
	 * @param breakpoint
	 */
	protected void setBreakpoint(IPBreakpoint breakpoint) {
		this.breakpoint = breakpoint;
	}
}
