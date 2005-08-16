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
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * @author clement chu
 *
 */
public class GotoSet implements IViewActionDelegate {
	private IStructuredSelection selection = null;
	
	public void init(IViewPart view) {}
	
	public void run(IAction action) {
		IPBreakpoint breakpoint = getPBreakpoint();
		if (breakpoint != null) {
			UIUtils.showView(IPTPDebugUIConstants.VIEW_PARALLELDEBUG);
			ParallelDebugView view = ParallelDebugView.getDebugViewInstance();
			if (view != null) {
				try {				
					String jid = breakpoint.getJobId();
					String sid = breakpoint.getSetId();
					view.changeJob(jid);
					IElementHandler elementHandler = view.getCurrentElementHandler();
					if (elementHandler != null) {
						view.selectSet(elementHandler.getSet(sid));
					}
					view.update();
					view.refresh();
				} catch (CoreException e) {
					PTPDebugUIPlugin.log(e);
				}
			}
		}
	}
	
	private IPBreakpoint getPBreakpoint() {
		if (selection.isEmpty())
			return null;
		
		Object obj = selection.getFirstElement();
		if (obj instanceof IPBreakpoint)
			return (IPBreakpoint)obj;
		
		return null;
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection)selection;
			action.setEnabled(this.selection.size()==1 && getPBreakpoint() != null);
		}
	}
}
