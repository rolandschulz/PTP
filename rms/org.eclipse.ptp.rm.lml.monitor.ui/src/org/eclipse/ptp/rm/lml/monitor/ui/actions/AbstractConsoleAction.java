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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.rm.lml.core.JobStatusData;
import org.eclipse.ptp.rm.lml.internal.core.model.Row;
import org.eclipse.ptp.rm.lml.ui.views.TableView;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Base class for actions on the job status object which read remote file output to a console.
 * 
 * @author arossi
 * 
 */
public abstract class AbstractConsoleAction implements IObjectActionDelegate {
	protected boolean error;
	protected ISelection selection;
	protected JobStatusData status;
	protected TableView view;

	/*
	 * Calls {@link ActionUtils#readRemoteFile(String, String)} (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		if (status.getRemoteId() != null && status.getConnectionName() != null) {
			String path = error ? status.getErrorPath() : status.getOutputPath();
			ActionUtils.readRemoteFile(status.getRemoteId(), status.getConnectionName(), path);
		}
	}

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
		if (((IStructuredSelection) selection).size() > 1) {
			action.setEnabled(false);
			return;
		}
		Row row = (Row) ((IStructuredSelection) selection).getFirstElement();
		status = row.status;
		if (status == null) {
			action.setEnabled(false);
		} else {
			action.setEnabled(getReady());
		}
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
	 * @return correct file path
	 */
	protected String getPath() {
		if (error) {
			return status.getErrorPath();
		}
		return status.getOutputPath();
	}

	/**
	 * @return correct file status
	 */
	protected boolean getReady() {
		if (error) {
			return status.getErrReady();
		}
		return status.getOutReady();
	}
}
