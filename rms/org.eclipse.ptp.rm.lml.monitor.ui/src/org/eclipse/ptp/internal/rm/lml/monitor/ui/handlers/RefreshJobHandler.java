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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.internal.rm.lml.monitor.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.control.core.ILaunchController;
import org.eclipse.ptp.rm.jaxb.control.core.LaunchControllerManager;
import org.eclipse.ptp.rm.lml.core.JobStatusData;
import org.eclipse.ptp.rm.lml.core.model.Row;
import org.eclipse.ui.handlers.HandlerUtil;

public class RefreshJobHandler extends AbstractHandler {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection != null && !selection.isEmpty()) {
			Job cancelJob = new Job(Messages.RefreshJobHandler_Refresh_job) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					ILaunchController controller = null;
					SubMonitor progress = SubMonitor.convert(monitor, selection.toArray().length);
					for (Object selected : selection.toArray()) {
						if (selected instanceof Row) {
							Row row = (Row) selected;
							JobStatusData status = row.status;
							try {
								if (controller == null) {
									String controlId = status.getString(JobStatusData.CONTROL_ID_ATTR);
									controller = LaunchControllerManager.getInstance().getLaunchController(controlId);
									controller.start(progress.newChild(10));
								}
								if (controller != null) {
									controller.getJobStatus(status.getJobId(), progress.newChild(10));
								}
								if (progress.isCanceled()) {
									break;
								}
							} catch (CoreException t) {
								return CoreExceptionUtils.getErrorStatus(Messages.RefreshJobHandler_Failed_to_refresh_job, t);
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
}
