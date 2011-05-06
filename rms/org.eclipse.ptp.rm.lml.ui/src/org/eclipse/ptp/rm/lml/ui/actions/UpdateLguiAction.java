package org.eclipse.ptp.rm.lml.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.rm.lml.core.ILMLManager;
import org.eclipse.ptp.rm.lml.core.LMLCorePlugin;
import org.eclipse.ptp.rm.lml.ui.messages.Messages;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class UpdateLguiAction extends Action{

	private final Shell shell;

	public UpdateLguiAction(Shell shell) {
		super(Messages.UpdateLguiAction_0);
		this.shell = shell;
	}

	public void dispose() {
	}

	public void run() {
		ILMLManager lmlManager = LMLCorePlugin.getDefault().getLMLManager();
		lmlManager.update();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	
	}
}
