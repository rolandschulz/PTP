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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.internal.rm.lml.monitor.ui.messages.Messages;
import org.eclipse.ptp.rm.lml.core.JobStatusData;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.core.model.Row;
import org.eclipse.ui.handlers.HandlerUtil;

public class RemoveJobHandler extends AbstractHandler {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection != null && !selection.isEmpty()) {
			if (MessageDialog.openQuestion(HandlerUtil.getActiveShell(event), Messages.RemoveJob_Cannot_undo,
					Messages.RemoveJob_Permanently_remove_job_entry)) {
				List<JobStatusData> data = new ArrayList<JobStatusData>();
				String controlId = null;
				for (Object selected : selection.toArray()) {
					if (selected instanceof Row) {
						Row row = (Row) selected;
						JobStatusData status = row.status;
						if (controlId == null) {
							controlId = status.getString(JobStatusData.CONTROL_ID_ATTR);
							if (controlId == null) {
								break;
							}
						}
						data.add(status);
						LMLManager.getInstance().removeUserJob(controlId, status.getJobId());
					}
				}
				if (controlId != null) {
					ActionUtils.removeFiles(controlId, data);
				}
				return Status.OK_STATUS;
			}
		}

		return null;
	}
}
