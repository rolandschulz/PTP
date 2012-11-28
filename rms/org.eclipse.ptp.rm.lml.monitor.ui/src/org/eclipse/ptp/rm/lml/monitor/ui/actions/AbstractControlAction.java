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

/**
 * Base class for actions on the job status object which initiate a batch control operation.
 * 
 * @author arossi
 * 
 */
public abstract class AbstractControlAction extends AbstractStatusAction {

	protected String operation;

	/*
	 * Restarts the resource manager control if it is not running. (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		Job j = new Job(operation) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				for (Row row : selected) {
					JobStatusData status = row.status;
					try {
						ActionUtils.callDoControl(status, operation, monitor);
						if (monitor.isCanceled()) {
							break;
						}
					} catch (CoreException t) {
						return CoreExceptionUtils.getErrorStatus(operation, t);
					}
				}
				return Status.OK_STATUS;
			}
		};

		j.setUser(true);
		j.schedule();
	}

	/*
	 * @see org.eclipse.ptp.rm.jaxb.ui.actions.AbstractStatusAction#validate(org. eclipse.jface.action.IAction,
	 * org.eclipse.ptp.rm.jaxb.ui.data.PersistentCommandJobStatus)
	 */
	@Override
	protected void validate(IAction action) {
		for (Row row : selected) {
			if (row.status == null) {
				action.setEnabled(false);
				return;
			} else if (row.status.isInteractive()) {
				action.setEnabled(false);
				return;
			} else if (!operationSupported(row.status, operation, view)) {
				action.setEnabled(false);
				return;
			} else if (!validateState(row.status)) {
				action.setEnabled(false);
				return;
			}
		}
		action.setEnabled(true);
	}

	/**
	 * Enables the action on basis of state semantics.
	 * 
	 * @param action
	 * @param status
	 */
	protected abstract boolean validateState(JobStatusData status);
}
