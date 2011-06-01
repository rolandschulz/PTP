/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.lml_jaxb.actions;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.rm.lml.core.model.jobs.JobStatusData;
import org.eclipse.ptp.rm.lml.internal.core.model.Row;
import org.eclipse.ptp.rm.lml.ui.views.TableView;
import org.eclipse.ptp.rm.lml_jaxb.messages.Messages;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

/**
 * Base class for actions on the job status object which read remote file output
 * to a console.
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
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (status != null) {
			Job j = new Job(Messages.ReadOutputFile) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					IOConsoleOutputStream stream = null;
					try {
						String contents = getContents();
						IOConsole console = new IOConsole(getPath(), null);
						ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
						console.activate();
						stream = console.newOutputStream();
						stream.write(contents.getBytes());
					} catch (Throwable t) {
						return CoreExceptionUtils.getErrorStatus(Messages.ReadOutputFileError, t);
					} finally {
						try {
							if (stream != null) {
								stream.flush();
								stream.close();
							}
						} catch (IOException t) {
						}
					}
					return Status.OK_STATUS;
				}

			};

			j.setUser(true);
			j.schedule();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
	 * .IAction, org.eclipse.jface.viewers.ISelection)
	 */
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
			return;
		}
		if (getReady()) {
			action.setEnabled(true);
		} else {
			action.setEnabled(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.
	 * action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		view = (TableView) targetPart;
	}

	/**
	 * @return correct file contents
	 * @throws CoreException
	 */
	protected String getContents() throws CoreException {
		if (error) {
			return view.doRead(status.getRmId(), status.getErrorPath(), true);
		}
		return view.doRead(status.getRmId(), status.getOutputPath(), true);
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
