/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.lml.monitor.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.ptp.core.jobs.IJobControl;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.rm.lml.core.JobStatusData;
import org.eclipse.ptp.rm.lml.internal.core.model.Row;
import org.eclipse.ptp.rm.lml.monitor.ui.messages.Messages;

/**
 * Cancels the job.
 * 
 * @author arossi
 * 
 */
public class CancelJob extends AbstractStatusAction {
	/*
	 * Restarts the resource manager control if it is not running. (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		Job j = new Job(Messages.CancelJob_Cancel_Job) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				for (Row row : selected) {
					JobStatusData status = row.status;
					try {
						ActionUtils.callDoControl(status, IJobControl.TERMINATE_OPERATION, monitor);
						if (monitor.isCanceled()) {
							break;
						}
					} catch (CoreException t) {
						return CoreExceptionUtils.getErrorStatus(Messages.CancelJob_Failed_to_cancel_job, t);
					}
				}
				return Status.OK_STATUS;
			}
		};

		j.setUser(true);
		j.schedule();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.actions.AbstractStatusAction#validate(org. eclipse.jface.action.IAction,
	 * org.eclipse.ptp.rm.jaxb.ui.data.PersistentCommandJobStatus)
	 */
	@Override
	protected void validate(IAction action) {
		for (Row row : selected) {
			JobStatusData status = row.status;
			if (status == null || !ActionUtils.isAuthorised(status)) {
				action.setEnabled(false);
				return;
			}
			if (!operationSupported(status, IJobControl.TERMINATE_OPERATION, view)) {
				action.setEnabled(false);
				return;
			}
			String state = status.getState();
			if (IJobStatus.COMPLETED.equals(state)) {
				action.setEnabled(false);
				return;
			}
		}
		action.setEnabled(true);
	}
}
