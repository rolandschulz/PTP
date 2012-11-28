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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.rm.lml.core.JobStatusData;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.internal.core.model.Row;
import org.eclipse.ptp.rm.lml.monitor.core.MonitorControlManager;
import org.eclipse.ptp.rm.lml.monitor.ui.messages.Messages;

/**
 * Removes the job from the list.
 * 
 * @author arossi
 * 
 */
public class RemoveJob extends AbstractStatusAction {
	/*
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		if (MessageDialog.openQuestion(view.getSite().getShell(), Messages.RemoveJob_Cannot_undo,
				Messages.RemoveJob_Permanently_remove_job_entry)) {
			List<JobStatusData> data = new ArrayList<JobStatusData>();
			for (Row row : selected) {
				JobStatusData status = row.status;
				data.add(status);
				String monitorId = MonitorControlManager.generateMonitorId(status.getRemoteId(), status.getConnectionName(),
						status.getMonitorType());
				LMLManager.getInstance().removeUserJob(monitorId, status.getJobId());
			}
			view.refresh();
			ActionUtils.removeFiles(data);
		}
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
			if (status == null || !status.getState().equals(JobStatusData.COMPLETED)) {
				action.setEnabled(false);
				return;
			}
		}
		action.setEnabled(true);
	}
}
