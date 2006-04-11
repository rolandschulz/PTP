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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Clement chu
 * 
 */
public class RemoveAllPVariableActionDelegate extends AbstractPVariableAction {
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		doAction(view.getViewSite().getShell());
	}
	
	/** Take action to remove all ptp varaible in the current selected job
	 * @param shell
	 */
	public static void doAction(Shell shell) {
		if (getCurrentRunningJob() == null)
			return;

		if (MessageDialog.openConfirm(shell, "Remove all variables", "Confirm to remove all variables?")) {
			PTPDebugCorePlugin.getPVariableManager().removeAllVariables(PTPDebugUIPlugin.getDefault().getUIDebugManager().getCurrentJob());
		}
	}
}
