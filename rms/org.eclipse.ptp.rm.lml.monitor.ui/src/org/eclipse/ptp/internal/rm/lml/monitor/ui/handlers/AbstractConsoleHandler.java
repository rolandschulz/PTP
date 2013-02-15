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
			if (status.getRemoteId() != null && status.getConnectionName() != null) {
				String path = isError() ? status.getErrorPath() : status.getOutputPath();
				ActionUtils.readRemoteFile(status.getRemoteId(), status.getConnectionName(), path);
			}
		}
		return null;
	}

	/**
	 * @return correct file path
	 */
	protected String getPath() {
		if (isError()) {
			return status.getErrorPath();
		}
		return status.getOutputPath();
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
