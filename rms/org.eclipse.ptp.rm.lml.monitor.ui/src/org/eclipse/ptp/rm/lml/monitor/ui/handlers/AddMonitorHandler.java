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

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		AddMonitorDialog dialog = new AddMonitorDialog(HandlerUtil.getActiveShell(event));
		if (dialog.open() == Dialog.OK) {
			MonitorControlManager.getInstance().createMonitorControl(dialog.getRemoteConnection().getRemoteServices().getId(),
					dialog.getRemoteConnection().getName(), dialog.getConfigurationName());
		}
		return null;
	}

}
