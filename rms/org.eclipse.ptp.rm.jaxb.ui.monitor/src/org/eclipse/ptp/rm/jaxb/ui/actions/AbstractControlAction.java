/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.rm.jaxb.ui.data.JobStatusData;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ui.progress.UIJob;

/**
 * Base class for actions on the job status object which initiate a batch
 * control operation.
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
	public void run(IAction action) {
		UIJob job = new UIJob(operation) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				for (JobStatusData status : selected) {
					try {
						view.callDoControl(status, true, operation, monitor);
					} catch (CoreException t) {
						MessageDialog.openError(view.getSite().getShell(), Messages.DoControlError, Messages.OperationFailed
								+ operation + COSP + t.getMessage());
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	/*
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.actions.AbstractStatusAction#validate(org.
	 * eclipse.jface.action.IAction,
	 * org.eclipse.ptp.rm.jaxb.ui.data.PersistentCommandJobStatus)
	 */
	@Override
	protected void validate(IAction action) {
		for (JobStatusData status : selected) {
			if (status.isInteractive()) {
				action.setEnabled(false);
				return;
			} else if (!validateState(status)) {
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
