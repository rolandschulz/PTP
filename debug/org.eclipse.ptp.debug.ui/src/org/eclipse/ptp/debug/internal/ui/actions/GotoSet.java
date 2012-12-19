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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.debug.ui.views.ParallelDebugView;
import org.eclipse.ptp.ui.IElementManager;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * @author clement chu
 * 
 */
public class GotoSet implements IViewActionDelegate {
	private IStructuredSelection selection = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IPBreakpoint breakpoint = getPBreakpoint();
		if (breakpoint != null) {
			IViewPart view = PTPDebugUIPlugin.getActiveWorkbenchWindow().getActivePage()
					.findView(IPTPDebugUIConstants.ID_VIEW_PARALLELDEBUG);
			if (view instanceof ParallelDebugView) {
				ParallelDebugView pview = (ParallelDebugView) view;
				try {
					String jid = breakpoint.getJobId();
					if (jid.equals(IPBreakpoint.GLOBAL)) {
						jid = IElementManager.EMPTY_ID;
					}

					pview.doChangeJob(jid);
					IElementHandler elementHandler = pview.getCurrentElementHandler();
					if (elementHandler != null) {
						pview.selectSet(elementHandler.getSet(breakpoint.getSetId()));
					}
					pview.refresh(true);
				} catch (CoreException e) {
					PTPDebugUIPlugin.log(e);
				}
			}
		}
	}

	/**
	 * Get PTP breakpoint
	 * 
	 * @return null if there is no ptp breakpoint
	 */
	private IPBreakpoint getPBreakpoint() {
		if (selection.isEmpty()) {
			return null;
		}

		Object obj = selection.getFirstElement();
		if (obj instanceof IPBreakpoint) {
			return (IPBreakpoint) obj;
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
			action.setEnabled(this.selection.size() == 1 && getPBreakpoint() != null);
		}
	}
}
