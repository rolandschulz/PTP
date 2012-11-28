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
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.rm.lml.core.JobStatusData;
import org.eclipse.ptp.rm.lml.internal.core.model.Row;
import org.eclipse.ptp.rm.lml.monitor.ui.messages.Messages;

/**
 * Refreshes the state of the job by invoking getJobStatus on the resource manager control.
 * 
 * @author arossi
 * 
 */
public class RefreshJobStatus extends AbstractStatusAction {
	/*
	 * Restarts the resource manager control if it is not running. (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		Job j = new Job(Messages.RefreshJobStatus_Refresh_Job_Status) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				for (Row row : selected) {
					JobStatusData status = row.status;
					try {
						ActionUtils.maybeUpdateJobState(status, monitor);
					} catch (CoreException t) {
						return CoreExceptionUtils.getErrorStatus(Messages.RefreshJobStatus_Failed_to_refresh_job_status, t);
					}
					if (monitor != null && monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
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
			if (status == null) {
				action.setEnabled(false);
				return;
			}
			if (!operationSupported(status, JOB_STATUS, view)) {
				action.setEnabled(false);
				return;
			}
		}
		action.setEnabled(true);
	}
}
