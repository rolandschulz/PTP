package org.eclipse.ptp.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.ui.LegendDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * @author ndebard
 */

public class ShowLegendAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	public void run(IAction action) {
		new LegendDialog(window.getShell()).open();
	}
	public void selectionChanged(IAction action, ISelection selection) {
	}	
	public void dispose() {
	}	
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}	
}

/*
public class ShowLegendAction extends ParallelAction {
	public ShowLegendAction(ViewPart view) {
		super(view);
	}

	protected void init(boolean isEnable) {
	    this.setText(UIMessage.getResourceString("ShowLegendAction.text"));
	    this.setToolTipText(UIMessage.getResourceString("ShowLegendAction.tooltip"));
	    this.setImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_SHOWLEGEND_ACTION_NORMAL));
	    this.setDisabledImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_SHOWLEGEND_ACTION_DISABLE));
	    this.setHoverImageDescriptor(ParallelImages.getDescriptor(ParallelImages.IMG_SHOWLEGEND_ACTION_HOVER));
	    this.setEnabled(getLaunchManager().isMPIRuning());
	}

	public void run() {
		LegendDialog ld = new LegendDialog(getShell());
		ld.open();
	}
}
*/
