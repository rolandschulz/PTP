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
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ptp.internal.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ptp.internal.debug.ui.messages.Messages;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Clement chu
 *
 */
public class EnableDisableBreakpointRulerAction extends AbstractBreakpointRulerAction {
	/** Constructor
	 * @param part
	 * @param info
	 */
	public EnableDisableBreakpointRulerAction(IWorkbenchPart part, IVerticalRulerInfo info) {
		setInfo(info);
		setTargetPart(part);
		setText(Messages.EnableDisableBreakpointRulerAction_Enable_Breakpoint_1);
		setId(IPTPDebugUIConstants.ACTION_ENABLE_DISABLE_BREAKPOINT);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		if (getBreakpoint() != null) {
			try {
				getBreakpoint().setEnabled(!getBreakpoint().isEnabled());
			} catch (CoreException e) {
				ErrorDialog.openError(getTargetPart().getSite().getShell(), Messages.EnableDisableBreakpointRulerAction_Enabling_disabling_breakpoints_1, Messages.EnableDisableBreakpointRulerAction_Exceptions_occurred_enabling_or_disabling_breakpoint_1, e.getStatus());
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		setBreakpoint(determineBreakpoint());
		if (getBreakpoint() == null) {
			setEnabled(false);
			return;
		}
		setEnabled(true);
		try {
			boolean enabled = getBreakpoint().isEnabled();
			setText(enabled?Messages.EnableDisableBreakpointRulerAction_Disable_Breakpoint_1:Messages.EnableDisableBreakpointRulerAction_Enable_Breakpoint_1);
		} catch (CoreException e) {
			DebugPlugin.log(e);
		}
	}
}
