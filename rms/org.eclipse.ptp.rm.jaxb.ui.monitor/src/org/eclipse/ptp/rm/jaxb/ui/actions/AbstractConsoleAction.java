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

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ptp.rm.jaxb.ui.data.JobStatusData;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.views.MonitorJobListView;
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
	protected JobStatusData status;
	protected MonitorJobListView view;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (status != null) {
			IOConsoleOutputStream stream = null;
			try {
				String contents = getContents();
				IOConsole console = new IOConsole(getPath(), null);
				ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
				console.activate();
				stream = console.newOutputStream();
				stream.write(contents.getBytes());
			} catch (Throwable t) {
				MessageDialog.openError(view.getSite().getShell(), Messages.ConsoleWriteError, t.getMessage());
			} finally {
				try {
					if (stream != null) {
						stream.flush();
						stream.close();
					}
				} catch (IOException t) {
				}
			}
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

		status = (JobStatusData) ((IStructuredSelection) selection).getFirstElement();
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
		view = (MonitorJobListView) targetPart;
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
