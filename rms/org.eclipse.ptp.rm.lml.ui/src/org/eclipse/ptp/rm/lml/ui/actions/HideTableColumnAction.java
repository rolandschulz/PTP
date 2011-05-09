package org.eclipse.ptp.rm.lml.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.rm.lml.ui.messages.Messages;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class HideTableColumnAction extends Action {
	
	private final Shell shell;
	
	private final String gid;

	public HideTableColumnAction(String gid, Shell shell) {
		super(Messages.HideColumn);
		this.shell = shell;
		this.gid = gid;
	}

	public void dispose() {
	}

	public void run() {
		MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Click!", "There will be an update and an additional tablecolumn ("+ getText()+") will be seen");
	}

	public void selectionChanged(IAction action, ISelection selection) {
	
	}

}