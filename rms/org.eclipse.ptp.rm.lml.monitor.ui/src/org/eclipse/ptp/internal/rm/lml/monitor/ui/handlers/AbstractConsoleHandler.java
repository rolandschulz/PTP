/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rm.lml.monitor.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.rm.jaxb.control.core.ILaunchController;
import org.eclipse.ptp.rm.jaxb.control.core.LaunchControllerManager;
import org.eclipse.ptp.rm.lml.core.JobStatusData;
import org.eclipse.ptp.rm.lml.internal.core.model.Row;
import org.eclipse.ui.handlers.HandlerUtil;

public abstract class AbstractConsoleHandler extends AbstractHandler {
	protected JobStatusData status;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection != null && !selection.isEmpty()) {
			Row row = (Row) selection.getFirstElement();
			status = row.status;
			String controlId = status.getString(JobStatusData.CONTROL_ID_ATTR);
			if (controlId != null) {
				ILaunchController control = LaunchControllerManager.getInstance().getLaunchController(controlId);
				if (control != null) {
					ActionUtils.readRemoteFile(control.getRemoteServicesId(), control.getConnectionName(), getPath());
				}
			}
		}
		return null;
	}

	/**
	 * @return correct file path
	 */
	protected String getPath() {
		String attr = isError() ? JobStatusData.STDERR_REMOTE_FILE_ATTR : JobStatusData.STDOUT_REMOTE_FILE_ATTR;
		return status.getString(attr);
	}

	/**
	 * @return correct file status
	 */
	protected boolean getReady() {
		if (isError()) {
			return status.getErrReady();
		}
		return status.getOutReady();
	}

	protected abstract boolean isError();
}
