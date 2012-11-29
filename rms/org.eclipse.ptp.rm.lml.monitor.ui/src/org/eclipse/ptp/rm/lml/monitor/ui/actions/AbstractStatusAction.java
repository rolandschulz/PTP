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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.jobs.IJobControl;
import org.eclipse.ptp.rm.jaxb.control.IJobController;
import org.eclipse.ptp.rm.jaxb.control.LaunchControllerManager;
import org.eclipse.ptp.rm.jaxb.core.data.ControlType;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.lml.core.JobStatusData;
import org.eclipse.ptp.rm.lml.internal.core.model.Row;
import org.eclipse.ptp.rm.lml.ui.views.TableView;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Base class for actions on the job status object.
 * 
 * @author arossi
 * 
 */
public abstract class AbstractStatusAction implements IObjectActionDelegate {
	protected static final String JOB_STATUS = "get-job-status";//$NON-NLS-1$
	protected static final String COSP = ": ";//$NON-NLS-1$

	protected List<Row> selected;
	protected TableView view;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action .IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection.isEmpty()) {
			action.setEnabled(false);
			return;
		}
		List<?> list = ((IStructuredSelection) selection).toList();
		selected = new ArrayList<Row>();
		for (Object o : list) {
			selected.add((Row) o);
		}
		validate(action);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface. action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		view = (TableView) targetPart;
	}

	/**
	 * Enables the action.
	 * 
	 * @param action
	 */
	protected abstract void validate(IAction action);

	/**
	 * Checks the JAXB data tree to see if the operation is implemented.
	 * 
	 * @param status
	 * @param operation
	 * @return
	 */
	protected static boolean operationSupported(JobStatusData status, String operation, IViewPart targetPart) {
		try {
			IJobController jobController = LaunchControllerManager.getInstance().getLaunchController(status.getRemoteId(),
					status.getConnectionName(), status.getConfigurationName());
			if (jobController != null) {
				ResourceManagerData data = jobController.getConfiguration();
				if (data != null) {
					ControlType control = data.getControlData();
					if (operation.equals(JOB_STATUS)) {
						return control.getGetJobStatus() != null;
					}
					if (operation.equals(IJobControl.HOLD_OPERATION)) {
						return control.getHoldJob() != null;
					}
					if (operation.equals(IJobControl.RELEASE_OPERATION)) {
						return control.getReleaseJob() != null;
					}
					if (operation.equals(IJobControl.RESUME_OPERATION)) {
						return control.getResumeJob() != null;
					}
					if (operation.equals(IJobControl.SUSPEND_OPERATION)) {
						return control.getSuspendJob() != null;
					}
					if (operation.equals(IJobControl.TERMINATE_OPERATION)) {
						return control.getTerminateJob() != null;
					}
				}
			}
		} catch (Throwable t) {
			PTPCorePlugin.log(t);
		}
		return false;
	}
}
