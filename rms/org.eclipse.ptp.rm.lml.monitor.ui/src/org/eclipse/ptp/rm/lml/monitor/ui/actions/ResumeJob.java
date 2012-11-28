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

import org.eclipse.ptp.core.jobs.IJobControl;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.rm.lml.core.JobStatusData;

/**
 * Resumes the job.
 * 
 * @author arossi
 * 
 */
public class ResumeJob extends AbstractControlAction {

	public ResumeJob() {
		operation = IJobControl.RESUME_OPERATION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.actions.AbstractBatchControlAction#validateState (org.eclipse.jface.action.IAction,
	 * org.eclipse.ptp.rm.jaxb.ui.data.PersistentCommandJobStatus)
	 */
	@Override
	protected boolean validateState(JobStatusData status) {
		String state = status.getState();
		if (!IJobStatus.SUSPENDED.equals(state)) {
			return false;
		}
		if (!ActionUtils.isAuthorised(status)) {
			return false;
		}
		return true;
	}
}
