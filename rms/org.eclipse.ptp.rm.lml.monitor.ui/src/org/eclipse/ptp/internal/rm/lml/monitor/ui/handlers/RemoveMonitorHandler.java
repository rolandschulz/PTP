/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rm.lml.monitor.ui.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.internal.rm.lml.monitor.ui.messages.Messages;
import org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl;
import org.eclipse.ptp.rm.lml.monitor.core.MonitorControlManager;
import org.eclipse.ui.handlers.HandlerUtil;

public class RemoveMonitorHandler extends AbstractHandler implements IHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			String msg = ((IStructuredSelection) selection).size() > 1 ? Messages.RemoveMonitorHandler_Are_you_sure_1
					: Messages.RemoveMonitorHandler_Are_you_sure_2;
			boolean confirm = MessageDialog.openConfirm(HandlerUtil.getActiveShell(event),
					Messages.RemoveMonitorHandler_Remove_Monitor, msg);
			if (confirm) {
				List<IMonitorControl> monitors = new ArrayList<IMonitorControl>();
				for (Iterator<?> itr = ((IStructuredSelection) selection).iterator(); itr.hasNext();) {
					Object sel = itr.next();
					if (sel instanceof IMonitorControl) {
						monitors.add((IMonitorControl) sel);
					}
				}
				MonitorControlManager.getInstance().removeMonitorControls(monitors.toArray(new IMonitorControl[0]));
			}
		}
		return null;
	}
}
