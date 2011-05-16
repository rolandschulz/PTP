package org.eclipse.ptp.rm.lml.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.ui.messages.Messages;

public class UpdateLguiAction extends Action {

	public UpdateLguiAction() {
		super(Messages.UpdateLguiAction_0);
	}

	@Override
	public void run() {
		LMLManager.getInstance().update();
	}
}
