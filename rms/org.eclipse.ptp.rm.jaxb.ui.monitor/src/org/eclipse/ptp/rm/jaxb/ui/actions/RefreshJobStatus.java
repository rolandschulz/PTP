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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.rm.jaxb.ui.data.JobStatusData;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rmsystem.IJobStatus;

/**
 * Refreshes the state of the job by invoking getJobStatus on the resource
 * manager control.
 * 
 * @author arossi
 * 
 */
public class RefreshJobStatus extends AbstractStatusAction {

	/*
	 * Restarts the control if it is not running. (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		try {
			view.maybeUpdateJobState(status, true);
		} catch (Throwable t) {
			MessageDialog.openError(view.getSite().getShell(), Messages.RefreshJobStatus,
					Messages.RefreshJobStatusError + COSP + t.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.actions.AbstractStatusAction#validate(org.
	 * eclipse.jface.action.IAction,
	 * org.eclipse.ptp.rm.jaxb.ui.data.PersistentCommandJobStatus)
	 */
	@Override
	protected void validate(IAction action, JobStatusData status) {
		String state = status.getState();
		if (IJobStatus.COMPLETED.equals(state)) {
			action.setEnabled(false);
		} else {
			action.setEnabled(true);
		}
	}
}