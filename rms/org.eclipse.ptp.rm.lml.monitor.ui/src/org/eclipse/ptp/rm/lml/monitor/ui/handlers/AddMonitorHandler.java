package org.eclipse.ptp.rm.lml.monitor.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ptp.rm.lml.monitor.core.MonitorControlManager;
import org.eclipse.ptp.rm.lml.monitor.ui.dialogs.AddMonitorDialog;
import org.eclipse.ui.handlers.HandlerUtil;

public class AddMonitorHandler extends AbstractHandler implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		AddMonitorDialog dialog = new AddMonitorDialog(HandlerUtil.getActiveShell(event));
		if (dialog.open() == Dialog.OK) {
			MonitorControlManager.getInstance().createMonitorControl(dialog.getSystemType(),
					dialog.getRemoteConnection().getRemoteServices().getId(), dialog.getRemoteConnection().getName());
		}
		return null;
	}

}
