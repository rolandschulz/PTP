/*
 * Created on Sep 23, 2004
 *
 */
package org.eclipse.ptp.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.ui.SearchDialog;
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
