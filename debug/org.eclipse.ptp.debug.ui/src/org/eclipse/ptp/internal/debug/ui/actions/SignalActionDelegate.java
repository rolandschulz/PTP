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

import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.debug.core.model.IPSignal;
import org.eclipse.ptp.internal.debug.ui.PTPDebugUIPlugin;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * @author Clement chu
 */
public class SignalActionDelegate extends ActionDelegate implements IObjectActionDelegate {
	private IPSignal fSignal = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (getSignal() != null) {
			final MultiStatus ms = new MultiStatus(PTPDebugUIPlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, "Unable to deliver the signal to the target.", null); //$NON-NLS-1$
			BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
				public void run() {
					try {
						doAction(getSignal());
					}
					catch(DebugException e) {
						ms.merge(e.getStatus());
					}
				}
			});
			if (!ms.isOK()) {
				IWorkbenchWindow window = PTPDebugUIPlugin.getActiveWorkbenchWindow();
				if (window != null) {
					PTPDebugUIPlugin.errorDialog("Operation failed.", ms); //$NON-NLS-1$
				}
				else {
					PTPDebugUIPlugin.log(ms);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if (element instanceof IPSignal) {
				boolean enabled = enablesFor((IPSignal)element);
				action.setEnabled(enabled);
				if (enabled) {
					setSignal((IPSignal)element);
					return;
				}
			}
		}
		action.setEnabled(false);
		setSignal(null);
	}

	protected void doAction(IPSignal signal) throws DebugException {
		signal.signal();
	}

	private boolean enablesFor(IPSignal signal) {
		return (signal != null && signal.getDebugTarget().isSuspended());
	}

	private void setSignal(IPSignal signal) {
		fSignal = signal;
	}

	protected IPSignal getSignal() {
		return fSignal;
	}
}
