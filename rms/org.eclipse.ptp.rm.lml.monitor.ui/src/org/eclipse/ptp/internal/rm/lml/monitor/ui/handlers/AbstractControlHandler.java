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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.internal.rm.lml.monitor.ui.messages.Messages;
import org.eclipse.ptp.rm.lml.core.JobStatusData;
import org.eclipse.ptp.rm.lml.internal.core.model.Row;
import org.eclipse.ui.handlers.HandlerUtil;

public abstract class AbstractControlHandler extends AbstractHandler {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection != null && !selection.isEmpty()) {
			Job cancelJob = new Job(Messages.AbstractControlHandler_ControlJob) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					for (Object selected : selection.toArray()) {
						if (selected instanceof Row) {
							Row row = (Row) selected;
							JobStatusData status = row.status;
							try {
								ActionUtils.callDoControl(status, getOperation(), monitor);
								if (monitor.isCanceled()) {
									break;
								}
							} catch (CoreException t) {
								return CoreExceptionUtils.getErrorStatus(Messages.AbstractControlHandler_Failed_to_cancel_job, t);
							}
						}
					}
					return Status.OK_STATUS;
				}
			};

			cancelJob.setUser(true);
			cancelJob.schedule();
		}
		return null;
	}

	protected abstract String getOperation();
}
