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

/*
 * Created on Sep 23, 2004
 *
 */
package org.eclipse.ptp.ui.actions.old;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.ui.old.SearchDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class SearchAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	public void run(IAction action) {
	    new SearchDialog(window.getShell()).open();	    
	    
	    /*
		String input = sd.open();
		if(input != null) {
			int selection = sd.getSelection();
			Integer intval;
			try {
				intval = new Integer(input);
			} catch(NumberFormatException e) {
				intval = null;
			}

			AbstractParallelView nodeView = (AbstractParallelView)window.getActivePage().findView(UIUtils.ParallelNodeStatusView_ID);
			AbstractParallelView treeView = (AbstractParallelView)window.getActivePage().findView(UIUtils.ParallelProcessesView_ID);
			
			if(selection == SearchDialog.NODE) {
			    if (nodeView != null)
			        nodeView.searchForNode(intval.intValue());
			    if (treeView != null)
			        treeView.searchForNode(intval.intValue());
			}
			else if(selection == SearchDialog.PROCESS) {
			    if (nodeView != null)
			        nodeView.searchForProcess(intval.intValue());
			    if (treeView != null)
			        treeView.searchForProcess(intval.intValue());
			}
		}*/
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}
	
	public void dispose() {
	}
	
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}	
}
