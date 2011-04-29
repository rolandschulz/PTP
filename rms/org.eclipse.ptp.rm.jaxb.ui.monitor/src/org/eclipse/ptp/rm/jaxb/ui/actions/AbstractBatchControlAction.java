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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.rm.jaxb.ui.data.JobStatusData;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;

/**
 * Base class for actions on the job status object.
 * 
 * @author arossi
 * 
 */
public abstract class AbstractBatchControlAction extends AbstractStatusAction {

	protected String operation;

	/*
	 * Restarts the control if it is not running. (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		try {
			view.callDoControl(status, true, operation);
		} catch (CoreException t) {
			MessageDialog.openError(view.getSite().getShell(), Messages.DoControlError, Messages.OperationFailed + operation + COSP
					+ t.getMessage());
		}
	}

	/*
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.actions.AbstractStatusAction#validate(org.
	 * eclipse.jface.action.IAction,
	 * org.eclipse.ptp.rm.jaxb.ui.data.PersistentCommandJobStatus)
	 */
	@Override
	protected void validate(IAction action, JobStatusData status) {
		if (status.isInteractive()) {
			action.setEnabled(false);
		} else {
			validateState(action, status);
		}
	}

	/**
	 * Enables the action on basis of state semantics.
	 * 
	 * @param action
	 * @param status
	 */
	protected abstract void validateState(IAction action, JobStatusData status);
}
