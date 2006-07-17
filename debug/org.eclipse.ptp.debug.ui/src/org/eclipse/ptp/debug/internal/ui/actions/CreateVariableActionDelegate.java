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
import org.eclipse.ptp.debug.internal.ui.dialogs.PTPVariablesDialog;

/**
 * @author Clement chu
 * 
 */
public class CreateVariableActionDelegate extends AbstractPVariableAction {
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
		super.init(action);
		action.setEnabled(false);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		new PTPVariablesDialog(view.getViewSite().getShell()).open();		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.listeners.IJobListener#jobChangedEvent(java.lang.String, java.lang.String)
	 */
	public void jobChangedEvent(String cur_job_id, String pre_job_id) {
		action.setEnabled(!uiManager.isJobStop(cur_job_id));
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.listeners.IJobListener#jobRemovedEvent(java.lang.String)
	 */
	public void jobRemovedEvent(String job_id) {
	}
}
